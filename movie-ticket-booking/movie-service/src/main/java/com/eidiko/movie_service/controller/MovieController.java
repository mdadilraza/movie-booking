package com.eidiko.movie_service.controller;

import com.eidiko.movie_service.dto.MovieRequest;
import com.eidiko.movie_service.dto.MovieResponse;
import com.eidiko.movie_service.dto.TestDto;
import com.eidiko.movie_service.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @PostMapping(value = "/addMovie" ,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieResponse> addMovie(@ModelAttribute MovieRequest request
    ) throws IOException {

            MovieResponse response = movieService.createMovie(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping("/id/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {

            MovieResponse response = movieService.getMovieById(id);
            return ResponseEntity.ok(response);

    }

    @GetMapping("/getAllMovies")
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        List<MovieResponse> movies = movieService.getAllMovies();
        return ResponseEntity.ok(movies);
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

}