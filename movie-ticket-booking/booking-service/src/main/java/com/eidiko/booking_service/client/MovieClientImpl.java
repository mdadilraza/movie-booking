package com.eidiko.booking_service.client;

import com.eidiko.booking_service.dto.MovieResponse;
import com.eidiko.booking_service.exception.MovieNotAvailableException;
import com.eidiko.booking_service.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class MovieClientImpl implements MovieClient {

    private final TokenService tokenService;
    private final WebClient webClient;

    @Override
    public void validateMovie(Long movieId) {
        String token = tokenService.extractToken();

        try {
            log.info("Validating movie with ID: {}", movieId);

            // Check if movie exists
            webClient.get()
                    .uri("/api/movies/id/{movieId}", movieId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            this::handleError
                    )
                    .bodyToMono(Void.class)
                    .block();

            // Fetch and cache the movie details
            MovieResponse movie = fetchMovieFromService(movieId);
            cacheMovie(movie);

        } catch (WebClientResponseException e) {
            log.error("Movie not found or error in movie-service: {}", e.getMessage());
            throw new MovieNotAvailableException("Movie not found: " + e.getResponseBodyAsString());

        } catch (Exception e) {
            log.warn("Movie service unavailable, attempting fallback from cache: {}", e.getMessage());
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

    private Mono<? extends Throwable> handleError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(errorBody -> {
                    HttpStatusCode status = response.statusCode();
                    log.error("Movie service error ({}): {}", status, errorBody);
                    return Mono.error(new WebClientResponseException(
                            status.value(),
                            "Movie service error",
                            response.headers().asHttpHeaders(),
                            errorBody.getBytes(),
                            null
                    ));
                });
    }

    @CachePut(value = "movieCache", key = "#movie.id")
    public MovieResponse cacheMovie(MovieResponse movie) {
        return movie;
    }

    @Cacheable(value = "movieCache", key = "#movieId")
    public MovieResponse getCachedMovie(Long movieId) {
        log.info("Looking up movieId {} from cache...", movieId);
        return null;
    }
}
