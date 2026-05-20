package com.javacore.spring_api_luvine.common.util;

import com.javacore.spring_api_luvine.common.exception.exceptions.TokenHashException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class TokenHash {

    private TokenHash() {}

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String hash(String token) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = messageDigest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception ex) {
            throw new TokenHashException();
        }
    }

    public static String generateSecureToken() {
        byte[] bytes = new byte[64];
        RANDOM.nextBytes(bytes);
        return Base64.getEncoder().withoutPadding().encodeToString(bytes);
    }
}