package com.hireconnect.applicationservice.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.hireconnect.applicationservice.client.dto.ApplicationSummaryDto;
import com.hireconnect.applicationservice.dto.request.ApplicationRequestDto;
import com.hireconnect.applicationservice.dto.request.ApplicationStatusUpdateRequestDto;
import com.hireconnect.applicationservice.dto.response.ApplicationResponseDto;
import com.hireconnect.applicationservice.dto.response.RecruiterJobApplicationResponseDto;
import com.hireconnect.applicationservice.security.AuthenticatedUser;
import com.hireconnect.applicationservice.service.ApplicationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// [Disha Gujar] : REST controller managing candidate job applications under /api/applications.
// Exposes endpoints for candidates to apply, view, and track applications; for recruiters to review
// and update statuses; and internal endpoints (/internal/**) for cross-service application summaries.
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApplicationResponseDto> applyToJob(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody ApplicationRequestDto requestDto
    ) {
        log.info("Apply to job request received for candidateId: {}, jobId: {}",
                user != null ? user.getUserId() : null, requestDto.getJobId());
        ApplicationResponseDto response = applicationService.applyToJob(user, requestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<List<ApplicationResponseDto>> getMyApplications(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Get my applications request received for candidateId: {}",
                user != null ? user.getUserId() : null);
        return ResponseEntity.ok(applicationService.getMyApplications(user));
    }

    @GetMapping("/me/{applicationId}")
    public ResponseEntity<ApplicationResponseDto> getMyApplicationById(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long applicationId
    ) {
        log.info("Get my application by id request received for candidateId: {}, applicationId: {}",
                user != null ? user.getUserId() : null, applicationId);
        return ResponseEntity.ok(applicationService.getMyApplicationById(user, applicationId));
    }

    @GetMapping("/recruiter")
    public ResponseEntity<List<ApplicationResponseDto>> getApplicationsForRecruiter(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Get recruiter applications request received for recruiterId: {}",
                user != null ? user.getUserId() : null);
        return ResponseEntity.ok(applicationService.getApplicationsForRecruiter(user));
    }

    @PutMapping("/{applicationId}/status")
    public ResponseEntity<ApplicationResponseDto> updateApplicationStatus(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationStatusUpdateRequestDto requestDto
    ) {
        log.info("Update application status request received for recruiterId: {}, applicationId: {}, status: {}",
                user != null ? user.getUserId() : null, applicationId, requestDto.getStatus());
        return ResponseEntity.ok(
                applicationService.updateApplicationStatus(user, applicationId, requestDto)
        );
    }

    @GetMapping("/internal/{applicationId}")
    public ResponseEntity<ApplicationSummaryDto> getApplicationSummary(@PathVariable Long applicationId) {
        log.info("Get application summary request received for applicationId: {}", applicationId);
        return ResponseEntity.ok(applicationService.getApplicationSummary(applicationId));
    }
    
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ApplicationResponseDto>> getApplicationsByJobId(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long jobId
    ) {
        log.info("Get applications by job id request received for recruiterId: {}, jobId: {}",
                user != null ? user.getUserId() : null, jobId);
        return ResponseEntity.ok(applicationService.getApplicationsByJobId(user, jobId));
    }
    
    @GetMapping("/recruiter/job/{jobId}")
    public ResponseEntity<List<RecruiterJobApplicationResponseDto>> getApplicationsForRecruiterJob(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long jobId
    ) {
        log.info("Get recruiter job applications with candidate preview request received for recruiterId: {}, jobId: {}",
                user != null ? user.getUserId() : null, jobId);
        return ResponseEntity.ok(applicationService.getApplicationsForRecruiterJob(user, jobId));
    }
    
    @GetMapping("/check")
    public Boolean hasCandidateAppliedToJob(
            @RequestParam Long candidateId,
            @RequestParam Long jobId
    ) {
        return applicationService.hasCandidateAppliedToJob(candidateId, jobId);
    }
}