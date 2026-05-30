package com.example.timetable.dto.response;

public record StudentResponse(

        Long id,

        // Student full name
        String fullName,

        // Student email
        String email,

        // Academic year
        String academicYear,

        // Study level
        int level,

        // Department name
        String departmentName
) {}
