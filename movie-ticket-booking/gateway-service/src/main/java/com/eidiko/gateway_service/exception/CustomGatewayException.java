package com.eidiko.gateway_service.exception;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
@Getter
public class CustomGatewayException extends RuntimeException {

    private final HttpStatus status;

    public CustomGatewayException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }


}

