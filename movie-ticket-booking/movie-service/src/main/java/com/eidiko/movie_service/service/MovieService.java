package com.eidiko.movie_service.service;

import com.eidiko.movie_service.dto.MovieRequest;
import com.eidiko.movie_service.dto.MovieResponse;

import java.util.List;


public interface MovieService {
    MovieResponse createMovie(MovieRequest request);
    MovieResponse getMovieById(Long id);
    List<MovieResponse> getAllMovies();
    MovieResponse updateMovie(Long id, MovieRequest request);
    void deleteMovie(Long id);
}
