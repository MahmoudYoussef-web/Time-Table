package com.example.timetable.dto.response;

public record TimeSlotResponse(

        Long id,

        // Day of the week
        String day,

        // Start time (HH:mm)
        String startTime,

        // End time (HH:mm)
        String endTime
) {}
