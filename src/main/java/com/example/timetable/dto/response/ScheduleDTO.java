package com.example.timetable.dto.response;

import java.util.List;

public record ScheduleDTO(

        Long id,

        double fitnessScore,

        int hardViolations,

        int softViolations,

        List<ScheduleEntryDTO> entries

) {
}
