package com.example.timetable.repository;

import com.example.timetable.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScheduleRepository
        extends JpaRepository<Schedule, Long> {

    // Get latest schedule
    Optional<Schedule> findTopByOrderByCreatedAtDesc();
}
