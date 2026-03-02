package com.example.timetable.repository;

import com.example.timetable.entity.ScheduleGenerationJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ScheduleGenerationJobRepository
        extends JpaRepository<ScheduleGenerationJob, UUID> {
}