package com.example.timetable.service;

import com.example.timetable.dto.response.ScheduleDTO;
import com.example.timetable.entity.Schedule;

public interface ScheduleService {

    Schedule generateSchedule(Long semesterId);

    ScheduleDTO getScheduleById(Long id);

    ScheduleDTO validateSchedule(Long id);

    ScheduleDTO getByInstructor(Long instructorId);

    ScheduleDTO publishSchedule(Long id);

    ScheduleDTO lockSchedule(Long id);

    void lockEntry(Long scheduleId, Long entryId);
}