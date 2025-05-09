package com.eidiko.booking_service.adapter;
import com.eidiko.booking_service.dto.UserResponse;
import com.eidiko.booking_service.exception.UserNotFoundException;
import com.eidiko.booking_service.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserClientImpl implements UserClient {

    private final WebClient webClient;
    private final TokenService tokenService;

    @Override
    public Long getUserId(String username) {
        String token = tokenService.extractToken();

        try {
            log.info("Requesting user ID for username: {}", username);

            UserResponse response = webClient.get()
                    .uri("http://localhost:8085/api/users/{username}", username)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            this::handleError
                    )
                    .bodyToMono(UserResponse.class)
                    .block();

            if (response == null ) {
                throw new UserNotFoundException("User ID not found in response for username: " + username);
            }

            log.info("User ID retrieved for username {}: {}", username, response.id());
            return response.id();

        } catch (WebClientResponseException e) {
            log.error("User not found or client error: {}", e.getMessage());
            throw new UserNotFoundException("User not found: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Error calling user service for username {}: {}", username, e.getMessage());
            throw new UserNotFoundException("Unable to get user ID for username " + username);
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
