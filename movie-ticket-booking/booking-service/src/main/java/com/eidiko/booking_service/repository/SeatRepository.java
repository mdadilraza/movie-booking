package com.eidiko.booking_service.repository;

import com.eidiko.booking_service.entity.SeatType;
import com.eidiko.booking_service.entity.Seats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatRepository extends JpaRepository<Seats ,Long> {
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Seats s WHERE s.typeName = :typeName")
    boolean existsByTypeName( @Param("typeName")SeatType typeName);

}
