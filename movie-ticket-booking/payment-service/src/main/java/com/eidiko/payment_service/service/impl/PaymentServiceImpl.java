package com.eidiko.payment_service.service.impl;

import com.eidiko.payment_service.constants.Constants;
import com.eidiko.payment_service.dto.PaymentRequest;
import com.eidiko.payment_service.dto.PaymentResponse;
import com.eidiko.payment_service.dto.RefundRequest;
import com.eidiko.payment_service.dto.RefundResponse;
import com.eidiko.payment_service.entity.Payment;
import com.eidiko.payment_service.exception.PaymentException;
import com.eidiko.payment_service.repository.PaymentRepository;
import com.eidiko.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("Creating payment for bookingId: {}, amount: {}, seats: {}",
                request.getBookingId(), request.getAmount(), request.getNumberOfSeats());

        Payment existingPayment = paymentRepository.findByBookingId(request.getBookingId());
        if (existingPayment != null) {
            throw new PaymentException("Payment already exists for bookingId: " + request.getBookingId());
        }

        Payment payment = new Payment();
        payment.setBookingId(request.getBookingId());
        payment.setAmount(request.getAmount());
        payment.setNumberOfSeats(request.getNumberOfSeats());
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setStatus("SUCCESS"); // Mock payment gateway

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment created with ID: {} for bookingId: {}", savedPayment.getId(), savedPayment.getBookingId());

        PaymentResponse response = new PaymentResponse();
        response.setId(savedPayment.getId());
        response.setBookingId(savedPayment.getBookingId());
        response.setAmount(savedPayment.getAmount());
        response.setTransactionId(savedPayment.getTransactionId());
        response.setStatus(savedPayment.getStatus());
        return response;
    }

    @Override
    @Transactional
    public RefundResponse processRefund(RefundRequest request) {
        log.info("Processing refund for bookingId: {}, seats: {}", request.getBookingId(), request.getSeatNumbers());

        Payment payment = paymentRepository.findByBookingId(request.getBookingId());
        Map<String, Double> seatRefunds = getSeatRefunds(request, payment);

        RefundResponse response = new RefundResponse();
        response.setBookingId(request.getBookingId());
        response.setSeatRefunds(seatRefunds);
        response.setStatus("INITIATED");
        response.setTransactionId(UUID.randomUUID().toString());
        log.info("Refund initiated for bookingId: {}", request.getBookingId());
        return response;
    }

    private static Map<String, Double> getSeatRefunds(RefundRequest request, Payment payment) {
        if (payment == null) {
            throw new PaymentException("No payment found for bookingId: " + request.getBookingId());
        }

        if (payment.getNumberOfSeats() == 0) {
            throw new PaymentException("Invalid number of seats for bookingId: " + request.getBookingId());
        }

        double ticketPrice = payment.getAmount() / payment.getNumberOfSeats(); // Price per seat
        Map<String, Double> seatRefunds = new HashMap<>();
        for (String seatNumber : request.getSeatNumbers()) {
            double refundAmount = ticketPrice * (1 - Constants.CANCELLATION_CHARGE);
            seatRefunds.put(seatNumber, refundAmount);
        }
        return seatRefunds;
    }
}
