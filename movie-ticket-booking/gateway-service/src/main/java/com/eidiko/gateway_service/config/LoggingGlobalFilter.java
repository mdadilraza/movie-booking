package com.eidiko.gateway_service.config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(0) // Ensure the filter runs early in the chain
@Slf4j
public class LoggingGlobalFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Log the incoming request
        log.info("Incoming Request: Method: {}, URL: {}, Headers: {}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI(),
                exchange.getRequest().getHeaders());

        // Process the chain and log the response
        return chain.filter(exchange).doOnSuccess(aVoid -> {
            HttpHeaders headers = exchange.getResponse().getHeaders();
            log.info("Outgoing Response: Status Code: {}, Headers: {}",
                    exchange.getResponse().getStatusCode(),
                    headers);
        });
    }
}