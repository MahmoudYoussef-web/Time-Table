package com.example.timetable.dto.response;

public record CourseResponse(

        Long id,

        // Course unique code
        String code,

        // Course name
        String name,

        // Number of credit hours
        int creditHours,

        // Department name
        String departmentName
) {}
