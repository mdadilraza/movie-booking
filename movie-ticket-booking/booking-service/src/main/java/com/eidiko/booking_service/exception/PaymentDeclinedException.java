package com.eidiko.booking_service.exception;

public class PaymentDeclinedException extends RuntimeException{
    public PaymentDeclinedException(String s) {
        super(s);
    }
}
