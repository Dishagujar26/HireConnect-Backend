package com.hireconnect.paymentservice.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import com.hireconnect.paymentservice.config.RazorpayProperties;
import com.hireconnect.paymentservice.dto.request.CreatePaymentOrderRequestDto;
import com.hireconnect.paymentservice.dto.request.VerifyPaymentRequestDto;
import com.hireconnect.paymentservice.dto.response.PaymentOrderResponseDto;
import com.hireconnect.paymentservice.dto.response.PaymentResponseDto;
import com.hireconnect.paymentservice.entity.Payment;
import com.hireconnect.paymentservice.enums.PaymentPurpose;
import com.hireconnect.paymentservice.enums.PaymentStatus;
import com.hireconnect.paymentservice.enums.Role;
import com.hireconnect.paymentservice.repository.PaymentRepository;
import com.hireconnect.paymentservice.security.AuthenticatedUser;

class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RazorpayProperties razorpayProperties;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private AuthenticatedUser user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new AuthenticatedUser(1L, "test@test.com", Role.RECRUITER);
        when(razorpayProperties.getKeyId()).thenReturn("rzp_test_YourKeyHere");
        when(razorpayProperties.getKeySecret()).thenReturn("secret");
        when(razorpayProperties.getWebhookSecret()).thenReturn("secret");
    }

    @Test
    void createOrder_MockSuccess() {
        CreatePaymentOrderRequestDto request = new CreatePaymentOrderRequestDto();
        request.setAmount(new BigDecimal("500.00"));
        request.setPurpose(PaymentPurpose.JOB_POSTING_PLAN);

        PaymentOrderResponseDto response = paymentService.createOrder(user, request);

        assertNotNull(response);
        assertTrue(response.getOrderId().startsWith("order_mock_"));
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void createOrder_Failure_ShouldThrowException() {
        CreatePaymentOrderRequestDto request = new CreatePaymentOrderRequestDto();
        request.setAmount(new BigDecimal("500.00"));
        request.setPurpose(PaymentPurpose.JOB_POSTING_PLAN);
        
        when(paymentRepository.save(any())).thenThrow(new RuntimeException("DB Error"));

        assertThrows(RuntimeException.class, () -> paymentService.createOrder(user, request));
    }

    @Test
    void verifyPayment_MockSuccess() {
        VerifyPaymentRequestDto request = new VerifyPaymentRequestDto();
        request.setRazorpayOrderId("order_mock_123");

        Payment payment = Payment.builder()
                .userId(1L)
                .providerOrderId("order_mock_123")
                .status(PaymentStatus.CREATED)
                .build();

        when(paymentRepository.findByProviderOrderId("order_mock_123")).thenReturn(Optional.of(payment));

        PaymentResponseDto response = paymentService.verifyPayment(user, request);

        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    void verifyPayment_Unauthorized() {
        VerifyPaymentRequestDto request = new VerifyPaymentRequestDto();
        request.setRazorpayOrderId("order_mock_123");

        Payment payment = Payment.builder()
                .userId(2L) // Different user
                .providerOrderId("order_mock_123")
                .build();

        when(paymentRepository.findByProviderOrderId("order_mock_123")).thenReturn(Optional.of(payment));

        assertThrows(RuntimeException.class, () -> paymentService.verifyPayment(user, request));
    }

    @Test
    void verifyPayment_OrderNotFound() {
        VerifyPaymentRequestDto request = new VerifyPaymentRequestDto();
        request.setRazorpayOrderId("invalid");

        when(paymentRepository.findByProviderOrderId("invalid")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentService.verifyPayment(user, request));
    }

    @Test
    void getMyPayments_Success() {
        when(paymentRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(new Payment()));
        
        List<PaymentResponseDto> response = paymentService.getMyPayments(user);
        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void handleWebhook_InvalidSignature_ShouldThrow() {
        String payload = "payload";
        assertThrows(RuntimeException.class, () -> paymentService.handleWebhook(payload, "invalid"));
    }

    @Test
    void handleWebhook_UnsupportedEvent_ShouldIgnore() {
        String payload = "{\"event\":\"unsupported\"}";
        String sig = "b37428538dfab2968d55330a78fda66ae380e3ded72db32c7cd57e9bc405e5b6";
        paymentService.handleWebhook(payload, sig);
    }

    @Test
    void handleWebhook_OrderPaid_Success() {
        String payload = "{\"event\":\"order.paid\",\"payload\":{\"payment\":{\"entity\":{\"order_id\":\"order_123\",\"id\":\"pay_123\"}}}}";
        String sig = "31644ed6ace3fce2497e232605494e534e6d18b2d90859c4e53815ed883d2ddd";
        
        Payment payment = Payment.builder()
                .id(1L)
                .providerOrderId("order_123")
                .status(PaymentStatus.CREATED)
                .build();
        when(paymentRepository.findByProviderOrderId("order_123")).thenReturn(Optional.of(payment));
        
        paymentService.handleWebhook(payload, sig);
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals("pay_123", payment.getProviderPaymentId());
    }
}
