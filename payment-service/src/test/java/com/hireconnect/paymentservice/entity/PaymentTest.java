package com.hireconnect.paymentservice.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.hireconnect.paymentservice.enums.PaymentPurpose;
import com.hireconnect.paymentservice.enums.PaymentStatus;
import com.hireconnect.paymentservice.enums.Role;

class PaymentTest {

    @Test
    void testGettersAndSetters() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setUserId(10L);
        payment.setEmail("test@test.com");
        payment.setRole(Role.RECRUITER);
        payment.setPurpose(PaymentPurpose.JOB_POSTING_PLAN);
        payment.setReferenceId(100L);
        payment.setAmount(new BigDecimal("500.00"));
        payment.setCurrency("INR");
        payment.setProviderOrderId("order_123");
        payment.setProviderPaymentId("pay_123");
        payment.setProviderSignature("sig_123");
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setDescription("desc");
        payment.setCreatedAt(LocalDateTime.now());

        assertEquals(1L, payment.getId());
        assertEquals(10L, payment.getUserId());
        assertEquals("test@test.com", payment.getEmail());
        assertEquals(Role.RECRUITER, payment.getRole());
        assertEquals(PaymentPurpose.JOB_POSTING_PLAN, payment.getPurpose());
        assertEquals(100L, payment.getReferenceId());
        assertEquals(new BigDecimal("500.00"), payment.getAmount());
        assertEquals("INR", payment.getCurrency());
        assertEquals("order_123", payment.getProviderOrderId());
        assertEquals("pay_123", payment.getProviderPaymentId());
        assertEquals("sig_123", payment.getProviderSignature());
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals("desc", payment.getDescription());
        assertNotNull(payment.getCreatedAt());
    }

    @Test
    void testBuilder() {
        Payment payment = Payment.builder()
                .id(1L)
                .userId(10L)
                .build();
        assertEquals(1L, payment.getId());
        assertEquals(10L, payment.getUserId());
    }

    @Test
    void testOnCreateAndOnUpdate() {
        Payment payment = new Payment();
        payment.onCreate();
        assertNotNull(payment.getCreatedAt());
        assertNotNull(payment.getUpdatedAt());
        payment.onUpdate();
        assertNotNull(payment.getUpdatedAt());
    }
}
