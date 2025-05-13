package com.eidiko.booking_service.exception;

public class SeatNotAvailableException extends RuntimeException{
    public SeatNotAvailableException(String message){
        super(message);
    }
    public SeatNotAvailableException(String message, Throwable throwable){
        super(message ,throwable);
    }
}
