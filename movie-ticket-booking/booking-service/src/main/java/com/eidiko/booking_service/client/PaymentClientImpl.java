package com.eidiko.booking_service.client;

import com.eidiko.booking_service.dto.PaymentRequest;
import com.eidiko.booking_service.dto.PaymentResponse;
import com.eidiko.booking_service.dto.RefundRequest;
import com.eidiko.booking_service.dto.RefundResponse;
import com.eidiko.booking_service.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
@Component
@RequiredArgsConstructor
public class PaymentClientImpl implements PaymentClient{
    private final WebClient webClient;
    private final TokenService tokenService;
    public PaymentResponse createPayment(PaymentRequest request) {
        return webClient
                .post()
                .uri("/api/payments")
                .header("Authorization", "Bearer " + tokenService.extractToken())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .block();
    }

    public RefundResponse processRefund(RefundRequest request) {
        return webClient
                .post()
                .uri("/api/payments/refund")
                .header("Authorization", "Bearer " + tokenService.extractToken())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RefundResponse.class)
                .block();
    }
}
