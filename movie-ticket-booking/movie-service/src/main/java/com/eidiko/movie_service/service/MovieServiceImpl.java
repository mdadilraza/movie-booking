package com.eidiko.movie_service.service;
import com.eidiko.movie_service.dto.MovieRequest;
import com.eidiko.movie_service.dto.MovieResponse;
import com.eidiko.movie_service.entity.Movie;
import com.eidiko.movie_service.exception.MovieNotFoundException;
import com.eidiko.movie_service.mapper.MapToResponse;
import com.eidiko.movie_service.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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

    @Override
    public List<MovieResponse> getAllMovies() {
        return movieRepository.findByIsActiveTrue()
                .stream()
                .map(MapToResponse::mapToResponse)
                .toList();
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