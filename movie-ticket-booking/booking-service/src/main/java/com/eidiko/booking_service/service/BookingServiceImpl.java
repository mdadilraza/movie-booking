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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
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

        // Validate showtime
        Showtime showtime = showtimeRepository.findByIdAndIsActiveTrue(request.getShowtimeId())
                .orElseThrow(() -> new ShowtimeNotFoundException("Showtime not found or deleted"));

        movieClient.validateMovie(showtime.getMovieId());

        // Check seat availability
        if (showtime.getAvailableSeats() < request.getSeatNumbers().size()) {
            log.warn("Not enough available seats for showtimeId: {}. Requested: {}, Available: {}",
                    request.getShowtimeId(), request.getSeatNumbers().size(), showtime.getAvailableSeats());
            throw new SeatNotAvailableException("Not enough available seats");
        }

        // Check for booked seats
        List<BookingSeat> existingSeats = bookingSeatRepository
                .findBookedSeatsByShowtimeIdAndSeatNumbers(request.getShowtimeId(), request.getSeatNumbers())
                .stream()
                .filter(seat -> seat.getStatus().equals(BookingStatus.CONFIRMED))
                .toList();
        if (!existingSeats.isEmpty() ) {
            Set<String> bookedSeats = existingSeats.stream()
                    .map(BookingSeat::getSeatNumber)
                    .collect(Collectors.toSet());
            log.warn("Seats already booked for showtimeId: {}: {}", request.getShowtimeId(), bookedSeats);
            throw new SeatAlreadyBookedException("Seats already booked: " + bookedSeats);
        }
        // Validate seat types
        Map<String, Long> seatTypes = request.getSeatTypes();
        if (!seatTypes.keySet().containsAll(request.getSeatNumbers()) || seatTypes.size() != request.getSeatNumbers().size()) {
            log.warn("Invalid seat types provided for showtimeId: {}", request.getShowtimeId());
            throw new IllegalArgumentException("Seat types must be provided for all seat numbers");
        }

        // Get user ID from token
        String username = authService.getCurrentUsername();
        Long userId = userClient.getUserId(username);
        if (userId == null) {
            log.warn("User ID not found for username : {}", username);
            throw new UserNotFoundException("User  ID not found");
        }

        // Create booking
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setShowtime(showtime);
        booking.setStatus(BookingStatus.CONFIRMED);

        // Create seats and calculate total price
        double totalPrice = 0.0;
        Set<BookingSeat> seatEntities = request.getSeatNumbers().stream().map(seatNumber -> {
            Long seatId = seatTypes.get(seatNumber);
            Seats seatType = seatRepository.findById(seatId)
                    .orElseThrow(() -> new EntityNotFoundException("Seat type with id " + seatId + " not found"));
            BookingSeat seatEntity = new BookingSeat();
            seatEntity.setSeatNumber(seatNumber);
            seatEntity.setStatus(BookingStatus.CONFIRMED);
            seatEntity.setBooking(booking);
            seatEntity.setSeats(seatType);
            return seatEntity;
        }).collect(Collectors.toSet());

        // Calculate total price
        for (BookingSeat seat : seatEntities) {
            totalPrice += seat.getSeats().getBasePrice();
        }
        booking.setTotalPrice(totalPrice);
        booking.setSeats(seatEntities);

        log.info("Before booking saving, totalPrice: {}", totalPrice);
        Booking bookingSaved = bookingRepository.save(booking);
        log.info("Booking saved with Id: {}", bookingSaved.getId());

        // Process payment
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setBookingId(bookingSaved.getId());
        paymentRequest.setAmount(totalPrice);
        paymentRequest.setNumberOfSeats(seatEntities.size());
        log.info("calling createPayment api");
        PaymentResponse paymentResponse = paymentClient.createPayment(paymentRequest);
       log.info("created payment with transactionId: {}",paymentResponse.getTransactionId());
        // Update showtime
        showtime.setAvailableSeats(showtime.getAvailableSeats() - seatEntities.size());
        showtimeRepository.save(showtime);

        log.info("Created booking with ID: {} for showtimeId: {}, totalPrice: {}",
                bookingSaved.getId(), request.getShowtimeId(), totalPrice);
        return bookingMapper.mapToBookingResponse(bookingSaved, paymentResponse);
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
       //cancel Validation
        validateCancellationStrategies(booking, request);
        //to check Authorized User to cancel the ticket
        authorizeUser(booking);
        //finding no of seats to cancel
        Set<String> seatsToCancel = determineSeatsToCancel(booking, request);
        //validate seat ownership
        validateSeatOwnership(booking, seatsToCancel);
        //validate seat status and provide the refund after cancel
        validateSeatStatusAndRefunds(booking, bookingId, seatsToCancel);
        //calculate total refund
        double totalRefundAmount = calculateTotalRefund(booking, seatsToCancel);

        RefundResponse refundResponse = processRefund(bookingId, seatsToCancel);
       //count of ticket to cancel
        int cancelCount = cancelSeatsAndSaveRefunds(booking, bookingId, seatsToCancel, refundResponse);
        //updating booking status after cancellation
        updateBookingStatus(booking);

        //update the showtime no of seats have to add after cancellation
        updateShowtimeIfNeeded(booking, cancelCount, totalRefundAmount);

        BookingResponse response = bookingMapper.mapToBookingResponse(
                booking,
                cancelCount > 0 ? "INITIATED" : null,
                totalRefundAmount
        );

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
//if user won't pass any seat no ,then cancel all the seats
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

            if (seat.getStatus() .equals(BookingStatus.CANCELED)) {
                log.warn("Seat {} already canceled for bookingId: {}", seatNumber, bookingId);
                throw new SeatNotAvailableException("Seat already canceled: " + seatNumber);
            }

            if (refundRepository.existsByBookingIdAndSeatNumber(bookingId, seatNumber)) {
                log.warn("Refund already processed for seat {} in bookingId: {}", seatNumber, bookingId);
                throw new SeatNotAvailableException("Refund already processed for seat: " + seatNumber);
            }
        }
    }

    private double calculateTotalRefund(Booking booking, Set<String> seatsToCancel) {
        return booking.getSeats().stream()
                .filter(seat -> seatsToCancel.contains(seat.getSeatNumber()))
                .mapToDouble(seat -> seat.getSeats().getBasePrice())
                .sum();
    }

    private RefundResponse processRefund(long bookingId, Set<String> seatsToCancel) {
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setBookingId(bookingId);
        refundRequest.setSeatNumbers(seatsToCancel);
        return paymentClient.processRefund(refundRequest);
    }

    private int cancelSeatsAndSaveRefunds(Booking booking, long bookingId,
                                          Set<String> seatsToCancel, RefundResponse refundResponse) {
        int cancelCount = 0;
        for (BookingSeat seat : booking.getSeats()) {
            if (seatsToCancel.contains(seat.getSeatNumber()) && !seat.getStatus().equals( BookingStatus.CANCELED)) {
                seat.setStatus(BookingStatus.CANCELED);
                cancelCount++;

                Refund refund = new Refund();
                refund.setBookingId(bookingId);
                refund.setSeatNumber(seat.getSeatNumber());
                refund.setAmount(seat.getSeats().getBasePrice());
                refund.setStatus(refundResponse.getStatus());
                refund.setTransactionId(refundResponse.getTransactionId());
                refundRepository.save(refund);
            }
        }
        return cancelCount;
    }

    private void updateBookingStatus(Booking booking) {
        boolean allSeatsCanceled = booking.getSeats().stream()
                .allMatch(seat -> seat.getStatus() .equals( BookingStatus.CANCELED));
        booking.setStatus(allSeatsCanceled ? BookingStatus.CANCELED : BookingStatus.PARTIAL_CANCELED);
    }

    private void updateShowtimeIfNeeded(Booking booking, int cancelCount, double totalRefundAmount) {
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
