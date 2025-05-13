package com.eidiko.booking_service.strategy.validation;

import com.eidiko.booking_service.constants.BookingStatus;
import com.eidiko.booking_service.entity.Booking;
import com.eidiko.booking_service.exception.SeatAlreadyCancelledException;
import org.springframework.stereotype.Component;

@Component
public class AllSeatsCanceledValidationStrategy implements CancellationValidationStrategy{
    @Override
    public void validate(Booking booking) {
        boolean anyMatch = booking.getSeats()
                .stream()
                .allMatch(bookingSeat -> bookingSeat.getStatus().equals(BookingStatus.CANCELED));
        if (anyMatch){
            throw new SeatAlreadyCancelledException("All Seat is already canceled.");
        }
    }
}
