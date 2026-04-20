package com.hireconnect.paymentservice.service;

import java.util.List;

import com.hireconnect.paymentservice.dto.request.CreatePaymentOrderRequestDto;
import com.hireconnect.paymentservice.dto.request.VerifyPaymentRequestDto;
import com.hireconnect.paymentservice.dto.response.PaymentOrderResponseDto;
import com.hireconnect.paymentservice.dto.response.PaymentResponseDto;
import com.hireconnect.paymentservice.security.AuthenticatedUser;

// [Disha Gujar] : Service interface defining the business logic contract for payment processing.
// Handles Razorpay order creation for job-feature purchases, HMAC-SHA256 signature-based payment
// verification, payment history retrieval per user, and Razorpay webhook event handling.
public interface PaymentService {
    PaymentOrderResponseDto createOrder(AuthenticatedUser user, CreatePaymentOrderRequestDto request);
    PaymentResponseDto verifyPayment(AuthenticatedUser user, VerifyPaymentRequestDto request);
    List<PaymentResponseDto> getMyPayments(AuthenticatedUser user);
    void handleWebhook(String payload, String signature);
}