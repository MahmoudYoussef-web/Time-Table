package com.example.timetable.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DepartmentRequest(

        @NotBlank(message = "Department code is required")
        String code,

        @NotBlank(message = "Department name is required")
        String name
) {}