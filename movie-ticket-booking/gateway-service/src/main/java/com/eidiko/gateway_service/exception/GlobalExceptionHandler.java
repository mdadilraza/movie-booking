package com.eidiko.gateway_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class GlobalExceptionHandler {

    private static final String BASE_ERROR_URI = "https://example.com/gateway/errors/";

    @ExceptionHandler(CustomGatewayException.class)
    public ProblemDetail handleCustomGatewayException(CustomGatewayException ex) {
        log.warn("Handled CustomGatewayException: {}", ex.getMessage());
        return buildProblemDetail(ex.getStatus(), ex.getMessage(), BASE_ERROR_URI + "custom-error");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unhandled exception in gateway", ex);
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected internal error occurred.",
                BASE_ERROR_URI + "internal-server-error");
    }

    private ProblemDetail buildProblemDetail(HttpStatus status, String detail, String typeUri) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(status.getReasonPhrase());
        pd.setType(URI.create(typeUri));
        return pd;
    }
}

