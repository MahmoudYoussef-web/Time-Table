package com.example.timetable.dto.response;

import java.util.List;

public record WeeklyScheduleDTO(
        List<DayScheduleDTO> days
) {}