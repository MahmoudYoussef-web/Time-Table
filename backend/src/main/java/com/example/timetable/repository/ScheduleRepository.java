package com.example.timetable.repository;

import com.example.timetable.entity.Schedule;
import com.example.timetable.entity.Semester;
import com.example.timetable.entity.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository
        extends JpaRepository<Schedule, Long> {

    Optional<Schedule> findTopBySemesterOrderByCreatedAtDesc(
            Semester semester);

    Optional<Schedule> findTopByOrderByCreatedAtDesc();

    Optional<Schedule> findTopByStatusOrderByCreatedAtDesc(
            ScheduleStatus status);

    boolean existsBySemesterIdAndStatus(
            Long semesterId,
            ScheduleStatus status);

    List<Schedule> findBySemesterIdAndStatus(
            Long semesterId,
            ScheduleStatus status);

    @Query("""
        SELECT s FROM Schedule s
        LEFT JOIN FETCH s.entries e
        LEFT JOIN FETCH e.section sec
        LEFT JOIN FETCH sec.course c
        LEFT JOIN FETCH c.department
        LEFT JOIN FETCH sec.instructor i
        LEFT JOIN FETCH i.user
        LEFT JOIN FETCH e.room
        LEFT JOIN FETCH e.timeSlot
        WHERE s.id = :id
        """)
    Optional<Schedule> findByIdWithDetails(@Param("id") Long id);
}