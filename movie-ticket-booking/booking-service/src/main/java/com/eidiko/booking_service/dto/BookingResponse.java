package com.eidiko.booking_service.dto;

import lombok.Data;

import java.util.List;
@Data
public class BookingResponse {
    private Long bookingId;
    private Long showtimeId;
    private Long userId;
    private String status;
    private List<SeatDto> seats;
    private String refundStatus;
    private Double refundAmount;
    private Long paymentId;
    private String paymentStatus;
    private String transactionId;
    private Double paymentAmount;





}
