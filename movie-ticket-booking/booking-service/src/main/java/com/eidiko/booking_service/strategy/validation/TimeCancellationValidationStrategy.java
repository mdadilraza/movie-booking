package com.eidiko.booking_service.strategy.validation;

import com.eidiko.booking_service.entity.Booking;
import com.eidiko.booking_service.exception.InvalidCancellationTimeException;
import com.eidiko.booking_service.exception.SeatAlreadyCancelledException;
import com.eidiko.booking_service.exception.SeatNotAvailableException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
@Component
public class TimeCancellationValidationStrategy implements CancellationValidationStrategy {

    @Override
    public void validate(Booking booking) {
        LocalDateTime showtime = booking.getShowtime().getShowtimeDate();
        if (LocalDateTime.now().isAfter(showtime.minusHours(2))) {
            throw new InvalidCancellationTimeException("Cancellations are only allowed up to 2 hours before the showtime.");
        }
    }
}
