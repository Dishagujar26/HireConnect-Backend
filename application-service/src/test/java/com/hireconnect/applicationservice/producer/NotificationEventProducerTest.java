package com.hireconnect.applicationservice.producer;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import com.hireconnect.applicationservice.event.NotificationEvent;
import com.hireconnect.applicationservice.enums.NotificationType;

class NotificationEventProducerTest {

    @Mock
    private KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @InjectMocks
    private NotificationEventProducer producer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendNotification_ShouldCallKafkaTemplate() {
        NotificationEvent event = NotificationEvent.builder()
                .recipientUserId(1L)
                .type(NotificationType.APPLICATION)
                .message("Test")
                .build();

        producer.sendNotification(event);

        verify(kafkaTemplate).send(eq("hireconnect-notifications"), eq(event));
    }

    @Test
    void sendNotification_WhenKafkaThrows_ShouldThrowException() {
        NotificationEvent event = NotificationEvent.builder().build();
        when(kafkaTemplate.send(anyString(), any())).thenThrow(new RuntimeException("Kafka down"));

        assertThrows(RuntimeException.class, () -> producer.sendNotification(event));
    }
}
