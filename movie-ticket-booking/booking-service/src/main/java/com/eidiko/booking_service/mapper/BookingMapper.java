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
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;


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



        Converter<BookingSeat, String> seatTypeConverter = ctx -> {
            if (ctx.getSource().getSeats() != null && ctx.getSource().getSeats().getTypeName() != null) {
                return ctx.getSource().getSeats().getTypeName().name();
            }
            return null;
        };

        Converter<BookingSeat, Double> priceConverter = ctx -> {
            if (ctx.getSource().getSeats() != null) {
                return ctx.getSource().getSeats().getBasePrice();
            }
            return null;
        };

        modelMapper.typeMap(BookingSeat.class, SeatDto.class).addMappings(mapper -> {
            mapper.map(BookingSeat::getSeatNumber, SeatDto::setSeatNumber);
            mapper.map(BookingSeat::getStatus, SeatDto::setStatus);
            mapper.using(seatTypeConverter).map(src -> src, SeatDto::setSeatType);
            mapper.using(priceConverter).map(src -> src, SeatDto::setPrice);
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
                .toList();
    }

}

