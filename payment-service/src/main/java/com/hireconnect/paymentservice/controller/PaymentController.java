package com.hireconnect.paymentservice.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.hireconnect.paymentservice.dto.request.CreatePaymentOrderRequestDto;
import com.hireconnect.paymentservice.dto.request.VerifyPaymentRequestDto;
import com.hireconnect.paymentservice.dto.response.PaymentOrderResponseDto;
import com.hireconnect.paymentservice.dto.response.PaymentResponseDto;
import com.hireconnect.paymentservice.security.AuthenticatedUser;
import com.hireconnect.paymentservice.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller handling payment operations under /api/payments.
 *
 * @author Disha Gujar
 */
// Integrates with Razorpay to expose order creation, payment verification, payment history retrieval,
// and a webhook endpoint for Razorpay server-to-server event notifications.
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    /**
 * Initiates a payment process by creating a Razorpay order.
 *
 * @author Disha Gujar
 */
    @PostMapping("/create-order")
    public ResponseEntity<PaymentOrderResponseDto> createOrder(
            Authentication authentication,
            @Valid @RequestBody CreatePaymentOrderRequestDto request
    ) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        logger.info("Create payment order request received for userId={}, purpose={}, referenceId={}",
                user.userId(), request.getPurpose(), request.getReferenceId());
        return ResponseEntity.ok(paymentService.createOrder(user, request));
    }

    /**
 * Verifies the Razorpay payment signature after a successful client-side payment.
 *
 * @author Disha Gujar
 */
    @PostMapping("/verify")
    public ResponseEntity<PaymentResponseDto> verifyPayment(
            Authentication authentication,
            @Valid @RequestBody VerifyPaymentRequestDto request
    ) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        logger.info("Verify payment request received for userId={}, orderId={}",
                user.userId(), request.getRazorpayOrderId());
        return ResponseEntity.ok(paymentService.verifyPayment(user, request));
    }

    /**
 * Retrieves all payment transaction history for the currently logged-in user.
 *
 * @author Disha Gujar
 */
    @GetMapping("/me")
    public ResponseEntity<List<PaymentResponseDto>> getMyPayments(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        logger.info("Fetch payments request received for userId={}", user.userId());
        return ResponseEntity.ok(paymentService.getMyPayments(user));
    }

    /**
 * Webhook endpoint to receive server-to-server notifications from Razorpay.
 *
 * @author Disha Gujar
 */
    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestHeader("X-Razorpay-Signature") String signature,
            @RequestBody String payload
    ) {
        logger.info("Webhook received for payment service");
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok("Webhook processed");
    }
}