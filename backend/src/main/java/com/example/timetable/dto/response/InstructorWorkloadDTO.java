package com.example.timetable.dto.response;

public record InstructorWorkloadDTO(
    String instructorName,
    long sectionCount,
    double estimatedHours
) {}
