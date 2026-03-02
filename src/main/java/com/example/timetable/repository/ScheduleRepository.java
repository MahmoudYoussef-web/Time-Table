package com.example.timetable.repository;

import com.example.timetable.entity.Schedule;
import com.example.timetable.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    Optional<Schedule> findTopBySemesterOrderByCreatedAtDesc(Semester semester);

    Optional<Schedule> findTopByOrderByCreatedAtDesc();
}