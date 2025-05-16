package com.eidiko.booking_service.repository;

import com.eidiko.booking_service.entity.SeatType;
import com.eidiko.booking_service.entity.Seats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seats ,Long> {
    boolean existsByTypeName(SeatType seatType);
}
