package com.javacore.spring_api_luvine.auth.dto;

public record LoginResponse(String accessToken, String refreshToken) {
}
