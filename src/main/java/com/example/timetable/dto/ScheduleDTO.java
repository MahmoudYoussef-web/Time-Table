package com.example.timetable.dto;

import java.util.List;

public record ScheduleDTO(
        Long id,

        // 🧠 Explainable Fitness (Schedule-level)
        double fitnessScore,
        int hardViolations,
        int softViolations,

        List<ScheduleEntryDTO> entries
) {}
