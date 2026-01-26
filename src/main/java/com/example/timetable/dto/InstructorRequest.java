package com.example.timetable.dto;

import jakarta.validation.constraints.NotBlank;

public record InstructorRequest(
        @NotBlank(message = "Instructor name is required")
        String name
) {}
