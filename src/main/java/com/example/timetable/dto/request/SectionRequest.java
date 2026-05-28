package com.example.timetable.dto.request;

import com.example.timetable.entity.enums.YearLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SectionRequest(

        @NotBlank(message = "Section name is required")
        String name,

        @NotNull(message = "Course id is required")
        Long courseId,

        @NotNull(message = "Instructor id is required")
        Long instructorId,

        @NotNull(message = "Semester id is required")
        Long semesterId,

        @Min(value = 1, message = "Capacity must be at least 1")
        int capacity,

        YearLevel yearLevel
) {}