package com.hireconnect.profileservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Comprehensive candidate profile DTO returned to recruiters when viewing a candidate's full profile.
 * Contains all public candidate information including skills, experience, education, social links
 * and resume metadata. Binary resume data is excluded — use the resume download endpoint for that.
 *
 * @author Disha Gujar
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateFullProfileDto {

    // ─── Identity ─────────────────────────────────────────────────────────────
    private Long userId;
    private String firstName;
    private String lastName;
    private String headline;
    private String location;
    private String about;
    private String profilePictureUrl;
    private String phone;

    // ─── Candidate Availability & Compensation ────────────────────────────────
    private Integer noticePeriodDays;
    private Long expectedSalary;
    private Long currentSalary;
    private String preferredWorkMode;
    private BigDecimal totalExperienceYears;
    private String nationality;
    private String gender;
    private LocalDate dateOfBirth;

    // ─── Career Information ───────────────────────────────────────────────────
    private List<SkillResponseDto> skills;
    private List<ExperienceResponseDto> experiences;
    private List<EducationResponseDto> educations;
    private List<SocialLinkResponseDto> socialLinks;

    // ─── Resume Metadata (no binary data) ────────────────────────────────────
    private ResumeResponseDto resume;

    // ─── Profile Timestamps ───────────────────────────────────────────────────
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
