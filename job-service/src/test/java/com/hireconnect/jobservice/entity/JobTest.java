package com.hireconnect.jobservice.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class JobTest {

    @Test
    void onCreate_ShouldSetTimestamps() {
        Job job = new Job();
        job.onCreate();
        
        assertNotNull(job.getCreatedAt());
        assertNotNull(job.getUpdatedAt());
    }

    @Test
    void onUpdate_ShouldUpdateTimestamp() throws InterruptedException {
        Job job = new Job();
        job.onCreate();
        java.time.LocalDateTime firstUpdate = job.getUpdatedAt();
        
        Thread.sleep(10);
        job.onUpdate();
        
        assertTrue(job.getUpdatedAt().isAfter(firstUpdate));
    }

    @Test
    void builder_ShouldSetFields() {
        Job job = Job.builder()
                .title("Dev")
                .jobType(JobType.FULL_TIME)
                .build();
        
        assertEquals("Dev", job.getTitle());
        assertEquals(JobType.FULL_TIME, job.getJobType());
        // isFeatured might be null if not explicitly set in builder because @Builder doesn't use the field's default value
        // unless @Builder.Default is used, which it isn't here.
    }
}
