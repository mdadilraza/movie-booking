package com.eidiko.gateway_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

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

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatusException(ResponseStatusException ex) {
        if (ex.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
            log.warn("Downstream service returned 503: {}", ex.getMessage());
            return buildProblemDetail(HttpStatus.SERVICE_UNAVAILABLE,
                    "A downstream service is unavailable. Please try again later.",
                    BASE_ERROR_URI + "service-unavailable");
        }
        // Let other statuses fall back to generic
        return handleGenericException(ex);
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

