package com.eidiko.gateway_service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/fallback")
@Slf4j
public class GatewayFallbackHandler {

    @RequestMapping(value = "/user-service", method = {RequestMethod.GET, RequestMethod.POST})
    public ProblemDetail userServiceFallback() {
        return buildProblemDetail("User service is currently unavailable.",
                "https://example.com/gateway/errors/user-service-down");
    }

    @RequestMapping(value = "/movie-service", method = {RequestMethod.GET, RequestMethod.POST})
    public ProblemDetail movieServiceFallback() {
        return buildProblemDetail("Movie service is currently unavailable.",
                "https://example.com/gateway/errors/movie-service-down");
    }

    @RequestMapping(value = "/booking-service", method = {RequestMethod.GET, RequestMethod.POST})
    public ProblemDetail bookingServiceFallback() {
        return buildProblemDetail("Booking service is currently unavailable.",
                "https://example.com/gateway/errors/booking-service-down");
    }

    @RequestMapping(value = "/payment-service", method = {RequestMethod.GET, RequestMethod.POST})
    public ProblemDetail paymentServiceFallback() {
        return buildProblemDetail("Payment service is currently unavailable.",
                "https://example.com/gateway/errors/payment-service-down");
    }

    private ProblemDetail buildProblemDetail(String detail, String typeUri) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, detail);
        problem.setTitle(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase());
        problem.setType(URI.create(typeUri));
        log.warn("Returning fallback: [{}] {}", HttpStatus.SERVICE_UNAVAILABLE, detail);
        return problem;
    }
}
