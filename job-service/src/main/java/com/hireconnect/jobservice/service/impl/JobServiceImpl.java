package com.hireconnect.jobservice.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hireconnect.jobservice.dto.request.JobRequestDto;
import com.hireconnect.jobservice.dto.response.JobResponseDto;
import com.hireconnect.jobservice.entity.ExperienceLevel;
import com.hireconnect.jobservice.entity.Job;
import com.hireconnect.jobservice.entity.JobStatus;
import com.hireconnect.jobservice.entity.JobType;
import com.hireconnect.jobservice.entity.Role;
import com.hireconnect.jobservice.exception.JobNotFoundException;
import com.hireconnect.jobservice.exception.UnauthorizedJobAccessException;
import com.hireconnect.jobservice.mapper.JobMapper;
import com.hireconnect.jobservice.repository.JobRepository;
import com.hireconnect.jobservice.service.JobService;
import com.hireconnect.jobservice.specification.JobSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    @Override
    @Transactional
    public JobResponseDto createJob(Long userId, Role role, JobRequestDto requestDto) {
        log.info("Creating job for recruiterId={} with title={}", userId, requestDto.getTitle());
        validateRecruiter(role);

        Job job = jobMapper.toEntity(requestDto, userId);
        Job savedJob = jobRepository.save(job);

        log.info("Job persisted successfully with jobId={} for recruiterId={}", savedJob.getJobId(), userId);
        return jobMapper.toResponseDto(savedJob);
    }

    @Override
    @Transactional
    public JobResponseDto updateJob(Long jobId, Long userId, Role role, JobRequestDto requestDto) {
        log.info("Updating jobId={} for recruiterId={}", jobId, userId);
        validateRecruiter(role);

        Job existingJob = jobRepository.findByJobIdAndRecruiterId(jobId, userId)
                .orElseThrow(() -> new JobNotFoundException("Job not found or you are not authorized to update it"));

        existingJob.setTitle(requestDto.getTitle());
        existingJob.setDescription(requestDto.getDescription());
        existingJob.setCompanyName(requestDto.getCompanyName());
        existingJob.setLocation(requestDto.getLocation());
        existingJob.setJobType(requestDto.getJobType());
        existingJob.setExperienceLevel(requestDto.getExperienceLevel());
        existingJob.setSalaryMin(requestDto.getSalaryMin());
        existingJob.setSalaryMax(requestDto.getSalaryMax());
        existingJob.setSkillsRequired(requestDto.getSkillsRequired());
        existingJob.setStatus(requestDto.getStatus());

        Job updatedJob = jobRepository.save(existingJob);
        log.info("Job updated successfully for jobId={} by recruiterId={}", jobId, userId);
        return jobMapper.toResponseDto(updatedJob);
    }

    @Override
    @Transactional
    public void deleteJob(Long jobId, Long userId, Role role) {
        log.info("Deleting jobId={} for recruiterId={}", jobId, userId);
        validateRecruiter(role);

        Job existingJob = jobRepository.findByJobIdAndRecruiterId(jobId, userId)
                .orElseThrow(() -> new JobNotFoundException("Job not found or you are not authorized to delete it"));

        jobRepository.delete(existingJob);
        log.info("Job deleted successfully for jobId={} by recruiterId={}", jobId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponseDto> getMyJobs(Long userId, Role role) {
        log.info("Fetching jobs for recruiterId={}", userId);
        validateRecruiter(role);

        return jobRepository.findByRecruiterIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(jobMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponseDto> getAllOpenJobs() {
        log.info("Fetching all open jobs");
        return jobRepository.findAll()
                .stream()
                .filter(job -> job.getStatus() == JobStatus.OPEN)
                .map(jobMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponseDto getOpenJobById(Long jobId) {
        log.info("Fetching open job details for jobId={}", jobId);
        Job job = jobRepository.findById(jobId)
                .filter(existingJob -> existingJob.getStatus() == JobStatus.OPEN)
                .orElseThrow(() -> new JobNotFoundException("Open job not found for jobId: " + jobId));

        return jobMapper.toResponseDto(job);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponseDto> searchOpenJobs(
            String keyword,
            String location,
            JobType jobType,
            ExperienceLevel experienceLevel,
            Double minSalary,
            Double maxSalary
    ) {
        log.info("Searching open jobs with keyword={}, location={}, jobType={}, experienceLevel={}, minSalary={}, maxSalary={}", keyword, location, jobType, experienceLevel, minSalary, maxSalary);
        return jobRepository.findAll(
                        JobSpecification.filterOpenJobs(
                                keyword,
                                location,
                                jobType,
                                experienceLevel,
                                minSalary,
                                maxSalary
                        )
                )
                .stream()
                .map(jobMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean doesJobExist(Long jobId) {
        log.debug("Checking if job exists for jobId={}", jobId);
        return jobRepository.existsByJobId(jobId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isJobOpen(Long jobId) {
        log.debug("Checking if job is open for jobId={}", jobId);
        return jobRepository.existsByJobIdAndStatus(jobId, JobStatus.OPEN);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isJobOwnedByRecruiter(Long jobId, Long recruiterId) {
        log.debug("Checking ownership for jobId={} and recruiterId={}", jobId, recruiterId);
        return jobRepository.findByJobIdAndRecruiterId(jobId, recruiterId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getJobIdsByRecruiter(Long recruiterId) {
        log.info("Fetching job ids for recruiterId={}", recruiterId);
        return jobRepository.findByRecruiterIdOrderByCreatedAtDesc(recruiterId)
                .stream()
                .map(Job::getJobId)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getRecruiterIdByJobId(Long jobId) {
        log.info("Fetching recruiterId for jobId={}", jobId);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException("Job not found for jobId: " + jobId));
        return job.getRecruiterId();
    }

    private void validateRecruiter(Role role) {
        if (role != Role.RECRUITER) {
            log.warn("Unauthorized job access attempt by role={}", role);
            throw new UnauthorizedJobAccessException("Only recruiters can perform this action");
        }
    }
    
    @Override
    @Transactional
    public void markAsFeatured(Long jobId, Long recruiterId, Role role) {
        log.info("Marking job as featured for jobId={} by recruiterId={}", jobId, recruiterId);
        validateRecruiter(role);

        Job job = jobRepository.findByJobIdAndRecruiterId(jobId, recruiterId)
                .orElseThrow(() -> new JobNotFoundException(
                        "Job not found or you are not authorized to promote this job"));

        job.setIsFeatured(true);
        jobRepository.save(job);
        log.info("Job marked as featured successfully for jobId={} by recruiterId={}", jobId, recruiterId);
    }
}