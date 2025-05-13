package com.eidiko.booking_service.dto;

import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be greater than 0")
    private Double amount;

    @NotNull(message = "Number of seats is required")
    @Min(value = 1, message = "There must be at least 1 seat")
    private Integer numberOfSeats;
}
