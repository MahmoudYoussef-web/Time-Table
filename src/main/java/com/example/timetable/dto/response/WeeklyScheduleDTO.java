package com.example.timetable.dto.response;

import java.util.Map;

public record WeeklyScheduleDTO(

        Map<String, Map<String, ScheduleEntryDTO>> table

) {
}
