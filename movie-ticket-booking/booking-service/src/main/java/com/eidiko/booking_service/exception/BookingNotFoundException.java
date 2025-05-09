package com.eidiko.booking_service.exception;

public class BookingNotFoundException extends RuntimeException{
    public BookingNotFoundException(String message){
        super(message);
    }
    public BookingNotFoundException(String message ,Throwable ex){
        super(message ,ex);
    }
}
