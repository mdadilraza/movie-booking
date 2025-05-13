package com.eidiko.payment_service.dto;

import lombok.Data;

import java.util.Map;
@Data
public class RefundResponse {
    private Long bookingId;
    private Map<String, Double> seatRefunds;
    private String status;
    private String transactionId;
}
