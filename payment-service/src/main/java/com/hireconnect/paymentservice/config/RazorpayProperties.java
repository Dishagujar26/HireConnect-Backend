package com.hireconnect.paymentservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;
/**
 * Domain entity or core component representing RazorpayProperties.
 *
 * @author Disha Gujar
 */

@Configuration
@ConfigurationProperties(prefix = "payment.razorpay")
@Getter
@Setter
public class RazorpayProperties {
    private String keyId;
    private String keySecret;
    private String webhookSecret;
}
