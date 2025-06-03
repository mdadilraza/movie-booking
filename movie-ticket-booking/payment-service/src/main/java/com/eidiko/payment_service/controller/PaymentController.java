package com.eidiko.payment_service.controller;

import com.eidiko.payment_service.dto.PaymentRequest;
import com.eidiko.payment_service.dto.PaymentResponse;
import com.eidiko.payment_service.dto.RefundRequest;
import com.eidiko.payment_service.dto.RefundResponse;
import com.eidiko.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest paymentRequest){
        return ResponseEntity.ok(paymentService.createPayment(paymentRequest));
    }
    @PostMapping("/refund")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<RefundResponse> createRefund(@Valid @RequestBody RefundRequest refundRequest){
   return ResponseEntity.ok(paymentService.processRefund(refundRequest));
    }
    @PostMapping("/payment/refund/{bookingId}")
    public ResponseEntity<String> refundPayment(@Valid @PathVariable Long bookingId) {
        paymentService.refundPaymentByBookingId(bookingId);
        return ResponseEntity.ok("Refund successful for booking ID: " + bookingId);
    }
}
