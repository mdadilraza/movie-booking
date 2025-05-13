package com.eidiko.booking_service.repository;

import com.eidiko.booking_service.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    boolean existsByBookingIdAndSeatNumber(long bookingId, String seatNumber);
}
