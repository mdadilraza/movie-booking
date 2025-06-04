package com.eidiko.booking_service.service;
import com.eidiko.booking_service.adapter.UserClient;
import com.eidiko.booking_service.client.MovieClient;
import com.eidiko.booking_service.client.PaymentClient;
import com.eidiko.booking_service.constants.BookingStatus;
import com.eidiko.booking_service.dto.*;
import com.eidiko.booking_service.entity.*;
import com.eidiko.booking_service.exception.*;
import com.eidiko.booking_service.mapper.BookingMapper;
import com.eidiko.booking_service.repository.*;
import com.eidiko.booking_service.strategy.factory.CancellationValidationStrategyFactory;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final ShowtimeRepository showtimeRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final RefundRepository refundRepository;
    private final MovieClient movieClient;
    private final PaymentClient paymentClient;
    private final UserClient userClient;
    private final AuthorizationService authService;
    private final BookingMapper bookingMapper;
    private final CancellationValidationStrategyFactory strategyFactory;
    private final SeatRepository seatRepository;

    @Override
    @Transactional
    public ShowtimeResponse createShowtime(ShowtimeRequest request) {
        log.info("Creating showtime for movieId: {}", request.getMovieId());
        if (!authService.isAdmin()) {
            throw new UnauthorizedBookingActionException("Only admin can create showtimes");
        }
        movieClient.validateMovie(request.getMovieId());
        Showtime showtime = new Showtime();
        showtime.setMovieId(request.getMovieId());
        showtime.setTheaterName(request.getTheaterName());
        showtime.setShowtimeDate(request.getShowtimeDate());
        showtime.setTotalSeats(request.getTotalSeats());
        showtime.setAvailableSeats(request.getTotalSeats());
        Showtime savedShowtime = showtimeRepository.save(showtime);
        log.info("Showtime created with ID: {}", savedShowtime.getId());
        return bookingMapper.mapToShowtimeResponse(savedShowtime);
    }

    @Override
    public List<ShowtimeResponse> getShowtimeByMovie(long movieId) {
        log.info("Fetching showtimes for movieId: {}", movieId);
        movieClient.validateMovie(movieId);
        return showtimeRepository.findByMovieIdAndIsActiveTrue(movieId)
                .stream()
                .map(bookingMapper::mapToShowtimeResponse)
                .toList();
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Creating booking for showtimeId: {}, seats: {}", request.getShowtimeId(), request.getSeatNumbers());

        // 1. Validate showtime with pessimistic lock
        Showtime showtime = showtimeRepository.findByIdAndIsActiveTrueWithLock(request.getShowtimeId())
                .orElseThrow(() -> new ShowtimeNotFoundException("Showtime not found or deleted"));

        movieClient.validateMovie(showtime.getMovieId());

        // 2. Check availability
        if (showtime.getAvailableSeats() < request.getSeatNumbers().size()) {
            log.warn("Not enough seats for showtimeId: {}. Requested: {}, Available: {}",
                    request.getShowtimeId(), request.getSeatNumbers().size(), showtime.getAvailableSeats());
            throw new SeatNotAvailableException("Not enough available seats");
        }

        // 3. Check for already booked seats
        List<BookingSeat> existingSeats = bookingSeatRepository
                .findBookedSeatsByShowtimeIdAndSeatNumbers(request.getShowtimeId(), request.getSeatNumbers())
                .stream()
                .filter(seat -> seat.getStatus().equals(BookingStatus.CONFIRMED))
                .toList();
        if (!existingSeats.isEmpty()) {
            Set<String> bookedSeats = existingSeats.stream().map(BookingSeat::getSeatNumber).collect(Collectors.toSet());
            throw new SeatAlreadyBookedException("Seats already booked: " + bookedSeats);
        }

        // 4. Validate seat types
        Map<String, Long> seatTypes = request.getSeatTypes();
        if (!seatTypes.keySet().containsAll(request.getSeatNumbers()) || seatTypes.size() != request.getSeatNumbers().size()) {
            throw new IllegalArgumentException("Seat types must be provided for all seat numbers");
        }

        // 5. Get user ID
        String username = authService.getCurrentUsername();
        Long userId = userClient.getUserId(username);
        if (userId == null) {
            throw new UserNotFoundException("User ID not found");
        }

        // 6. Fetch all required seat types in one call
        List<Long> seatIds = new ArrayList<>(seatTypes.values());
        Map<Long, Seats> seatMap = seatRepository.findAllById(seatIds).stream()
                .collect(Collectors.toMap(Seats::getId, s -> s));

        // 7. Create booking and seats
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setShowtime(showtime);
        booking.setStatus(BookingStatus.CONFIRMED);

        Set<BookingSeat> seatEntities = new HashSet<>();
        double totalPrice = 0.0;

        for (String seatNumber : request.getSeatNumbers()) {
            Long seatTypeId = seatTypes.get(seatNumber);
            Seats seatType = seatMap.get(seatTypeId);
            if (seatType == null) {
                throw new EntityNotFoundException("Seat type with id " + seatTypeId + " not found");
            }

            BookingSeat seat = new BookingSeat();
            seat.setSeatNumber(seatNumber);
            seat.setStatus(BookingStatus.CONFIRMED);
            seat.setBooking(booking);
            seat.setSeats(seatType);

            totalPrice += seatType.getBasePrice();
            seatEntities.add(seat);
        }

        booking.setSeats(seatEntities);
        booking.setTotalPrice(totalPrice);


        Booking bookingSaved = bookingRepository.save(booking);

        // 8. Create payment
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setBookingId(bookingSaved.getId());
        paymentRequest.setAmount(totalPrice);
        paymentRequest.setNumberOfSeats(seatEntities.size());

        try {
            PaymentResponse paymentResponse = paymentClient.createPayment(paymentRequest);

            // 9. Update available seats
            showtime.setAvailableSeats(showtime.getAvailableSeats() - seatEntities.size());
            showtimeRepository.save(showtime);

            return bookingMapper.mapToBookingResponse(bookingSaved, paymentResponse);

        } catch (Exception ex) {
            handleBookingFailure(bookingSaved, showtime, seatEntities.size());
            throw new PaymentDeclinedException("Payment failed: " + ex.getMessage());
        }
    }
    private void handleBookingFailure(Booking booking, Showtime showtime, int seatCount) {
        log.error("Payment failed for bookingId: {}. Rolling back...", booking.getId());
        booking.setStatus(BookingStatus.CANCELED);
        booking.getSeats().forEach(seat -> seat.setStatus(BookingStatus.CANCELED));
        bookingRepository.save(booking);

        showtime.setAvailableSeats(showtime.getAvailableSeats() + seatCount);
        showtimeRepository.save(showtime);

        try {
            RefundResponse refundResponse = paymentClient.refundPaymentByBookingId(booking.getId());
            log.info("Refund issued successfully for bookingId: {}", refundResponse.getBookingId());
        } catch (Exception refundEx) {
            log.error("Refund failed for bookingId: {}. Manual action may be required.", booking.getId());
        }
    }



    @Override
    public List<BookingResponse> getUserBookings() {
        Long userId = userClient.getUserId(authService.getCurrentUsername());
        if (userId == null) {
            log.warn("User ID not found for username  : {}", authService.getCurrentUsername());
            throw new UserNotFoundException("User ID not found");
        }
        log.info("Fetching bookings for userId: {}", userId);
        return bookingRepository.findByUserId(userId)
                .stream()
                .map(bookingMapper::mapToBookingResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<BookingResponse> cancelSeats(long bookingId, CancelSeatRequest request) {
        log.info("Canceling seats for bookingId: {}, seats: {}", bookingId, request.getSeats());

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new SeatNotAvailableException("Booking not found"));

        validateCancellationStrategies(booking, request);
        authorizeUser(booking);

        Set<String> seatsToCancel = determineSeatsToCancel(booking, request);
        validateSeatOwnership(booking, seatsToCancel);

        // Async validation of seat status & refund
        CompletableFuture<Void> validationFuture = CompletableFuture.runAsync(() ->
                validateSeatStatusAndRefunds(booking, bookingId, seatsToCancel)
        );

        // Async refund calculation
        CompletableFuture<BigDecimal> refundAmountFuture = CompletableFuture.supplyAsync(() ->
                calculateTotalRefund(booking, seatsToCancel)
        );

        CompletableFuture<RefundResponse> refundResponseFuture = refundAmountFuture.thenApplyAsync(totalRefund -> {
            RefundRequest refundRequest = new RefundRequest();
            refundRequest.setBookingId(bookingId);
            refundRequest.setSeatNumbers(seatsToCancel);
            return paymentClient.processRefund(refundRequest);
        });

        CompletableFuture<Integer> cancelCountFuture = refundResponseFuture.thenApplyAsync(refundResponse ->
                cancelSeatsAndSaveRefunds(booking, bookingId, seatsToCancel, refundResponse)
        );

        CompletableFuture.allOf(validationFuture, cancelCountFuture).join();

        int cancelCount = cancelCountFuture.join();
        BigDecimal totalRefundAmount = refundAmountFuture.join();
        RefundResponse refundResponse = refundResponseFuture.join();

        updateBookingStatus(booking);
        updateShowtimeIfNeeded(booking, cancelCount, totalRefundAmount);

        BookingResponse response = bookingMapper.mapToBookingResponse(
                booking,
                cancelCount > 0 ? "INITIATED" : null,
                totalRefundAmount.doubleValue()
        );
        response.setRefundAmount(refundResponse.getRefundedAmount());
        return List.of(response);
    }


    private void validateCancellationStrategies(Booking booking, CancelSeatRequest request) {
        var strategies = strategyFactory.getStrategies(booking, request.getSeats());
        strategies.forEach(strategy -> strategy.validate(booking));
    }

    private void authorizeUser(Booking booking) {
        Long currentUserId = userClient.getUserId(authService.getCurrentUsername());
        if (currentUserId == null ||
                (!booking.getUserId().equals(currentUserId) && !authService.isAdmin())) {
            log.warn("Unauthorized attempt to cancel booking {} by userId: {}", booking.getId(), currentUserId);
            throw new UnauthorizedBookingActionException("Only the user or admin can cancel");
        }
    }

    private Set<String> determineSeatsToCancel(Booking booking, CancelSeatRequest request) {
        return (request.getSeats() == null || request.getSeats().isEmpty())
                ? booking.getSeats().stream().map(BookingSeat::getSeatNumber).collect(Collectors.toSet())
                : request.getSeats();
    }

    private void validateSeatOwnership(Booking booking, Set<String> seatsToCancel) {
        Set<String> bookingSeats = booking.getSeats().stream()
                .map(BookingSeat::getSeatNumber).collect(Collectors.toSet());
        if (!bookingSeats.containsAll(seatsToCancel)) {
            log.warn("Invalid seats requested for cancellation: {}", seatsToCancel);
            throw new SeatNotAvailableException("Some seats are not part of this booking");
        }
    }

    private void validateSeatStatusAndRefunds(Booking booking, long bookingId, Set<String> seatsToCancel) {
        for (String seatNumber : seatsToCancel) {
            BookingSeat seat = booking.getSeats().stream()
                    .filter(s -> s.getSeatNumber().equals(seatNumber))
                    .findFirst()
                    .orElseThrow();

            if (StringUtils.equals(seat.getStatus(), BookingStatus.CANCELED)) {
                log.warn("Seat {} already canceled for bookingId: {}", seatNumber, bookingId);
                throw new SeatNotAvailableException("Seat already canceled: " + seatNumber);
            }

            if (refundRepository.existsByBookingIdAndSeatNumber(bookingId, seatNumber)) {
                log.warn("Refund already processed for seat {} in bookingId: {}", seatNumber, bookingId);
                throw new SeatNotAvailableException("Refund already processed for seat: " + seatNumber);
            }
        }
    }

    private BigDecimal calculateTotalRefund(Booking booking, Set<String> seatsToCancel) {
        return booking.getSeats().stream()
                .filter(seat -> seatsToCancel.contains(seat.getSeatNumber()))
                .map(seat -> BigDecimal.valueOf(seat.getSeats().getBasePrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int cancelSeatsAndSaveRefunds(Booking booking, long bookingId,
                                          Set<String> seatsToCancel, RefundResponse refundResponse) {
        int cancelCount = 0;
        for (BookingSeat seat : booking.getSeats()) {
            if (seatsToCancel.contains(seat.getSeatNumber()) && !StringUtils.equals(seat.getStatus(), BookingStatus.CANCELED)) {
                seat.setStatus(BookingStatus.CANCELED);
                cancelCount++;

                Refund refund = new Refund();
                refund.setBookingId(bookingId);
                refund.setSeatNumber(seat.getSeatNumber());
                refund.setAmount(BigDecimal.valueOf(seat.getSeats().getBasePrice()).doubleValue());
                refund.setStatus(refundResponse.getStatus());
                refund.setTransactionId(refundResponse.getTransactionId());
                refundRepository.save(refund);
            }
        }
        return cancelCount;
    }
    private void updateBookingStatus(Booking booking) {
        boolean allSeatsCanceled = booking.getSeats().stream()
                .allMatch(seat -> StringUtils.equals(seat.getStatus(), BookingStatus.CANCELED));
        booking.setStatus(allSeatsCanceled ? BookingStatus.CANCELED : BookingStatus.PARTIAL_CANCELED);
    }

    private void updateShowtimeIfNeeded(Booking booking, int cancelCount, BigDecimal totalRefundAmount) {
        if (cancelCount > 0) {
            Showtime showtime = booking.getShowtime();
            showtime.setAvailableSeats(showtime.getAvailableSeats() + cancelCount);
            showtimeRepository.save(showtime);
            bookingRepository.save(booking);
            log.info("Canceled {} seats for bookingId: {}, refund initiated for amount: {}",
                    cancelCount, booking.getId(), totalRefundAmount);
        }
    }

}
