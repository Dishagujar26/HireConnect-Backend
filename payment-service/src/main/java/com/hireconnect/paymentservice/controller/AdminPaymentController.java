package com.hireconnect.paymentservice.controller;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.hireconnect.paymentservice.security.AuthenticatedUser;
import com.hireconnect.paymentservice.enums.Role;

import com.hireconnect.paymentservice.entity.Payment;
import com.hireconnect.paymentservice.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

/**
 * Admin-only controller for platform financial oversight.
 * Provides global access to transaction history and revenue analytics.
 *
 * @author Disha Gujar
 */
@RestController
@RequestMapping("/api/payments/admin")
@RequiredArgsConstructor
public class AdminPaymentController {

    private static final Logger log = LoggerFactory.getLogger(AdminPaymentController.class);

    private final PaymentRepository paymentRepository;

    private boolean isAdmin(String role) {
        return "ADMIN".equalsIgnoreCase(role);
    }

    /**
     * Returns a list of all transactions on the platform.
     */
    @GetMapping("/transactions")
    public ResponseEntity<?> getAllTransactions(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        if (user == null || user.role() != Role.ADMIN) {
            return ResponseEntity.status(403).body("Access denied: Admin role required.");
        }
        log.info("Admin requested all transactions.");
        List<Payment> payments = paymentRepository.findAll();
        return ResponseEntity.ok(payments);
    }

    /**
     * Returns aggregate financial stats.
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getFinancialStats(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        if (user == null || user.role() != Role.ADMIN) {
            return ResponseEntity.status(403).body("Access denied: Admin role required.");
        }

        BigDecimal totalRevenue = paymentRepository.sumTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        long successCount = paymentRepository.countSuccessfulPayments();

        log.info("Admin requested financial stats. Revenue={}", totalRevenue);
        return ResponseEntity.ok(new FinanceStatsDto(totalRevenue, successCount));
    }

    public record FinanceStatsDto(BigDecimal totalRevenue, long successCount) {}
}
