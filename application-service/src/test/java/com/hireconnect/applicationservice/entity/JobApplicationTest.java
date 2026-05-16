package com.hireconnect.applicationservice.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import com.hireconnect.applicationservice.enums.ApplicationStatus;

class JobApplicationTest {

    @Test
    void onCreate_ShouldSetTimestampsAndStatus() {
        JobApplication app = new JobApplication();
        app.onCreate();
        
        assertNotNull(app.getAppliedAt());
        assertNotNull(app.getUpdatedAt());
        assertEquals(ApplicationStatus.APPLIED, app.getStatus());
    }

    @Test
    void onUpdate_ShouldUpdateTimestamp() throws InterruptedException {
        JobApplication app = new JobApplication();
        app.onCreate();
        java.time.LocalDateTime firstUpdate = app.getUpdatedAt();
        
        Thread.sleep(10);
        app.onUpdate();
        
        assertTrue(app.getUpdatedAt().isAfter(firstUpdate));
    }

    @Test
    void builder_ShouldSetFields() {
        JobApplication app = JobApplication.builder()
                .jobId(100L)
                .candidateId(1L)
                .status(ApplicationStatus.SHORTLISTED)
                .build();
        
        assertEquals(100L, app.getJobId());
        assertEquals(1L, app.getCandidateId());
        assertEquals(ApplicationStatus.SHORTLISTED, app.getStatus());
    }
}
