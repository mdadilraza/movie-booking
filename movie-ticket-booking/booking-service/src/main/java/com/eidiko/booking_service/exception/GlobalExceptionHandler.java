package com.eidiko.booking_service.exception;

import jakarta.validation.ConstraintViolationException;
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
    private static final String BASE_URI = "https://example.com/errors/";

    // 1. Hibernate Validator (DTO field validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        LOGGER.warn("Validation failed: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Validation Error",
                "Invalid request fields.",
                BASE_URI + "validation"
        );
        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    // 2. Hibernate Constraint-level validation
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        LOGGER.warn("Constraint violation: {}", ex.getMessage());
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Constraint Violation",
                ex.getMessage(),
                BASE_URI + "constraint-violation"
        );
    }

    // 3. BookingNotFoundException
    @ExceptionHandler(BookingNotFoundException.class)
    public ProblemDetail handleBookingNotFound(BookingNotFoundException ex) {
        return createProblemDetail(HttpStatus.NOT_FOUND, "Booking Not Found", ex.getMessage(), BASE_URI + "booking-not-found");
    }

    // 4. SeatNotFoundException
    @ExceptionHandler(SeatNotAvailableException.class)
    public ProblemDetail handleSeatNotFound(SeatNotAvailableException ex) {
        return createProblemDetail(HttpStatus.NOT_FOUND, "Seat Not Found", ex.getMessage(), BASE_URI + "seat-not-found");
    }

    // 5. SeatAlreadyCancelledException
    @ExceptionHandler(SeatAlreadyCancelledException.class)
    public ProblemDetail handleSeatAlreadyCancelled(SeatAlreadyCancelledException ex) {
        return createProblemDetail(HttpStatus.CONFLICT, "Seat Already Cancelled", ex.getMessage(), BASE_URI + "seat-already-cancelled");
    }

    // 6. InvalidCancellationTimeException
    @ExceptionHandler(InvalidCancellationTimeException.class)
    public ProblemDetail handleInvalidCancellationTime(InvalidCancellationTimeException ex) {
        return createProblemDetail(HttpStatus.BAD_REQUEST, "Invalid Cancellation Time", ex.getMessage(), BASE_URI + "invalid-cancellation-time");
    }

    // 7. MovieNotAvailableException
    @ExceptionHandler(MovieNotAvailableException.class)
    public ProblemDetail handleMovieNotAvailable(MovieNotAvailableException ex) {
        return createProblemDetail(HttpStatus.NOT_FOUND, "Movie Not Available", ex.getMessage(), BASE_URI + "movie-not-available");
    }

    // 8. ShowtimeNotFoundException
    @ExceptionHandler(ShowtimeNotFoundException.class)
    public ProblemDetail handleShowtimeNotFound(ShowtimeNotFoundException ex) {
        return createProblemDetail(HttpStatus.NOT_FOUND, "Showtime Not Found", ex.getMessage(), BASE_URI + "showtime-not-found");
    }

    // 9. UnauthorizedBookingActionException
    @ExceptionHandler(UnauthorizedBookingActionException.class)
    public ProblemDetail handleUnauthorizedAction(UnauthorizedBookingActionException ex) {
        return createProblemDetail(HttpStatus.FORBIDDEN, "Unauthorized Booking Action", ex.getMessage(), BASE_URI + "unauthorized-booking-action");
    }

    // 10. UserNotFoundException
    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        return createProblemDetail(HttpStatus.NOT_FOUND, "User Not Found", ex.getMessage(), BASE_URI + "user-not-found");
    }

    // 11. Authorization/AccessDenied
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        LOGGER.warn("Access denied: {}", ex.getMessage());
        return createProblemDetail(HttpStatus.FORBIDDEN, "Access Denied", "You are not authorized to access this resource.", BASE_URI + "access-denied");
    }

    // 12. Malformed request JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleBadRequest(HttpMessageNotReadableException ex) {
        LOGGER.warn("Malformed JSON: {}", ex.getMessage());
        return createProblemDetail(HttpStatus.BAD_REQUEST, "Malformed Request", "Check your JSON syntax.", BASE_URI + "bad-request");
    }

    // 13. Generic fallback
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneralError(Exception ex) {
        LOGGER.error("Unexpected error", ex);
        return createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error", "An unexpected error occurred.", BASE_URI + "internal-error");
    }

    private ProblemDetail createProblemDetail(HttpStatus status, String title, String detail, String typeUri) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setType(URI.create(typeUri));
        return problemDetail;
    }
}
