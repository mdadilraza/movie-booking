package com.eidiko.movie_service.repository;
import com.eidiko.movie_service.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    Page<Movie> findByIsActiveTrue(Pageable pageable);
    Optional<Movie> findByIdAndIsActiveTrue(Long id);
}
