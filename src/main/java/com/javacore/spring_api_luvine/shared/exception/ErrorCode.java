package com.javacore.spring_api_luvine.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // AUTH
    EMAIL_AREADY_EXISTS(HttpStatus.CONFLICT),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),

    // USER
    INVALID_NAME(HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST),

    // SHARED
    TOKEN_HASH(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }
}