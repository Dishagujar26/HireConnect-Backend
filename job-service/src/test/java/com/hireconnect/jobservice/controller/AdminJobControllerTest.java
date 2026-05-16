package com.hireconnect.jobservice.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.hireconnect.jobservice.entity.Job;
import com.hireconnect.jobservice.entity.Role;
import com.hireconnect.jobservice.repository.JobRepository;
import com.hireconnect.jobservice.security.AuthenticatedUser;
import com.hireconnect.jobservice.service.JobService;

class AdminJobControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobService jobService;

    @InjectMocks
    private AdminJobController adminJobController;

    private AuthenticatedUser adminUser;
    private AuthenticatedUser recruiterUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminUser = new AuthenticatedUser(1L, "admin@test.com", Role.ADMIN);
        recruiterUser = new AuthenticatedUser(2L, "recruiter@test.com", Role.RECRUITER);

        mockMvc = MockMvcBuilders
                .standaloneSetup(adminJobController)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter,
                                                   ModelAndViewContainer mavContainer,
                                                   NativeWebRequest webRequest,
                                                   WebDataBinderFactory binderFactory) {
                        return adminUser;
                    }
                })
                .build();
    }

    @Test
    void getAllJobsForAdmin_Success() throws Exception {
        when(jobRepository.findAll()).thenReturn(List.of(new Job()));

        mockMvc.perform(get("/api/jobs/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllJobsForAdmin_ForbiddenForRecruiter() throws Exception {
        MockMvc recruiterMvc = createMockMvcWithUser(recruiterUser);

        recruiterMvc.perform(get("/api/jobs/admin/all"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllJobsForAdmin_NullUser_Forbidden() throws Exception {
        MockMvc nullUserMvc = createMockMvcWithUser(null);
        nullUserMvc.perform(get("/api/jobs/admin/all"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminDeleteJob_Success() throws Exception {
        when(jobRepository.existsByJobId(10L)).thenReturn(true);
        doNothing().when(jobRepository).deleteById(10L);

        mockMvc.perform(delete("/api/jobs/admin/10"))
                .andExpect(status().isOk())
                .andExpect(content().string("Job deleted successfully by admin."));
    }

    @Test
    void adminDeleteJob_NotFound() throws Exception {
        when(jobRepository.existsByJobId(10L)).thenReturn(false);

        mockMvc.perform(delete("/api/jobs/admin/10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminDeleteJob_NullUser_Forbidden() throws Exception {
        MockMvc nullUserMvc = createMockMvcWithUser(null);
        nullUserMvc.perform(delete("/api/jobs/admin/10"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getJobStats_Success() throws Exception {
        when(jobRepository.count()).thenReturn(100L);

        mockMvc.perform(get("/api/jobs/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalJobs").value(100));
    }

    @Test
    void getJobStats_NullUser_Forbidden() throws Exception {
        MockMvc nullUserMvc = createMockMvcWithUser(null);
        nullUserMvc.perform(get("/api/jobs/admin/stats"))
                .andExpect(status().isForbidden());
    }

    private MockMvc createMockMvcWithUser(AuthenticatedUser user) {
        return MockMvcBuilders.standaloneSetup(adminJobController)
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
