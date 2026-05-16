package com.hireconnect.notificationservice.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireconnect.notificationservice.enums.Role;
import com.hireconnect.notificationservice.repository.NotificationRepository;
import com.hireconnect.notificationservice.security.AuthenticatedUser;

class AdminNotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private AdminNotificationController adminNotificationController;

    private AuthenticatedUser adminUser;
    private AuthenticatedUser candidateUser;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminUser = new AuthenticatedUser(1L, "admin@test.com", Role.ADMIN);
        candidateUser = new AuthenticatedUser(2L, "candidate@test.com", Role.CANDIDATE);

        mockMvc = createMockMvcWithUser(adminUser);
    }

    @Test
    void broadcastMessage_Success() throws Exception {
        AdminNotificationController.BroadcastRequest request = 
            new AdminNotificationController.BroadcastRequest("Title", "Message", List.of(1L, 2L));

        mockMvc.perform(post("/api/notifications/admin/broadcast")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recipientCount").value(2));

        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    void broadcastMessage_Forbidden() throws Exception {
        MockMvc candidateMvc = createMockMvcWithUser(candidateUser);
        AdminNotificationController.BroadcastRequest request = 
            new AdminNotificationController.BroadcastRequest("Title", "Message", List.of(1L));

        candidateMvc.perform(post("/api/notifications/admin/broadcast")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void broadcastMessage_NullUser_Forbidden() throws Exception {
        MockMvc nullUserMvc = createMockMvcWithUser(null);
        AdminNotificationController.BroadcastRequest request = 
            new AdminNotificationController.BroadcastRequest("Title", "Message", List.of(1L));

        nullUserMvc.perform(post("/api/notifications/admin/broadcast")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void broadcastMessage_InvalidInput_BlankTitle() throws Exception {
        AdminNotificationController.BroadcastRequest request = 
            new AdminNotificationController.BroadcastRequest("", "Message", List.of(1L));

        mockMvc.perform(post("/api/notifications/admin/broadcast")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void broadcastMessage_NoUserIds() throws Exception {
        AdminNotificationController.BroadcastRequest request = 
            new AdminNotificationController.BroadcastRequest("Title", "Message", List.of());

        mockMvc.perform(post("/api/notifications/admin/broadcast")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private MockMvc createMockMvcWithUser(AuthenticatedUser user) {
        return MockMvcBuilders.standaloneSetup(adminNotificationController)
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
