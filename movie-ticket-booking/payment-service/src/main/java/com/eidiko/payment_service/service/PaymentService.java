package com.eidiko.payment_service.service;

import com.eidiko.payment_service.dto.PaymentRequest;
import com.eidiko.payment_service.dto.PaymentResponse;
import com.eidiko.payment_service.dto.RefundRequest;
import com.eidiko.payment_service.dto.RefundResponse;
import jakarta.validation.Valid;

public interface PaymentService {
     PaymentResponse createPayment(PaymentRequest request);
    RefundResponse processRefund(RefundRequest request);

    RefundResponse refundPaymentByBookingId( Long bookingId);
}
