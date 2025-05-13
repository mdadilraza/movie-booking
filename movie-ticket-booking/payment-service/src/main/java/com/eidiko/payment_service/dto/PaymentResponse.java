package com.eidiko.payment_service.dto;

import lombok.Data;

@Data
public class PaymentResponse {
    private Long id;
    private Long bookingId;
    private Double amount;
    private String transactionId;
    private String status;
}
