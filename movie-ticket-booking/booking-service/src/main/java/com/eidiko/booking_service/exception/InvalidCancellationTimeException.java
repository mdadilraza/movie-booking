package com.eidiko.booking_service.exception;

public class InvalidCancellationTimeException extends RuntimeException {
    public InvalidCancellationTimeException(String message){
        super(message);
    }
    public InvalidCancellationTimeException(String message ,Throwable throwable){
        super(message,throwable);
    }
}
