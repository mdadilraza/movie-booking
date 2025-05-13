package com.eidiko.booking_service.service;

import com.eidiko.booking_service.adapter.UserClient;
import com.eidiko.booking_service.client.MovieClient;
import com.eidiko.booking_service.client.PaymentClient;
import com.eidiko.booking_service.constants.BookingStatus;
import com.eidiko.booking_service.dto.*;
import com.eidiko.booking_service.entity.Booking;
import com.eidiko.booking_service.entity.BookingSeat;
import com.eidiko.booking_service.entity.Refund;
import com.eidiko.booking_service.entity.Showtime;
import com.eidiko.booking_service.exception.SeatNotAvailableException;
import com.eidiko.booking_service.exception.ShowtimeNotFoundException;
import com.eidiko.booking_service.exception.UnauthorizedBookingActionException;
import com.eidiko.booking_service.exception.UserNotFoundException;
import com.eidiko.booking_service.mapper.BookingMapper;
import com.eidiko.booking_service.repository.BookingRepository;
import com.eidiko.booking_service.repository.BookingSeatRepository;
import com.eidiko.booking_service.repository.RefundRepository;
import com.eidiko.booking_service.repository.ShowtimeRepository;
import com.eidiko.booking_service.strategy.factory.CancellationValidationStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService{
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

    private static final double TICKET_PRICE = 10.0; // Fallback price

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
                .findBookedSeatsByShowtimeIdAndSeatNumbers(request.getShowtimeId(), request.getSeatNumbers());
        if (!existingSeats.isEmpty()) {
            Set<String> bookedSeats = existingSeats.stream()
                    .map(BookingSeat::getSeatNumber)
                    .collect(Collectors.toSet());
            log.warn("Seats already booked for showtimeId: {}: {}", request.getShowtimeId(), bookedSeats);
            throw new SeatNotAvailableException("Seats already booked: " + bookedSeats);
        }

        // Get user ID from token
        String username = authService.getCurrentUsername();
        Long userId = userClient.getUserId(username);
        if (userId == null) {
            log.warn("User ID not found for username : {}", username);
            throw new UserNotFoundException("User ID not found");
        }

        // Create booking
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setShowtime(showtime);
        booking.setStatus(BookingStatus.CONFIRMED);

        // Create seats
        Set<BookingSeat> seatEntities = request.getSeatNumbers().stream().map(seat -> {
            BookingSeat seatEntity = new BookingSeat();
            seatEntity.setSeatNumber(seat);
            seatEntity.setStatus(BookingStatus.CONFIRMED);
            seatEntity.setBooking(booking);
            return seatEntity;
        }).collect(Collectors.toSet());

        booking.setSeats(seatEntities);
        Booking bookingSaved = bookingRepository.save(booking);

        // Process payment
        double totalAmount = TICKET_PRICE * seatEntities.size();
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setBookingId(bookingSaved.getId());
        paymentRequest.setAmount(totalAmount);
        paymentRequest.setNumberOfSeats(seatEntities.size());
        PaymentResponse paymentResponse = paymentClient.createPayment(paymentRequest);

        // Update showtime
        showtime.setAvailableSeats(showtime.getAvailableSeats() - seatEntities.size());
        showtimeRepository.save(showtime);

        log.info("Created booking with ID: {} for showtimeId: {}", bookingSaved.getId(), request.getShowtimeId());
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

        // Validate cancellation
        var strategies = strategyFactory.getStrategies(booking, request.getSeats());
        strategies.forEach(strategy -> strategy.validate(booking));

        // Authorization
        Long currentUserId = userClient.getUserId(authService.getCurrentUsername());
        if (currentUserId == null) {
            log.warn("User ID not found for username: {}", authService.getCurrentUsername());
            throw new UserNotFoundException("User ID not found");
        }
        if (!booking.getUserId().equals(currentUserId) && !authService.isAdmin()) {
            log.warn("Unauthorized attempt to cancel booking {} by userId: {}", bookingId, currentUserId);
            throw new UnauthorizedBookingActionException("Only the user or admin can cancel");
        }

        // Determine seats to cancel
        Set<String> seatsToCancel = request.getSeats() == null || request.getSeats().isEmpty()
                ? booking.getSeats().stream().map(BookingSeat::getSeatNumber).collect(Collectors.toSet())
                : request.getSeats();

        // Validate seats belong to booking
        Set<String> bookingSeats = booking.getSeats().stream()
                .map(BookingSeat::getSeatNumber)
                .collect(Collectors.toSet());
        if (!bookingSeats.containsAll(seatsToCancel)) {
            log.warn("Invalid seats requested for cancellation: {}", seatsToCancel);
            throw new SeatNotAvailableException("Some seats are not part of this booking");
        }

        // Check if seats are already canceled or refunded
        for (String seatNumber : seatsToCancel) {
            if (booking.getSeats().stream()
                    .filter(seat -> seat.getSeatNumber().equals(seatNumber))
                    .anyMatch(seat -> seat.getStatus().equals(BookingStatus.CANCELED))) {
                log.warn("Seat {} already canceled for bookingId: {}", seatNumber, bookingId);
                throw new SeatNotAvailableException("Seat already canceled: " + seatNumber);
            }
            if (refundRepository.existsByBookingIdAndSeatNumber(bookingId, seatNumber)) {
                log.warn("Refund already processed for seat {} in bookingId: {}", seatNumber, bookingId);
                throw new SeatNotAvailableException("Refund already processed for seat: " + seatNumber);
            }
        }

        // Process refunds
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setBookingId(bookingId);
        refundRequest.setSeatNumbers(seatsToCancel);
        RefundResponse refundResponse = paymentClient.processRefund(refundRequest);

        // Update seats and store refunds
        int cancelCount = 0;
        double totalRefundAmount = 0.0;
        for (BookingSeat seat : booking.getSeats()) {
            if (seatsToCancel.contains(seat.getSeatNumber()) && !seat.getStatus().equals(BookingStatus.CANCELED)) {
                seat.setStatus(BookingStatus.CANCELED);
                cancelCount++;

                // Store refund
                Refund refund = new Refund();
                refund.setBookingId(bookingId);
                refund.setSeatNumber(seat.getSeatNumber());
                refund.setAmount(refundResponse.getSeatRefunds().get(seat.getSeatNumber()));
                refund.setStatus(refundResponse.getStatus());
                refund.setTransactionId(refundResponse.getTransactionId());
                refundRepository.save(refund);

                totalRefundAmount += refund.getAmount();
            }
        }

        // Update booking status
        boolean allSeatsCanceled = booking.getSeats().stream()
                .allMatch(seat -> seat.getStatus().equals(BookingStatus.CANCELED));
        booking.setStatus(allSeatsCanceled ? BookingStatus.CANCELED : BookingStatus.PARTIAL_CANCELED);

        // Update showtime
        if (cancelCount > 0) {
            Showtime showtime = booking.getShowtime();
            showtime.setAvailableSeats(showtime.getAvailableSeats() + cancelCount);
            showtimeRepository.save(showtime);
            bookingRepository.save(booking);
            log.info("Canceled {} seats for bookingId: {}, refund initiated for amount: {}",
                    cancelCount, bookingId, totalRefundAmount);
        }

        // Map response
        BookingResponse response = bookingMapper.mapToBookingResponse(
                booking,
                cancelCount > 0 ? "INITIATED" : null,
                totalRefundAmount
        );
        return List.of(response);
    }
}
