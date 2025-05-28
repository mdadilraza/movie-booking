package com.eidiko.booking_service.client;

import com.eidiko.booking_service.constants.PaymentStatus;
import com.eidiko.booking_service.dto.PaymentRequest;
import com.eidiko.booking_service.dto.PaymentResponse;
import com.eidiko.booking_service.dto.RefundRequest;
import com.eidiko.booking_service.dto.RefundResponse;
import com.eidiko.booking_service.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentClientImpl implements PaymentClient {

    private final WebClient webClient;
    private final TokenService tokenService;


    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        try {
            return webClient.post()
                    .uri("/api/payments")
                    .header("Authorization", "Bearer " + tokenService.extractToken())
                    .bodyValue(request)
                    .exchangeToMono(response -> {
                        if (response.statusCode().is2xxSuccessful()) {
                            return response.bodyToMono(PaymentResponse.class);
                        } else {
                            return response.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        log.error("Payment service returned status {} with body {}", response.statusCode(), body);
                                        return Mono.error(new RuntimeException("Payment service error"));
                                    });
                        }
                    })
                    .onErrorResume(ex -> {
                        log.error("Fallback: Payment service is unavailable.", ex);
                        return Mono.just(PaymentResponse.builder()
                                .bookingId(request.getBookingId())
                                .amount(request.getAmount())
                                .status(PaymentStatus.FAILED)
                                .transactionId("N/A")
                                .build());
                    })
                    .block();

        } catch (Exception e) {
            log.error("Exception occurred during payment creation: {}", e.getMessage());
            return PaymentResponse.builder()
                    .bookingId(request.getBookingId())
                    .amount(request.getAmount())
                    .status(PaymentStatus.FAILED)
                    .transactionId("N/A")
                    .build();
        }
    }

    @Override
    public RefundResponse processRefund(RefundRequest request) {
        try {
            return webClient
                    .post()
                    .uri("/api/payments/refund")
                    .header("Authorization", "Bearer " + tokenService.extractToken())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(RefundResponse.class)
                    .onErrorResume(ex -> {
                        log.error("Fallback: Refund processing failed. Reason: {}", ex.getMessage());
                        return Mono.just(RefundResponse.builder()
                                .status(PaymentStatus.FAILED)
                                        .bookingId(request.getBookingId())
                                        .transactionId("N/A")
                                        .seatRefunds(Map.of())
                                .build());
                    })
                    .block();
        } catch (Exception e) {
            log.error("Exception during refund: {}", e.getMessage());
            return RefundResponse.builder()
                    .status(PaymentStatus.FAILED)
                    .bookingId(request.getBookingId())
                    .transactionId("N/A")
                    .seatRefunds(Map.of())
                    .build();
        }
    }
}
