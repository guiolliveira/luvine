package com.javacore.spring_api_luvine.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.mail")
public record MailSenderProperties(String fromEmail) {
}
