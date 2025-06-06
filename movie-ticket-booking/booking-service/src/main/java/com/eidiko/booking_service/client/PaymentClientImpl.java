package com.eidiko.booking_service.client;
import com.eidiko.booking_service.constants.CommonConstants;
import com.eidiko.booking_service.dto.PaymentRequest;
import com.eidiko.booking_service.dto.PaymentResponse;
import com.eidiko.booking_service.dto.RefundRequest;
import com.eidiko.booking_service.dto.RefundResponse;
import com.eidiko.booking_service.exception.PaymentDeclinedException;
import com.eidiko.booking_service.exception.RefundDeclinedException;
import com.eidiko.booking_service.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentClientImpl implements PaymentClient {

    private final WebClient webClient;
    private final TokenService tokenService;
    private final ReactiveResilience4JCircuitBreakerFactory circuitBreakerFactory;


    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        String token = tokenService.extractToken();

        Mono<PaymentResponse> responseMono = webClient.post()
                .uri("/api/payments")
                .header(CommonConstants.HEADER, CommonConstants.BEARER+" " + token)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponse.class);

        return circuitBreakerFactory.create(CommonConstants.PAYMENT_SERVICE_CIRCUIT_BREAKER)
                .run(responseMono, throwable -> {
                    log.error(" Payment service call failed. Aborting booking. Reason: {}", throwable.getMessage());
                    return Mono.error(new PaymentDeclinedException("Payment was declined due to insufficient funds or invalid details."));
                }).block();

    }

    @Override
    public RefundResponse processRefund(RefundRequest request) {
        String token = tokenService.extractToken();

        Mono<RefundResponse> responseMono = webClient.post()
                .uri("/api/payments/refund")
                .header(CommonConstants.HEADER, CommonConstants.BEARER+" "+ token)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RefundResponse.class);

        return circuitBreakerFactory.create(CommonConstants.PAYMENT_SERVICE_CIRCUIT_BREAKER)
                .run(responseMono, throwable -> {
                    log.error("Payment service failed to Refund . Reason: {}", throwable.getMessage());
                    return Mono.error(new RefundDeclinedException("Refund failed. Please try again later."));
                }).block();
    }

    @Override
    public RefundResponse refundPaymentByBookingId(Long id) {
        String token = tokenService.extractToken();

        Mono<RefundResponse> responseMono = webClient.post()
                .uri("/api/payments/payment/refund/{bookingId}",id)
                .header(CommonConstants.HEADER, CommonConstants.BEARER+" "+ token)
                .retrieve()
                .bodyToMono(RefundResponse.class);

        return circuitBreakerFactory.create(CommonConstants.PAYMENT_SERVICE_CIRCUIT_BREAKER)
                .run(responseMono, throwable -> {
                    log.error("Payment service failed to refund. Reason: {}", throwable.getMessage());
                    return Mono.error(new RefundDeclinedException("Refund failed. Please try again later."));
                }).block();    }
}
