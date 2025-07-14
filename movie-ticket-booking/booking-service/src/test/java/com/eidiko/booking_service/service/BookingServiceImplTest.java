package com.eidiko.booking_service.service;

import com.eidiko.booking_service.adapter.UserClient;
import com.eidiko.booking_service.client.MovieClient;
import com.eidiko.booking_service.client.PaymentClient;
import com.eidiko.booking_service.constants.BookingStatus;
import com.eidiko.booking_service.dto.*;
import com.eidiko.booking_service.entity.*;


import com.eidiko.booking_service.exception.*;
import com.eidiko.booking_service.mapper.BookingMapper;
import com.eidiko.booking_service.repository.BookingRepository;
import com.eidiko.booking_service.repository.BookingSeatRepository;
import com.eidiko.booking_service.repository.SeatRepository;
import com.eidiko.booking_service.repository.ShowtimeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDateTime;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock
    private AuthorizationService authService;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private MovieClient movieClient;
    @Mock
    private PaymentClient paymentClient;
    @Mock
    private UserClient userClient;
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private BookingSeatRepository bookingSeatRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void testCreateShowtime_success() {

        ShowtimeRequest request = new ShowtimeRequest();
        request.setMovieId(101L);
        request.setTheaterName("INOX");
        request.setShowtimeDate(LocalDateTime.of(2025, 7, 10, 18, 0));
        request.setTotalSeats(100);

        Showtime savedShowtime = new Showtime();
        savedShowtime.setId(1L);
        savedShowtime.setMovieId(101L);
        savedShowtime.setTheaterName("INOX");
        savedShowtime.setShowtimeDate(request.getShowtimeDate());
        savedShowtime.setTotalSeats(100);
        savedShowtime.setAvailableSeats(100);

        ShowtimeResponse response = new ShowtimeResponse();
        response.setId(1L);
        response.setMovieId(101L);
        response.setTheaterName("INOX");
        response.setShowtimeDate(request.getShowtimeDate());

        // Act
        when(authService.isAdmin()).thenReturn(true);
        doNothing().when(movieClient).validateMovie(101L);
        when(showtimeRepository.save(any())).thenReturn(savedShowtime);
        when(bookingMapper.mapToShowtimeResponse(savedShowtime)).thenReturn(response);
        ShowtimeResponse result = bookingService.createShowtime(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(101L, result.getMovieId());
        assertEquals("INOX", result.getTheaterName());
        verify(movieClient, times(1)).validateMovie(101L);
        verify(bookingMapper, times(1)).mapToShowtimeResponse(savedShowtime);

    }

    @Test
    void testCreateShowtime_notAdmin_shouldThrowException() {
        ShowtimeRequest request = new ShowtimeRequest();
        request.setMovieId(202L);

        when(authService.isAdmin()).thenReturn(false);

        Exception ex = assertThrows(UnauthorizedBookingActionException.class,
                () -> bookingService.createShowtime(request));

        assertTrue(ex.getMessage().contains("Only admin can create showtimes"));
        verify(movieClient, never()).validateMovie(any());
        verify(showtimeRepository, never()).save(any());
    }

    @Test
     void testGetShowtimeByMovie_success() {
        long movieId = 1L;
        doNothing().when(movieClient).validateMovie(movieId);

        Showtime showtime = new Showtime();
        showtime.setId(1L);
        showtime.setMovieId(movieId);
        showtime.setShowtimeDate(LocalDateTime.of(2025,7,14,13,25,22));
        showtime.setTheaterName("INOX");
        showtime.setTotalSeats(300);
        showtime.setAvailableSeats(200);
        showtime.setActive(true);
        showtime.setCreatedAt(LocalDateTime.now());
        showtime.setUpdatedAt(LocalDateTime.now());
        List<Showtime> mockShowtimeList = List.of(showtime);
        when(showtimeRepository.findByMovieIdAndIsActiveTrue(movieId)).thenReturn(mockShowtimeList);
        ShowtimeResponse expectedResponse = ShowtimeResponse.builder()
                .id(1L)
                .theaterName("INOX")
                .updatedAt(LocalDateTime.now())
                .active(true)
                .availableSeats(200)
                .totalSeats(300)
                .movieId(movieId)
                .showtimeDate(LocalDateTime.of(2025,7,14,13,25,22))
                .createdAt(LocalDateTime.now())
                .build();
        when(bookingMapper.mapToShowtimeResponse(showtime)).thenReturn(expectedResponse);
        List<ShowtimeResponse> result = bookingService.getShowtimeByMovie(movieId);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L ,movieId);
        assertEquals(200 ,result.get(0).getAvailableSeats());
        assertEquals("INOX", result.get(0).getTheaterName());
        verify(movieClient, times(1)).validateMovie(movieId);
        verify(showtimeRepository, times(1)).findByMovieIdAndIsActiveTrue(movieId);
        verify(bookingMapper, times(1)).mapToShowtimeResponse(showtime);
    }
    @Test
    void testGetShowtimeByMovie_movieNotFound_throwsException() {
        long movieId =999L;
        doThrow(new MovieNotAvailableException("Movie Not Found")).when(movieClient).validateMovie(movieId);
        MovieNotAvailableException ex = assertThrows(MovieNotAvailableException.class, () ->
                bookingService.getShowtimeByMovie(movieId));
        assertEquals("Movie Not Found" ,ex.getMessage());
        verify(movieClient, times(1)).validateMovie(movieId);
        verify(showtimeRepository, never()).findByMovieIdAndIsActiveTrue(any());

    }

    @Test
    void testCreateBooking_success() {
        // 1. Arrange
        BookingRequest request = new BookingRequest();
        request.setShowtimeId(1L);
        request.setSeatNumbers(Set.of("A1", "A2"));
        request.setSeatTypes(Map.of("A1", 101L, "A2", 102L));

        Showtime showtime = new Showtime();
        showtime.setId(1L);
        showtime.setMovieId(55L);
        showtime.setAvailableSeats(100);

        Booking bookingToSave = new Booking();
        bookingToSave.setUserId(1001L);
        bookingToSave.setShowtime(showtime);
        bookingToSave.setStatus(BookingStatus.CONFIRMED);

        Booking savedBooking = new Booking();
        savedBooking.setId(999L);
        savedBooking.setShowtime(showtime);
        savedBooking.setStatus(BookingStatus.CONFIRMED);
        savedBooking.setUserId(1001L);
        savedBooking.setSeats(new HashSet<>());

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setBookingId(999L);
        paymentResponse.setStatus("SUCCESS");

        BookingResponse bookingResponse = new BookingResponse();
        bookingResponse.setBookingId(999L);
        bookingResponse.setRefundAmount(500.0);

        // 2. Mocks
        when(showtimeRepository.findByIdAndIsActiveTrueWithLock(1L)).thenReturn(Optional.of(showtime));
        doNothing().when(movieClient).validateMovie(55L);
        when(bookingSeatRepository.findBookedSeatsByShowtimeIdAndSeatNumbers(eq(1L), anySet()))
                .thenReturn(Collections.emptyList());
        when(authService.getCurrentUsername()).thenReturn("john_doe");
        when(userClient.getUserId("john_doe")).thenReturn(1001L);
        when(seatRepository.findAllById(anyList())).thenReturn(List.of(
                new Seats(101L, SeatType.STANDARD, 250.0),
                new Seats(102L, SeatType.VIP, 950.0)
        ));

        when(bookingRepository.save(any())).thenReturn(savedBooking);
        when(paymentClient.createPayment(any())).thenReturn(paymentResponse);
        when(showtimeRepository.save(any())).thenReturn(showtime);
        when(bookingMapper.mapToBookingResponse(any(), any())).thenReturn(bookingResponse);

        // 3. Act
        BookingResponse result = bookingService.createBooking(request);

        // 4. Assert
        assertNotNull(result);
        assertEquals(999L, result.getBookingId());
        assertEquals(500.0, result.getRefundAmount());

        verify(showtimeRepository).save(showtime);
        verify(paymentClient).createPayment(any());
        verify(bookingMapper).mapToBookingResponse(savedBooking, paymentResponse);
    }
    

    @Test
    void testCreateBooking_showtimeNotFound() {
        BookingRequest request = new BookingRequest();
        request.setShowtimeId(1L);

        when(showtimeRepository.findByIdAndIsActiveTrueWithLock(1L)).thenReturn(Optional.empty());

        assertThrows(ShowtimeNotFoundException.class, () -> bookingService.createBooking(request));
    }
    @Test
    void testCreateBooking_notEnoughSeats() {
        BookingRequest request = new BookingRequest();
        request.setShowtimeId(1L);
        request.setSeatNumbers(Set.of("A1", "A2"));

        Showtime showtime = new Showtime();
        showtime.setId(1L);
        showtime.setMovieId(101L);
        showtime.setAvailableSeats(1);

        when(showtimeRepository.findByIdAndIsActiveTrueWithLock(1L)).thenReturn(Optional.of(showtime));
        doNothing().when(movieClient).validateMovie(101L);

        assertThrows(SeatNotAvailableException.class, () -> bookingService.createBooking(request));
    }
    @Test
    void testCreateBooking_seatsAlreadyBooked() {
        BookingRequest request = new BookingRequest();
        request.setShowtimeId(1L);
        request.setSeatNumbers(Set.of("A1"));

        Showtime showtime = new Showtime();
        showtime.setId(1L);
        showtime.setMovieId(101L);
        showtime.setAvailableSeats(10);

        BookingSeat seat = new BookingSeat();
        seat.setSeatNumber("A1");
        seat.setStatus(BookingStatus.CONFIRMED);

        when(showtimeRepository.findByIdAndIsActiveTrueWithLock(1L)).thenReturn(Optional.of(showtime));
        doNothing().when(movieClient).validateMovie(101L);
        when(bookingSeatRepository.findBookedSeatsByShowtimeIdAndSeatNumbers(1L, Set.of("A1"))).thenReturn(List.of(seat));

        assertThrows(SeatAlreadyBookedException.class, () -> bookingService.createBooking(request));
    }
    @Test
    void testCreateBooking_invalidSeatTypes() {
        BookingRequest request = new BookingRequest();
        request.setShowtimeId(1L);
        request.setSeatNumbers(Set.of("A1", "A2"));
        request.setSeatTypes(Map.of("A1", 10L)); // missing A2

        Showtime showtime = new Showtime();
        showtime.setId(1L);
        showtime.setMovieId(101L);
        showtime.setAvailableSeats(10);

        when(showtimeRepository.findByIdAndIsActiveTrueWithLock(1L)).thenReturn(Optional.of(showtime));
        doNothing().when(movieClient).validateMovie(101L);
        when(bookingSeatRepository.findBookedSeatsByShowtimeIdAndSeatNumbers(any(), any())).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> bookingService.createBooking(request));
    }
    @Test
    void testCreateBooking_userNotFound() {
        BookingRequest request = new BookingRequest();
        request.setShowtimeId(1L);
        request.setSeatNumbers(Set.of("A1"));
        request.setSeatTypes(Map.of("A1", 1L));

        Showtime showtime = new Showtime();
        showtime.setId(1L);
        showtime.setMovieId(101L);
        showtime.setAvailableSeats(10);

        when(showtimeRepository.findByIdAndIsActiveTrueWithLock(1L)).thenReturn(Optional.of(showtime));
        doNothing().when(movieClient).validateMovie(101L);
        when(bookingSeatRepository.findBookedSeatsByShowtimeIdAndSeatNumbers(any(), any())).thenReturn(List.of());
        when(authService.getCurrentUsername()).thenReturn("john_doe");
        when(userClient.getUserId("john_doe")).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> bookingService.createBooking(request));
    }






}
