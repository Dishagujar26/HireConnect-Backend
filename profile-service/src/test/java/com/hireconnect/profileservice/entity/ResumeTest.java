package com.hireconnect.profileservice.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ResumeTest {

    @Test
    void onUpload_ShouldSetTimestamp() {
        Resume resume = new Resume();
        resume.onUpload();
        assertNotNull(resume.getUploadedAt());
    }

    @Test
    void builder_ShouldSetFields() {
        Resume resume = Resume.builder()
                .fileName("test.pdf")
                .contentType("application/pdf")
                .fileData(new byte[]{1, 2, 3})
                .build();
        
        assertEquals("test.pdf", resume.getFileName());
        assertEquals("application/pdf", resume.getContentType());
        assertArrayEquals(new byte[]{1, 2, 3}, resume.getFileData());
    }
}
