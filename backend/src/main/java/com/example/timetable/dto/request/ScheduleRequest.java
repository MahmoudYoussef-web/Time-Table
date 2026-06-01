package com.example.timetable.dto.request;

import jakarta.validation.constraints.NotNull;

public record ScheduleRequest(

        // Section ID
        @NotNull(message = "Section id is required")
        Long sectionId,

        // Instructor ID
        @NotNull(message = "Instructor id is required")
        Long instructorId,

        // Room ID
        @NotNull(message = "Room id is required")
        Long roomId,

        // TimeSlot ID
        @NotNull(message = "Time slot id is required")
        Long timeSlotId,

        // Class type (LECTURE, LAB, TUTORIAL)
        String type
) {}
