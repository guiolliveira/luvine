package com.javacore.spring_api_luvine.auth.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@ConfigurationProperties(prefix = "spring.jwt")
public record TokenProperties(
        RSAPrivateKey privateKey,
        RSAPublicKey publicKey
) {
}
