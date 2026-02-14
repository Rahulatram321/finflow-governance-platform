package com.company.finflow.common.web;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        Map<String, String> errors = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                this::fieldMessage,
                (first, second) -> first
            ));
        return build(HttpStatus.BAD_REQUEST, "Validation failed", errors, request);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingHeader(
        MissingRequestHeaderException exception,
        HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_REQUEST, "Missing required header: " + exception.getHeaderName(), null, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(
        MethodArgumentTypeMismatchException exception,
        HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_REQUEST, "Invalid value for parameter: " + exception.getName(), null, request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Object>> handleResponseStatusException(
        ResponseStatusException exception,
        HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String message = exception.getReason() == null ? "Request failed" : exception.getReason();
        return build(status, message, null, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(
        DataIntegrityViolationException exception,
        HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, "Data integrity violation", null, request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoResourceFound(
        NoResourceFoundException exception,
        HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, "Resource not found", null, request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalState(
        IllegalStateException exception,
        HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, exception.getMessage(), null, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
        AuthenticationException exception,
        HttpServletRequest request
    ) {
        return build(HttpStatus.UNAUTHORIZED, "Authentication failed", null, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
        AccessDeniedException exception,
        HttpServletRequest request
    ) {
        return build(HttpStatus.FORBIDDEN, "Access denied", null, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
        Exception exception,
        HttpServletRequest request
    ) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected system failure", null, request);
    }

    private String fieldMessage(FieldError error) {
        return error.getDefaultMessage() == null ? "is invalid" : error.getDefaultMessage();
    }

    private ResponseEntity<ApiResponse<Object>> build(
        HttpStatus status,
        String message,
        Map<String, String> errors,
        HttpServletRequest request
    ) {
        String finalMessage = message;
        if (request != null && request.getRequestURI() != null && !request.getRequestURI().isBlank()) {
            finalMessage = message + " (" + request.getRequestURI() + ")";
        }
        ApiResponse<Object> body = errors == null
            ? ApiResponse.failure(finalMessage)
            : ApiResponse.failure(finalMessage, errors);
        return ResponseEntity.status(status).body(body);
    }
}
