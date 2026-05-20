package com.javacore.spring_api_luvine.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // AUTH
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED),
    INVALID_CODE(HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_VERIFIED(HttpStatus.CONFLICT),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS),
    EMAIL_NOT_VERIFIED(HttpStatus.UNAUTHORIZED),
    PROVIDER_CONFLICT(HttpStatus.UNAUTHORIZED),
    INVALID_AUTHENTICATION_TOKEN(HttpStatus.UNAUTHORIZED),

    // USER
    INVALID_NAME(HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST),
    INVALID_CEP(HttpStatus.BAD_REQUEST),
    INVALID_PHONE(HttpStatus.BAD_REQUEST),
    USER_SESSION_INVALID(HttpStatus.UNAUTHORIZED),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND),
    ADDRESS_INACTIVE(HttpStatus.BAD_REQUEST),
    ADDRESS_ALREADY_EXISTS(HttpStatus.CONFLICT),
    UNCHANGED_VALUE(HttpStatus.BAD_REQUEST),
    ROLE_ALREADY_ASSIGNED(HttpStatus.BAD_REQUEST),
    SELF_PROMOTION_NOT_ALLOWED(HttpStatus.BAD_REQUEST),
    INSUFFICIENT_PERMISSIONS(HttpStatus.FORBIDDEN),
    NO_PROFILE_CHANGES_PROVIDED(HttpStatus.BAD_REQUEST),

    // SHARED
    TOKEN_HASH(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }
}