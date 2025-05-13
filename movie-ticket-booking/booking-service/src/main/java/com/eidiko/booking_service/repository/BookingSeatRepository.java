package com.eidiko.booking_service.repository;

import com.eidiko.booking_service.entity.BookingSeat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface BookingSeatRepository extends JpaRepository<BookingSeat ,Long> {
    List<BookingSeat> findBookedSeatsByShowtimeIdAndSeatNumbers(@NotNull(message = "Showtime ID must not be null") Long showtimeId, @NotEmpty(message = "Seat numbers must not be empty or provide at least one seat") Set<@NotEmpty(message = "Seat number cannot be blank") String> seatNumbers);
}
