package com.eidiko.booking_service.mapper;

import com.eidiko.booking_service.dto.BookingResponse;
import com.eidiko.booking_service.dto.PaymentResponse;
import com.eidiko.booking_service.dto.SeatDto;
import com.eidiko.booking_service.dto.ShowtimeResponse;
import com.eidiko.booking_service.entity.Booking;
import com.eidiko.booking_service.entity.BookingSeat;
import com.eidiko.booking_service.entity.Showtime;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingMapper {

    private final ModelMapper modelMapper;

    @PostConstruct
    public void configureModelMapper() {
        // Configure mapping for Booking to BookingResponse
        modelMapper.addMappings(new PropertyMap<Booking, BookingResponse>() {
            @Override
            protected void configure() {
                map().setBookingId(source.getId());
                map().setShowtimeId(source.getShowtime().getId());
                map().setUserId(source.getUserId());
                map().setStatus(source.getStatus());

            }
        });

        // Configure mapping for BookingSeat to SeatDto
        modelMapper.addMappings(new PropertyMap<BookingSeat, SeatDto>() {
            @Override
            protected void configure() {
                map().setSeatNumber(source.getSeatNumber());
                map().setStatus(source.getStatus());
            }
        });

    }

    public ShowtimeResponse mapToShowtimeResponse(Showtime showtime) {
        log.debug("Mapping Showtime entity to ShowtimeResponse for showtimeId: {}", showtime.getId());
        return modelMapper.map(showtime, ShowtimeResponse.class);
    }

    public BookingResponse mapToBookingResponse(Booking booking) {
        log.debug("Mapping Booking entity to BookingResponse for bookingId: {}", booking.getId());
        return modelMapper.map(booking, BookingResponse.class);
    }

    public BookingResponse mapToBookingResponse(Booking booking, PaymentResponse paymentResponse) {
        log.debug("Mapping Booking entity to BookingResponse for bookingId: {} with PaymentResponse", booking.getId());
        BookingResponse response = modelMapper.map(booking, BookingResponse.class);
        response.setSeats(mapSeats(booking.getSeats()));
        response.setPaymentId(paymentResponse.getId());
        response.setPaymentStatus(paymentResponse.getStatus());
        response.setTransactionId(paymentResponse.getTransactionId());
        response.setPaymentAmount(paymentResponse.getAmount());
        response.setRefundStatus(null);
        response.setRefundAmount(null);
        return response;
    }

    public BookingResponse mapToBookingResponse(Booking booking, String refundStatus, Double refundAmount) {
        log.debug("Mapping Booking entity to BookingResponse for bookingId: {} with refundStatus: {}, refundAmount: {}",
                booking.getId(), refundStatus, refundAmount);
        BookingResponse response = modelMapper.map(booking, BookingResponse.class);
        response.setSeats(mapSeats(booking.getSeats()));
        response.setPaymentId(0L); // Default for cancelSeats
        response.setPaymentStatus(null); // Default for cancelSeats
        response.setTransactionId(null); // Default for cancelSeats
        response.setPaymentAmount(0.0); // Default for cancelSeats
        response.setRefundStatus(refundStatus);
        response.setRefundAmount(refundAmount);
        return response;
    }

    private List<SeatDto> mapSeats(Set<BookingSeat> seats) {
        log.debug("Mapping {} BookingSeat entities to SeatDto", seats.size());
        return seats.stream()
                .map(seat -> modelMapper.map(seat, SeatDto.class))
                .collect(Collectors.toList());
    }

}

