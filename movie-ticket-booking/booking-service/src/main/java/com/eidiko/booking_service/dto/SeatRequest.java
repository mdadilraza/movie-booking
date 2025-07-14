package com.eidiko.booking_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SeatRequest {

    @NotBlank(message = "Seat type name must not be blank")
    private String typeName; // STANDARD, PREMIUM, VIP

    @NotNull(message = "Base price must not be null")
    @Positive(message = "Base price must be positive")
    private Double basePrice;
}
