package com.hireconnect.profileservice.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.hireconnect.profileservice.client.ApplicationServiceClient;
import com.hireconnect.profileservice.client.JobServiceClient;
import com.hireconnect.profileservice.dto.request.ProfileRequestDto;
import com.hireconnect.profileservice.dto.response.CandidateFullProfileDto;
import com.hireconnect.profileservice.dto.response.CandidateProfilePreviewDto;
import com.hireconnect.profileservice.dto.response.ProfileResponseDto;
import com.hireconnect.profileservice.entity.Profile;
import com.hireconnect.profileservice.entity.Resume;
import com.hireconnect.profileservice.entity.Role;
import com.hireconnect.profileservice.exception.ProfileAlreadyExistsException;
import com.hireconnect.profileservice.mapper.ProfileMapper;
import com.hireconnect.profileservice.repository.ProfileRepository;
import com.hireconnect.profileservice.repository.ResumeRepository;
import com.hireconnect.profileservice.security.AuthenticatedUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileMapper profileMapper;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private JobServiceClient jobServiceClient;

    @Mock
    private ApplicationServiceClient applicationServiceClient;

    @InjectMocks
    private ProfileServiceImpl profileService;

    private AuthenticatedUser candidateUser;
    private AuthenticatedUser recruiterUser;
    private ProfileRequestDto requestDto;
    private Profile profile;
    private ProfileResponseDto responseDto;

    @BeforeEach
    void setUp() {
        candidateUser = new AuthenticatedUser(1L, Role.CANDIDATE);
        recruiterUser = new AuthenticatedUser(2L, Role.RECRUITER);

        requestDto = new ProfileRequestDto();
        requestDto.setFirstName("John");
        requestDto.setLastName("Doe");

        profile = Profile.builder()
                .id(100L)
                .userId(1L)
                .role(Role.CANDIDATE)
                .firstName("John")
                .lastName("Doe")
                .headline("Java Developer")
                .location("Bhopal")
                .profilePictureUrl("pic.png")
                .build();

        responseDto = ProfileResponseDto.builder()
                .id(100L)
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .headline("Java Developer")
                .location("Bhopal")
                .profilePictureUrl("pic.png")
                .build();
    }

    @Test
    void createProfile_Success() {
        when(profileRepository.existsByUserId(1L)).thenReturn(false);
        when(profileMapper.toProfileEntity(requestDto, 1L, Role.CANDIDATE)).thenReturn(profile);
        when(profileRepository.save(profile)).thenReturn(profile);
        when(profileMapper.toProfileResponseDto(profile)).thenReturn(responseDto);

        ProfileResponseDto result = profileService.createProfile(1L, Role.CANDIDATE, requestDto);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
    }

    @Test
    void createProfile_AlreadyExists_ShouldThrowException() {
        when(profileRepository.existsByUserId(1L)).thenReturn(true);

        assertThrows(ProfileAlreadyExistsException.class,
                () -> profileService.createProfile(1L, Role.CANDIDATE, requestDto));
    }

    @Test
    void getProfileByUserId_ExistingProfile_Success() {
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profileMapper.toProfileResponseDto(profile)).thenReturn(responseDto);

        ProfileResponseDto result = profileService.getProfileByUserId(candidateUser);

        assertEquals("John", result.getFirstName());
    }

    @Test
    void getProfileByUserId_ProfileMissing_ShouldAutoCreate() {
        Profile blankProfile = Profile.builder()
                .id(200L)
                .userId(1L)
                .role(Role.CANDIDATE)
                .firstName("")
                .lastName("")
                .build();

        ProfileResponseDto blankResponse = ProfileResponseDto.builder()
                .id(200L)
                .userId(1L)
                .firstName("")
                .lastName("")
                .build();

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenReturn(blankProfile);
        when(profileMapper.toProfileResponseDto(blankProfile)).thenReturn(blankResponse);

        ProfileResponseDto result = profileService.getProfileByUserId(candidateUser);

        assertNotNull(result);
        assertEquals(200L, result.getId());
    }

    @Test
    void getProfileByUserIdInternal_ExistingProfile_Success() {
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profileMapper.toProfileResponseDto(profile)).thenReturn(responseDto);

        ProfileResponseDto result = profileService.getProfileByUserIdInternal(1L);

        assertEquals("John", result.getFirstName());
    }

    @Test
    void getProfileByUserIdInternal_ProfileMissing_ShouldAutoCreate() {
        Profile blankProfile = Profile.builder()
                .id(201L)
                .userId(5L)
                .role(Role.CANDIDATE)
                .firstName("")
                .lastName("")
                .build();

        ProfileResponseDto blankResponse = ProfileResponseDto.builder()
                .id(201L)
                .userId(5L)
                .build();

        when(profileRepository.findByUserId(5L)).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenReturn(blankProfile);
        when(profileMapper.toProfileResponseDto(blankProfile)).thenReturn(blankResponse);

        ProfileResponseDto result = profileService.getProfileByUserIdInternal(5L);

        assertEquals(201L, result.getId());
    }

    @Test
    void updateProfile_ExistingProfile_Success() {
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        doNothing().when(profileMapper).updateProfileEntity(profile, requestDto);
        when(profileRepository.save(profile)).thenReturn(profile);
        when(profileMapper.toProfileResponseDto(profile)).thenReturn(responseDto);

        ProfileResponseDto result = profileService.updateProfile(candidateUser, requestDto);

        assertEquals("John", result.getFirstName());
        verify(profileMapper).updateProfileEntity(profile, requestDto);
    }

    @Test
    void updateProfile_ProfileMissing_ShouldAutoCreateAndUpdate() {
        Profile blankProfile = Profile.builder()
                .id(300L)
                .userId(1L)
                .role(Role.CANDIDATE)
                .firstName("")
                .lastName("")
                .build();

        ProfileResponseDto updatedResponse = ProfileResponseDto.builder()
                .id(300L)
                .userId(1L)
                .firstName("John")
                .build();

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenReturn(blankProfile);
        doNothing().when(profileMapper).updateProfileEntity(blankProfile, requestDto);
        when(profileMapper.toProfileResponseDto(blankProfile)).thenReturn(updatedResponse);

        ProfileResponseDto result = profileService.updateProfile(candidateUser, requestDto);

        assertEquals(300L, result.getId());
    }

    @Test
    void getCandidateProfilePreviewByUserId_Success() {
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profileMapper.toProfileResponseDto(profile)).thenReturn(responseDto);

        CandidateProfilePreviewDto result = profileService.getCandidateProfilePreviewByUserId(1L);

        assertEquals("John", result.getFirstName());
        assertEquals("Java Developer", result.getHeadline());
    }

    @Test
    void getCandidateFullProfile_Success() {
        CandidateFullProfileDto fullProfileDto = new CandidateFullProfileDto();

        when(jobServiceClient.isJobOwnedByRecruiter(10L, 2L)).thenReturn(true);
        when(applicationServiceClient.hasCandidateAppliedToJob(1L, 10L)).thenReturn(true);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profileMapper.toCandidateFullProfileDto(profile)).thenReturn(fullProfileDto);

        CandidateFullProfileDto result = profileService.getCandidateFullProfile(recruiterUser, 1L, 10L);

        assertNotNull(result);
    }

    @Test
    void getCandidateFullProfile_NonRecruiter_ShouldThrowException() {
        assertThrows(RuntimeException.class,
                () -> profileService.getCandidateFullProfile(candidateUser, 1L, 10L));
    }

    @Test
    void getCandidateFullProfile_JobNotOwned_ShouldThrowException() {
        when(jobServiceClient.isJobOwnedByRecruiter(10L, 2L)).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> profileService.getCandidateFullProfile(recruiterUser, 1L, 10L));
    }

    @Test
    void getCandidateFullProfile_CandidateNotApplied_ShouldThrowException() {
        when(jobServiceClient.isJobOwnedByRecruiter(10L, 2L)).thenReturn(true);
        when(applicationServiceClient.hasCandidateAppliedToJob(1L, 10L)).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> profileService.getCandidateFullProfile(recruiterUser, 1L, 10L));
    }

    @Test
    void getCandidateFullProfile_ProfileMissing_ShouldThrowException() {
        when(jobServiceClient.isJobOwnedByRecruiter(10L, 2L)).thenReturn(true);
        when(applicationServiceClient.hasCandidateAppliedToJob(1L, 10L)).thenReturn(true);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> profileService.getCandidateFullProfile(recruiterUser, 1L, 10L));
    }

    @Test
    void fullProfileFallback_ShouldThrowException() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> profileService.fullProfileFallback(recruiterUser, 1L, 10L, new RuntimeException("down")));

        assertTrue(exception.getMessage().contains("Service temporarily unavailable"));
    }

    @Test
    void uploadResume_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "dummy pdf".getBytes()
        );

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(resumeRepository.findByProfile(profile)).thenReturn(Optional.empty());
        when(resumeRepository.save(any(Resume.class))).thenReturn(new Resume());

        String result = profileService.uploadResume(candidateUser, file);

        assertEquals("Resume uploaded successfully", result);
        verify(resumeRepository).save(any(Resume.class));
    }

    @Test
    void uploadResume_ExistingResume_ShouldUpdateResume() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "dummy pdf".getBytes()
        );

        Resume existingResume = new Resume();

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(resumeRepository.findByProfile(profile)).thenReturn(Optional.of(existingResume));
        when(resumeRepository.save(any(Resume.class))).thenReturn(existingResume);

        String result = profileService.uploadResume(candidateUser, file);

        assertEquals("Resume uploaded successfully", result);
    }

    @Test
    void uploadResume_EmptyFile_ShouldThrowException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThrows(RuntimeException.class,
                () -> profileService.uploadResume(candidateUser, file));
    }

    @Test
    void uploadResume_NonPdf_ShouldThrowException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.txt",
                "text/plain",
                "dummy".getBytes()
        );

        assertThrows(RuntimeException.class,
                () -> profileService.uploadResume(candidateUser, file));
    }

    @Test
    void uploadResume_ProfileMissing_ShouldAutoCreateProfile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "dummy".getBytes()
        );

        Profile blankProfile = Profile.builder()
                .id(400L)
                .userId(1L)
                .role(Role.CANDIDATE)
                .firstName("")
                .lastName("")
                .build();

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenReturn(blankProfile);
        when(resumeRepository.findByProfile(blankProfile)).thenReturn(Optional.empty());
        when(resumeRepository.save(any(Resume.class))).thenReturn(new Resume());

        String result = profileService.uploadResume(candidateUser, file);

        assertEquals("Resume uploaded successfully", result);
    }

    @Test
    void getMyResume_Success() {
        Resume resume = new Resume();
        resume.setFileName("resume.pdf");

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(resumeRepository.findByProfile(profile)).thenReturn(Optional.of(resume));

        Resume result = profileService.getMyResume(candidateUser);

        assertEquals("resume.pdf", result.getFileName());
    }

    @Test
    void getMyResume_NotFound_ShouldThrowException() {
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(resumeRepository.findByProfile(profile)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> profileService.getMyResume(candidateUser));
    }

    @Test
    void getResumeForRecruiter_Success() {
        Resume resume = new Resume();
        resume.setFileName("resume.pdf");

        when(jobServiceClient.isJobOwnedByRecruiter(10L, 2L)).thenReturn(true);
        when(applicationServiceClient.hasCandidateAppliedToJob(1L, 10L)).thenReturn(true);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(resumeRepository.findByProfile(profile)).thenReturn(Optional.of(resume));

        Resume result = profileService.getResumeForRecruiter(recruiterUser, 1L, 10L);

        assertEquals("resume.pdf", result.getFileName());
    }

    @Test
    void getResumeForRecruiter_NonRecruiter_ShouldThrowException() {
        assertThrows(RuntimeException.class,
                () -> profileService.getResumeForRecruiter(candidateUser, 1L, 10L));
    }

    @Test
    void getResumeForRecruiter_JobNotOwned_ShouldThrowException() {
        when(jobServiceClient.isJobOwnedByRecruiter(10L, 2L)).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> profileService.getResumeForRecruiter(recruiterUser, 1L, 10L));
    }

    @Test
    void getResumeForRecruiter_CandidateNotApplied_ShouldThrowException() {
        when(jobServiceClient.isJobOwnedByRecruiter(10L, 2L)).thenReturn(true);
        when(applicationServiceClient.hasCandidateAppliedToJob(1L, 10L)).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> profileService.getResumeForRecruiter(recruiterUser, 1L, 10L));
    }

    @Test
    void getResumeForRecruiter_ProfileNotFound_ShouldThrowException() {
        when(jobServiceClient.isJobOwnedByRecruiter(10L, 2L)).thenReturn(true);
        when(applicationServiceClient.hasCandidateAppliedToJob(1L, 10L)).thenReturn(true);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> profileService.getResumeForRecruiter(recruiterUser, 1L, 10L));
    }

    @Test
    void getResumeForRecruiter_ResumeNotFound_ShouldThrowException() {
        when(jobServiceClient.isJobOwnedByRecruiter(10L, 2L)).thenReturn(true);
        when(applicationServiceClient.hasCandidateAppliedToJob(1L, 10L)).thenReturn(true);
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(resumeRepository.findByProfile(profile)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> profileService.getResumeForRecruiter(recruiterUser, 1L, 10L));
    }

    @Test
    void jobServiceFallback_ShouldThrowException() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> profileService.jobServiceFallback(recruiterUser, 1L, 10L, new RuntimeException("down")));

        assertTrue(exception.getMessage().contains("Service temporarily unavailable"));
    }



    @Test
void uploadResume_WhenRepositoryThrowsException_ShouldThrowRuntimeException() throws Exception {

    MockMultipartFile file = new MockMultipartFile(
            "file",
            "resume.pdf",
            "application/pdf",
            "dummy".getBytes()
    );

    when(profileRepository.findByUserId(1L))
            .thenReturn(Optional.of(profile));

    when(resumeRepository.findByProfile(profile))
            .thenThrow(new RuntimeException("DB error"));

    assertThrows(RuntimeException.class,
            () -> profileService.uploadResume(candidateUser, file));
}

@Test
void getMyResume_ProfileMissing_ShouldAutoCreateProfile() {

    Profile blankProfile = Profile.builder()
            .id(500L)
            .userId(1L)
            .role(Role.CANDIDATE)
            .build();

    Resume resume = new Resume();
    resume.setFileName("resume.pdf");

    when(profileRepository.findByUserId(1L))
            .thenReturn(Optional.empty());

    when(profileRepository.save(any(Profile.class)))
            .thenReturn(blankProfile);

    when(resumeRepository.findByProfile(blankProfile))
            .thenReturn(Optional.of(resume));

    Resume result = profileService.getMyResume(candidateUser);

    assertEquals("resume.pdf", result.getFileName());
}

@Test
void getResumeForRecruiter_WhenResumeExists_ShouldReturnResume() {

    Resume resume = new Resume();
    resume.setFileName("candidate_resume.pdf");

    when(jobServiceClient.isJobOwnedByRecruiter(10L, 2L))
            .thenReturn(true);

    when(applicationServiceClient.hasCandidateAppliedToJob(1L, 10L))
            .thenReturn(true);

    when(profileRepository.findByUserId(1L))
            .thenReturn(Optional.of(profile));

    when(resumeRepository.findByProfile(profile))
            .thenReturn(Optional.of(resume));

    Resume result =
            profileService.getResumeForRecruiter(recruiterUser, 1L, 10L);

    assertEquals("candidate_resume.pdf", result.getFileName());
}

@Test
void getResumeForRecruiter_WhenApplicationServiceFails_ShouldThrowException() {

    when(jobServiceClient.isJobOwnedByRecruiter(10L, 2L))
            .thenReturn(true);

    when(applicationServiceClient.hasCandidateAppliedToJob(1L, 10L))
            .thenThrow(new RuntimeException("Application service down"));

    assertThrows(RuntimeException.class,
            () -> profileService.getResumeForRecruiter(recruiterUser, 1L, 10L));
}

@Test
void getCandidateFullProfile_WhenApplicationServiceFails_ShouldThrowException() {

    when(jobServiceClient.isJobOwnedByRecruiter(10L, 2L))
            .thenReturn(true);

    when(applicationServiceClient.hasCandidateAppliedToJob(1L, 10L))
            .thenThrow(new RuntimeException("Application service down"));

    assertThrows(RuntimeException.class,
            () -> profileService.getCandidateFullProfile(recruiterUser, 1L, 10L));
}

@Test
void getCandidateFullProfile_WhenJobServiceFails_ShouldThrowException() {

    when(jobServiceClient.isJobOwnedByRecruiter(10L, 2L))
            .thenThrow(new RuntimeException("Job service down"));

    assertThrows(RuntimeException.class,
            () -> profileService.getCandidateFullProfile(recruiterUser, 1L, 10L));
}

@Test
void fullProfileFallback_ShouldThrowServiceUnavailableMessage() {

    RuntimeException ex = assertThrows(RuntimeException.class,
            () -> profileService.fullProfileFallback(
                    recruiterUser,
                    1L,
                    10L,
                    new RuntimeException("service down")));

    assertTrue(ex.getMessage().contains("temporarily unavailable"));
}

@Test
void jobServiceFallback_ShouldThrowServiceUnavailableMessage() {

    RuntimeException ex = assertThrows(RuntimeException.class,
            () -> profileService.jobServiceFallback(
                    recruiterUser,
                    1L,
                    10L,
                    new RuntimeException("service down")));

    assertTrue(ex.getMessage().contains("temporarily unavailable"));
}

@Test
void getCandidateProfilePreview_ShouldBuildPreviewCorrectly() {

    when(profileRepository.findByUserId(1L))
            .thenReturn(Optional.of(profile));

    when(profileMapper.toProfileResponseDto(profile))
            .thenReturn(responseDto);

    CandidateProfilePreviewDto preview =
            profileService.getCandidateProfilePreviewByUserId(1L);

    assertEquals("John", preview.getFirstName());
    assertEquals("Doe", preview.getLastName());
    assertEquals("Java Developer", preview.getHeadline());
}

@Test
void updateProfile_WhenMapperUpdatesProfile_ShouldSaveUpdatedProfile() {

    when(profileRepository.findByUserId(1L))
            .thenReturn(Optional.of(profile));

    doAnswer(invocation -> {
        Profile p = invocation.getArgument(0);
        p.setFirstName("Updated");
        return null;
    }).when(profileMapper).updateProfileEntity(any(Profile.class), any(ProfileRequestDto.class));

    when(profileRepository.save(any(Profile.class)))
            .thenReturn(profile);

    when(profileMapper.toProfileResponseDto(any(Profile.class)))
            .thenReturn(responseDto);

    ProfileResponseDto result =
            profileService.updateProfile(candidateUser, requestDto);

    assertNotNull(result);
}
}