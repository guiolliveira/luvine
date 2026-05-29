package com.javacore.spring_api_luvine.common.exception;

import com.javacore.spring_api_luvine.common.exception.exceptions.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        HttpStatus status = ex.getErrorCode().getStatus();

        log.warn("event=business_exception errorCode={} status={} path={}",
                ex.getErrorCode().name(), status.value(), request.getRequestURI());

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

        log.warn("event=validation_failed path={} fields={}", request.getRequestURI(), details);

        return buildError(HttpStatus.BAD_REQUEST, "Argumento Inválido", "ARGUMENT_NOT_VALID", details, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMisMatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        log.warn("event=argument_type_mismatch path={} param={}", request.getRequestURI(), ex.getName());

        return buildError(HttpStatus.BAD_REQUEST, "Parâmetro Inválido", "ARGUMENT_TYPE_MISMATCH", null, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        log.warn("event=data_integrity_violation path={}", request.getRequestURI(), ex);

        return buildError(HttpStatus.BAD_REQUEST, "Erro de Violação", "INTEGRITY_VIOLATION", null, request);
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ApiError> handleRequestCookieException(
            MissingRequestCookieException ex, HttpServletRequest request) {

        log.warn("event=missing_cookie cookieName={} path={}", ex.getCookieName(), request.getRequestURI());

        return buildError(HttpStatus.BAD_REQUEST, "O cookie é obrigatório", "MISSING_REQUEST_COOKIE", null, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {

        log.warn("event=authentication_failed message={} path={}", ex.getMessage(), request.getRequestURI());

        return buildError(
                HttpStatus.UNAUTHORIZED,
                "Credenciais inválidas",
                "AUTHENTICATION_UNAUTHORIZED",
                null,
                request
        );
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiError> handleAuthorizationDeniedException(
            AuthorizationDeniedException ex, HttpServletRequest request) {

        log.warn("event=authorization_failed message={} path={}", ex.getMessage(), request.getRequestURI());

        return buildError(HttpStatus.FORBIDDEN, "Acesso Negado", "AUTHORIZATION_DENIED", null, request);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticFailureException(
            ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {

        log.warn(
                "event=optimistic_lock_failure entity={} path={}",
                ex.getPersistentClassName(),
                request.getRequestURI()
        );

        return buildError(
                HttpStatus.CONFLICT,
                "O recurso foi alterado por outra operação. Tente novamente",
                "OPTIMISTIC_LOCK_FAILURE",
                null,
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("event=unhandled_exception path={} exceptionType={}",
                request.getRequestURI(), ex.getClass().getSimpleName(), ex);

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