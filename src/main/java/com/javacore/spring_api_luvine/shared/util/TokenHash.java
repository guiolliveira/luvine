package com.javacore.spring_api_luvine.shared.util;

import com.javacore.spring_api_luvine.shared.exception.TokenHashException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public final class TokenHash {

    private TokenHash() {}

    public static String hash(String token) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = messageDigest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception ex) {
            throw new TokenHashException();
        }
    }
}