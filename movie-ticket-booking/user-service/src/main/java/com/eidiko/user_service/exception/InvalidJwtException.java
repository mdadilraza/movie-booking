package com.eidiko.user_service.exception;

public class InvalidJwtException extends RuntimeException{
    public InvalidJwtException(String message ,Throwable throwable ){
        super(message,throwable);
    }
}
