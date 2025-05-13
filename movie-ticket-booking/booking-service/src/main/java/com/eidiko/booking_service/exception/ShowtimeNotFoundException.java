package com.eidiko.booking_service.exception;

public class ShowtimeNotFoundException extends RuntimeException {
    public ShowtimeNotFoundException(String showtimeNotFoundOrDeleted) {
        super(showtimeNotFoundOrDeleted);
    }
}
