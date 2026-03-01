package com.example.timetable.dto.request;

public record TimeSlotRequest(
        String day,
        String startTime,
        String endTime
) {}
