package com.eidiko.booking_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShowtimeRequest {
    @NotNull(message = "Movie ID is required")
    @Positive(message = "Movie ID must be positive")
    private Long movieId;

    @NotBlank(message = "Theater name is required")
    @Size(max = 100, message = "Theater name must be less than 100 characters")
    private String theaterName;

    @NotNull(message = "Showtime date is required")
    @FutureOrPresent(message = "Showtime date must be in the present or future")
    private LocalDateTime showtimeDate;

    @NotNull(message = "Total seats is required")
    @Positive(message = "Total seats must be positive")
    @Max(value = 1000, message = "Total seats cannot exceed 1000")
    private Integer totalSeats;
}
