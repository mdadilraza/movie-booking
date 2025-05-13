package com.eidiko.booking_service.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String status;

    @Column(name = "transaction_id")
    private String transactionId;
}
