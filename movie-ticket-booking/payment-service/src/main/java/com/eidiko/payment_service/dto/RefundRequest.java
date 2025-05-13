package com.eidiko.payment_service.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;
@Data
public class RefundRequest {
    @NotNull(message = "Booking ID must not be null")
    private Long bookingId;

    @NotEmpty(message = "Seat numbers list must not be empty")
    private Set<@NotEmpty(message = "Seat number must not be empty") String> seatNumbers;
}
