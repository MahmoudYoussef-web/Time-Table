package com.example.timetable.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SectionRequest(

        // Section name (e.g. A, B, Lab1)
        @NotBlank(message = "Section name is required")
        String name,

        // Course ID
        @NotNull(message = "Course id is required")
        Long courseId,

        // Instructor ID
        @NotNull(message = "Instructor id is required")
        Long instructorId,

        // Maximum number of students
        @Min(value = 1, message = "Capacity must be at least 1")
        int capacity
) {}
