package com.example.timetable.dto.response;

public record SectionResponse(

        Long id,

        // Section name
        String name,

        // Course info
        String courseCode,
        String courseName,

        // Instructor name
        String instructorName,

        // Maximum capacity
        int capacity
) {}
