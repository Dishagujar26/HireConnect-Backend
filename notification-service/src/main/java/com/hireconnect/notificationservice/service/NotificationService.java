package com.hireconnect.notificationservice.service;

import org.springframework.data.domain.Page;

import com.hireconnect.notificationservice.dto.request.NotificationCreateRequestDto;
import com.hireconnect.notificationservice.dto.response.NotificationResponseDto;
import com.hireconnect.notificationservice.security.AuthenticatedUser;

/**
 * Service interface for notification management.
 * Defines methods for creating, retrieving, and managing notifications.
 * @author Disha Gujar
 */
public interface NotificationService {

    /**
     * Creates a new notification based on the provided request DTO.
     * 
     * @param requestDto the notification creation request data
     * @return the created NotificationResponseDto
     
 * @author Disha Gujar
 */
    NotificationResponseDto createNotification(NotificationCreateRequestDto requestDto);

    /**
     * Retrieves paginated notifications for the authenticated user.
     * 
     * @param user the authenticated user
     * @param page the page number
     * @param size the page size
     * @return a page of NotificationResponseDto
     
 * @author Disha Gujar
 */
    Page<NotificationResponseDto> getMyNotifications(AuthenticatedUser user, int page, int size);

    /**
     * Gets the count of unread notifications for the authenticated user.
     * 
     * @param user the authenticated user
     * @return the unread notification count
     
 * @author Disha Gujar
 */
    long getUnreadCount(AuthenticatedUser user);

    /**
     * Marks a notification as read for the authenticated user.
     * 
     * @param user the authenticated user
     * @param notificationId the ID of the notification
     * @return the updated NotificationResponseDto
     
 * @author Disha Gujar
 */
    NotificationResponseDto markAsRead(AuthenticatedUser user, Long notificationId);

    /**
     * Deletes a notification for the authenticated user.
     * 
     * @param user the authenticated user
     * @param notificationId the ID of the notification to delete
     
 * @author Disha Gujar
 */
    void deleteNotification(AuthenticatedUser user, Long notificationId);
}