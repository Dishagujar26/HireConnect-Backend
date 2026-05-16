package com.hireconnect.applicationservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hireconnect.applicationservice.client.dto.ApplicationSummaryDto;
import com.hireconnect.applicationservice.dto.request.ApplicationRequestDto;
import com.hireconnect.applicationservice.dto.request.ApplicationStatusUpdateRequestDto;
import com.hireconnect.applicationservice.dto.response.ApplicationResponseDto;
import com.hireconnect.applicationservice.dto.response.RecruiterJobApplicationResponseDto;
import com.hireconnect.applicationservice.security.AuthenticatedUser;
import com.hireconnect.applicationservice.service.ApplicationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for managing job applications.
 * Provides endpoints for candidates to apply and track applications,
 * and for recruiters to review and update application statuses.
 * @author Disha Gujar
 */
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Slf4j
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * Allows a candidate to apply for a job.
     * 
     * @param user the authenticated candidate
     * @param requestDto the application request data
     * @return the created ApplicationResponseDto
     
 * @author Disha Gujar
 */
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

    /**
     * Retrieves all job applications submitted by the authenticated candidate.
     * 
     * @param user the authenticated candidate
     * @return a list of ApplicationResponseDto
     
 * @author Disha Gujar
 */
    @GetMapping("/me")
    public ResponseEntity<List<ApplicationResponseDto>> getMyApplications(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Get my applications request received for candidateId: {}",
                user != null ? user.getUserId() : null);
        return ResponseEntity.ok(applicationService.getMyApplications(user));
    }
    /**
     * Retrieves my application by id.
     *
     * @author Disha Gujar
     */

    @GetMapping("/me/{applicationId}")
    public ResponseEntity<ApplicationResponseDto> getMyApplicationById(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long applicationId
    ) {
        log.info("Get my application by id request received for candidateId: {}, applicationId: {}",
                user != null ? user.getUserId() : null, applicationId);
        return ResponseEntity.ok(applicationService.getMyApplicationById(user, applicationId));
    }

    /**
     * Retrieves all job applications for the authenticated recruiter.
     * 
     * @param user the authenticated recruiter
     * @return a list of RecruiterJobApplicationResponseDto
     
 * @author Disha Gujar
 */
    @GetMapping("/recruiter")
    public ResponseEntity<List<RecruiterJobApplicationResponseDto>> getApplicationsForRecruiter(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Get recruiter applications request received for recruiterId: {}",
                user != null ? user.getUserId() : null);
        return ResponseEntity.ok(applicationService.getApplicationsForRecruiter(user));
    }

    /**
     * Updates the status of a job application.
     * 
     * @param user the authenticated recruiter
     * @param applicationId the ID of the application
     * @param requestDto the status update request data
     * @return the updated ApplicationResponseDto
     
 * @author Disha Gujar
 */
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
    /**
     * Retrieves application summary.
     *
     * @author Disha Gujar
     */

    @GetMapping("/internal/{applicationId}")
    public ResponseEntity<ApplicationSummaryDto> getApplicationSummary(@PathVariable Long applicationId) {
        log.info("Get application summary request received for applicationId: {}", applicationId);
        return ResponseEntity.ok(applicationService.getApplicationSummary(applicationId));
    }
    /**
     * Retrieves applications by job id.
     *
     * @author Disha Gujar
     */
    
    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ApplicationResponseDto>> getApplicationsByJobId(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long jobId
    ) {
        log.info("Get applications by job id request received for recruiterId: {}, jobId: {}",
                user != null ? user.getUserId() : null, jobId);
        return ResponseEntity.ok(applicationService.getApplicationsByJobId(user, jobId));
    }
    
    /**
     * Fetches applications for a specific job with detailed candidate profile previews.
     * 
     * @param user the authenticated recruiter
     * @param jobId the ID of the job
     * @return a list of RecruiterJobApplicationResponseDto
     
 * @author Disha Gujar
 */
    @GetMapping("/recruiter/job/{jobId}")
    public ResponseEntity<List<RecruiterJobApplicationResponseDto>> getApplicationsForRecruiterJob(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long jobId
    ) {
        log.info("Get recruiter job applications with candidate preview request received for recruiterId: {}, jobId: {}",
                user != null ? user.getUserId() : null, jobId);
        return ResponseEntity.ok(applicationService.getApplicationsForRecruiterJob(user, jobId));
    }
    /**
     * Checks ifs candidate applied to job.
     *
     * @author Disha Gujar
     */
    
    @GetMapping("/check")
    public Boolean hasCandidateAppliedToJob(
            @RequestParam Long candidateId,
            @RequestParam Long jobId
    ) {
        return applicationService.hasCandidateAppliedToJob(candidateId, jobId);
    }

    /**
     * Downloads the candidate offer letter as a PDF (true direct file download).
     */
    @GetMapping("/offer-letter/pdf")
    public ResponseEntity<byte[]> downloadOfferLetterPdf(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam Long candidateId,
            @RequestParam Long jobId
    ) {
        byte[] pdfBytes = applicationService.downloadOfferLetterPdf(user, candidateId, jobId);

        String fileName = "offer-letter-" + candidateId + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
