package com.hireconnect.interviewservice.producer;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.hireconnect.interviewservice.event.NotificationEvent;
import com.hireconnect.interviewservice.enums.NotificationType;

@ExtendWith(MockitoExtension.class)
class NotificationEventProducerTest {

    @Mock
    private KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @InjectMocks
    private NotificationEventProducer producer;

    @Test
    void sendNotification_Success() {
        NotificationEvent event = NotificationEvent.builder()
                .recipientUserId(1L)
                .type(NotificationType.INTERVIEW)
                .build();

        producer.sendNotification(event);

        verify(kafkaTemplate, times(1)).send(eq("hireconnect-notifications"), eq(event));
    }
}
