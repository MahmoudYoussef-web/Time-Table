package com.example.timetable.dto;

import java.util.Map;

public record WeeklyScheduleDTO(
        Map<String, Map<String, ScheduleEntryDTO>> table
) {}
