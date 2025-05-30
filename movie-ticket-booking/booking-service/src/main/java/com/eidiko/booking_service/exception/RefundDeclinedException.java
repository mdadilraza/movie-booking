package com.eidiko.booking_service.exception;

public class RefundDeclinedException extends RuntimeException{
    public RefundDeclinedException(String s) {
        super(s);
    }
}
