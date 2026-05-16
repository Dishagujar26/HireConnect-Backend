package com.hireconnect.jobservice.service;

import java.util.List;

import com.hireconnect.jobservice.dto.request.JobRequestDto;
import com.hireconnect.jobservice.dto.response.JobResponseDto;
import com.hireconnect.jobservice.entity.ExperienceLevel;
import com.hireconnect.jobservice.entity.JobType;
import com.hireconnect.jobservice.entity.Role;

/**
 * Service interface for job management.
 * Defines the contract for job CRUD operations, searching, and internal utility methods.
 * @author Disha Gujar
 */
public interface JobService {

    /**
     * Creates a new job posting.
     * 
     * @param userId the ID of the recruiter
     * @param role the role of the user (must be RECRUITER)
     * @param requestDto the job creation request data
     * @return the created JobResponseDto
     
 * @author Disha Gujar
 */
    JobResponseDto createJob(Long userId, Role role, JobRequestDto requestDto);

    /**
     * Updates an existing job posting.
     * 
     * @param jobId the ID of the job
     * @param userId the ID of the recruiter
     * @param role the role of the user
     * @param requestDto the updated job data
     * @return the updated JobResponseDto
     
 * @author Disha Gujar
 */
    JobResponseDto updateJob(Long jobId, Long userId, Role role, JobRequestDto requestDto);

    /**
     * Deletes a job posting.
     * 
     * @param jobId the ID of the job
     * @param userId the ID of the recruiter
     * @param role the role of the user
     
 * @author Disha Gujar
 */
    void deleteJob(Long jobId, Long userId, Role role);

    /**
     * Retrieves all jobs created by a specific recruiter.
     * 
     * @param userId the ID of the recruiter
     * @param role the role of the user
     * @return a list of JobResponseDto
     
 * @author Disha Gujar
 */
    List<JobResponseDto> getMyJobs(Long userId, Role role);

    /**
     * Retrieves all open job postings.
     * 
     * @return a list of open JobResponseDto
     
 * @author Disha Gujar
 */
    List<JobResponseDto> getAllOpenJobs();

    /**
     * Retrieves an open job by its ID.
     * 
     * @param jobId the ID of the job
     * @return the JobResponseDto
     
 * @author Disha Gujar
 */
    JobResponseDto getOpenJobById(Long jobId);

    /**
     * Searches for open jobs based on various filters.
     * 
     * @param keyword search keyword
     * @param location location filter
     * @param jobType job type filter
     * @param experienceLevel experience level filter
     * @param minSalary minimum salary
     * @param maxSalary maximum salary
     * @return a list of matching JobResponseDto
     
 * @author Disha Gujar
 */
    List<JobResponseDto> searchOpenJobs(
            String keyword,
            String location,
            JobType jobType,
            ExperienceLevel experienceLevel,
            Double minSalary,
            Double maxSalary
    );

    /**
     * Checks if a job exists.
     * 
     * @param jobId the ID of the job
     * @return true if exists, false otherwise
     
 * @author Disha Gujar
 */
    boolean doesJobExist(Long jobId);

    /**
     * Checks if a job is currently open.
     * 
     * @param jobId the ID of the job
     * @return true if open, false otherwise
     
 * @author Disha Gujar
 */
    boolean isJobOpen(Long jobId);

    /**
     * Checks if a job is owned by a specific recruiter.
     * 
     * @param jobId the ID of the job
     * @param recruiterId the ID of the recruiter
     * @return true if owned, false otherwise
     
 * @author Disha Gujar
 */
    boolean isJobOwnedByRecruiter(Long jobId, Long recruiterId);

    /**
     * Retrieves all job IDs created by a recruiter.
     * 
     * @param recruiterId the ID of the recruiter
     * @return a list of job IDs
     
 * @author Disha Gujar
 */
    List<Long> getJobIdsByRecruiter(Long recruiterId);

    /**
     * Retrieves the recruiter ID for a given job.
     * 
     * @param jobId the ID of the job
     * @return the recruiter ID
     
 * @author Disha Gujar
 */
    Long getRecruiterIdByJobId(Long jobId);
    
    /**
     * Marks a job as featured.
     * 
     * @param jobId the ID of the job
     * @param recruiterId the ID of the recruiter
     * @param role the role of the user
     
 * @author Disha Gujar
 */
    void markAsFeatured(Long jobId, Long recruiterId, Role role);




    java.util.List<com.hireconnect.jobservice.dto.response.RecommendedJobResponseDto> getRecommendedJobs(java.util.List<String> candidateSkills, int topN);

    com.hireconnect.jobservice.dto.response.MatchScoreResponseDto computeMatchScore(Long jobId, java.util.List<String> candidateSkills);
}