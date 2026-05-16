package com.hireconnect.profileservice.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.hireconnect.profileservice.dto.request.*;
import com.hireconnect.profileservice.dto.response.*;
import com.hireconnect.profileservice.entity.*;

class ProfileMapperTest {

    private final ProfileMapper mapper = new ProfileMapper();

    @Test
    void toProfileEntity_FullRequest_Success() {
        ProfileRequestDto request = new ProfileRequestDto();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setSkills(Collections.singletonList(new SkillRequestDto("Java", "Advanced")));
        request.setEducations(Collections.singletonList(new EducationRequestDto("MIT", "MS", "CS", LocalDate.now(), null, "A", "Desc", false)));
        request.setExperiences(Collections.singletonList(new ExperienceRequestDto("Google", "SDE", "Full-time", "NY", LocalDate.now(), null, true, "Work", "Tech")));
        request.setSocialLinks(Collections.singletonList(new SocialLinkRequestDto("LinkedIn", "url")));
        request.setRecruiterDetail(new RecruiterDetailRequestDto("ACME", "acme.com", "Cool", "HR", "Tech", "100", "logo", "li", "Devs", 5));

        Profile profile = mapper.toProfileEntity(request, 1L, Role.CANDIDATE);

        assertNotNull(profile);
        assertEquals(1L, profile.getUserId());
        assertEquals("John", profile.getFirstName());
        assertEquals(1, profile.getSkills().size());
        assertEquals(1, profile.getEducations().size());
        assertEquals(1, profile.getExperiences().size());
        assertEquals(1, profile.getSocialLinks().size());
        assertNotNull(profile.getRecruiterDetail());
    }

    @Test
    void toProfileEntity_NullRequest_ReturnsNull() {
        assertNull(mapper.toProfileEntity(null, 1L, Role.CANDIDATE));
    }

    @Test
    void toProfileResponseDto_FullProfile_Success() {
        Profile profile = Profile.builder()
                .id(100L)
                .userId(1L)
                .firstName("John")
                .role(Role.CANDIDATE)
                .build();
        profile.setSkills(Collections.singletonList(Skill.builder().id(1L).name("Java").build()));
        profile.setResume(Resume.builder().id(2L).fileName("r.pdf").build());

        ProfileResponseDto response = mapper.toProfileResponseDto(profile);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(1, response.getSkills().size());
        assertNotNull(response.getResume());
    }

    @Test
    void toProfileResponseDto_NullProfile_ReturnsNull() {
        assertNull(mapper.toProfileResponseDto(null));
    }

    @Test
    void updateProfileEntity_FullUpdate_Success() {
        Profile existing = Profile.builder().id(100L).userId(1L).build();
        ProfileRequestDto request = new ProfileRequestDto();
        request.setFirstName("Updated");
        request.setRecruiterDetail(new RecruiterDetailRequestDto("NewCo", null, null, null, null, null, null, null, null, null));

        mapper.updateProfileEntity(existing, request);

        assertEquals("Updated", existing.getFirstName());
        assertNotNull(existing.getRecruiterDetail());
        assertEquals("NewCo", existing.getRecruiterDetail().getCompanyName());
    }

    @Test
    void toCandidateFullProfileDto_Success() {
        Profile profile = Profile.builder().userId(1L).firstName("John").build();
        com.hireconnect.profileservice.dto.response.CandidateFullProfileDto dto = mapper.toCandidateFullProfileDto(profile);
        assertNotNull(dto);
        assertEquals("John", dto.getFirstName());
    }

    @Test
    void toCandidateFullProfileDto_Null_ReturnsNull() {
        assertNull(mapper.toCandidateFullProfileDto(null));
    }
}
