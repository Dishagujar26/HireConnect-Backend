package com.hireconnect.jobservice.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.hireconnect.jobservice.security.AuthenticatedUser;

import com.hireconnect.jobservice.repository.JobRepository;
import com.hireconnect.jobservice.service.JobService;

import lombok.RequiredArgsConstructor;

/**
 * Admin-only controller for platform-level job content moderation.
 * Provides a global view of all jobs and the ability to forcefully delete
 * any job that violates platform policies — bypassing the owner check.
 *
 * All endpoints are protected at the API-Gateway level (ROLE_ADMIN required).
 * An inline role check is also performed for defense-in-depth.
 *
 * @author Disha Gujar
 */
@RestController
@RequestMapping("/api/jobs/admin")
@RequiredArgsConstructor
public class AdminJobController {

    private static final Logger log = LoggerFactory.getLogger(AdminJobController.class);

    private final JobRepository jobRepository;
    private final JobService jobService;

    /**
     * Validates that the caller carries an ADMIN role header (forwarded by API Gateway).
     */
    private boolean isAdmin(String role) {
        return "ADMIN".equalsIgnoreCase(role);
    }

    /**
     * Returns ALL jobs on the platform regardless of recruiter or status.
     * Admin-only: used for global content oversight.
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllJobsForAdmin(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        if (user == null || user.getRole() != com.hireconnect.jobservice.entity.Role.ADMIN) {
            return ResponseEntity.status(403).body("Access denied: Admin role required.");
        }
        log.info("Admin requested all jobs listing.");
        List<com.hireconnect.jobservice.entity.Job> jobs = jobRepository.findAll();
        return ResponseEntity.ok(jobs);
    }

    /**
     * Forcefully deletes any job by its ID, bypassing owner checks.
     * Admin-only: for removing policy-violating listings.
     */
    @DeleteMapping("/{jobId}")
    public ResponseEntity<?> adminDeleteJob(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long jobId
    ) {
        if (user == null || user.getRole() != com.hireconnect.jobservice.entity.Role.ADMIN) {
            return ResponseEntity.status(403).body("Access denied: Admin role required.");
        }

        if (!jobRepository.existsByJobId(jobId)) {
            return ResponseEntity.status(404).body("Job not found with id: " + jobId);
        }

        jobRepository.deleteById(jobId);
        log.info("Admin force-deleted jobId={}.", jobId);
        return ResponseEntity.ok("Job deleted successfully by admin.");
    }

    /**
     * Returns aggregate job stats: total jobs count.
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getJobStats(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        if (user == null || user.getRole() != com.hireconnect.jobservice.entity.Role.ADMIN) {
            return ResponseEntity.status(403).body("Access denied: Admin role required.");
        }
        long total = jobRepository.count();
        log.info("Admin requested job stats: total={}", total);
        return ResponseEntity.ok(new JobStatsDto(total));
    }

    public record JobStatsDto(long totalJobs) {}
}
