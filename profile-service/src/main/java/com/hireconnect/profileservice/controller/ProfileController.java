package com.hireconnect.profileservice.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import com.hireconnect.profileservice.dto.request.ProfileRequestDto;
import com.hireconnect.profileservice.dto.response.CandidateProfilePreviewDto;
import com.hireconnect.profileservice.dto.response.ProfileResponseDto;
import com.hireconnect.profileservice.entity.Resume;
import com.hireconnect.profileservice.security.AuthenticatedUser;
import com.hireconnect.profileservice.service.ProfileService;
import com.hireconnect.profileservice.service.ResumeParserService;
import com.hireconnect.profileservice.dto.ParsedResumeDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for managing candidate and recruiter profiles.
 * Provides endpoints for profile creation, retrieval, updates, and resume management.
 * @author Disha Gujar
 */
@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final ProfileService profileService;
    private final ResumeParserService resumeParserService;

    /**
     * Creates a new user profile.
     * 
     * @param user the authenticated user
     * @param requestDto the profile creation request data
     * @return the created ProfileResponseDto
     
 * @author Disha Gujar
 */
    @PostMapping
    public ResponseEntity<ProfileResponseDto> createProfile(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody ProfileRequestDto requestDto
    ) {
        log.info("Received create profile request for userId={} with role={}", user.getUserId(), user.getRole());

        ProfileResponseDto response = profileService.createProfile(
                user.getUserId(),
                user.getRole(),
                requestDto
        );

        log.info("Profile created successfully for userId={}", user.getUserId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves the profile of the authenticated user.
     * 
     * @param user the authenticated user
     * @return the ProfileResponseDto
     
 * @author Disha Gujar
 */
    @GetMapping("/me")
    public ResponseEntity<ProfileResponseDto> getMyProfile(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Received get my profile request for userId={} role={}", user.getUserId(), user.getRole());

        ProfileResponseDto response = profileService.getProfileByUserId(user);

        log.info("Profile fetched successfully for userId={}", user.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the profile of the authenticated user.
     * 
     * @param user the authenticated user
     * @param requestDto the profile update request data
     * @return the updated ProfileResponseDto
     
 * @author Disha Gujar
 */
    @PutMapping("/me")
    public ResponseEntity<ProfileResponseDto> updateMyProfile(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody ProfileRequestDto requestDto
    ) {
        log.info("Received update profile request for userId={}", user.getUserId());

        ProfileResponseDto response = profileService.updateProfile(
                user,
                requestDto
        );

        log.info("Profile updated successfully for userId={}", user.getUserId());
        return ResponseEntity.ok(response);
    }
    /**
     * Retrieves candidate profile preview.
     *
     * @author Disha Gujar
     */

    @GetMapping("/internal/candidates/{userId}/preview")
    public ResponseEntity<CandidateProfilePreviewDto> getCandidateProfilePreview(
            @PathVariable Long userId
    ) {
        log.info("Received candidate profile preview request for userId={}", userId);

        CandidateProfilePreviewDto response = profileService.getCandidateProfilePreviewByUserId(userId);

        log.info("Candidate profile preview fetched successfully for userId={}", userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Uploads a resume for the authenticated candidate.
     * 
     * @param file the multipart file representing the resume
     * @param user the authenticated candidate
     * @return a success message or the resume URL
     
 * @author Disha Gujar
 */
    @PostMapping("/resume/upload")
    public ResponseEntity<String> uploadResume(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Received resume upload request for userId={}", user.getUserId());
        return ResponseEntity.ok(profileService.uploadResume(user, file));
    }

    /**
     * Allows a candidate to download their own resume.
     * 
     * @param user the authenticated candidate
     * @return the resume file data
     
 * @author Disha Gujar
 */
    @GetMapping("/resume/my")
    public ResponseEntity<byte[]> downloadMyResume(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Received own resume download request for userId={}", user.getUserId());

        Resume resume = profileService.getMyResume(user);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resume.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(resume.getContentType()))
                .body(resume.getFileData());
    }

    /**
     * Allows a recruiter to download a candidate's resume if they have applied for a job owned by the recruiter.
     * 
     * @param candidateId the ID of the candidate
     * @param jobId the ID of the job
     * @param user the authenticated recruiter
     * @return the resume file data
     
 * @author Disha Gujar
 */
    @GetMapping("/resume/recruiter/{candidateId}/{jobId}")
    public ResponseEntity<byte[]> downloadResumeForRecruiter(
            @PathVariable Long candidateId,
            @PathVariable Long jobId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Received recruiter resume download request for recruiterId={}, candidateId={}, jobId={}",
                user.getUserId(), candidateId, jobId);

        Resume resume = profileService.getResumeForRecruiter(user, candidateId, jobId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resume.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(resume.getContentType()))
                .body(resume.getFileData());
    }

    /**
     * Allows a recruiter to view the full comprehensive profile of a candidate who
     * has applied to one of their jobs.
     *
     * @param candidateId the candidate's userId
     * @param jobId       the job the candidate applied for (used for authorization)
     * @param user        the authenticated recruiter
     * @return full candidate profile DTO
     *
     * @author Disha Gujar
     */
    @GetMapping("/recruiter/candidates/{candidateId}/full")
    public ResponseEntity<com.hireconnect.profileservice.dto.response.CandidateFullProfileDto> getCandidateFullProfile(
            @PathVariable Long candidateId,
            @org.springframework.web.bind.annotation.RequestParam Long jobId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Received full candidate profile request | recruiterId={} | candidateId={} | jobId={}",
                user.getUserId(), candidateId, jobId);

        com.hireconnect.profileservice.dto.response.CandidateFullProfileDto response =
                profileService.getCandidateFullProfile(user, candidateId, jobId);

        log.info("Full candidate profile returned | recruiterId={} | candidateId={}", user.getUserId(), candidateId);
        return ResponseEntity.ok(response);
    }

    /**
     * Parses the candidate's uploaded resume to extract skills.
     * 
     * @param user the authenticated candidate
     * @return the extracted skills
     */
    @PostMapping("/resume/parse")
    public ResponseEntity<ParsedResumeDto> parseMyResume(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        log.info("Received parse resume request for userId={}", user.getUserId());
        
        Resume resume = profileService.getMyResume(user);
        if (resume == null || resume.getFileData() == null) {
            throw new RuntimeException("No resume found to parse.");
        }
        
        ParsedResumeDto response = resumeParserService.parsePdfResume(resume.getFileData());
        
        return ResponseEntity.ok(response);
    }
}

