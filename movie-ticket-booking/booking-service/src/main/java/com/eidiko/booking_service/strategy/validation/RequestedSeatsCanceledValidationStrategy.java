package com.eidiko.booking_service.strategy.validation;
import com.eidiko.booking_service.entity.Booking;
import com.eidiko.booking_service.entity.BookingSeat;
import com.eidiko.booking_service.exception.SeatNotAvailableException;

import java.util.Set;

public class RequestedSeatsCanceledValidationStrategy implements CancellationValidationStrategy {

    private final Set<String> requestedSeats;

    public RequestedSeatsCanceledValidationStrategy(Set<String> requestedSeats) {
        this.requestedSeats = requestedSeats;
    }

    @Override
    public void validate(Booking booking) {
        for (String seatNumber : requestedSeats) {
            BookingSeat seat = booking.getSeats().stream()
                    .filter(s -> s.getSeatNumber().equals(seatNumber))
                    .findFirst()
                    .orElseThrow(() -> new SeatNotAvailableException("Seat " + seatNumber + " does not exist."));
            if ("CANCELED".equals(seat.getStatus())) {
                throw new SeatNotAvailableException("Seat " + seatNumber + " is already canceled.");
            }
        }
    }
}

