package com.hireconnect.jobservice.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireconnect.jobservice.dto.request.JobRequestDto;
import com.hireconnect.jobservice.dto.response.JobResponseDto;
import com.hireconnect.jobservice.entity.ExperienceLevel;
import com.hireconnect.jobservice.entity.JobStatus;
import com.hireconnect.jobservice.entity.JobType;
import com.hireconnect.jobservice.entity.Role;
import com.hireconnect.jobservice.security.AuthenticatedUser;
import com.hireconnect.jobservice.service.JobService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.*;

import java.util.List;

class JobControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JobService jobService;

    @InjectMocks
    private JobController jobController;

    private ObjectMapper objectMapper;
    private AuthenticatedUser recruiter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        recruiter = new AuthenticatedUser(
                2L,
                "recruiter@test.com",
                Role.RECRUITER
        );

        objectMapper = new ObjectMapper();

        mockMvc = createMockMvcWithUser(recruiter);
    }

    private JobRequestDto request() {
        JobRequestDto dto = new JobRequestDto();
        dto.setTitle("Java Developer");
        dto.setDescription("Spring Boot role");
        dto.setCompanyName("HireConnect");
        dto.setLocation("Bhopal");
        dto.setJobType(JobType.FULL_TIME);
        dto.setExperienceLevel(ExperienceLevel.FRESHER);
        dto.setSalaryMin(300000.0);
        dto.setSalaryMax(600000.0);
        dto.setSkillsRequired("Java, Spring Boot");
        dto.setStatus(JobStatus.OPEN);
        return dto;
    }

    private JobResponseDto response() {
        return JobResponseDto.builder()
                .jobId(10L)
                .title("Java Developer")
                .build();
    }

    @Test
    void createJob_ShouldReturnCreated() throws Exception {
        when(jobService.createJob(anyLong(), any(Role.class), any(JobRequestDto.class)))
                .thenReturn(response());

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobId").value(10));
    }

    @Test
    void updateJob_ShouldReturnOk() throws Exception {
        when(jobService.updateJob(eq(10L), anyLong(), any(Role.class), any(JobRequestDto.class)))
                .thenReturn(response());

        mockMvc.perform(put("/api/jobs/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isOk());
    }

    @Test
    void deleteJob_ShouldReturnOk() throws Exception {
        doNothing().when(jobService)
                .deleteJob(10L, 2L, Role.RECRUITER);

        mockMvc.perform(delete("/api/jobs/10"))
                .andExpect(status().isOk())
                .andExpect(content().string("Job deleted successfully"));
    }

    @Test
    void getMyJobs_ShouldReturnOk() throws Exception {
        when(jobService.getMyJobs(2L, Role.RECRUITER))
                .thenReturn(List.of(response()));

        mockMvc.perform(get("/api/jobs/recruiter/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jobId").value(10));
    }

    @Test
    void getAllOpenJobs_ShouldReturnOk() throws Exception {
        when(jobService.getAllOpenJobs())
                .thenReturn(List.of(response()));

        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk());
    }

    @Test
    void getOpenJobById_ShouldReturnOk() throws Exception {
        when(jobService.getOpenJobById(10L))
                .thenReturn(response());

        mockMvc.perform(get("/api/jobs/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(10));
    }

    @Test
    void searchJobs_ShouldReturnOk() throws Exception {
        when(jobService.searchOpenJobs(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(response()));

        mockMvc.perform(get("/api/jobs/search")
                        .param("keyword", "Java"))
                .andExpect(status().isOk());
    }

    @Test
    void doesJobExist_ShouldReturnOk() throws Exception {
        when(jobService.doesJobExist(10L)).thenReturn(true);
        mockMvc.perform(get("/api/jobs/internal/10/exists")).andExpect(status().isOk());
    }

    @Test
    void markJobAsFeatured_Success() throws Exception {
        doNothing().when(jobService).markAsFeatured(10L, 2L, Role.RECRUITER);
        mockMvc.perform(put("/api/jobs/10/feature"))
                .andExpect(status().isOk())
                .andExpect(content().string("Job marked as featured"));
    }

    @Test
    void markJobAsFeatured_Unauthorized() throws Exception {
        MockMvc nullMvc = createMockMvcWithUser(null);
        nullMvc.perform(put("/api/jobs/10/feature"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getRecommendedJobs_WithLimit() throws Exception {
        mockMvc.perform(get("/api/jobs/recommended")
                        .param("skills", "Java")
                        .param("limit", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getRecommendedJobs_DefaultLimit() throws Exception {
        mockMvc.perform(get("/api/jobs/recommended")
                        .param("skills", "Java"))
                .andExpect(status().isOk());
    }

    @Test
    void getMatchScore_Success() throws Exception {
        mockMvc.perform(get("/api/jobs/10/match-score")
                        .param("skills", "Java"))
                .andExpect(status().isOk());
    }

    private MockMvc createMockMvcWithUser(AuthenticatedUser user) {
        return MockMvcBuilders.standaloneSetup(jobController)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter p) { return true; }
                    @Override
                    public Object resolveArgument(MethodParameter p, ModelAndViewContainer m, NativeWebRequest w, WebDataBinderFactory f) {
                        return user;
                    }
                }).build();
    }
}