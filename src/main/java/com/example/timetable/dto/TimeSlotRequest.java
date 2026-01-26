package com.example.timetable.dto;

import jakarta.validation.constraints.NotBlank;

public record TimeSlotRequest(

        @NotBlank(message = "Day of week is required")
        String dayOfWeek,

        @NotBlank(message = "Start time is required")
        String startTime,

        @NotBlank(message = "End time is required")
        String endTime
) {}
