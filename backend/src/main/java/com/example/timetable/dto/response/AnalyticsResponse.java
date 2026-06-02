package com.example.timetable.dto.response;

import java.util.List;

public record AnalyticsResponse(
    long totalSchedules,
    long totalInstructors,
    long totalRooms,
    long totalCourses,
    double averageFitnessScore,
    long totalHardViolations,
    List<RoomUtilizationDTO> roomUtilization,
    List<InstructorWorkloadDTO> instructorWorkload
) {}
