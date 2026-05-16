package com.hireconnect.notificationservice.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.hireconnect.notificationservice.dto.response.NotificationResponseDto;
import com.hireconnect.notificationservice.security.AuthenticatedUser;
import com.hireconnect.notificationservice.service.NotificationService;
import com.hireconnect.notificationservice.enums.Role;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private AuthenticatedUser user;
    private NotificationResponseDto responseDto;

    @BeforeEach
    void setUp() {
        user = new AuthenticatedUser(1L, "test@test.com", Role.CANDIDATE);
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .setMessageConverters(converter)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().equals(AuthenticatedUser.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return user;
                    }
                })
                .build();

        responseDto = NotificationResponseDto.builder()
                .id(100L)
                .userId(1L)
                .title("Test Title")
                .message("Test Message")
                .type(null)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getMyNotifications_Success() throws Exception {
        when(notificationService.getMyNotifications(any(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(new ArrayList<>(List.of(responseDto)), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/notifications"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(100L));
    }

    @Test
    void getUnreadCount_Success() throws Exception {
        when(notificationService.getUnreadCount(any())).thenReturn(5L);

        mockMvc.perform(get("/api/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void markAsRead_Success() throws Exception {
        when(notificationService.markAsRead(any(), anyLong())).thenReturn(responseDto);

        mockMvc.perform(put("/api/notifications/100/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void deleteNotification_Success() throws Exception {
        mockMvc.perform(delete("/api/notifications/100"))
                .andExpect(status().isOk())
                .andExpect(content().string("\"Notification deleted successfully\""));
    }
}
