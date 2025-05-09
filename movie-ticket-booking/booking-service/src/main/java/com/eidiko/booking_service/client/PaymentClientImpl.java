package com.eidiko.booking_service.client;

import com.eidiko.booking_service.dto.PaymentResponse;
import com.eidiko.booking_service.dto.RefundResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
@Component
@RequiredArgsConstructor
public class PaymentClientImpl implements PaymentClient{
    private final WebClient webClient;
    public PaymentResponse createPayment(PaymentRequest request) {
        return webClient
                .post()
                .uri("http://payment-service/api/payments")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .block();
    }

    public RefundResponse processRefund(RefundRequest request) {
        return webClient
                .post()
                .uri("http://payment-service/api/payments/refund")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RefundResponse.class)
                .block();
    }
}
