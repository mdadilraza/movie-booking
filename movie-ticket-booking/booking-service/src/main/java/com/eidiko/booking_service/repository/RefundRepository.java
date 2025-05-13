package com.eidiko.booking_service.repository;

import com.eidiko.booking_service.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefundRepository extends JpaRepository<Refund, Long> {

@Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END 
            FROM Refund r WHERE r.bookingId = :bookingId AND r.seatNumber = :seatNumber
        """)
    boolean existsByBookingIdAndSeatNumber(@Param("bookingId") long bookingId,
                                           @Param("seatNumber") String seatNumber);
}
