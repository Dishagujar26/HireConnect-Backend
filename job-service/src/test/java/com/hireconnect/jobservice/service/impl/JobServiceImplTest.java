package com.hireconnect.jobservice.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import com.hireconnect.jobservice.dto.request.JobRequestDto;
import com.hireconnect.jobservice.dto.response.JobResponseDto;
import com.hireconnect.jobservice.entity.Job;
import com.hireconnect.jobservice.entity.JobStatus;
import com.hireconnect.jobservice.entity.Role;
import com.hireconnect.jobservice.exception.JobNotFoundException;
import com.hireconnect.jobservice.exception.UnauthorizedJobAccessException;
import com.hireconnect.jobservice.mapper.JobMapper;
import com.hireconnect.jobservice.repository.JobRepository;
/**
 * Domain entity or core component representing JobServiceImplTest.
 *
 * @author Disha Gujar
 */

@ExtendWith(MockitoExtension.class)
public class JobServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobMapper jobMapper;

    @InjectMocks
    private JobServiceImpl jobService;

    private JobRequestDto jobRequestDto;
    private Job job;
    private JobResponseDto jobResponseDto;

    @BeforeEach
    void setUp() {
        jobRequestDto = new JobRequestDto();
        jobRequestDto.setTitle("Software Engineer");

        job = new Job();
        job.setJobId(10L);
        job.setRecruiterId(2L);
        job.setTitle("Software Engineer");
        job.setStatus(JobStatus.OPEN);

        jobResponseDto = JobResponseDto.builder()
                .jobId(10L)
                .title("Software Engineer")
                .build();
    }

    @Test
    void createJob_Success() {
        when(jobMapper.toEntity(jobRequestDto, 2L)).thenReturn(job);
        when(jobRepository.save(any(Job.class))).thenReturn(job);
        when(jobMapper.toResponseDto(job)).thenReturn(jobResponseDto);

        JobResponseDto response = jobService.createJob(2L, Role.RECRUITER, jobRequestDto);

        assertNotNull(response);
        assertEquals(10L, response.getJobId());
        assertEquals("Software Engineer", response.getTitle());
    }

    @Test
    void createJob_Unauthorized_ThrowsException() {
        assertThrows(UnauthorizedJobAccessException.class, () -> 
            jobService.createJob(1L, Role.CANDIDATE, jobRequestDto));
    }

    @Test
    void updateJob_Success() {
        when(jobRepository.findByJobIdAndRecruiterId(10L, 2L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any(Job.class))).thenReturn(job);
        when(jobMapper.toResponseDto(job)).thenReturn(jobResponseDto);

        JobResponseDto response = jobService.updateJob(10L, 2L, Role.RECRUITER, jobRequestDto);

        assertNotNull(response);
        assertEquals(10L, response.getJobId());
    }

    @Test
    void updateJob_NotFound_ThrowsException() {
        when(jobRepository.findByJobIdAndRecruiterId(10L, 2L)).thenReturn(Optional.empty());

        assertThrows(JobNotFoundException.class, () -> 
            jobService.updateJob(10L, 2L, Role.RECRUITER, jobRequestDto));
    }

    @Test
    void deleteJob_Success() {
        when(jobRepository.findByJobIdAndRecruiterId(10L, 2L)).thenReturn(Optional.of(job));
        doNothing().when(jobRepository).delete(job);

        assertDoesNotThrow(() -> jobService.deleteJob(10L, 2L, Role.RECRUITER));
        verify(jobRepository, times(1)).delete(job);
    }

    @Test
    void getOpenJobById_Success() {
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
        when(jobMapper.toResponseDto(job)).thenReturn(jobResponseDto);

        JobResponseDto response = jobService.getOpenJobById(10L);

        assertNotNull(response);
        assertEquals(10L, response.getJobId());
    }

    @Test
    void getOpenJobById_JobClosed_ThrowsException() {
        job.setStatus(JobStatus.CLOSED);
        when(jobRepository.findById(10L)).thenReturn(Optional.of(job));

        assertThrows(JobNotFoundException.class, () -> jobService.getOpenJobById(10L));
    }

    @Test
void deleteJob_NotFound_ShouldThrowException() {
    when(jobRepository.findByJobIdAndRecruiterId(10L, 2L)).thenReturn(Optional.empty());

    assertThrows(JobNotFoundException.class,
            () -> jobService.deleteJob(10L, 2L, Role.RECRUITER));
}

@Test
void deleteJob_Unauthorized_ShouldThrowException() {
    assertThrows(UnauthorizedJobAccessException.class,
            () -> jobService.deleteJob(10L, 1L, Role.CANDIDATE));
}

@Test
void updateJob_Unauthorized_ShouldThrowException() {
    assertThrows(UnauthorizedJobAccessException.class,
            () -> jobService.updateJob(10L, 1L, Role.CANDIDATE, jobRequestDto));
}

@Test
void getMyJobs_Success() {
    when(jobRepository.findByRecruiterIdOrderByCreatedAtDesc(2L)).thenReturn(List.of(job));
    when(jobMapper.toResponseDto(job)).thenReturn(jobResponseDto);

    List<JobResponseDto> result = jobService.getMyJobs(2L, Role.RECRUITER);

    assertEquals(1, result.size());
}

@Test
void getMyJobs_Unauthorized_ShouldThrowException() {
    assertThrows(UnauthorizedJobAccessException.class,
            () -> jobService.getMyJobs(1L, Role.CANDIDATE));
}

@Test
void getAllOpenJobs_ShouldReturnOnlyOpenJobs() {
    Job closedJob = new Job();
    closedJob.setJobId(20L);
    closedJob.setStatus(JobStatus.CLOSED);

    when(jobRepository.findAll()).thenReturn(List.of(job, closedJob));
    when(jobMapper.toResponseDto(job)).thenReturn(jobResponseDto);

    List<JobResponseDto> result = jobService.getAllOpenJobs();

    assertEquals(1, result.size());
}

@Test
void getOpenJobById_NotFound_ShouldThrowException() {
    when(jobRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(JobNotFoundException.class,
            () -> jobService.getOpenJobById(99L));
}

@Test
void searchOpenJobs_Success() {
    when(jobRepository.findAll(any(Specification.class))).thenReturn(List.of(job));
    when(jobMapper.toResponseDto(job)).thenReturn(jobResponseDto);

    List<JobResponseDto> result = jobService.searchOpenJobs(
            "Java",
            "Bhopal",
            null,
            null,
            300000.0,
            600000.0
    );

    assertEquals(1, result.size());
}

@Test
void doesJobExist_ShouldReturnTrue() {
    when(jobRepository.existsByJobId(10L)).thenReturn(true);

    assertTrue(jobService.doesJobExist(10L));
}

@Test
void isJobOpen_ShouldReturnTrue() {
    when(jobRepository.existsByJobIdAndStatus(10L, JobStatus.OPEN)).thenReturn(true);

    assertTrue(jobService.isJobOpen(10L));
}

@Test
void isJobOwnedByRecruiter_ShouldReturnTrue() {
    when(jobRepository.findByJobIdAndRecruiterId(10L, 2L)).thenReturn(Optional.of(job));

    assertTrue(jobService.isJobOwnedByRecruiter(10L, 2L));
}

@Test
void isJobOwnedByRecruiter_ShouldReturnFalse() {
    when(jobRepository.findByJobIdAndRecruiterId(10L, 2L)).thenReturn(Optional.empty());

    assertFalse(jobService.isJobOwnedByRecruiter(10L, 2L));
}

@Test
void getJobIdsByRecruiter_Success() {
    Job job2 = new Job();
    job2.setJobId(11L);

    when(jobRepository.findByRecruiterIdOrderByCreatedAtDesc(2L))
            .thenReturn(List.of(job, job2));

    List<Long> result = jobService.getJobIdsByRecruiter(2L);

    assertEquals(List.of(10L, 11L), result);
}

@Test
void getRecruiterIdByJobId_Success() {
    when(jobRepository.findById(10L)).thenReturn(Optional.of(job));

    Long result = jobService.getRecruiterIdByJobId(10L);

    assertEquals(2L, result);
}

@Test
void getRecruiterIdByJobId_NotFound_ShouldThrowException() {
    when(jobRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(JobNotFoundException.class,
            () -> jobService.getRecruiterIdByJobId(99L));
}

@Test
void markAsFeatured_Success() {
    when(jobRepository.findByJobIdAndRecruiterId(10L, 2L)).thenReturn(Optional.of(job));
    when(jobRepository.save(job)).thenReturn(job);

    jobService.markAsFeatured(10L, 2L, Role.RECRUITER);

    assertTrue(job.getIsFeatured());
    verify(jobRepository).save(job);
}

@Test
void markAsFeatured_Unauthorized_ShouldThrowException() {
    assertThrows(UnauthorizedJobAccessException.class,
            () -> jobService.markAsFeatured(10L, 1L, Role.CANDIDATE));
}

@Test
void markAsFeatured_NotFound_ShouldThrowException() {
    when(jobRepository.findByJobIdAndRecruiterId(10L, 2L)).thenReturn(Optional.empty());

    assertThrows(JobNotFoundException.class,
            () -> jobService.markAsFeatured(10L, 2L, Role.RECRUITER));
}

@Test
void computeMatchScore_ShouldReturnCorrectScore() {
    job.setSkillsRequired("Java, Spring Boot, MySQL");

    when(jobRepository.findById(10L)).thenReturn(Optional.of(job));

    var result = jobService.computeMatchScore(10L, List.of("Java", "MySQL"));

    assertEquals(66, result.getScore());
    assertEquals(List.of("java", "mysql"), result.getMatchedSkills());
    assertEquals(List.of("spring boot"), result.getMissingSkills());
}

@Test
void computeMatchScore_NoRequiredSkills_ShouldReturnZero() {
    job.setSkillsRequired("");

    when(jobRepository.findById(10L)).thenReturn(Optional.of(job));

    var result = jobService.computeMatchScore(10L, List.of("Java"));

    assertEquals(0, result.getScore());
}

@Test
void computeMatchScore_JobNotFound_ShouldThrowException() {
    when(jobRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(JobNotFoundException.class,
            () -> jobService.computeMatchScore(99L, List.of("Java")));
}

@Test
void getRecommendedJobs_ShouldReturnSortedLimitedJobs() {
    Job job1 = new Job();
    job1.setJobId(1L);
    job1.setStatus(JobStatus.OPEN);
    job1.setSkillsRequired("Java, Spring Boot");

    Job job2 = new Job();
    job2.setJobId(2L);
    job2.setStatus(JobStatus.OPEN);
    job2.setSkillsRequired("Python, Django");

    Job closedJob = new Job();
    closedJob.setJobId(3L);
    closedJob.setStatus(JobStatus.CLOSED);
    closedJob.setSkillsRequired("Java");

    JobResponseDto response1 = JobResponseDto.builder().jobId(1L).title("Java Job").build();
    JobResponseDto response2 = JobResponseDto.builder().jobId(2L).title("Python Job").build();

    when(jobRepository.findAll()).thenReturn(List.of(job1, job2, closedJob));
    when(jobMapper.toResponseDto(job1)).thenReturn(response1);
    when(jobMapper.toResponseDto(job2)).thenReturn(response2);

    var result = jobService.getRecommendedJobs(List.of("Java", "Spring Boot"), 1);

    assertEquals(1, result.size());
    assertEquals(100, result.get(0).getMatchScore());
}

@Test
void getRecommendedJobs_NoRequiredSkills_ShouldReturnZeroScore() {
    Job job1 = new Job();
    job1.setJobId(1L);
    job1.setStatus(JobStatus.OPEN);
    job1.setSkillsRequired(null);

    JobResponseDto response1 = JobResponseDto.builder().jobId(1L).title("Any Skill Job").build();

    when(jobRepository.findAll()).thenReturn(List.of(job1));
    when(jobMapper.toResponseDto(job1)).thenReturn(response1);

    var result = jobService.getRecommendedJobs(List.of("Java"), 5);

    assertEquals(1, result.size());
    assertEquals(0, result.get(0).getMatchScore());
}
}
