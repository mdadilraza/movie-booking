package com.eidiko.movie_service.controller;

import com.eidiko.movie_service.dto.MoviePageResponse;
import com.eidiko.movie_service.dto.MovieRequest;
import com.eidiko.movie_service.dto.MovieResponse;
import com.eidiko.movie_service.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {
    private static final Set<String> VALID_SORT_FIELDS =
            Set.of("title", "genre", "releaseDate", "duration");

    private final MovieService movieService;

    @PostMapping(value = "/addMovie", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> addMovie(@ModelAttribute MovieRequest request) throws IOException {
        MovieResponse response = movieService.createMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        MovieResponse response = movieService.getMovieById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getAllMovies")
    public ResponseEntity<MoviePageResponse> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        log.info("Received request: page={}, size={}, sortBy={}, direction={}", page, size, sortBy, direction);

        if (!VALID_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortBy);
        }

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(Sort.Order.desc(sortBy))
                : Sort.by(Sort.Order.asc(sortBy));

        Pageable pageable = PageRequest.of(page, size, sort);
        log.info("pageable value: {}", pageable);
        MoviePageResponse allMovies = movieService.getAllMovies(pageable);
        log.info("allMovies after service: {}", pageable);
        return ResponseEntity.ok(allMovies);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> updateMovie(
            @PathVariable Long id,
            @RequestBody MovieRequest request
    ) {
        MovieResponse response = movieService.updateMovie(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/fallback/movies/{id}")
    public Mono<MovieResponse> getCachedMovie(@PathVariable Long id) {
        return Mono.just(movieService.getMovieById(id))
                .onErrorResume(e -> Mono.empty()); // return empty or cached stale if any
    }
}
