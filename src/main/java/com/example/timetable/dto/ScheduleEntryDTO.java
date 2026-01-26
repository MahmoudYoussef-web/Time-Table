package com.example.timetable.dto;

public record ScheduleEntryDTO(
        Long sectionId,

        String courseCode,
        String courseName,

        String instructorName,
        String roomNumber,

        String dayOfWeek,
        String startTime,
        String endTime,

        // 🧠 Explainable Fitness (Entry-level)
        int hardViolations,
        int softViolations
) {}
