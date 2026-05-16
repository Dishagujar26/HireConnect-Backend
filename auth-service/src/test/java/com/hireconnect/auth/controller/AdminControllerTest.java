package com.hireconnect.auth.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.hireconnect.auth.entity.Role;
import com.hireconnect.auth.entity.UserCredential;
import com.hireconnect.auth.exception.GlobalExceptionHandler;
import com.hireconnect.auth.repository.AuthRepository;
import com.hireconnect.auth.security.JwtService;

class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthRepository authRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AdminController adminController;

    private final String ADMIN_TOKEN = "Bearer admin-token";
    private final String NON_ADMIN_TOKEN = "Bearer user-token";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        ReflectionTestUtils.setField(adminController, "adminEmail", "admin@hireconnect.com");

        // Default: admin token resolves to ADMIN role, non-admin to CANDIDATE
        when(jwtService.extractRole("admin-token")).thenReturn("ADMIN");
        when(jwtService.extractRole("user-token")).thenReturn("CANDIDATE");
    }

    // ── getAllUsers ────────────────────────────────────────────────────────────

    @Test
    void getAllUsers_AsAdmin_ShouldReturnUserList() throws Exception {
        UserCredential u1 = new UserCredential();
        u1.setUserId(1L);
        u1.setEmail("alice@example.com");
        u1.setRole(Role.CANDIDATE);
        u1.setIsActive(true);

        UserCredential u2 = new UserCredential();
        u2.setUserId(2L);
        u2.setEmail("bob@example.com");
        u2.setRole(Role.RECRUITER);
        u2.setIsActive(true);

        when(authRepository.findAll()).thenReturn(Arrays.asList(u1, u2));

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("alice@example.com"))
                .andExpect(jsonPath("$[1].email").value("bob@example.com"));
    }

    @Test
    void getAllUsers_AsNonAdmin_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", NON_ADMIN_TOKEN))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_NoAuthHeader_ShouldReturn400() throws Exception {
        // Missing required @RequestHeader → Spring returns 400 Bad Request
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllUsers_UserWithNullRole_ShouldReturnUnknown() throws Exception {
        UserCredential u = new UserCredential();
        u.setUserId(3L);
        u.setEmail("norole@example.com");
        u.setRole(null);
        u.setIsActive(true);

        when(authRepository.findAll()).thenReturn(Collections.singletonList(u));

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("UNKNOWN"));
    }

    // ── suspendUser ────────────────────────────────────────────────────────────

    @Test
    void suspendUser_AsAdmin_ExistingUser_ShouldSuspend() throws Exception {
        UserCredential u = new UserCredential();
        u.setUserId(5L);
        u.setEmail("user@example.com");
        u.setRole(Role.CANDIDATE);
        u.setIsActive(true);

        when(authRepository.findById(5L)).thenReturn(Optional.of(u));
        when(authRepository.save(any())).thenReturn(u);

        mockMvc.perform(put("/api/v1/admin/users/5/suspend")
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string("User suspended successfully."));

        verify(authRepository).save(u);
    }

    @Test
    void suspendUser_AsNonAdmin_ShouldReturn403() throws Exception {
        mockMvc.perform(put("/api/v1/admin/users/5/suspend")
                        .header("Authorization", NON_ADMIN_TOKEN))
                .andExpect(status().isForbidden());
    }

    @Test
    void suspendUser_UserNotFound_ShouldReturn400() throws Exception {
        when(authRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/admin/users/99/suspend")
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isBadRequest());
    }

    @Test
    void suspendUser_AdminSelf_ShouldReturnBadRequest() throws Exception {
        UserCredential admin = new UserCredential();
        admin.setUserId(1L);
        admin.setEmail("admin@hireconnect.com");
        admin.setRole(Role.ADMIN);
        admin.setIsActive(true);

        when(authRepository.findById(1L)).thenReturn(Optional.of(admin));

        mockMvc.perform(put("/api/v1/admin/users/1/suspend")
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cannot suspend the platform admin."));
    }

    // ── activateUser ────────────────────────────────────────────────────────────

    @Test
    void activateUser_AsAdmin_ExistingUser_ShouldActivate() throws Exception {
        UserCredential u = new UserCredential();
        u.setUserId(6L);
        u.setEmail("inactive@example.com");
        u.setRole(Role.CANDIDATE);
        u.setIsActive(false);

        when(authRepository.findById(6L)).thenReturn(Optional.of(u));
        when(authRepository.save(any())).thenReturn(u);

        mockMvc.perform(put("/api/v1/admin/users/6/activate")
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string("User reactivated successfully."));

        verify(authRepository).save(u);
    }

    @Test
    void activateUser_AsNonAdmin_ShouldReturn403() throws Exception {
        mockMvc.perform(put("/api/v1/admin/users/6/activate")
                        .header("Authorization", NON_ADMIN_TOKEN))
                .andExpect(status().isForbidden());
    }

    @Test
    void activateUser_UserNotFound_ShouldReturn400() throws Exception {
        when(authRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/admin/users/99/activate")
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isBadRequest());
    }

    // ── getPlatformStats ────────────────────────────────────────────────────────

    @Test
    void getPlatformStats_AsAdmin_ShouldReturnStats() throws Exception {
        when(authRepository.count()).thenReturn(100L);
        when(authRepository.countByRole(Role.CANDIDATE)).thenReturn(60L);
        when(authRepository.countByRole(Role.RECRUITER)).thenReturn(35L);
        when(authRepository.countByIsActive(true)).thenReturn(90L);

        mockMvc.perform(get("/api/v1/admin/stats")
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(100))
                .andExpect(jsonPath("$.candidates").value(60))
                .andExpect(jsonPath("$.recruiters").value(35))
                .andExpect(jsonPath("$.activeUsers").value(90));
    }

    @Test
    void getPlatformStats_AsNonAdmin_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stats")
                        .header("Authorization", NON_ADMIN_TOKEN))
                .andExpect(status().isForbidden());
    }

    @Test
    void isAdmin_NullHeader_ShouldReturn400() throws Exception {
        // Missing required @RequestHeader → Spring returns 400 Bad Request
        mockMvc.perform(get("/api/v1/admin/stats"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void isAdmin_NoBearer_ShouldReturnFalse() throws Exception {
        // Header without "Bearer " prefix
        mockMvc.perform(get("/api/v1/admin/stats")
                        .header("Authorization", "Basic sometoken"))
                .andExpect(status().isForbidden());
    }
}
