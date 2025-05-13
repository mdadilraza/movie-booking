package com.eidiko.booking_service.client;
import com.eidiko.booking_service.exception.MovieNotAvailableException;
import com.eidiko.booking_service.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class MovieClientImpl implements MovieClient{
    private final TokenService tokenService;
    private final WebClient webClient;
    @Override
    public void validateMovie(Long movieId) {
        String token = tokenService.extractToken();

        try {
            log.info("Requesting movie to validate by movieId: {}", movieId);

            webClient.get()
                    .uri("/api/users/{username}", movieId)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            this::handleError
                    )
                    .bodyToMono(Void.class)
                    .block();

        }
    catch (WebClientResponseException e) {
        log.error("Movie not found or client error: {}", e.getMessage());
        throw new MovieNotAvailableException("Movie not found: " + e.getResponseBodyAsString());
    } catch (Exception e) {
        log.error("Error calling Movie service for MovieId {}: {}", movieId, e.getMessage());
        throw new MovieNotAvailableException("Unable to get Movie  for movieId " + movieId);
    }
    }

    private Mono<? extends Throwable> handleError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(errorBody -> {
                    HttpStatusCode status = response.statusCode();
                    log.error("User service error ({}): {}", status, errorBody);
                    return Mono.error(new WebClientResponseException(
                            status.value(),
                            "User service error",
                            response.headers().asHttpHeaders(),
                            errorBody.getBytes(),
                            null
                    ));
                });
    }
    }
