package com.eidiko.booking_service.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString(exclude = "booking")
@EqualsAndHashCode(exclude = "booking")
public class BookingSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booking_seat_seq")
    @SequenceGenerator(name = "booking_seat_seq", schema = "ORDER_DB"
            , sequenceName = "BOOKING_SEAT_SEQ", allocationSize = 1)
    private long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonBackReference
    private Booking booking;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Column(nullable = false)
    private String status;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SEAT_ID", nullable = false)
    private Seats seats;
}
