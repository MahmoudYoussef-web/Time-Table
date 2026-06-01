package com.example.timetable.dto.response;

import com.example.timetable.entity.enums.SemesterStatus;

import java.time.LocalDate;

public record SemesterResponse(

        Long id,

        String name,

        LocalDate startDate,

        LocalDate endDate,

        SemesterStatus status
) {}