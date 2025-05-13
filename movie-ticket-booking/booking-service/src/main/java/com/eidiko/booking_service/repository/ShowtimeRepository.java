package com.eidiko.booking_service.repository;

import com.eidiko.booking_service.entity.Showtime;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface ShowtimeRepository extends JpaRepository<Showtime ,Long> {

    List<Showtime> findByMovieIdAndIsActiveTrue(Long movieId);

    Optional<Showtime> findByIdAndIsActiveTrue(@NotNull(message = "Showtime ID must not be null") Long showtimeId);
}
