package com.eidiko.booking_service.service;

import com.eidiko.booking_service.dto.*;
import com.eidiko.booking_service.entity.Booking;
import com.eidiko.booking_service.entity.Showtime;

import java.util.List;

public interface BookingService {
    ShowtimeResponse createShowtime(ShowtimeRequest showtimeRequest);
    BookingResponse createBooking(BookingRequest bookingRequest);
    List<BookingResponse> getUserBookings();
    List<ShowtimeResponse> getShowtimeByMovie(long movieId);

    List<BookingResponse> cancelSeats(long bookingId, CancelSeatRequest cancelSeatRequest);
}
