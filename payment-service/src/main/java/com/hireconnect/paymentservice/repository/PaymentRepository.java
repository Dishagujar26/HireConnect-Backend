package com.hireconnect.paymentservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hireconnect.paymentservice.entity.Payment;
/**
 * Repository interface for database operations related to Payment entities.
 *
 * @author Disha Gujar
 */

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByProviderOrderId(String providerOrderId);
    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Admin methods
    @org.springframework.data.jpa.repository.Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCESS'")
    java.math.BigDecimal sumTotalRevenue();

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'SUCCESS'")
    long countSuccessfulPayments();
}
