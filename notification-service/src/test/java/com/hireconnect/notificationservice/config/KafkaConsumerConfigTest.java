package com.hireconnect.notificationservice.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import com.hireconnect.notificationservice.event.NotificationEvent;

class KafkaConsumerConfigTest {

    @Test
    void config_ShouldReturnBeans() {
        KafkaConsumerConfig config = new KafkaConsumerConfig();
        ReflectionTestUtils.setField(config, "bootstrapServers", "localhost:9092");

        ConsumerFactory<String, NotificationEvent> factory = config.consumerFactory();
        assertNotNull(factory);
        
        ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> containerFactory = config.kafkaListenerContainerFactory();
        assertNotNull(containerFactory);
    }
}
