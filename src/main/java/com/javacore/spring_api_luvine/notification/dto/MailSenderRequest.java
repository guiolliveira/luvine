package com.javacore.spring_api_luvine.notification.dto;

public record MailSenderRequest(
        String to,
        String name,
        String code
) {
}
