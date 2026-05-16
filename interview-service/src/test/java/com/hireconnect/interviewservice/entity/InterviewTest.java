package com.hireconnect.interviewservice.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.hireconnect.interviewservice.enums.InterviewStatus;
import com.hireconnect.interviewservice.enums.InterviewType;

class InterviewTest {

    @Test
    void testGettersAndSetters() {
        Interview interview = new Interview();
        interview.setId(1L);
        interview.setApplicationId(10L);
        interview.setJobId(100L);
        interview.setCandidateId(2L);
        interview.setCandidateEmail("test@test.com");
        interview.setRecruiterId(3L);
        interview.setInterviewType(InterviewType.ONLINE);
        interview.setScheduledAt(LocalDateTime.now());
        interview.setDurationMinutes(60);
        interview.setMeetingLink("link");
        interview.setLocation("location");
        interview.setNotes("notes");
        interview.setTechnicalScore(8);
        interview.setCommunicationScore(9);
        interview.setFeedback("good");
        interview.setStatus(InterviewStatus.SCHEDULED);

        assertEquals(1L, interview.getId());
        assertEquals(10L, interview.getApplicationId());
        assertEquals(100L, interview.getJobId());
        assertEquals(2L, interview.getCandidateId());
        assertEquals("test@test.com", interview.getCandidateEmail());
        assertEquals(3L, interview.getRecruiterId());
        assertEquals(InterviewType.ONLINE, interview.getInterviewType());
        assertNotNull(interview.getScheduledAt());
        assertEquals(60, interview.getDurationMinutes());
        assertEquals("link", interview.getMeetingLink());
        assertEquals("location", interview.getLocation());
        assertEquals("notes", interview.getNotes());
        assertEquals(8, interview.getTechnicalScore());
        assertEquals(9, interview.getCommunicationScore());
        assertEquals("good", interview.getFeedback());
        assertEquals(InterviewStatus.SCHEDULED, interview.getStatus());
    }

    @Test
    void testBuilder() {
        Interview interview = Interview.builder()
                .id(1L)
                .applicationId(10L)
                .build();
        assertEquals(1L, interview.getId());
        assertEquals(10L, interview.getApplicationId());
    }

    @Test
    void testOnCreate() {
        Interview interview = new Interview();
        interview.onCreate();
        assertNotNull(interview.getCreatedAt());
        assertNotNull(interview.getUpdatedAt());
        assertEquals(InterviewStatus.SCHEDULED, interview.getStatus());
    }

    @Test
    void testOnCreate_ExistingStatus() {
        Interview interview = new Interview();
        interview.setStatus(InterviewStatus.COMPLETED);
        interview.onCreate();
        assertEquals(InterviewStatus.COMPLETED, interview.getStatus());
    }

    @Test
    void testOnUpdate() {
        Interview interview = new Interview();
        interview.onUpdate();
        assertNotNull(interview.getUpdatedAt());
    }
}
