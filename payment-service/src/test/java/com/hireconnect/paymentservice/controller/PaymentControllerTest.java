package com.hireconnect.paymentservice.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireconnect.paymentservice.dto.request.CreatePaymentOrderRequestDto;
import com.hireconnect.paymentservice.dto.request.VerifyPaymentRequestDto;
import com.hireconnect.paymentservice.dto.response.PaymentOrderResponseDto;
import com.hireconnect.paymentservice.dto.response.PaymentResponseDto;
import com.hireconnect.paymentservice.enums.PaymentPurpose;
import com.hireconnect.paymentservice.enums.PaymentStatus;
import com.hireconnect.paymentservice.enums.Role;
import com.hireconnect.paymentservice.security.AuthenticatedUser;
import com.hireconnect.paymentservice.service.PaymentService;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentService paymentService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PaymentController paymentController;

    private AuthenticatedUser user;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        user = new AuthenticatedUser(1L, "test@test.com", Role.CANDIDATE);
        lenient().when(authentication.getPrincipal()).thenReturn(user);
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
    }

    @Test
    void createOrder_Success() throws Exception {
        CreatePaymentOrderRequestDto request = new CreatePaymentOrderRequestDto();
        request.setAmount(new BigDecimal("100.00"));
        request.setPurpose(PaymentPurpose.JOB_POSTING_PLAN);
        request.setReferenceId(123L);

        PaymentOrderResponseDto response = PaymentOrderResponseDto.builder()
                .paymentId(100L)
                .orderId("order_123")
                .amount(new BigDecimal("100.00"))
                .build();

        when(paymentService.createOrder(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/payments/create-order")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order_123"));
    }

    @Test
    void verifyPayment_Success() throws Exception {
        VerifyPaymentRequestDto request = new VerifyPaymentRequestDto();
        request.setRazorpayOrderId("order_123");
        request.setRazorpayPaymentId("pay_123");
        request.setRazorpaySignature("sig123");

        PaymentResponseDto response = PaymentResponseDto.builder()
                .status(PaymentStatus.SUCCESS)
                .build();

        when(paymentService.verifyPayment(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/payments/verify")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void getMyPayments_Success() throws Exception {
        PaymentResponseDto response = PaymentResponseDto.builder()
                .id(100L)
                .status(PaymentStatus.SUCCESS)
                .build();

        when(paymentService.getMyPayments(any())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/payments/me")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));
    }

    @Test
    void webhook_Success() throws Exception {
        mockMvc.perform(post("/api/payments/webhook")
                .header("X-Razorpay-Signature", "sig123")
                .content("payload"))
                .andExpect(status().isOk())
                .andExpect(content().string("Webhook processed"));
    }

    @Test
    void createOrder_InvalidRequest_ShouldReturn400() throws Exception {
        CreatePaymentOrderRequestDto request = new CreatePaymentOrderRequestDto();
        // Missing required fields like amount, purpose

        mockMvc.perform(post("/api/payments/create-order")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
