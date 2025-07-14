package com.eidiko.booking_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeatDto {
    private String seatNumber;
    private String status;
    private String seatType;
    private double price;
}
