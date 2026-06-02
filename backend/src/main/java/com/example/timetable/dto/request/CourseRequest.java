package com.example.timetable.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CourseRequest(

        // Course unique code (e.g. CS101)
        @NotBlank(message = "Course code is required")
        String code,

        // Course name (e.g. Data Structures)
        @NotBlank(message = "Course name is required")
        String name,

        // Number of credit hours
        @Min(value = 1, message = "Credit hours must be at least 1")
        @Max(value = 6, message = "Credit hours must be at most 6")
        int creditHours,

        // Department ID
        @NotNull(message = "Department id is required")
        Long departmentId
) {}
