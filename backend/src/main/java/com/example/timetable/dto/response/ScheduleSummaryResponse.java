package com.example.timetable.dto.response;

public record ScheduleSummaryResponse(
    Long id,
    String semesterName,
    String status,
    double fitnessScore,
    int hardViolations,
    int softViolations,
    String createdAt
) {}
