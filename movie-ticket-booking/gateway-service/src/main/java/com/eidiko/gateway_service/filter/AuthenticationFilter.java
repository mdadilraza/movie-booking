package com.eidiko.gateway_service.filter;

import com.eidiko.gateway_service.config.AuthProperties;
import com.eidiko.gateway_service.exception.CustomGatewayException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Map;

@Component
@Slf4j

public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final WebClient.Builder webClientBuilder;
    private final AuthProperties authProperties ;



    public AuthenticationFilter(WebClient.Builder webClientBuilder, AuthProperties authProperties) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
        this.authProperties = authProperties;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();
            request.getHeaders().forEach((name, values) ->
                    log.info("Header '{}': {}", name, values));

            log.info("Incoming request path: {}", path);

            // Bypass token validation for open endpoints
            if (authProperties.getOpenEndpoints().contains(path)) {
                log.info("Open_Endpoints {}" ,authProperties.getOpenEndpoints());
                log.info("Skipping authentication for open endpoint: {}", path);
                return chain.filter(exchange);
            }

            // Check for Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new CustomGatewayException("Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7); // remove "Bearer "

            // Validate token via Auth service
            return webClientBuilder.build()
                    .post()
                    .uri("http://localhost:8081/api/auth/validate")
                    .bodyValue(Map.of("token", token))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("Auth service responded with error: {}", errorBody);
                                        return Mono.error(new CustomGatewayException(
                                                "Token validation failed from auth service", HttpStatus.UNAUTHORIZED
                                        ));
                                    })
                    )

                    .bodyToMono(Map.class)
                    .flatMap(response -> {
                        String username = (String) response.get("username");
                        String role = (String) response.get("role");

                        log.info("Token validated successfully: user={}, role={}", username, role);

                        // Mutate request with user info headers
                        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                .header("X-Auth-User", username)
                                .header("X-Auth-Role", role)
                                .build();

                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    });
        };
    }

    public static class Config {
        Config(){}
    }
}
