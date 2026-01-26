package com.example.timetable.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload for creating a class section")
public record ClassSectionRequest(

        @NotNull(message = "Course id is required")
        @Schema(
                description = "ID of the course",
                example = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Long courseId,

        @NotNull(message = "Instructor id is required")
        @Schema(
                description = "ID of the instructor",
                example = "2",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Long instructorId
) {}
