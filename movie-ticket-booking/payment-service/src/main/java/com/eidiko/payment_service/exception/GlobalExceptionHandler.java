package com.eidiko.payment_service.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String BASE_ERROR_URI = "https://api.eidiko.com/payment-service/errors/";

    @ExceptionHandler(PaymentAlreadyExistException.class)
    public ProblemDetail handlePaymentException(PaymentAlreadyExistException ex) {
        log.warn("Payment Already Exists: {}", ex.getMessage());
        HttpStatus status;
        String title;
        String typeUri;


            status = HttpStatus.CONFLICT;
            title = "Payment Already Exists";
            typeUri = BASE_ERROR_URI + "payment-already-exists";


        return createProblemDetail(status, title, ex.getMessage(), typeUri);
    }
    @ExceptionHandler(PaymentNotFoundException.class)
    public ProblemDetail handlePaymentNotFoundException(PaymentNotFoundException ex) {
        log.warn("Payment not found : {}", ex.getMessage());
        HttpStatus status;
        String title;
        String typeUri;


        status = HttpStatus.NOT_FOUND;
        title = "Payment Not Found";
        typeUri = BASE_ERROR_URI + "payment-not-found";


        return createProblemDetail(status, title, ex.getMessage(), typeUri);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "Invalid request data",
                BASE_ERROR_URI + "validation-failed"
        );
        problemDetail.setProperty("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ProblemDetail handleWebClientResponseException(WebClientResponseException ex) {
        log.error("WebClient error: {}", ex.getMessage());
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String title = "External Service Error";
        String detail = "Failed to communicate with external service: " + ex.getMessage();

        return createProblemDetail(
                status,
                title,
                detail,
                BASE_ERROR_URI + "external-service-error"
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred",
                BASE_ERROR_URI + "internal-server-error"
        );
    }

    private ProblemDetail createProblemDetail(HttpStatus status, String title, String detail, String typeUri) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setType(URI.create(typeUri));
        return problemDetail;
    }
}
