package com.hireconnect.jobservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.hireconnect.jobservice.dto.request.JobRequestDto;
import com.hireconnect.jobservice.dto.response.JobResponseDto;
import com.hireconnect.jobservice.entity.ExperienceLevel;
import com.hireconnect.jobservice.entity.JobType;
import com.hireconnect.jobservice.security.AuthenticatedUser;
import com.hireconnect.jobservice.service.JobService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for managing job postings.
 * Provides endpoints for recruiter-specific CRUD operations, public job search,
 * and internal inter-service communication.
 * @author Disha Gujar
 */
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobController {

    private final JobService jobService;

    /**
     * Creates a new job posting for a recruiter.
     * 
     * @param user the authenticated recruiter
     * @param requestDto the job creation details
     * @return the created JobResponseDto
     
 * @author Disha Gujar
 */
    @PostMapping
    public ResponseEntity<JobResponseDto> createJob(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody JobRequestDto requestDto
    ) {
        log.info("Create job request received for recruiterId={} with title={}", user.getUserId(), requestDto.getTitle());
        JobResponseDto response = jobService.createJob(
                user.getUserId(),
                user.getRole(),
                requestDto
        );
        log.info("Job created successfully with jobId={} for recruiterId={}", response.getJobId(), user.getUserId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Updates an existing job posting.
     * 
     * @param jobId the ID of the job to update
     * @param user the authenticated recruiter
     * @param requestDto the updated job details
     * @return the updated JobResponseDto
     
 * @author Disha Gujar
 */
    @PutMapping("/{jobId}")
    public ResponseEntity<JobResponseDto> updateJob(
            @PathVariable Long jobId,
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody JobRequestDto requestDto
    ) {
        log.info("Update job request received for jobId={} by recruiterId={}", jobId, user.getUserId());
        JobResponseDto response = jobService.updateJob(
                jobId,
                user.getUserId(),
                user.getRole(),
                requestDto
        );
        log.info("Job updated successfully for jobId={} by recruiterId={}", jobId, user.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a job posting.
     * 
     * @param jobId the ID of the job to delete
     * @param user the authenticated recruiter
     * @return a success message
     
 * @author Disha Gujar
 */
    @DeleteMapping("/{jobId}")
    public ResponseEntity<String> deleteJob(
            @PathVariable Long jobId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Delete job request received for jobId={} by recruiterId={}", jobId, user.getUserId());
        jobService.deleteJob(jobId, user.getUserId(), user.getRole());
        log.info("Job deleted successfully for jobId={} by recruiterId={}", jobId, user.getUserId());
        return ResponseEntity.ok("Job deleted successfully");
    }

    /**
     * Retrieves all job postings created by the authenticated recruiter.
     * 
     * @param user the authenticated recruiter
     * @return a list of JobResponseDto
     
 * @author Disha Gujar
 */
    @GetMapping("/recruiter/me")
    public ResponseEntity<List<JobResponseDto>> getMyJobs(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Fetch recruiter jobs request received for recruiterId={}", user.getUserId());
        return ResponseEntity.ok(jobService.getMyJobs(user.getUserId(), user.getRole()));
    }

    /**
     * Fetches all open job postings.
     * 
     * @return a list of open jobs
     
 * @author Disha Gujar
 */
    @GetMapping
    public ResponseEntity<List<JobResponseDto>> getAllOpenJobs() {
        log.info("Fetch all open jobs request received");
        return ResponseEntity.ok(jobService.getAllOpenJobs());
    }
    /**
     * Retrieves open job by id.
     *
     * @author Disha Gujar
     */

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponseDto> getOpenJobById(@PathVariable Long jobId) {
        log.info("Fetch open job by id request received for jobId={}", jobId);
        return ResponseEntity.ok(jobService.getOpenJobById(jobId));
    }

    /**
     * Searches for open jobs based on various criteria.
     * 
     * @param keyword keyword to search in title/description
     * @param location location filter
     * @param jobType type of job (FULL_TIME, PART_TIME, etc.)
     * @param experienceLevel level of experience required
     * @param minSalary minimum salary filter
     * @param maxSalary maximum salary filter
     * @return a list of matching job postings
     
 * @author Disha Gujar
 */
    @GetMapping("/search")
    public ResponseEntity<List<JobResponseDto>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) ExperienceLevel experienceLevel,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary
    ) {
        log.info("Search jobs request received with keyword={}, location={}, jobType={}, experienceLevel={}, minSalary={}, maxSalary={}", keyword, location, jobType, experienceLevel, minSalary, maxSalary);
        return ResponseEntity.ok(
                jobService.searchOpenJobs(
                        keyword,
                        location,
                        jobType,
                        experienceLevel,
                        minSalary,
                        maxSalary
                )
        );
    }
    /**
     * Does job exist.
     *
     * @author Disha Gujar
     */

    @GetMapping("/internal/{jobId}/exists")
    public ResponseEntity<Boolean> doesJobExist(@PathVariable Long jobId) {
        log.info("Internal exists check request received for jobId={}", jobId);
        return ResponseEntity.ok(jobService.doesJobExist(jobId));
    }
    /**
     * Checks if job open.
     *
     * @author Disha Gujar
     */

    @GetMapping("/internal/{jobId}/open")
    public ResponseEntity<Boolean> isJobOpen(@PathVariable Long jobId) {
        log.info("Internal open status check request received for jobId={}", jobId);
        return ResponseEntity.ok(jobService.isJobOpen(jobId));
    }
    /**
     * Checks if job owned by recruiter.
     *
     * @author Disha Gujar
     */

    @GetMapping("/internal/{jobId}/recruiter/{recruiterId}/ownership")
    public ResponseEntity<Boolean> isJobOwnedByRecruiter(
            @PathVariable Long jobId,
            @PathVariable Long recruiterId
    ) {
        log.info("Internal ownership check request received for jobId={} and recruiterId={}", jobId, recruiterId);
        return ResponseEntity.ok(jobService.isJobOwnedByRecruiter(jobId, recruiterId));
    }
    /**
     * Retrieves job ids by recruiter.
     *
     * @author Disha Gujar
     */

    @GetMapping("/internal/recruiter/{recruiterId}/job-ids")
    public ResponseEntity<List<Long>> getJobIdsByRecruiter(@PathVariable Long recruiterId) {
        log.info("Internal recruiter job ids request received for recruiterId={}", recruiterId);
        return ResponseEntity.ok(jobService.getJobIdsByRecruiter(recruiterId));
    }
    /**
     * Retrieves recruiter id by job id.
     *
     * @author Disha Gujar
     */

    @GetMapping("/internal/{jobId}/recruiter-id")
    public ResponseEntity<Long> getRecruiterIdByJobId(@PathVariable Long jobId) {
        log.info("Internal recruiter id lookup request received for jobId={}", jobId);
        return ResponseEntity.ok(jobService.getRecruiterIdByJobId(jobId));
    }
    
    /**
     * Marks a job as featured.
     * 
     * @param jobId the ID of the job to feature
     * @param user the authenticated recruiter
     * @return a success message
     
 * @author Disha Gujar
 */
    @PutMapping("/{jobId}/feature")
    public ResponseEntity<String> markJobAsFeatured(
            @PathVariable Long jobId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Feature job request received for jobId={} by recruiterId={}", jobId, user != null ? user.getUserId() : null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body("User not authenticated");
        }

        jobService.markAsFeatured(jobId, user.getUserId(), user.getRole());
        log.info("Job marked as featured successfully for jobId={}", jobId);
        return ResponseEntity.ok("Job marked as featured");
    }

    /**
     * Retrieves recommended jobs based on candidate skills.
     * 
     * @param skills the candidate skills
     * @param limit the number of jobs to return
     * @return list of recommended jobs
     */
    @GetMapping("/recommended")
    public ResponseEntity<List<com.hireconnect.jobservice.dto.response.RecommendedJobResponseDto>> getRecommendedJobs(
            @RequestParam List<String> skills,
            @RequestParam(defaultValue = "6") int limit
    ) {
        log.info("Recommended jobs request received with skills={}", skills);
        return ResponseEntity.ok(jobService.getRecommendedJobs(skills, limit));
    }

    /**
     * Retrieves the match score for a candidate against a job.
     * 
     * @param jobId the ID of the job
     * @param skills the candidate skills
     * @return the match score dto
     */
    @GetMapping("/{jobId}/match-score")
    public ResponseEntity<com.hireconnect.jobservice.dto.response.MatchScoreResponseDto> getMatchScore(
            @PathVariable Long jobId,
            @RequestParam List<String> skills
    ) {
        log.info("Match score request received for jobId={} with skills={}", jobId, skills);
        return ResponseEntity.ok(jobService.computeMatchScore(jobId, skills));
    }
}
