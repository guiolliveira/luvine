package com.javacore.spring_api_luvine.common.messaging.dto;

import java.util.Map;

public record EmailMessageRequest(
        String to,
        String name,
        String subject,
        String templateName,
        Map<String, Object> variables
) {
}
