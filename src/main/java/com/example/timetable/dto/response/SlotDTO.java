package com.example.timetable.dto.response;

public record SlotDTO(
        String startTime,
        String endTime,
        ScheduleEntryDTO entry
) {}