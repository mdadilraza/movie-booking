package com.eidiko.movie_service.mapper;

import com.eidiko.movie_service.dto.MovieResponse;
import com.eidiko.movie_service.entity.Movie;

public class MapToResponse {
    private MapToResponse() {

    }

    public static MovieResponse mapToResponse(Movie movie) {
        MovieResponse response = new MovieResponse();
        response.setId(movie.getId());
        response.setTitle(movie.getTitle());
        response.setDescription(movie.getDescription());
        response.setGenre(movie.getGenre());
        response.setDuration(movie.getDuration());
        response.setReleaseDate(movie.getReleaseDate());
        response.setCreatedAt(movie.getCreatedAt());
        response.setUpdatedAt(movie.getUpdatedAt());
        response.setActive(movie.isActive());
        return response;
    }
}
