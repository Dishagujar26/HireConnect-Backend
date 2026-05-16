package com.hireconnect.notificationservice.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.hireconnect.notificationservice.dto.request.NotificationCreateRequestDto;
import com.hireconnect.notificationservice.dto.response.NotificationResponseDto;
import com.hireconnect.notificationservice.entity.Notification;
import com.hireconnect.notificationservice.enums.NotificationType;
import com.hireconnect.notificationservice.exception.ResourceNotFoundException;
import com.hireconnect.notificationservice.exception.UnauthorizedException;
import com.hireconnect.notificationservice.mapper.NotificationMapper;
import com.hireconnect.notificationservice.repository.NotificationRepository;
import com.hireconnect.notificationservice.security.AuthenticatedUser;
import com.hireconnect.notificationservice.service.EmailService;
import com.hireconnect.notificationservice.enums.Role;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private AuthenticatedUser user;
    private Notification notification;
    private NotificationCreateRequestDto createRequestDto;
    private NotificationResponseDto responseDto;

    @BeforeEach
    void setUp() {
        user = new AuthenticatedUser(1L, "test@test.com", Role.CANDIDATE);

        notification = new Notification(
                100L, 
                1L, 
                "Test Title", 
                "Test Message", 
                NotificationType.APPLICATION, 
                false, 
                LocalDateTime.now()
        );

        createRequestDto = new NotificationCreateRequestDto();
        createRequestDto.setRecipientUserId(1L);
        createRequestDto.setRecipientEmail("test@test.com");
        createRequestDto.setTitle("Test Title");
        createRequestDto.setMessage("Test Message");
        createRequestDto.setType(NotificationType.APPLICATION);
        createRequestDto.setSendEmail(true);

        responseDto = NotificationResponseDto.builder()
                .id(100L)
                .userId(1L)
                .title("Test Title")
                .message("Test Message")
                .type(NotificationType.APPLICATION)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createNotification_Success() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toResponseDto(any(Notification.class))).thenReturn(responseDto);

        NotificationResponseDto result = notificationService.createNotification(createRequestDto);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void createNotification_WithoutEmail_Success() {
        createRequestDto.setSendEmail(false);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toResponseDto(any(Notification.class))).thenReturn(responseDto);

        notificationService.createNotification(createRequestDto);

        verify(emailService, times(0)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void getMyNotifications_Success() {
        Page<Notification> page = new PageImpl<>(List.of(notification));
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(page);
        when(notificationMapper.toResponseDto(any(Notification.class))).thenReturn(responseDto);

        Page<NotificationResponseDto> result = notificationService.getMyNotifications(user, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getUnreadCount_Success() {
        when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(5L);

        long count = notificationService.getUnreadCount(user);

        assertEquals(5L, count);
    }

    @Test
    void markAsRead_Success() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(notificationMapper.toResponseDto(any(Notification.class))).thenReturn(responseDto);

        NotificationResponseDto result = notificationService.markAsRead(user, 100L);

        assertNotNull(result);
        assertTrue(notification.getIsRead());
    }

    @Test
    void markAsRead_NotFound_ThrowsException() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> notificationService.markAsRead(user, 100L));
    }

    @Test
    void markAsRead_Unauthorized_ThrowsException() {
        notification.setUserId(99L);
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification));

        assertThrows(UnauthorizedException.class, () -> notificationService.markAsRead(user, 100L));
    }

    @Test
    void deleteNotification_Success() {
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification));

        notificationService.deleteNotification(user, 100L);

        verify(notificationRepository, times(1)).delete(any(Notification.class));
    }

    @Test
    void deleteNotification_Unauthorized_ThrowsException() {
        notification.setUserId(99L);
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(notification));

        assertThrows(UnauthorizedException.class, () -> notificationService.deleteNotification(user, 100L));
    }
}
