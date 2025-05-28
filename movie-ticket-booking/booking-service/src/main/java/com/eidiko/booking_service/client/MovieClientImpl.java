package com.eidiko.booking_service.client;

import com.eidiko.booking_service.dto.MovieResponse;
import com.eidiko.booking_service.exception.MovieNotAvailableException;
import com.eidiko.booking_service.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class MovieClientImpl implements MovieClient {
    private final TokenService tokenService;
    private final WebClient webClient;

    @Override
    public void validateMovie(Long movieId) {
        try {
            MovieResponse movie = fetchMovieFromService(movieId);
            cacheMovie(movie); // Update the cache after successful fetch
        } catch (Exception e) {
            log.error("Movie service unavailable, attempting fallback from cache.");
            MovieResponse cached = getCachedMovie(movieId);
            if (cached == null) {
                throw new MovieNotAvailableException("Movie service down and movie not in cache");
            }
            log.info("Movie fallback succeeded using cache for movieId {}", movieId);
        }
    }

    public MovieResponse fetchMovieFromService(Long movieId) {
        String token = tokenService.extractToken();
        return webClient.get()
                .uri("/api/movies/id/{movieId}", movieId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(MovieResponse.class)
                .block();
    }

    @CachePut(value = "movieCache", key = "#movie.id")
    public MovieResponse cacheMovie(MovieResponse movie) {
        return movie;
    }

    @Cacheable(value = "movieCache", key = "#movieId")
    public MovieResponse getCachedMovie(Long movieId) {
        log.info("Fetching movieId {} from cache as fallback", movieId);
        return null; // Will return cached value if available
    }
}
