package com.eidiko.movie_service.repository;
import com.eidiko.movie_service.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByIsActiveTrue();
    Optional<Movie> findByIdAndIsActiveTrue(Long id);
}
