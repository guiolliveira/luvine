package com.javacore.spring_api_luvine.common.limiter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.data.redis")
public record ValkeyProperties(
        String host,
        int port,
        String password
) {
}
