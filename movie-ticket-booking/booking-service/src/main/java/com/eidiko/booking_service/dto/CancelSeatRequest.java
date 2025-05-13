package com.eidiko.booking_service.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data

public class CancelSeatRequest {
    @NotEmpty(message = "Seat numbers must not be empty or provide at least one seat")
    private Set<@NotEmpty(message = "Seat number cannot be blank")String> seats;
}
