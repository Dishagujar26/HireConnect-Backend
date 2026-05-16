package com.hireconnect.interviewservice.service;

import java.util.List;

import com.hireconnect.interviewservice.dto.request.InterviewScheduleRequestDto;
import com.hireconnect.interviewservice.dto.request.InterviewUpdateRequestDto;
import com.hireconnect.interviewservice.dto.request.InterviewCompleteRequestDto;
import com.hireconnect.interviewservice.dto.response.InterviewResponseDto;
import com.hireconnect.interviewservice.security.AuthenticatedUser;

/**
 * Service interface defining the business logic contract for interview management.
 */
public interface InterviewService {

    InterviewResponseDto scheduleInterview(AuthenticatedUser user, InterviewScheduleRequestDto requestDto);

    List<InterviewResponseDto> getRecruiterInterviews(AuthenticatedUser user);

    List<InterviewResponseDto> getCandidateInterviews(AuthenticatedUser user);

    InterviewResponseDto getInterviewDetails(AuthenticatedUser user, Long interviewId);

    InterviewResponseDto updateInterview(AuthenticatedUser user, Long interviewId, InterviewUpdateRequestDto requestDto);

    InterviewResponseDto completeInterview(AuthenticatedUser user, Long interviewId, InterviewCompleteRequestDto requestDto);

    InterviewResponseDto cancelInterview(AuthenticatedUser user, Long interviewId);
}