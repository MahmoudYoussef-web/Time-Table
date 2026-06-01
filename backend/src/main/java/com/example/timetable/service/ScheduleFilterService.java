package com.example.timetable.service;

import com.example.timetable.dto.response.WeeklyScheduleDTO;

public interface ScheduleFilterService {

    WeeklyScheduleDTO getByInstructor(Long instructorId);

    WeeklyScheduleDTO getByDepartment(Long departmentId);

    WeeklyScheduleDTO getByCourse(Long courseId);
}

