package com.eidiko.booking_service.client;

import com.eidiko.booking_service.dto.PaymentRequest;
import com.eidiko.booking_service.dto.PaymentResponse;
import com.eidiko.booking_service.dto.RefundRequest;
import com.eidiko.booking_service.dto.RefundResponse;

public interface PaymentClient {
    PaymentResponse createPayment(PaymentRequest request);
    RefundResponse processRefund(RefundRequest request);
}
