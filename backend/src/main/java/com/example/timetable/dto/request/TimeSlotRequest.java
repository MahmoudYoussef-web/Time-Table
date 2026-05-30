package com.example.timetable.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TimeSlotRequest(

        @NotBlank(message = "Day is required")
        String day,

        @NotBlank(message = "Start time is required")
        @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "Start time must be in HH:mm format")
        String startTime,

        @NotBlank(message = "End time is required")
        @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "End time must be in HH:mm format")
        String endTime
) {}
