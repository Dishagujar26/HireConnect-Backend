package com.hireconnect.notificationservice.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import com.hireconnect.notificationservice.dto.response.NotificationResponseDto;
import com.hireconnect.notificationservice.entity.Notification;
import com.hireconnect.notificationservice.enums.NotificationType;

class NotificationMapperTest {

    private final NotificationMapper mapper = new NotificationMapper();

    @Test
    void toResponseDto_Success() {
        Notification notification = new Notification(
                1L, 10L, "T", "M", NotificationType.SYSTEM, true, LocalDateTime.now());
        
        NotificationResponseDto result = mapper.toResponseDto(notification);
        
        assertEquals(1L, result.getId());
        assertEquals(10L, result.getUserId());
        assertEquals("T", result.getTitle());
        assertTrue(result.getIsRead());
    }
}
