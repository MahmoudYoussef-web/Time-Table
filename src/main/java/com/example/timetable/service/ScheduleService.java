package com.example.timetable.service;

import com.example.timetable.dto.response.ScheduleDTO;
import com.example.timetable.entity.ScheduleGenerationJob;
import com.example.timetable.scheduling.constraints.ConstraintViolation;

import java.util.List;
import java.util.UUID;

public interface ScheduleService {

    UUID generateScheduleAsync(Long semesterId);

    ScheduleGenerationJob getJob(UUID jobId);

    ScheduleDTO getScheduleById(Long id);

    ScheduleDTO validateSchedule(Long id);

    ScheduleDTO publishSchedule(Long id);

    ScheduleDTO lockSchedule(Long id);

    ScheduleDTO getByInstructor(Long instructorId);

    void lockEntry(Long scheduleId, Long entryId);

    List<ConstraintViolation> getConflicts(Long scheduleId);
}