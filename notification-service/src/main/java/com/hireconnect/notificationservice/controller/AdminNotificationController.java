package com.hireconnect.notificationservice.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.hireconnect.notificationservice.security.AuthenticatedUser;
import com.hireconnect.notificationservice.enums.Role;

import com.hireconnect.notificationservice.entity.Notification;
import com.hireconnect.notificationservice.enums.NotificationType;
import com.hireconnect.notificationservice.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

/**
 * Admin-only controller for platform-wide broadcast messaging.
 * The admin dashboard fetches all userIds from auth-service and passes them
 * here, keeping microservices data isolation intact.
 *
 * @author Disha Gujar
 */
@RestController
@RequestMapping("/api/notifications/admin")
@RequiredArgsConstructor
public class AdminNotificationController {

    private static final Logger log = LoggerFactory.getLogger(AdminNotificationController.class);

    private final NotificationRepository notificationRepository;

    /**
     * Broadcasts a message to the list of userIds provided.
     * The admin dashboard is responsible for fetching all userIds from auth-service
     * and passing them in the request body.
     *
     * Request body: { "title": "...", "message": "...", "userIds": [1, 2, 3, ...] }
     */
    @PostMapping("/broadcast")
    public ResponseEntity<?> broadcastMessage(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody BroadcastRequest request
    ) {
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).body("Access denied: Admin role required.");
        }

        if (request.title() == null || request.title().isBlank()
                || request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest().body("Title and message are required.");
        }

        if (request.userIds() == null || request.userIds().isEmpty()) {
            return ResponseEntity.badRequest().body("At least one userId is required.");
        }

        List<Notification> notifications = request.userIds().stream()
                .map(userId -> Notification.builder()
                        .userId(userId)
                        .title(request.title())
                        .message(request.message())
                        .type(NotificationType.SYSTEM)
                        .isRead(false)
                        .createdAt(LocalDateTime.now())
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
        log.info("Admin broadcast sent to {} users. Targeted User IDs: {}. Title: '{}'", 
                notifications.size(), request.userIds(), request.title());
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("recipientCount", notifications.size());
        response.put("title", request.title());
        
        log.info("Returning successful response to Gateway for broadcast: {}", request.title());
        return ResponseEntity.ok(response);
    }

    public record BroadcastRequest(String title, String message, List<Long> userIds) {}
    public record BroadcastResultDto(int recipientCount, String title) {}
}
