package com.company.finflow.common.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ApiResponse<T> {
    boolean success;
    T data;
    String message;
    String timestamp;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Map<String, String> errors;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .message(null)
            .timestamp(Instant.now().toString())
            .errors(null)
            .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .data(data)
            .message(message)
            .timestamp(Instant.now().toString())
            .errors(null)
            .build();
    }

    public static <T> ApiResponse<T> failure(String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .data(null)
            .message(message)
            .timestamp(Instant.now().toString())
            .errors(null)
            .build();
    }

    public static <T> ApiResponse<T> failure(String message, Map<String, String> errors) {
        return ApiResponse.<T>builder()
            .success(false)
            .data(null)
            .message(message)
            .timestamp(Instant.now().toString())
            .errors(errors)
            .build();
    }
}
