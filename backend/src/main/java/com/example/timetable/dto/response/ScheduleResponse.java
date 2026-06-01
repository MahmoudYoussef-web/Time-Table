package com.example.timetable.dto.response;

public record ScheduleResponse(

        Long id,

        // Section info
        String sectionName,
        String courseName,

        // Instructor name
        String instructorName,

        // Room info
        String building,
        String roomNumber,

        // Time info
        String day,
        String startTime,
        String endTime,

        // Class type
        String type
) {}
