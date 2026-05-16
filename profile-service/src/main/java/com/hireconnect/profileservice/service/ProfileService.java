package com.hireconnect.profileservice.service;

import org.springframework.web.multipart.MultipartFile;

import com.hireconnect.profileservice.dto.request.ProfileRequestDto;
import com.hireconnect.profileservice.dto.response.CandidateFullProfileDto;
import com.hireconnect.profileservice.dto.response.CandidateProfilePreviewDto;
import com.hireconnect.profileservice.dto.response.ProfileResponseDto;
import com.hireconnect.profileservice.entity.Role;
import com.hireconnect.profileservice.entity.Resume;
import com.hireconnect.profileservice.security.AuthenticatedUser;

/**
 * Service interface defining the business logic contract for user profile management.
 * Covers profile CRUD, candidate previews/full profiles for recruiters, and resume management.
 *
 * @author Disha Gujar
 */
public interface ProfileService {

    ProfileResponseDto createProfile(Long userId, Role role, ProfileRequestDto requestDto);

    ProfileResponseDto getProfileByUserId(AuthenticatedUser user);

    ProfileResponseDto getProfileByUserIdInternal(Long userId);

    ProfileResponseDto updateProfile(AuthenticatedUser user, ProfileRequestDto requestDto);

    CandidateProfilePreviewDto getCandidateProfilePreviewByUserId(Long userId);

    /**
     * Returns a comprehensive candidate profile for a recruiter to review.
     * Verifies that the recruiter owns the job and the candidate has applied to it.
     *
     * @param recruiter   the authenticated recruiter
     * @param candidateId the candidate's userId
     * @param jobId       the job the candidate applied for
     * @return full candidate profile DTO
     */
    CandidateFullProfileDto getCandidateFullProfile(AuthenticatedUser recruiter, Long candidateId, Long jobId);

    String uploadResume(AuthenticatedUser user, MultipartFile file);

    Resume getMyResume(AuthenticatedUser user);

    Resume getResumeForRecruiter(AuthenticatedUser user, Long candidateId, Long jobId);
}