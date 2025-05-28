package com.eidiko.movie_service.service;

import com.eidiko.movie_service.dto.MoviePageResponse;
import com.eidiko.movie_service.dto.MovieRequest;
import com.eidiko.movie_service.dto.MovieResponse;
import com.eidiko.movie_service.entity.Movie;
import com.eidiko.movie_service.exception.MovieNotFoundException;
import com.eidiko.movie_service.mapper.MapToResponse;
import com.eidiko.movie_service.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @CacheEvict(value = {"movies", "moviesPage"}, allEntries = true)
    public MovieResponse createMovie(MovieRequest request) throws IOException {
        log.info("Uploading file to cloudinary");
        String url = cloudinaryService.uploadImage(request.getMoviePoster());
        log.info("MoviePoster URL from cloudinary - {}", url);
        Movie movie = new Movie();
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setGenre(request.getGenre());
        movie.setDuration(request.getDuration());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setMoviePosterUrl(url);
        Movie savedMovie = movieRepository.save(movie);
        return MapToResponse.mapToResponse(savedMovie);
    }

    @Override
    @Cacheable(value = "movies", key = "#id")
    public MovieResponse getMovieById(Long id) {
        log.info("Fetching movie {} from DB", id);
        Movie movie = movieRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found or deleted"));
        return MapToResponse.mapToResponse(movie);
    }

    @Override
    @Cacheable(value = "moviesPage", key = "{#pageable.pageNumber, #pageable.pageSize, #pageable.sort}")
    @Transactional(readOnly = true)
    public MoviePageResponse getAllMovies(Pageable pageable) {
        log.info("Fetching all movies from DB with paging: {}", pageable);
        Page<Movie> moviePage = movieRepository.findByIsActiveTrue(pageable);

        log.info("Movie page data: {}", moviePage);
        MoviePageResponse response = new MoviePageResponse();
        response.setMovies(moviePage.getContent().stream()
                .map(movie -> {
                    MovieResponse dto = new MovieResponse();
                    dto.setId(movie.getId());
                    dto.setTitle(movie.getTitle());
                    dto.setPosterUrl(movie.getMoviePosterUrl());
                    dto.setDescription(movie.getDescription());
                    dto.setGenre(movie.getGenre());
                    dto.setDuration(movie.getDuration());
                    dto.setReleaseDate(movie.getReleaseDate());
                    dto.setActive(movie.isActive());
                    return dto;
                })
                .toList());
        response.setPageNumber(moviePage.getNumber());
        response.setPageSize(moviePage.getSize());
        response.setTotalElements(moviePage.getTotalElements());
        response.setTotalPages(moviePage.getTotalPages());
        response.setFirst(moviePage.isFirst());
        response.setLast(moviePage.isLast());
        response.setSortField(pageable.getSort().isSorted() ? pageable.getSort().get().findFirst().orElseThrow().getProperty() : null);
        response.setSortDirection(pageable.getSort().isSorted() ? pageable.getSort().get().findFirst().orElseThrow().getDirection().name().toLowerCase() : null);

        log.info("Fetched {} movies for page {}", moviePage.getNumberOfElements(), pageable.getPageNumber());
        return response;
    }

    @Override
    @CacheEvict(value = {"movies", "moviesPage"}, allEntries = true)
    public MovieResponse updateMovie(Long id, MovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found or deleted with Id: " + id));
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setGenre(request.getGenre());
        movie.setDuration(request.getDuration());
        movie.setReleaseDate(request.getReleaseDate());
        Movie updatedMovie = movieRepository.save(movie);
        return MapToResponse.mapToResponse(updatedMovie);
    }

    @Override
    @CacheEvict(value = {"movies", "moviesPage"}, allEntries = true)
    public void deleteMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found or already deleted"));
        movie.setActive(false); // Soft delete
        movieRepository.save(movie);
    }
}
