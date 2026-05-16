package com.hireconnect.interviewservice.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hireconnect.interviewservice.dto.request.InterviewCompleteRequestDto;
import com.hireconnect.interviewservice.dto.request.InterviewScheduleRequestDto;
import com.hireconnect.interviewservice.dto.request.InterviewUpdateRequestDto;
import com.hireconnect.interviewservice.dto.response.InterviewResponseDto;
import com.hireconnect.interviewservice.enums.InterviewStatus;
import com.hireconnect.interviewservice.enums.Role;
import com.hireconnect.interviewservice.security.AuthenticatedUser;
import com.hireconnect.interviewservice.service.InterviewService;

class InterviewControllerTest {

    @Mock
    private InterviewService interviewService;

    @InjectMocks
    private InterviewController interviewController;

    private AuthenticatedUser recruiterUser;
    private AuthenticatedUser candidateUser;
    private InterviewResponseDto responseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        recruiterUser = new AuthenticatedUser(2L, "recruiter@example.com", Role.RECRUITER);
        candidateUser = new AuthenticatedUser(1L, "candidate@example.com", Role.CANDIDATE);

        responseDto = InterviewResponseDto.builder()
                .id(50L)
                .applicationId(10L)
                .jobId(100L)
                .candidateId(1L)
                .recruiterId(2L)
                .status(InterviewStatus.SCHEDULED)
                .build();
    }

    @Test
    void scheduleInterview_ShouldReturnCreated() {
        InterviewScheduleRequestDto request = new InterviewScheduleRequestDto();
        request.setApplicationId(10L);

        when(interviewService.scheduleInterview(eq(recruiterUser), any()))
                .thenReturn(responseDto);

        ResponseEntity<InterviewResponseDto> response =
                interviewController.scheduleInterview(recruiterUser, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(50L, response.getBody().getId());
    }

    @Test
    void scheduleInterview_NullUser_ShouldStillCallService() {
        InterviewScheduleRequestDto request = new InterviewScheduleRequestDto();
        request.setApplicationId(10L);
        when(interviewService.scheduleInterview(isNull(), any())).thenReturn(responseDto);
        
        ResponseEntity<InterviewResponseDto> response = interviewController.scheduleInterview(null, request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void getRecruiterInterviews_ShouldReturnOk() {
        when(interviewService.getRecruiterInterviews(recruiterUser))
                .thenReturn(List.of(responseDto));

        ResponseEntity<List<InterviewResponseDto>> response =
                interviewController.getRecruiterInterviews(recruiterUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getCandidateInterviews_ShouldReturnOk() {
        when(interviewService.getCandidateInterviews(candidateUser))
                .thenReturn(List.of(responseDto));

        ResponseEntity<List<InterviewResponseDto>> response =
                interviewController.getCandidateInterviews(candidateUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getInterviewDetails_ShouldReturnOk() {
        when(interviewService.getInterviewDetails(candidateUser, 50L))
                .thenReturn(responseDto);

        ResponseEntity<InterviewResponseDto> response =
                interviewController.getInterviewDetails(candidateUser, 50L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(50L, response.getBody().getId());
    }

    @Test
    void updateInterview_ShouldReturnOk() {
        InterviewUpdateRequestDto request = new InterviewUpdateRequestDto();
        request.setStatus(InterviewStatus.RESCHEDULED);

        when(interviewService.updateInterview(eq(recruiterUser), eq(50L), any()))
                .thenReturn(responseDto);

        ResponseEntity<InterviewResponseDto> response =
                interviewController.updateInterview(recruiterUser, 50L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void completeInterview_ShouldReturnOk() {
        InterviewCompleteRequestDto request = new InterviewCompleteRequestDto();
        request.setTechnicalScore(8);
        request.setCommunicationScore(9);
        request.setFeedback("Good candidate");

        InterviewResponseDto completed = InterviewResponseDto.builder()
                .id(50L)
                .status(InterviewStatus.COMPLETED)
                .technicalScore(8)
                .communicationScore(9)
                .feedback("Good candidate")
                .build();

        when(interviewService.completeInterview(eq(recruiterUser), eq(50L), any()))
                .thenReturn(completed);

        ResponseEntity<InterviewResponseDto> response =
                interviewController.completeInterview(recruiterUser, 50L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(InterviewStatus.COMPLETED, response.getBody().getStatus());
    }

    @Test
    void cancelInterview_ShouldReturnOk() {
        InterviewResponseDto cancelled = InterviewResponseDto.builder()
                .id(50L)
                .status(InterviewStatus.CANCELLED)
                .build();

        when(interviewService.cancelInterview(recruiterUser, 50L))
                .thenReturn(cancelled);

        ResponseEntity<InterviewResponseDto> response =
                interviewController.cancelInterview(recruiterUser, 50L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(InterviewStatus.CANCELLED, response.getBody().getStatus());
    }

    @Test
    void cancelInterview_InvalidId_ShouldReturnBadRequest() {
        ResponseEntity<InterviewResponseDto> response =
                interviewController.cancelInterview(recruiterUser, 0L);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        response = interviewController.cancelInterview(recruiterUser, -1L);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        response = interviewController.cancelInterview(recruiterUser, null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}