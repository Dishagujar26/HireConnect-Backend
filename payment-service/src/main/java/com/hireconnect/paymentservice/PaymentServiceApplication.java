/**
 * Entry point for the Payment Service — Razorpay-integrated payment processing microservice.
 *
 * @author Disha Gujar
 */
// Handles payment order creation, Razorpay signature verification, webhook event processing,
// and stores payment transaction history for recruiter job-feature purchases.
package com.hireconnect.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
/**
 * Main entry point for the PaymentServiceApplication.
 *
 * @author Disha Gujar
 */

@SpringBootApplication
@EnableFeignClients
public class PaymentServiceApplication {
    /**
     * Main.
     *
     * @author Disha Gujar
     */
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
