package com.eidiko.booking_service.dto;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class ShowtimeResponse {
    private Long id;
    private Long movieId;
    private String theaterName;
    private LocalDateTime showtimeDate;
    private Integer totalSeats;
    private Integer availableSeats;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean active;
}
