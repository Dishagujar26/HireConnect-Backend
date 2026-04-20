package com.hireconnect.notificationservice.service;

import org.springframework.data.domain.Page;

import com.hireconnect.notificationservice.dto.request.NotificationCreateRequestDto;
import com.hireconnect.notificationservice.dto.response.NotificationResponseDto;
import com.hireconnect.notificationservice.security.AuthenticatedUser;

// [Disha Gujar] : Service interface defining the business logic contract for notification management.
// Covers in-app notification creation (called internally from Kafka consumers), paginated retrieval
// per authenticated user, unread count, mark-as-read, and notification deletion operations.
public interface NotificationService {

    NotificationResponseDto createNotification(NotificationCreateRequestDto requestDto);

    Page<NotificationResponseDto> getMyNotifications(AuthenticatedUser user, int page, int size);

    long getUnreadCount(AuthenticatedUser user);

    NotificationResponseDto markAsRead(AuthenticatedUser user, Long notificationId);

    void deleteNotification(AuthenticatedUser user, Long notificationId);
}