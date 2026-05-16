package com.hireconnect.jobservice.mapper;

import org.springframework.stereotype.Component;

import com.hireconnect.jobservice.dto.request.JobRequestDto;
import com.hireconnect.jobservice.dto.response.JobResponseDto;
import com.hireconnect.jobservice.entity.Job;
/**
 * Domain entity or core component representing JobMapper.
 *
 * @author Disha Gujar
 */

@Component
public class JobMapper {
    /**
     * To entity.
     *
     * @author Disha Gujar
     */

    public Job toEntity(JobRequestDto requestDto, Long recruiterId) {
        return Job.builder()
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .companyName(requestDto.getCompanyName())
                .location(requestDto.getLocation())
                .jobType(requestDto.getJobType())
                .experienceLevel(requestDto.getExperienceLevel())
                .salaryMin(requestDto.getSalaryMin())
                .salaryMax(requestDto.getSalaryMax())
                .skillsRequired(requestDto.getSkillsRequired())
                .status(requestDto.getStatus())
                .recruiterId(recruiterId)
                .build();
    }
    /**
     * Updates entity.
     *
     * @author Disha Gujar
     */

    public void updateEntity(Job job, JobRequestDto requestDto) {
        job.setTitle(requestDto.getTitle());
        job.setDescription(requestDto.getDescription());
        job.setCompanyName(requestDto.getCompanyName());
        job.setLocation(requestDto.getLocation());
        job.setJobType(requestDto.getJobType());
        job.setExperienceLevel(requestDto.getExperienceLevel());
        job.setSalaryMin(requestDto.getSalaryMin());
        job.setSalaryMax(requestDto.getSalaryMax());
        job.setSkillsRequired(requestDto.getSkillsRequired());
        job.setStatus(requestDto.getStatus());
    }
    /**
     * To response dto.
     *
     * @author Disha Gujar
     */

    public JobResponseDto toResponseDto(Job job) {
        return JobResponseDto.builder()
                .jobId(job.getJobId())
                .title(job.getTitle())
                .description(job.getDescription())
                .companyName(job.getCompanyName())
                .location(job.getLocation())
                .jobType(job.getJobType())
                .experienceLevel(job.getExperienceLevel())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .skillsRequired(job.getSkillsRequired())
                .status(job.getStatus())
                .recruiterId(job.getRecruiterId())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .isFeatured(job.getIsFeatured())   // ⭐ THIS LINE FIXES EVERYTHING
                .build();
    }
}
