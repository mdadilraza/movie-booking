package com.eidiko.booking_service.exception;

public class SeatAlreadyCancelledException extends RuntimeException {
    public SeatAlreadyCancelledException(String s) {
        super(s);
    }
}
