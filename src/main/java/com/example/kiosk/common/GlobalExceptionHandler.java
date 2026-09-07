package com.example.kiosk.common;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

@RestControllerAdvice
public class GlobalExceptionHandler {


    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Pattern ERROR_CODE = Pattern.compile("^[A-Z][A-Z0-9_]{2,}$");

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatusCode status = ex.getStatusCode();
        String reason = ex.getReason();
        boolean reasonIsCode = reason != null && ERROR_CODE.matcher(reason).matches();

        String code = reasonIsCode ? reason : statusName(status);
        String message = reason != null ? reason : statusName(status).replace('_', ' ');

        return ResponseEntity.status(status)
                .body(ApiError.of(code, message, status.value(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiError.FieldValidationError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiError.FieldValidationError(fe.getField(), fe.getDefaultMessage()))
                .toList();

        return ResponseEntity.badRequest().body(new ApiError(
                "VALIDATION_ERROR", "Request validation failed", HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(), Instant.now(), fieldErrors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(ApiError.of(
                "MALFORMED_REQUEST", "Request body is missing or malformed",
                HttpStatus.BAD_REQUEST.value(), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        logger.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError.of(
                "INTERNAL_ERROR", "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR.value(), request.getRequestURI()));
    }


    private static String statusName(HttpStatusCode status) {
        if (status instanceof HttpStatus resolved) {
            return resolved.name();
        }
        return "ERROR";
    }
}
