package com.example.timetable.dto.request;

import java.time.LocalDate;

public record SemesterRequest(

        String name,

        LocalDate startDate,

        LocalDate endDate
) {}