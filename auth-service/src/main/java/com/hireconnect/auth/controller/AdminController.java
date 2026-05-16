package com.hireconnect.auth.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hireconnect.auth.entity.Role;
import com.hireconnect.auth.entity.UserCredential;
import com.hireconnect.auth.repository.AuthRepository;
import com.hireconnect.auth.security.JwtService;

import lombok.RequiredArgsConstructor;

/**
 * Admin-only controller for platform-level user governance.
 * All endpoints require ROLE_ADMIN (enforced at API-Gateway level).
 *
 * @author Disha Gujar
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final AuthRepository authRepository;
    private final JwtService jwtService;

    @Value("${auth.admin-email}")
    private String adminEmail;

    /**
     * Guard: validates the caller is the designated admin.
     * Returns true if the Bearer token belongs to ROLE_ADMIN.
     */
    private boolean isAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return false;
        String token = authHeader.substring(7);
        return "ADMIN".equals(jwtService.extractRole(token));
    }

    /**
     * Lists all registered users (candidates and recruiters).
     * Admin-only endpoint.
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        if (!isAdmin(authHeader)) {
            return ResponseEntity.status(403).body("Access denied: Admin role required.");
        }

        log.info("Admin requested full user list.");
        List<UserCredential> users = authRepository.findAll();

        List<AdminUserDto> result = users.stream()
                .map(u -> new AdminUserDto(
                        u.getUserId(),
                        u.getEmail(),
                        u.getRole() != null ? u.getRole().name() : "UNKNOWN",
                        u.getIsActive()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * Suspends (deactivates) a user account by userId.
     * Admin-only endpoint.
     */
    @PutMapping("/users/{userId}/suspend")
    public ResponseEntity<?> suspendUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId
    ) {
        if (!isAdmin(authHeader)) {
            return ResponseEntity.status(403).body("Access denied: Admin role required.");
        }

        log.info("Admin requested suspension of userId={}", userId);
        UserCredential user = authRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (user.getEmail().equalsIgnoreCase(adminEmail)) {
            return ResponseEntity.badRequest().body("Cannot suspend the platform admin.");
        }

        user.setIsActive(false);
        authRepository.save(user);
        log.info("User userId={} suspended by admin.", userId);
        return ResponseEntity.ok("User suspended successfully.");
    }

    /**
     * Reactivates a previously suspended user account.
     * Admin-only endpoint.
     */
    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<?> activateUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId
    ) {
        if (!isAdmin(authHeader)) {
            return ResponseEntity.status(403).body("Access denied: Admin role required.");
        }

        log.info("Admin requested reactivation of userId={}", userId);
        UserCredential user = authRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        user.setIsActive(true);
        authRepository.save(user);
        log.info("User userId={} reactivated by admin.", userId);
        return ResponseEntity.ok("User reactivated successfully.");
    }

    /**
     * Gets platform statistics: total users, total candidates, total recruiters.
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getPlatformStats(@RequestHeader("Authorization") String authHeader) {
        if (!isAdmin(authHeader)) {
            return ResponseEntity.status(403).body("Access denied: Admin role required.");
        }

        long totalUsers = authRepository.count();
        long candidates = authRepository.countByRole(Role.CANDIDATE);
        long recruiters = authRepository.countByRole(Role.RECRUITER);
        long active = authRepository.countByIsActive(true);

        log.info("Admin fetched platform stats: total={}, candidates={}, recruiters={}, active={}",
                totalUsers, candidates, recruiters, active);

        return ResponseEntity.ok(new AdminStatsDto(totalUsers, candidates, recruiters, active));
    }

    // ── Inner DTOs (lightweight, no separate files needed) ───────────────────

    public record AdminUserDto(Long userId, String email, String role, Boolean active) {}
    public record AdminStatsDto(long totalUsers, long candidates, long recruiters, long activeUsers) {}
}
