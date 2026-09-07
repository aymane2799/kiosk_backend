package com.example.kiosk.common;

import java.time.Instant;
import java.util.List;

public record ApiError(
        String code,
        String message,
        int status,
        String path,
        Instant timestamp,
        List<FieldValidationError> errors
) {
    public record FieldValidationError(String field, String message) {}

    public static ApiError of(String code, String message, int Status ,String path) {
        return new ApiError(code, message, Status, path, Instant.now(), List.of());
    }
}
