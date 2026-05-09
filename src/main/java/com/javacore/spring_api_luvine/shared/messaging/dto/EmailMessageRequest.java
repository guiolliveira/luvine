package com.javacore.spring_api_luvine.shared.messaging.dto;

public record EmailMessageRequest(
        String to,
        String subject,
        String body
) {
}
