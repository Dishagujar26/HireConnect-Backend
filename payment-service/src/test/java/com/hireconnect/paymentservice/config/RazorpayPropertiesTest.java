package com.hireconnect.paymentservice.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RazorpayPropertiesTest {

    @Test
    void testGettersAndSetters() {
        RazorpayProperties props = new RazorpayProperties();
        props.setKeyId("id");
        props.setKeySecret("secret");
        props.setWebhookSecret("webhook");

        assertEquals("id", props.getKeyId());
        assertEquals("secret", props.getKeySecret());
        assertEquals("webhook", props.getWebhookSecret());
    }
}
