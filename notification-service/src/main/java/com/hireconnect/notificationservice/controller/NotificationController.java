package com.hireconnect.notificationservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.hireconnect.notificationservice.dto.response.NotificationResponseDto;
import com.hireconnect.notificationservice.security.AuthenticatedUser;
import com.hireconnect.notificationservice.service.NotificationService;

import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing in-app notifications.
 * Provides endpoints for fetching, marking as read, and deleting notifications.
 * @author Disha Gujar
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    /**
     * Retrieves a paginated list of notifications for the authenticated user.
     * 
     * @param user the authenticated user
     * @param page the page number (default 0)
     * @param size the page size (default 10)
     * @return a page of NotificationResponseDto
     
 * @author Disha Gujar
 */
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDto>> getMyNotifications(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        logger.info("Fetching notifications for userId={}, page={}, size={}", user.getUserId(), page, size);
        return ResponseEntity.ok(notificationService.getMyNotifications(user, page, size));
    }

    /**
     * Returns the count of unread notifications for the currently logged-in user.
     * 
     * @param user the authenticated user
     * @return the count of unread notifications
     
 * @author Disha Gujar
 */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        logger.info("Fetching unread notification count for userId={}", user.getUserId());
        return ResponseEntity.ok(notificationService.getUnreadCount(user));
    }

    /**
     * Marks a specific notification as read.
     * 
     * @param user the authenticated user
     * @param notificationId the ID of the notification to mark as read
     * @return the updated NotificationResponseDto
     
 * @author Disha Gujar
 */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponseDto> markAsRead(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long notificationId
    ) {
        logger.info("Mark as read requested for notificationId={} by userId={}", notificationId, user.getUserId());
        return ResponseEntity.ok(notificationService.markAsRead(user, notificationId));
    }

    /**
     * Deletes a specific notification from the user's history.
     * 
     * @param user the authenticated user
     * @param notificationId the ID of the notification to delete
     * @return a success message
     
 * @author Disha Gujar
 */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<String> deleteNotification(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long notificationId
    ) {
        logger.info("Delete notification requested for notificationId={} by userId={}", notificationId, user.getUserId());
        notificationService.deleteNotification(user, notificationId);
        logger.info("Notification deleted successfully. notificationId={}, userId={}", notificationId, user.getUserId());
        return ResponseEntity.ok("Notification deleted successfully");
    }
}
