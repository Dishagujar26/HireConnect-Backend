package com.hireconnect.notificationservice.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.hireconnect.notificationservice.dto.request.NotificationCreateRequestDto;
import com.hireconnect.notificationservice.event.NotificationEvent;
import com.hireconnect.notificationservice.service.NotificationService;

import lombok.RequiredArgsConstructor;

/**
 * Kafka Consumer for notification events.
 * Listens to the notification topic and triggers notification creation and email dispatch.
 * @author Disha Gujar
 */
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationService notificationService;

    /**
     * Consumes a NotificationEvent from Kafka.
     * Maps the event to a request DTO and calls the NotificationService to process it.
     * 
     * @param event the notification event received from Kafka
     
 * @author Disha Gujar
 */
    @KafkaListener(
            topics = "${app.kafka.notification-topic}",
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    /**
     * Consume notification event.
     *
     * @author Disha Gujar
     */
    public void consumeNotificationEvent(NotificationEvent event) {
        logger.info(
                "Received notification event for recipientUserId={}, type={}, sendEmail={}",
                event.getRecipientUserId(),
                event.getType(),
                event.getSendEmail()
        );

        NotificationCreateRequestDto requestDto = new NotificationCreateRequestDto();
        requestDto.setRecipientUserId(event.getRecipientUserId());
        requestDto.setRecipientEmail(event.getRecipientEmail());
        requestDto.setTitle(event.getTitle());
        requestDto.setMessage(event.getMessage());
        requestDto.setType(event.getType());
        requestDto.setSendEmail(event.getSendEmail());

        notificationService.createNotification(requestDto);

        logger.info("Notification event processed successfully for recipientUserId={}", event.getRecipientUserId());
    }
}
