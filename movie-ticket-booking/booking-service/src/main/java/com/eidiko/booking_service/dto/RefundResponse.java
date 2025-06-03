package com.eidiko.booking_service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
@Data
@Builder
public class RefundResponse {
    private Long bookingId;
    private Map<String, Double> seatRefunds;
    private String status;
    private String transactionId;
    private double refundedAmount;
}
