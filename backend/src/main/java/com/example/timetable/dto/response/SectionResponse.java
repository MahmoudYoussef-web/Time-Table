package com.example.timetable.dto.response;

import com.example.timetable.entity.enums.SessionType;
import com.example.timetable.entity.enums.YearLevel;

public record SectionResponse(

        Long id,
        String name,
        String courseCode,
        String courseName,
        String instructorName,
        int capacity,
        YearLevel yearLevel,
        SessionType sessionType
) {}
