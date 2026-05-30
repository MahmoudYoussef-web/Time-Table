package com.example.timetable.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public record StudentRequest(

        // Linked user ID
        @NotNull(message = "User id is required")
        Long userId,

        // Academic year (e.g. 2025/2026)
        @NotBlank(message = "Academic year is required")
        String academicYear,

        // Study level (1, 2, 3, 4)
        @Min(value = 1, message = "Level must be at least 1")
        int level,

        // Department ID
        @NotNull(message = "Department id is required")
        Long departmentId
) {}
