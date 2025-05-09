package com.eidiko.movie_service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String BASE_ERROR_URI = "https://example.com/errors/";

    @ExceptionHandler(MovieNotFoundException.class)
    public ProblemDetail handleMovieNotFoundException(MovieNotFoundException ex) {
        LOGGER.warn("Movie not found: {}", ex.getMessage());
        return createProblemDetail(HttpStatus.NOT_FOUND, "Movie Not Found", ex.getMessage(), BASE_ERROR_URI + "movie-not-found");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        LOGGER.warn("Validation failed: {}", ex.getMessage());

        Map<String, String> validationErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(error.getField(), error.getDefaultMessage());
        }

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "One or more fields have invalid values.",
                BASE_ERROR_URI + "validation-error"
        );
        problemDetail.setProperty("errors", validationErrors);
        return problemDetail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        LOGGER.error("Invalid request body", ex);
        return createProblemDetail(HttpStatus.BAD_REQUEST, "Invalid Request Body", "Malformed JSON or incorrect data format.", BASE_ERROR_URI + "bad-request");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(AccessDeniedException ex) {
        LOGGER.warn("Access denied", ex);
        return createProblemDetail(HttpStatus.FORBIDDEN, "Access Denied", "You do not have permission to perform this action.", BASE_ERROR_URI + "access-denied");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        LOGGER.error("Unexpected error occurred", ex);
        return createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred.", BASE_ERROR_URI + "internal-server-error");
    }

    private ProblemDetail createProblemDetail(HttpStatus status, String title, String detail, String typeUri) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setType(URI.create(typeUri));
        return problemDetail;
    }
}
