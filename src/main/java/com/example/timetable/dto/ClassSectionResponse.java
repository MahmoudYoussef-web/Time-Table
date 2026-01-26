package com.example.timetable.dto;

public record ClassSectionResponse(
        Long id,
        String courseCode,
        String courseName,
        String instructorName
) {}
