package com.javacore.spring_api_luvine.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        HttpStatus status = ex.getErrorCode().getStatus();

        return buildError(status, ex.getMessage(), ex.getErrorCode().name(), null, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return buildError(HttpStatus.BAD_REQUEST, "Argumento Inválido", "ARGUMENT_NOT_VALID", details, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMisMatchException(HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "Parâmetro Inválido", "ARGUMENT_TYPE_MISMATCH", null, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegrityViolationException(HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "Erro de Violação", "INTEGRITY_VIOLATION", null, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Erro Inesperado", "INTERNAL_SERVER_ERROR", null, request);
    }

    private ResponseEntity<ApiError> buildError(
            HttpStatus status, String message,
            String errorCode, List<String> details,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                status.value(),
                status.getReasonPhrase(),
                message,
                errorCode,
                details,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(apiError);
    }
}