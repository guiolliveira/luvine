package com.javacore.spring_api_luvine.shared.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class GenerateCode {
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generate() {
        int code = 10000 + RANDOM.nextInt(90000);
        return String.valueOf(code);
    }
}
