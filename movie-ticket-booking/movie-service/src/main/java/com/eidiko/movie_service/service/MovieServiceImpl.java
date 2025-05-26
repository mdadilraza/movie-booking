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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public MovieResponse createMovie(MovieRequest request) throws IOException {
       log.info("uploading file to cloudinary");
        String url = cloudinaryService.uploadImage(request.getMoviePoster());
        log.info("moviePoster Url from cloudinary -{}", url);
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
    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found or Deleted "));
        return MapToResponse.mapToResponse(movie);
    }

    @Transactional(readOnly = true)
    public MoviePageResponse getAllMovies(Pageable pageable) {
        log.info("Fetching all active movies with pageable: {}", pageable);
       Page<Movie> moviePage = movieRepository.findByIsActiveTrue(pageable);

       // Page<Movie> moviePage = movieRepository.findAll(pageable);
        log.info("movie page  data: {}",moviePage);
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
        response.setSortField(pageable.getSort().isSorted() ? pageable.getSort().get().findFirst().get().getProperty() : null);
        response.setSortDirection(pageable.getSort().isSorted() ? pageable.getSort().get().findFirst().get().getDirection().name().toLowerCase() : null);

        log.info("Fetched {} movies for page {}", moviePage.getNumberOfElements(), pageable.getPageNumber());
        return response;
    }

    @Override
    public MovieResponse updateMovie(Long id, MovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found or deleted with Id :" + id));
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setGenre(request.getGenre());
        movie.setDuration(request.getDuration());
        movie.setReleaseDate(request.getReleaseDate());
        Movie updatedMovie = movieRepository.save(movie);
        return MapToResponse.mapToResponse(updatedMovie);
    }

    @Override
    public void deleteMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found or Already deleted "));
        movie.setActive(false); // Soft delete
        movieRepository.save(movie);
    }
}