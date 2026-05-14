package com.javacore.spring_api_luvine.notification.dto;

import java.util.Map;

public record MailSenderRequest(
        String to,
        String name,
        String subject,
        String templateName,
        Map<String, Object> variables
) {
}
