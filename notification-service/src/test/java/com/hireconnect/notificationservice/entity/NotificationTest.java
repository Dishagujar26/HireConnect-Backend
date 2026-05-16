package com.hireconnect.notificationservice.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import com.hireconnect.notificationservice.enums.NotificationType;

class NotificationTest {

    @Test
    void onCreate_ShouldSetTimestampAndDefaultIsRead() {
        Notification notification = new Notification();
        notification.onCreate();
        
        assertNotNull(notification.getCreatedAt());
        assertFalse(notification.getIsRead());
    }

    @Test
    void builder_ShouldSetFields() {
        Notification notification = Notification.builder()
                .userId(1L)
                .title("T")
                .type(NotificationType.SYSTEM)
                .build();
        
        assertEquals(1L, notification.getUserId());
        assertEquals("T", notification.getTitle());
        assertEquals(NotificationType.SYSTEM, notification.getType());
    }
}
