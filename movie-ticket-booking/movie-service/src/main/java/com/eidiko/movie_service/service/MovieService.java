package com.eidiko.movie_service.service;

import com.eidiko.movie_service.dto.MoviePageResponse;
import com.eidiko.movie_service.dto.MovieRequest;
import com.eidiko.movie_service.dto.MovieResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.io.IOException;



public interface MovieService {
    MovieResponse createMovie(MovieRequest request
    ) throws IOException;
    MovieResponse getMovieById(Long id);
    MoviePageResponse getAllMovies(Pageable pageable);
    MovieResponse updateMovie(Long id, MovieRequest request);
    void deleteMovie(Long id);
}
