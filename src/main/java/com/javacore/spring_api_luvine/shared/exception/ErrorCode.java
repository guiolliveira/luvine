package com.javacore.spring_api_luvine.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // USER
    INVALID_NAME(HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }
}