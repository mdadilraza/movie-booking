package com.eidiko.booking_service.mapper;

import com.eidiko.booking_service.dto.BookingResponse;
import com.eidiko.booking_service.dto.ShowtimeResponse;
import com.eidiko.booking_service.entity.Booking;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BookingMapper {

    private final ModelMapper modelMapper;

    public BookingMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public ShowtimeResponse mapToShowtimeResponse(com.eidiko.booking_service.entity.Showtime showtime) {
        return modelMapper.map(showtime, ShowtimeResponse.class);
    }

    public BookingResponse mapToBookingResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getId());
        response.setUserId(booking.getUserId());
        response.setShowtimeId(booking.getShowtime().getId());
        response.setStatus(booking.getStatus());


        Set<BookingResponse.SeatStatus> seatStatusSet = booking.getSeats().stream()
                .map(seat -> {
                    BookingResponse.SeatStatus seatStatus = new BookingResponse.SeatStatus();
                    seatStatus.setSeatNumber(seat.getSeatNumber());
                    seatStatus.setStatus(seat.getStatus());
                    return seatStatus;
                })
                .collect(Collectors.toSet());

        response.setSeats(seatStatusSet);
        return response;
    }
}

