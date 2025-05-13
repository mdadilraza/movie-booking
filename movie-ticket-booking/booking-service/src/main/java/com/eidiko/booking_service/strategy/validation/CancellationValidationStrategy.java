package com.eidiko.booking_service.strategy.validation;

import com.eidiko.booking_service.entity.Booking;

public interface CancellationValidationStrategy {
    void validate(Booking booking);
}
