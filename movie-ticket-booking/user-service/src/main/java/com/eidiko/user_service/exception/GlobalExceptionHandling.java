package com.eidiko.user_service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
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
public class GlobalExceptionHandling {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandling.class);
    private static final String BASE_ERROR_URI = "https://example.com/errors/";

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ProblemDetail> handleUserAlreadyExistException(UserAlreadyExistException e) {
        LOGGER.warn("User already exists: {}", e.getMessage());
        return buildResponseEntity(HttpStatus.CONFLICT,
                "User Already Exists",
                "User is already registered, please try with a different username or email.",
                BASE_ERROR_URI + "user-already-exists");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUserNotFoundException(UserNotFoundException e) {
        LOGGER.warn("User not found: {}", e.getMessage());
        return buildResponseEntity(HttpStatus.NOT_FOUND,
                "User Not Found",
                "User with the provided username or email could not be found.",
                BASE_ERROR_URI + "user-not-found");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationExceptions(MethodArgumentNotValidException ex) {
        LOGGER.warn("Validation failed: {}", ex.getMessage());

        Map<String, String> validationErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(error.getField(), error.getDefaultMessage());
        }

        ProblemDetail problemDetail = buildProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "One or more fields have invalid values.",
                BASE_ERROR_URI + "validation-error"
        );
        problemDetail.setProperty("errors", validationErrors);

        return new ResponseEntity<>(problemDetail, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        LOGGER.error("Invalid request body {}", ex.getMessage());
        return buildResponseEntity(HttpStatus.BAD_REQUEST,
                "Invalid Request Body",
                "Malformed JSON or incorrect data format.",
                BASE_ERROR_URI + "bad-request");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(AccessDeniedException ex) {
        LOGGER.warn("Access denied {}", ex.getMessage());
        return buildResponseEntity(HttpStatus.FORBIDDEN,
                "Access Denied",
                "You do not have permission to perform this action.",
                BASE_ERROR_URI + "access-denied");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
        LOGGER.error("Unexpected error occurred {}", ex.getMessage());
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred.",
                BASE_ERROR_URI + "internal-server-error");
    }
    @ExceptionHandler(InvalidJwtException.class)
    public ResponseEntity<ProblemDetail> handleInvalidJwtException(InvalidJwtException ex){
        LOGGER.error(ex.getMessage());
        return buildResponseEntity(HttpStatus.UNAUTHORIZED,
                "InvalidJwtException",
                "Invalid Jwt Tokens Or Expired Tokens",
                BASE_ERROR_URI + "invalid-jwt-tokens");

    }
    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleJwtAuthenticationException(JwtAuthenticationException ex){
        LOGGER.error(ex.getMessage());
        return buildResponseEntity(HttpStatus.UNAUTHORIZED,
                "JwtAuthenticationException",
                "Failed to Validate Token ",
                BASE_ERROR_URI + "invalid-jwt-tokens");

    }


    // Helper to create ProblemDetail object
    private ProblemDetail buildProblemDetail(HttpStatus status, String title, String detail, String typeUri) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setType(URI.create(typeUri));
        return problemDetail;
    }

    // Helper to wrap ProblemDetail in ResponseEntity
    private ResponseEntity<ProblemDetail> buildResponseEntity(HttpStatus status, String title, String detail, String typeUri) {
        ProblemDetail problemDetail = buildProblemDetail(status, title, detail, typeUri);
        return new ResponseEntity<>(problemDetail, status);
    }
}
