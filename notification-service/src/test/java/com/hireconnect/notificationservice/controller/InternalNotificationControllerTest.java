package com.hireconnect.notificationservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireconnect.notificationservice.dto.request.NotificationCreateRequestDto;
import com.hireconnect.notificationservice.dto.response.NotificationResponseDto;
import com.hireconnect.notificationservice.enums.NotificationType;
import com.hireconnect.notificationservice.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class InternalNotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private InternalNotificationController internalNotificationController;

    private ObjectMapper objectMapper = new ObjectMapper();
    private NotificationCreateRequestDto requestDto;
    private NotificationResponseDto responseDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(internalNotificationController).build();

        requestDto = new NotificationCreateRequestDto();
        requestDto.setRecipientUserId(1L);
        requestDto.setRecipientEmail("test@test.com");
        requestDto.setTitle("Test");
        requestDto.setMessage("Body");
        requestDto.setType(NotificationType.APPLICATION);
        requestDto.setSendEmail(true);

        responseDto = NotificationResponseDto.builder()
                .id(100L)
                .userId(1L)
                .title("Test")
                .message("Body")
                .type(NotificationType.APPLICATION)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createNotification_Success() throws Exception {
        when(notificationService.createNotification(any(NotificationCreateRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/internal/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void createNotification_ValidationFailure_ShouldReturn400() throws Exception {
        requestDto.setTitle(null); // Invalid: Title is required

        mockMvc.perform(post("/internal/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }
}
