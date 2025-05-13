package com.eidiko.booking_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.Set;


@Data
public class BookingRequest {
    @NotNull(message = "Showtime ID must not be null")
    private Long showtimeId;

    @NotEmpty(message = "Seat numbers must not be empty or provide at least one seat")
    private Set<@NotEmpty(message = "Seat number cannot be blank") String> seatNumbers;
}
