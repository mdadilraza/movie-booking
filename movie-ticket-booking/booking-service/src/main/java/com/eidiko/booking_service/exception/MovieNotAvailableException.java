package com.eidiko.booking_service.exception;
public class MovieNotAvailableException extends RuntimeException{
    public MovieNotAvailableException(String message){
        super(message);
    }
    public MovieNotAvailableException(String message ,Throwable ex){
        super(message ,ex);
    }
}
