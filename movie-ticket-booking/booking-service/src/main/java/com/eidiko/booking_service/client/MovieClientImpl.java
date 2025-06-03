package com.eidiko.booking_service.client;
import com.eidiko.booking_service.dto.MovieResponse;
import com.eidiko.booking_service.exception.MovieNotAvailableException;
import com.eidiko.booking_service.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
@Slf4j
public class MovieClientImpl implements MovieClient {

    private final TokenService tokenService;
    private final WebClient webClient;

    @Override
    public void validateMovie(Long movieId) {
        try {
            log.info("Validating movie with ID: {}", movieId);

            // Try movie-service
            MovieResponse movieResponse = fetchMovieFromService(movieId);
            log.info("movie-fetched with movie title {}",movieResponse.getTitle());

        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                log.error("Movie not found in movie-service: {}", e.getMessage());
                throw new MovieNotAvailableException("Movie not found: " + e.getResponseBodyAsString());
            }

            log.error("Movie service error ({}): {}", e.getStatusCode(), e.getMessage());
            throw new MovieNotAvailableException("Movie service is currently unavailable. Please try again later.");
        } catch (Exception e) {
            log.error("Movie service unreachable: {}", e.getMessage());
            throw new MovieNotAvailableException("Movie service is currently unavailable. Please try again later.");
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
}
