package com.eidiko.booking_service.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
@ToString(exclude = "seats")
@EqualsAndHashCode(exclude = "seats")
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_user_id", columnList = "userId"),
        @Index(name = "idx_booking_showtime_id", columnList = "showtime_id")
})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booking_seq")
    @SequenceGenerator(name = "booking_seq", schema = "ORDER_DB", sequenceName = "BOOKING_SEQ", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;


    @Column(nullable = false)
    private String status;

    @Column(name = "TOTAL_PRICE", nullable = false)
    private Double totalPrice;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true
    ,fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<BookingSeat> seats = new HashSet<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
