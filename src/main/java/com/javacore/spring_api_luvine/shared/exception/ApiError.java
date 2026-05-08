package com.javacore.spring_api_luvine.shared.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public class ApiError {
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "America/Sao_Paulo")
    private final Instant timestamp;

    private final Integer status;
    private final String error;
    private final String message;
    private final String errorCode;
    private final List<String> details;
    private final String path;

    public ApiError(Integer status, String error, String message, String errorCode, List<String> details, String path) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.errorCode = errorCode;
        this.details = details == null ? List.of() : List.copyOf(details);
        this.path = path;
    }
}