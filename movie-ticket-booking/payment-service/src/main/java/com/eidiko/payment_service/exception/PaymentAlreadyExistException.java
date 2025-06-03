package com.eidiko.payment_service.exception;

public class PaymentAlreadyExistException extends RuntimeException{
    public PaymentAlreadyExistException(String message){
        super(message);
    }
}
