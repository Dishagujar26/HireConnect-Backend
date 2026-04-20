package com.hireconnect.interviewservice.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.hireconnect.interviewservice.dto.request.InterviewScheduleRequestDto;
import com.hireconnect.interviewservice.dto.request.InterviewUpdateRequestDto;
import com.hireconnect.interviewservice.dto.response.InterviewResponseDto;
import com.hireconnect.interviewservice.security.AuthenticatedUser;
import com.hireconnect.interviewservice.service.InterviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// [Disha Gujar] : REST controller managing interview scheduling operations under /api/interviews.
// Provides endpoints for recruiters to schedule, update, and cancel interviews,
// and for both recruiters and candidates to view their respective interview lists and details.
@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private static final Logger log = LoggerFactory.getLogger(InterviewController.class);

    private final InterviewService interviewService;

    // [Disha Gujar] : Allows a recruiter to schedule an interview for a candidate.
    @PostMapping
    public ResponseEntity<InterviewResponseDto> scheduleInterview(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody InterviewScheduleRequestDto requestDto
    ) {
        log.info("Schedule interview request received by recruiterId: {} for applicationId: {}",
                user != null ? user.getUserId() : null, requestDto.getApplicationId());
        InterviewResponseDto response = interviewService.scheduleInterview(user, requestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // [Disha Gujar] : Retrieves all interviews scheduled by the currently logged-in recruiter.
    @GetMapping("/recruiter")
    public ResponseEntity<List<InterviewResponseDto>> getRecruiterInterviews(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Fetch recruiter interviews request received for recruiterId: {}",
                user != null ? user.getUserId() : null);
        return ResponseEntity.ok(interviewService.getRecruiterInterviews(user));
    }

    // [Disha Gujar] : Retrieves all interviews assigned to the currently logged-in candidate.
    @GetMapping("/candidate")
    public ResponseEntity<List<InterviewResponseDto>> getCandidateInterviews(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Fetch candidate interviews request received for candidateId: {}",
                user != null ? user.getUserId() : null);
        return ResponseEntity.ok(interviewService.getCandidateInterviews(user));
    }

    @GetMapping("/{interviewId}")
    public ResponseEntity<InterviewResponseDto> getInterviewDetails(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long interviewId
    ) {
        log.info("Get interview details request received for interviewId: {}, userId: {}",
                interviewId, user != null ? user.getUserId() : null);
        return ResponseEntity.ok(interviewService.getInterviewDetails(user, interviewId));
    }

    // [Disha Gujar] : Updates the details of a scheduled interview.
    @PutMapping("/{interviewId}")
    public ResponseEntity<InterviewResponseDto> updateInterview(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long interviewId,
            @Valid @RequestBody InterviewUpdateRequestDto requestDto
    ) {
        log.info("Update interview request received for interviewId: {}, recruiterId: {}",
                interviewId, user != null ? user.getUserId() : null);
        return ResponseEntity.ok(interviewService.updateInterview(user, interviewId, requestDto));
    }

    // [Disha Gujar] : Cancels a scheduled interview and notifies the candidate.
    @DeleteMapping("/cancel/{interviewId}")
    public ResponseEntity<InterviewResponseDto> cancelInterview(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long interviewId
    ) {
        log.info("Cancel interview request received for interviewId: {}, recruiterId: {}",
                interviewId, user != null ? user.getUserId() : null);
        if (interviewId == null || interviewId <= 0) {
            log.warn("Cancel interview request rejected due to invalid interviewId: {}", interviewId);
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(interviewService.cancelInterview(user, interviewId));
    }
}