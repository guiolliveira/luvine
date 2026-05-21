package com.javacore.spring_api_luvine.auth.application.dto;

public record LoginResponse(String accessToken, String refreshToken) {
}
