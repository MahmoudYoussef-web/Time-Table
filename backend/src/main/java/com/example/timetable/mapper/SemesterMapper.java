package com.example.timetable.mapper;

import com.example.timetable.dto.request.SemesterRequest;
import com.example.timetable.dto.response.SemesterResponse;
import com.example.timetable.entity.Semester;

public class SemesterMapper {

    public static Semester toEntity(SemesterRequest request) {

        Semester semester = new Semester();

        semester.setName(request.name());
        semester.setStartDate(request.startDate());
        semester.setEndDate(request.endDate());

        return semester;
    }

    public static SemesterResponse toResponse(Semester semester) {

        return new SemesterResponse(
                semester.getId(),
                semester.getName(),
                semester.getStartDate(),
                semester.getEndDate(),
                semester.getStatus()
        );
    }
}