package com.eidiko.booking_service.exception;

public class UnauthorizedBookingActionException extends RuntimeException {
    public UnauthorizedBookingActionException(String message) {
        super(message);
    }
}