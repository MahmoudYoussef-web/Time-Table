package com.example.timetable.dto;

public record TimeSlotResponse(
        Long id,
        String dayOfWeek,
        String startTime,
        String endTime
) {}
