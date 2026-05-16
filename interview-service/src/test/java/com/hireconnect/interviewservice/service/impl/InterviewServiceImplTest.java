package com.hireconnect.interviewservice.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hireconnect.interviewservice.client.ApplicationServiceClient;
import com.hireconnect.interviewservice.client.dto.ApplicationSummaryDto;
import com.hireconnect.interviewservice.dto.request.InterviewCompleteRequestDto;
import com.hireconnect.interviewservice.dto.request.InterviewScheduleRequestDto;
import com.hireconnect.interviewservice.dto.request.InterviewUpdateRequestDto;
import com.hireconnect.interviewservice.dto.response.InterviewResponseDto;
import com.hireconnect.interviewservice.entity.Interview;
import com.hireconnect.interviewservice.enums.InterviewStatus;
import com.hireconnect.interviewservice.enums.InterviewType;
import com.hireconnect.interviewservice.enums.Role;
import com.hireconnect.interviewservice.exception.BadRequestException;
import com.hireconnect.interviewservice.exception.ResourceNotFoundException;
import com.hireconnect.interviewservice.exception.UnauthorizedException;
import com.hireconnect.interviewservice.producer.NotificationEventProducer;
import com.hireconnect.interviewservice.repository.InterviewRepository;
import com.hireconnect.interviewservice.security.AuthenticatedUser;

@ExtendWith(MockitoExtension.class)
class InterviewServiceImplTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private ApplicationServiceClient applicationServiceClient;

    @Mock
    private NotificationEventProducer notificationEventProducer;

    @InjectMocks
    private InterviewServiceImpl interviewService;

    private AuthenticatedUser recruiterUser;
    private AuthenticatedUser candidateUser;
    private InterviewScheduleRequestDto requestDto;
    private ApplicationSummaryDto applicationSummary;
    private Interview interview;

    @BeforeEach
    void setUp() {
        recruiterUser = new AuthenticatedUser(2L, "recruiter@test.com", Role.RECRUITER);
        candidateUser = new AuthenticatedUser(1L, "candidate@test.com", Role.CANDIDATE);
        
        requestDto = new InterviewScheduleRequestDto();
        requestDto.setApplicationId(100L);
        requestDto.setInterviewType(InterviewType.ONLINE);
        requestDto.setScheduledAt(LocalDateTime.now().plusDays(1));
        requestDto.setDurationMinutes(60);
        requestDto.setMeetingLink("http://zoom.us/test");

        applicationSummary = ApplicationSummaryDto.builder()
                .id(100L)
                .jobId(10L)
                .candidateId(1L)
                .recruiterId(2L)
                .candidateEmail("candidate@test.com")
                .status("SHORTLISTED")
                .build();

        interview = Interview.builder()
                .id(500L)
                .applicationId(100L)
                .candidateId(1L)
                .candidateEmail("candidate@test.com")
                .recruiterId(2L)
                .interviewType(InterviewType.ONLINE)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .status(InterviewStatus.SCHEDULED)
                .build();
    }

    @Test
    void scheduleInterview_Success() {
        when(applicationServiceClient.getApplicationSummary(100L)).thenReturn(applicationSummary);
        when(interviewRepository.save(any(Interview.class))).thenReturn(interview);

        InterviewResponseDto response = interviewService.scheduleInterview(recruiterUser, requestDto);

        assertNotNull(response);
        assertEquals(InterviewStatus.SCHEDULED, response.getStatus());
        verify(notificationEventProducer).sendNotification(any());
    }

    @Test
    void scheduleInterview_ApplicationNotFound() {
        when(applicationServiceClient.getApplicationSummary(100L)).thenReturn(null);
        assertThrows(ResourceNotFoundException.class, () -> interviewService.scheduleInterview(recruiterUser, requestDto));
    }

    @Test
    void applicationServiceFallback_ShouldThrowException() {
        assertThrows(RuntimeException.class, () -> 
            interviewService.applicationServiceFallback(recruiterUser, requestDto, new RuntimeException("Service down")));
    }

    @Test
    void getRecruiterInterviews_Success() {
        when(interviewRepository.findByRecruiterIdOrderByScheduledAtDesc(2L)).thenReturn(List.of(interview));
        List<InterviewResponseDto> result = interviewService.getRecruiterInterviews(recruiterUser);
        assertFalse(result.isEmpty());
    }

    @Test
    void getCandidateInterviews_Success() {
        when(interviewRepository.findByCandidateIdOrderByScheduledAtDesc(1L)).thenReturn(List.of(interview));
        List<InterviewResponseDto> result = interviewService.getCandidateInterviews(candidateUser);
        assertFalse(result.isEmpty());
    }

    @Test
    void updateInterview_Success() {
        InterviewUpdateRequestDto updateDto = new InterviewUpdateRequestDto();
        updateDto.setStatus(InterviewStatus.COMPLETED);
        
        when(interviewRepository.findByIdAndRecruiterId(500L, 2L)).thenReturn(Optional.of(interview));
        when(interviewRepository.save(any())).thenReturn(interview);

        InterviewResponseDto response = interviewService.updateInterview(recruiterUser, 500L, updateDto);
        assertNotNull(response);
        verify(notificationEventProducer).sendNotification(any());
    }

    @Test
    void cancelInterview_Success() {
        when(interviewRepository.findByIdAndRecruiterId(500L, 2L)).thenReturn(Optional.of(interview));
        when(interviewRepository.save(any())).thenReturn(interview);

        interviewService.cancelInterview(recruiterUser, 500L);
        assertEquals(InterviewStatus.CANCELLED, interview.getStatus());
        verify(notificationEventProducer).sendNotification(any());
    }

    @Test
    void completeInterview_WithHireAction_Success() {
        InterviewCompleteRequestDto completeDto = new InterviewCompleteRequestDto();
        completeDto.setTechnicalScore(9);
        completeDto.setSelectionAction(InterviewCompleteRequestDto.SelectionAction.HIRE);
        
        when(interviewRepository.findByIdAndRecruiterId(500L, 2L)).thenReturn(Optional.of(interview));
        when(interviewRepository.save(any())).thenReturn(interview);

        interviewService.completeInterview(recruiterUser, 500L, completeDto);
        
        assertEquals(InterviewStatus.COMPLETED, interview.getStatus());
        verify(applicationServiceClient).updateApplicationStatus(eq(100L), any());
    }

    @Test
    void completeInterview_CancelledInterview_ShouldThrowException() {
        interview.setStatus(InterviewStatus.CANCELLED);
        when(interviewRepository.findByIdAndRecruiterId(500L, 2L)).thenReturn(Optional.of(interview));
        
        InterviewCompleteRequestDto completeDto = new InterviewCompleteRequestDto();
        assertThrows(BadRequestException.class, () -> interviewService.completeInterview(recruiterUser, 500L, completeDto));
    }

    @Test
    void getInterviewDetails_UnauthorizedCandidate() {
        interview.setCandidateId(99L);
        when(interviewRepository.findById(500L)).thenReturn(Optional.of(interview));
        assertThrows(UnauthorizedException.class, () -> interviewService.getInterviewDetails(candidateUser, 500L));
    }

    @Test
    void getInterviewDetails_UnauthorizedRecruiter() {
        interview.setRecruiterId(99L);
        when(interviewRepository.findById(500L)).thenReturn(Optional.of(interview));
        assertThrows(UnauthorizedException.class, () -> interviewService.getInterviewDetails(recruiterUser, 500L));
    }
}
