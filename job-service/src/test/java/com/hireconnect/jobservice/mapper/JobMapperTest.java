package com.hireconnect.jobservice.mapper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.hireconnect.jobservice.dto.request.JobRequestDto;
import com.hireconnect.jobservice.dto.response.JobResponseDto;
import com.hireconnect.jobservice.entity.Job;
import com.hireconnect.jobservice.entity.JobStatus;

class JobMapperTest {

    private final JobMapper mapper = new JobMapper();

    @Test
    void toEntity_Success() {
        JobRequestDto request = new JobRequestDto();
        request.setTitle("Java Dev");
        request.setStatus(JobStatus.OPEN);

        Job job = mapper.toEntity(request, 1L);

        assertEquals("Java Dev", job.getTitle());
        assertEquals(1L, job.getRecruiterId());
        assertEquals(JobStatus.OPEN, job.getStatus());
    }

    @Test
    void updateEntity_Success() {
        Job job = new Job();
        JobRequestDto request = new JobRequestDto();
        request.setTitle("Senior Java Dev");

        mapper.updateEntity(job, request);

        assertEquals("Senior Java Dev", job.getTitle());
    }

    @Test
    void toResponseDto_Success() {
        Job job = new Job();
        job.setJobId(100L);
        job.setTitle("Dev");
        job.setIsFeatured(true);

        JobResponseDto response = mapper.toResponseDto(job);

        assertEquals(100L, response.getJobId());
        assertEquals("Dev", response.getTitle());
        assertTrue(response.getIsFeatured());
    }
}
