package com.hireconnect.notificationservice.consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hireconnect.notificationservice.dto.request.NotificationCreateRequestDto;
import com.hireconnect.notificationservice.event.NotificationEvent;
import com.hireconnect.notificationservice.service.NotificationService;
import com.hireconnect.notificationservice.enums.NotificationType;

@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventConsumer notificationEventConsumer;

    @Test
    void consumeNotificationEvent_Success() {
        NotificationEvent event = new NotificationEvent();
        event.setRecipientUserId(1L);
        event.setRecipientEmail("test@test.com");
        event.setTitle("Test");
        event.setMessage("Body");
        event.setType(NotificationType.APPLICATION);
        event.setSendEmail(true);

        notificationEventConsumer.consumeNotificationEvent(event);

        verify(notificationService, times(1)).createNotification(any(NotificationCreateRequestDto.class));
    }
}
