// [Disha Gujar] : Entry point for the Payment Service — Razorpay-integrated payment processing microservice.
// Handles payment order creation, Razorpay signature verification, webhook event processing,
// and stores payment transaction history for recruiter job-feature purchases.
package com.hireconnect.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PaymentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}