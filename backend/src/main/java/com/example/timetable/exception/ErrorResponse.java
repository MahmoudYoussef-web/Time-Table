package com.example.timetable.exception;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
    boolean success,
    String message,
    List<String> errors,
    String timestamp,
    String path
) {
    public static ErrorResponse of(String message, HttpServletRequest request) {
        return new ErrorResponse(false, message, List.of(),
            LocalDateTime.now().toString(), request.getRequestURI());
    }

    public static ErrorResponse ofErrors(String message, List<String> errors,
            HttpServletRequest request) {
        return new ErrorResponse(false, message, errors,
            LocalDateTime.now().toString(), request.getRequestURI());
    }
}
