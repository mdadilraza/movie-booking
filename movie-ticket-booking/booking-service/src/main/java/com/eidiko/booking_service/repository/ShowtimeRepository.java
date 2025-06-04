package com.eidiko.booking_service.repository;

import com.eidiko.booking_service.entity.Showtime;
import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ShowtimeRepository extends JpaRepository<Showtime ,Long> {

    List<Showtime> findByMovieIdAndIsActiveTrue(Long movieId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Showtime s WHERE s.id = :id AND s.isActive = true")
    Optional<Showtime> findByIdAndIsActiveTrueWithLock(Long id);}
