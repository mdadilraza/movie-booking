package com.eidiko.payment_service.service.impl;

import com.eidiko.payment_service.constants.Constants;
import com.eidiko.payment_service.constants.PaymentStatus;
import com.eidiko.payment_service.dto.PaymentRequest;
import com.eidiko.payment_service.dto.PaymentResponse;
import com.eidiko.payment_service.dto.RefundRequest;
import com.eidiko.payment_service.dto.RefundResponse;
import com.eidiko.payment_service.entity.Payment;
import com.eidiko.payment_service.exception.PaymentAlreadyExistException;
import com.eidiko.payment_service.exception.PaymentNotFoundException;
import com.eidiko.payment_service.repository.PaymentRepository;
import com.eidiko.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

        Payment existingPayment = paymentRepository.findByBookingId(request.getBookingId())
                .orElse(null);
        if (existingPayment != null) {
            throw new PaymentAlreadyExistException("Payment already exists for bookingId: " + request.getBookingId());
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

        Payment payment = paymentRepository.findByBookingId(request.getBookingId())
                .orElseThrow(() -> new PaymentAlreadyExistException("Payment Not Found for booking Id "+ request.getBookingId()));
        Map<String, Double> seatRefunds = getSeatRefunds(request, payment);
       log.info("seatRefunds {}",seatRefunds);
        RefundResponse response = new RefundResponse();
        response.setBookingId(request.getBookingId());
        response.setSeatRefunds(seatRefunds);
        response.setStatus(PaymentStatus.INITIATED.name());
        response.setTransactionId(UUID.randomUUID().toString());
        response.setRefundedAmount(seatRefunds.values()
                .stream().mapToDouble(Double::doubleValue).sum());
        log.info("Refund initiated for bookingId: {} ",
                request.getBookingId());
        log.info("response :{}",response);
        return response;
    }

    private static Map<String, Double> getSeatRefunds(RefundRequest request, Payment payment) {
        if (payment == null) {
            throw new PaymentNotFoundException("No payment found for bookingId: " + request.getBookingId());
        }

        if (payment.getNumberOfSeats() == 0) {
            throw new PaymentAlreadyExistException("Invalid number of seats for bookingId: " + request.getBookingId());
        }

        double ticketPrice = payment.getAmount() / payment.getNumberOfSeats(); // Price per seat
        Map<String, Double> seatRefunds = new HashMap<>();
        for (String seatNumber : request.getSeatNumbers()) {
            double refundAmount = ticketPrice * (1 - Constants.CANCELLATION_CHARGE);
            log.info("refundAmount :{}",refundAmount);
            seatRefunds.put(seatNumber, refundAmount);
        }
        return seatRefunds;
    }
    @Override
    public RefundResponse refundPaymentByBookingId(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for bookingId: " + bookingId));

        // Here you'd integrate with your payment gateway to initiate refund using payment.getTransactionId() etc.
        payment.setStatus(PaymentStatus.REFUNDED.name()); // Enum you define
        paymentRepository.save(payment);

        RefundResponse refundResponse = new RefundResponse();
        refundResponse.setBookingId(bookingId);
        refundResponse.setTransactionId(payment.getTransactionId());
        refundResponse.setStatus(payment.getStatus());
        refundResponse.setRefundedAmount(payment.getAmount());

        return refundResponse;
    }
}
