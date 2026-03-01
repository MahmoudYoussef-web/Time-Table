package com.example.timetable.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ApiError {

    private final int status;

    private final String message;

    private final LocalDateTime timestamp;
}
