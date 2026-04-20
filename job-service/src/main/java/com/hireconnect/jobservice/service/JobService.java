package com.hireconnect.jobservice.service;

import java.util.List;

import com.hireconnect.jobservice.dto.request.JobRequestDto;
import com.hireconnect.jobservice.dto.response.JobResponseDto;
import com.hireconnect.jobservice.entity.ExperienceLevel;
import com.hireconnect.jobservice.entity.JobType;
import com.hireconnect.jobservice.entity.Role;

// [Disha Gujar] : Service interface defining the business logic contract for job management.
// Covers job CRUD, multi-criteria open-job search, featured job marking, and internal utility methods
// used by other services to verify job existence, open status, and recruiter ownership.
public interface JobService {

    JobResponseDto createJob(Long userId, Role role, JobRequestDto requestDto);

    JobResponseDto updateJob(Long jobId, Long userId, Role role, JobRequestDto requestDto);

    void deleteJob(Long jobId, Long userId, Role role);

    List<JobResponseDto> getMyJobs(Long userId, Role role);

    List<JobResponseDto> getAllOpenJobs();

    JobResponseDto getOpenJobById(Long jobId);

    List<JobResponseDto> searchOpenJobs(
            String keyword,
            String location,
            JobType jobType,
            ExperienceLevel experienceLevel,
            Double minSalary,
            Double maxSalary
    );

    boolean doesJobExist(Long jobId);

    boolean isJobOpen(Long jobId);

    boolean isJobOwnedByRecruiter(Long jobId, Long recruiterId);

    List<Long> getJobIdsByRecruiter(Long recruiterId);

    Long getRecruiterIdByJobId(Long jobId);
    
    void markAsFeatured(Long jobId, Long recruiterId, Role role);
}