package com.example.timetable.dto.response;

public record ScheduleEntryDTO(

        Long sectionId,

        String courseCode,

        String courseName,

        String instructorName,

        String roomNumber,

        String dayOfWeek,

        String startTime,

        String endTime,

        int hardViolations,

        int softViolations

) {
}
