package com.example.timetable.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public record EnrollmentRequest(

        // Student ID
        @NotNull(message = "Student id is required")
        Long studentId,

        // Section ID
        @NotNull(message = "Section id is required")
        Long sectionId,

        // Enrollment status (ACTIVE, DROPPED, COMPLETED)
        @NotBlank(message = "Status is required")
        String status
) {}
