package com.hireconnect.applicationservice.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.hireconnect.applicationservice.client.JobServiceClient;
import com.hireconnect.applicationservice.client.ProfileServiceClient;
import com.hireconnect.applicationservice.client.dto.ApplicationSummaryDto;
import com.hireconnect.applicationservice.client.dto.CandidateProfilePreviewDto;
import com.hireconnect.applicationservice.client.dto.CandidateFullProfileForOfferDto;
import com.hireconnect.applicationservice.client.dto.JobOfferDetailsDto;
import com.hireconnect.applicationservice.dto.request.ApplicationRequestDto;
import com.hireconnect.applicationservice.dto.request.ApplicationStatusUpdateRequestDto;
import com.hireconnect.applicationservice.dto.response.ApplicationResponseDto;
import com.hireconnect.applicationservice.dto.response.RecruiterJobApplicationResponseDto;
import com.hireconnect.applicationservice.entity.JobApplication;
import com.hireconnect.applicationservice.enums.ApplicationStatus;
import com.hireconnect.applicationservice.enums.Role;
import com.hireconnect.applicationservice.exception.BadRequestException;
import com.hireconnect.applicationservice.exception.ResourceNotFoundException;
import com.hireconnect.applicationservice.exception.UnauthorizedException;
import com.hireconnect.applicationservice.producer.NotificationEventProducer;
import com.hireconnect.applicationservice.repository.JobApplicationRepository;
import com.hireconnect.applicationservice.security.AuthenticatedUser;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private JobServiceClient jobServiceClient;

    @Mock
    private NotificationEventProducer notificationEventProducer;

    @Mock
    private ProfileServiceClient profileServiceClient;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private AuthenticatedUser candidateUser;
    private AuthenticatedUser recruiterUser;
    private ApplicationRequestDto applicationRequestDto;
    private JobApplication application;

    @BeforeEach
    void setUp() {
        candidateUser = new AuthenticatedUser(1L, "candidate@test.com", Role.CANDIDATE);
        recruiterUser = new AuthenticatedUser(2L, "recruiter@test.com", Role.RECRUITER);
        
        applicationRequestDto = new ApplicationRequestDto();
        applicationRequestDto.setJobId(10L);
        applicationRequestDto.setResumeUrl("http://resume.com");
        applicationRequestDto.setCoverLetter("Hello");

        application = JobApplication.builder()
                .id(100L)
                .jobId(10L)
                .candidateId(1L)
                .candidateEmail("candidate@test.com")
                .recruiterId(2L)
                .status(ApplicationStatus.APPLIED)
                .build();
    }

    // --- applyToJob ---

    @Test
    void applyToJob_Success() {
        when(jobServiceClient.doesJobExist(10L)).thenReturn(true);
        when(jobServiceClient.isJobOpen(10L)).thenReturn(true);
        when(jobApplicationRepository.existsByJobIdAndCandidateId(10L, 1L)).thenReturn(false);
        when(jobServiceClient.getRecruiterIdByJobId(10L)).thenReturn(2L);
        when(jobApplicationRepository.save(any(JobApplication.class))).thenReturn(application);

        ApplicationResponseDto response = applicationService.applyToJob(candidateUser, applicationRequestDto);

        assertNotNull(response);
        assertEquals(ApplicationStatus.APPLIED, response.getStatus());
        verify(notificationEventProducer, times(2)).sendNotification(any());
    }

    @Test
    void applyToJob_DuplicateIntegrityViolation_ShouldThrowBadRequest() {
        when(jobServiceClient.doesJobExist(10L)).thenReturn(true);
        when(jobServiceClient.isJobOpen(10L)).thenReturn(true);
        when(jobApplicationRepository.existsByJobIdAndCandidateId(10L, 1L)).thenReturn(false);
        when(jobServiceClient.getRecruiterIdByJobId(10L)).thenReturn(2L);
        when(jobApplicationRepository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(BadRequestException.class, () -> applicationService.applyToJob(candidateUser, applicationRequestDto));
    }

    @Test
    void applyToJob_JobNotFound_ShouldThrowException() {
        when(jobServiceClient.doesJobExist(10L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> applicationService.applyToJob(candidateUser, applicationRequestDto));
    }

    @Test
    void applyToJob_JobClosed_ShouldThrowException() {
        when(jobServiceClient.doesJobExist(10L)).thenReturn(true);
        when(jobServiceClient.isJobOpen(10L)).thenReturn(false);
        assertThrows(BadRequestException.class, () -> applicationService.applyToJob(candidateUser, applicationRequestDto));
    }

    @Test
    void applyToJob_AlreadyApplied_ShouldThrowException() {
        when(jobServiceClient.doesJobExist(10L)).thenReturn(true);
        when(jobServiceClient.isJobOpen(10L)).thenReturn(true);
        when(jobApplicationRepository.existsByJobIdAndCandidateId(10L, 1L)).thenReturn(true);
        assertThrows(BadRequestException.class, () -> applicationService.applyToJob(candidateUser, applicationRequestDto));
    }

    @Test
    void jobServiceFallback_ShouldThrowRuntimeException() {
        assertThrows(RuntimeException.class, () -> 
            applicationService.jobServiceFallback(candidateUser, applicationRequestDto, new Exception("Down")));
    }

    // --- getMyApplications ---

    @Test
    void getMyApplications_Success() {
        when(jobApplicationRepository.findByCandidateIdOrderByAppliedAtDesc(1L)).thenReturn(List.of(application));
        List<ApplicationResponseDto> result = applicationService.getMyApplications(candidateUser);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    // --- getMyApplicationById ---

    @Test
    void getMyApplicationById_Success() {
        when(jobApplicationRepository.findByIdAndCandidateId(100L, 1L)).thenReturn(Optional.of(application));
        ApplicationResponseDto response = applicationService.getMyApplicationById(candidateUser, 100L);
        assertNotNull(response);
        assertEquals(100L, response.getId());
    }

    @Test
    void getMyApplicationById_NotFound_ShouldThrow() {
        when(jobApplicationRepository.findByIdAndCandidateId(100L, 1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> applicationService.getMyApplicationById(candidateUser, 100L));
    }

    // --- getApplicationsForRecruiter ---

    @Test
    void getApplicationsForRecruiter_Success() {
        when(jobServiceClient.getJobIdsByRecruiter(2L)).thenReturn(List.of(10L));
        when(jobApplicationRepository.findByJobIdInOrderByAppliedAtDesc(any())).thenReturn(List.of(application));
        when(profileServiceClient.getCandidateProfilePreview(anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(new CandidateProfilePreviewDto());

        List<RecruiterJobApplicationResponseDto> result = applicationService.getApplicationsForRecruiter(recruiterUser);
        assertFalse(result.isEmpty());
        assertNotNull(result.get(0).getCandidateProfile());
    }

    @Test
    void getApplicationsForRecruiter_NoJobs_ShouldReturnEmpty() {
        when(jobServiceClient.getJobIdsByRecruiter(2L)).thenReturn(Collections.emptyList());
        List<RecruiterJobApplicationResponseDto> result = applicationService.getApplicationsForRecruiter(recruiterUser);
        assertTrue(result.isEmpty());
    }

    @Test
    void getApplicationsForRecruiter_ProfileFailure_ShouldStillReturn() {
        when(jobServiceClient.getJobIdsByRecruiter(2L)).thenReturn(List.of(10L));
        when(jobApplicationRepository.findByJobIdInOrderByAppliedAtDesc(any())).thenReturn(List.of(application));
        when(profileServiceClient.getCandidateProfilePreview(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Profile Error"));

        List<RecruiterJobApplicationResponseDto> result = applicationService.getApplicationsForRecruiter(recruiterUser);
        assertEquals(1, result.size());
        assertNull(result.get(0).getCandidateProfile());
    }

    @Test
    void profileServiceFallbackRecruiter_Success() {
        when(jobServiceClient.getJobIdsByRecruiter(2L)).thenReturn(List.of(10L));
        when(jobApplicationRepository.findByJobIdInOrderByAppliedAtDesc(any())).thenReturn(List.of(application));
        
        List<RecruiterJobApplicationResponseDto> result = applicationService.profileServiceFallbackRecruiter(recruiterUser, new Exception("Error"));
        assertEquals(1, result.size());
    }

    // --- updateApplicationStatus ---

    @Test
    void updateApplicationStatus_Recruiter_Success() {
        ApplicationStatusUpdateRequestDto req = new ApplicationStatusUpdateRequestDto();
        req.setStatus(ApplicationStatus.SHORTLISTED);

        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.of(application));
        when(jobServiceClient.isJobOwnedByRecruiter(10L, 2L)).thenReturn(true);
        when(jobApplicationRepository.save(any())).thenReturn(application);

        ApplicationResponseDto result = applicationService.updateApplicationStatus(recruiterUser, 100L, req);
        assertNotNull(result);
        verify(notificationEventProducer).sendNotification(any());
    }

    @Test
    void updateApplicationStatus_Recruiter_TryToSetApplied_ShouldThrow() {
        ApplicationStatusUpdateRequestDto req = new ApplicationStatusUpdateRequestDto();
        req.setStatus(ApplicationStatus.APPLIED);
        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.of(application));

        assertThrows(BadRequestException.class, () -> applicationService.updateApplicationStatus(recruiterUser, 100L, req));
    }

    @Test
    void updateApplicationStatus_Recruiter_NotOwner_ShouldThrow() {
        ApplicationStatusUpdateRequestDto req = new ApplicationStatusUpdateRequestDto();
        req.setStatus(ApplicationStatus.SHORTLISTED);
        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.of(application));
        when(jobServiceClient.isJobOwnedByRecruiter(10L, 2L)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> applicationService.updateApplicationStatus(recruiterUser, 100L, req));
    }

    @Test
    void updateApplicationStatus_Candidate_OfferAccepted_Success() {
        application.setStatus(ApplicationStatus.ACCEPTED); // Means recruiter made an offer
        ApplicationStatusUpdateRequestDto req = new ApplicationStatusUpdateRequestDto();
        req.setStatus(ApplicationStatus.OFFER_ACCEPTED);

        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.of(application));
        when(jobApplicationRepository.save(any())).thenReturn(application);

        ApplicationResponseDto result = applicationService.updateApplicationStatus(candidateUser, 100L, req);
        assertEquals(ApplicationStatus.OFFER_ACCEPTED, application.getStatus());
    }

    @Test
    void updateApplicationStatus_Candidate_InvalidInitialStatus_ShouldThrow() {
        application.setStatus(ApplicationStatus.APPLIED);
        ApplicationStatusUpdateRequestDto req = new ApplicationStatusUpdateRequestDto();
        req.setStatus(ApplicationStatus.OFFER_ACCEPTED);
        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.of(application));

        assertThrows(BadRequestException.class, () -> applicationService.updateApplicationStatus(candidateUser, 100L, req));
    }

    @Test
    void updateApplicationStatus_Candidate_WrongUser_ShouldThrow() {
        application.setCandidateId(999L);
        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.of(application));
        assertThrows(UnauthorizedException.class, () -> applicationService.updateApplicationStatus(candidateUser, 100L, new ApplicationStatusUpdateRequestDto()));
    }

    @Test
    void updateApplicationStatus_OfferEnumPersistenceFallback_Success() {
        application.setStatus(ApplicationStatus.ACCEPTED);
        ApplicationStatusUpdateRequestDto req = new ApplicationStatusUpdateRequestDto();
        req.setStatus(ApplicationStatus.OFFER_ACCEPTED);

        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.of(application));
        // First save fails with DataIntegrity (e.g. DB enum missing OFFER_ACCEPTED)
        when(jobApplicationRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException("enum error"))
                .thenReturn(application);

        ApplicationResponseDto result = applicationService.updateApplicationStatus(candidateUser, 100L, req);
        assertEquals(ApplicationStatus.ACCEPTED, application.getStatus());
    }

    // --- getApplicationSummary ---

    @Test
    void getApplicationSummary_Success() {
        when(jobApplicationRepository.findById(100L)).thenReturn(Optional.of(application));
        ApplicationSummaryDto result = applicationService.getApplicationSummary(100L);
        assertEquals(100L, result.getId());
    }

    // --- getApplicationsByJobId ---

    @Test
    void getApplicationsByJobId_Success() {
        when(jobServiceClient.isJobOwnedByRecruiter(10L, 2L)).thenReturn(true);
        when(jobApplicationRepository.findByJobIdOrderByAppliedAtDesc(10L)).thenReturn(List.of(application));
        
        List<ApplicationResponseDto> result = applicationService.getApplicationsByJobId(recruiterUser, 10L);
        assertEquals(1, result.size());
    }

    @Test
    void getApplicationsByJobId_NotAuthorized_ShouldThrow() {
        when(jobServiceClient.isJobOwnedByRecruiter(10L, 2L)).thenReturn(false);
        assertThrows(UnauthorizedException.class, () -> applicationService.getApplicationsByJobId(recruiterUser, 10L));
    }

    // --- getApplicationsForRecruiterJob ---

    @Test
    void getApplicationsForRecruiterJob_Success() {
        when(jobServiceClient.isJobOwnedByRecruiter(10L, 2L)).thenReturn(true);
        when(jobApplicationRepository.findByJobIdOrderByAppliedAtDesc(10L)).thenReturn(List.of(application));
        when(profileServiceClient.getCandidateProfilePreview(any(), any(), any(), any())).thenReturn(new CandidateProfilePreviewDto());

        List<RecruiterJobApplicationResponseDto> result = applicationService.getApplicationsForRecruiterJob(recruiterUser, 10L);
        assertEquals(1, result.size());
    }

    @Test
    void getApplicationsForRecruiterJob_Fallback_Success() {
        when(jobApplicationRepository.findByJobIdOrderByAppliedAtDesc(10L)).thenReturn(List.of(application));
        List<RecruiterJobApplicationResponseDto> result = applicationService.profileServiceFallback(recruiterUser, 10L, new Exception());
        assertEquals(1, result.size());
        assertNull(result.get(0).getCandidateProfile());
    }

    // --- hasCandidateAppliedToJob ---

    @Test
    void hasCandidateAppliedToJob_Success() {
        when(jobApplicationRepository.existsByCandidateIdAndJobId(1L, 10L)).thenReturn(true);
        assertTrue(applicationService.hasCandidateAppliedToJob(1L, 10L));
    }

    // --- downloadOfferLetterPdf ---

    @Test
    void downloadOfferLetterPdf_Success() throws Exception {
        application.setStatus(ApplicationStatus.OFFER_ACCEPTED);
        when(jobServiceClient.isJobOwnedByRecruiter(10L, 2L)).thenReturn(true);
        when(jobApplicationRepository.findByJobIdAndCandidateId(10L, 1L)).thenReturn(Optional.of(application));
        
        CandidateFullProfileForOfferDto profile = CandidateFullProfileForOfferDto.builder()
                .firstName("John")
                .lastName("Doe")
                .build();
        when(profileServiceClient.getCandidateFullProfileForOffer(any(), any(), any(), anyLong(), anyLong()))
                .thenReturn(profile);
        
        JobOfferDetailsDto job = new JobOfferDetailsDto();
        job.setCompanyName("HireConnect");
        job.setTitle("Dev");
        when(jobServiceClient.getJobById(10L)).thenReturn(job);

        byte[] pdf = applicationService.downloadOfferLetterPdf(recruiterUser, 1L, 10L);
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void downloadOfferLetterPdf_OfferNotAccepted_ShouldThrow() {
        application.setStatus(ApplicationStatus.SHORTLISTED);
        when(jobServiceClient.isJobOwnedByRecruiter(10L, 2L)).thenReturn(true);
        when(jobApplicationRepository.findByJobIdAndCandidateId(10L, 1L)).thenReturn(Optional.of(application));
        
        assertThrows(BadRequestException.class, () -> applicationService.downloadOfferLetterPdf(recruiterUser, 1L, 10L));
    }

    @Test
    void validateCandidate_WrongRole_ShouldThrow() {
        assertThrows(UnauthorizedException.class, () -> applicationService.getMyApplications(recruiterUser));
    }
}
