package com.hireconnect.applicationservice.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hireconnect.applicationservice.client.dto.ApplicationSummaryDto;
import com.hireconnect.applicationservice.dto.request.ApplicationRequestDto;
import com.hireconnect.applicationservice.dto.request.ApplicationStatusUpdateRequestDto;
import com.hireconnect.applicationservice.dto.response.ApplicationResponseDto;
import com.hireconnect.applicationservice.dto.response.RecruiterJobApplicationResponseDto;
import com.hireconnect.applicationservice.enums.ApplicationStatus;
import com.hireconnect.applicationservice.enums.Role;
import com.hireconnect.applicationservice.security.AuthenticatedUser;
import com.hireconnect.applicationservice.service.ApplicationService;

class ApplicationControllerTest {

    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private ApplicationController applicationController;

    private AuthenticatedUser candidateUser;
    private AuthenticatedUser recruiterUser;
    private ApplicationResponseDto responseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        candidateUser = new AuthenticatedUser(1L, "candidate@example.com", Role.CANDIDATE);
        recruiterUser = new AuthenticatedUser(2L, "recruiter@example.com", Role.RECRUITER);

        responseDto = ApplicationResponseDto.builder()
                .id(10L)
                .jobId(100L)
                .candidateId(1L)
                .recruiterId(2L)
                .status(ApplicationStatus.APPLIED)
                .build();
    }

    @Test
    void applyToJob_ShouldReturnCreated() {
        ApplicationRequestDto request = new ApplicationRequestDto();
        request.setJobId(100L);

        when(applicationService.applyToJob(eq(candidateUser), any(ApplicationRequestDto.class)))
                .thenReturn(responseDto);

        ResponseEntity<ApplicationResponseDto> response =
                applicationController.applyToJob(candidateUser, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(10L, response.getBody().getId());
    }

    @Test
    void getMyApplications_ShouldReturnOk() {
        when(applicationService.getMyApplications(candidateUser))
                .thenReturn(List.of(responseDto));

        ResponseEntity<List<ApplicationResponseDto>> response =
                applicationController.getMyApplications(candidateUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getMyApplicationById_ShouldReturnOk() {
        when(applicationService.getMyApplicationById(candidateUser, 10L))
                .thenReturn(responseDto);

        ResponseEntity<ApplicationResponseDto> response =
                applicationController.getMyApplicationById(candidateUser, 10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10L, response.getBody().getId());
    }

    @Test
    void getApplicationsForRecruiter_ShouldReturnOk() {
        RecruiterJobApplicationResponseDto recruiterDto =
                RecruiterJobApplicationResponseDto.builder()
                        .applicationId(10L)
                        .jobId(100L)
                        .candidateId(1L)
                        .status(ApplicationStatus.APPLIED)
                        .build();

        when(applicationService.getApplicationsForRecruiter(recruiterUser))
                .thenReturn(List.of(recruiterDto));

        ResponseEntity<List<RecruiterJobApplicationResponseDto>> response =
                applicationController.getApplicationsForRecruiter(recruiterUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void updateApplicationStatus_ShouldReturnOk() {
        ApplicationStatusUpdateRequestDto request = new ApplicationStatusUpdateRequestDto();
        request.setStatus(ApplicationStatus.SHORTLISTED);

        ApplicationResponseDto updated = ApplicationResponseDto.builder()
                .id(10L)
                .status(ApplicationStatus.SHORTLISTED)
                .build();

        when(applicationService.updateApplicationStatus(eq(recruiterUser), eq(10L), any()))
                .thenReturn(updated);

        ResponseEntity<ApplicationResponseDto> response =
                applicationController.updateApplicationStatus(recruiterUser, 10L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ApplicationStatus.SHORTLISTED, response.getBody().getStatus());
    }

    @Test
    void getApplicationSummary_ShouldReturnOk() {
        ApplicationSummaryDto summary = ApplicationSummaryDto.builder()
                .id(10L)
                .jobId(100L)
                .candidateId(1L)
                .build();

        when(applicationService.getApplicationSummary(10L))
                .thenReturn(summary);

        ResponseEntity<ApplicationSummaryDto> response =
                applicationController.getApplicationSummary(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10L, response.getBody().getId());
    }

    @Test
    void getApplicationsByJobId_ShouldReturnOk() {
        when(applicationService.getApplicationsByJobId(recruiterUser, 100L))
                .thenReturn(List.of(responseDto));

        ResponseEntity<List<ApplicationResponseDto>> response =
                applicationController.getApplicationsByJobId(recruiterUser, 100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getApplicationsForRecruiterJob_ShouldReturnOk() {
        RecruiterJobApplicationResponseDto recruiterDto =
                RecruiterJobApplicationResponseDto.builder()
                        .applicationId(10L)
                        .jobId(100L)
                        .candidateId(1L)
                        .status(ApplicationStatus.APPLIED)
                        .build();

        when(applicationService.getApplicationsForRecruiterJob(recruiterUser, 100L))
                .thenReturn(List.of(recruiterDto));

        ResponseEntity<List<RecruiterJobApplicationResponseDto>> response =
                applicationController.getApplicationsForRecruiterJob(recruiterUser, 100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void hasCandidateAppliedToJob_ShouldReturnTrue() {
        when(applicationService.hasCandidateAppliedToJob(1L, 100L))
                .thenReturn(true);

        Boolean result = applicationController.hasCandidateAppliedToJob(1L, 100L);

        assertTrue(result);
    }

    @Test
    void downloadOfferLetterPdf_ShouldReturnPdf() {
        byte[] pdf = "dummy-pdf".getBytes();

        when(applicationService.downloadOfferLetterPdf(recruiterUser, 1L, 100L))
                .thenReturn(pdf);

        ResponseEntity<byte[]> response =
                applicationController.downloadOfferLetterPdf(recruiterUser, 1L, 100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(pdf, response.getBody());
        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
    }
}