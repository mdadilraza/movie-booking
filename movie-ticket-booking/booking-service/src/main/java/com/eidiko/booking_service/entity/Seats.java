package com.eidiko.booking_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "SEATS", schema = "ORDER_DB", indexes = {
        @Index(name = "idx_typeName", columnList = "typeName")
})
@Getter
@Setter
@NoArgsConstructor
public class Seats {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seat_type_seq")
    @SequenceGenerator(name = "seat_type_seq", schema = "ORDER_DB", sequenceName = "SEAT_TYPE_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "Seat_TYPE_NAME", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private SeatType typeName;

    @Column(name = "BASE_PRICE", nullable = false)
    private Double basePrice;
}
