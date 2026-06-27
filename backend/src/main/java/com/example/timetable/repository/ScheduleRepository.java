package com.example.timetable.repository;

import com.example.timetable.entity.Schedule;
import com.example.timetable.entity.Semester;
import com.example.timetable.entity.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository
        extends JpaRepository<Schedule, Long> {

    @Query("SELECT s FROM Schedule s JOIN FETCH s.semester ORDER BY s.createdAt DESC")
    List<Schedule> findAllWithSemester();

    Optional<Schedule> findTopBySemesterOrderByCreatedAtDesc(
            Semester semester);

    Optional<Schedule> findTopByOrderByCreatedAtDesc();

    Optional<Schedule> findTopByStatusOrderByCreatedAtDesc(
            ScheduleStatus status);

    List<Schedule> findBySemesterId(Long semesterId);

    boolean existsBySemesterId(Long semesterId);

    boolean existsBySemesterIdAndStatus(
            Long semesterId,
            ScheduleStatus status);

    List<Schedule> findBySemesterIdAndStatus(
            Long semesterId,
            ScheduleStatus status);

    @Query("SELECT COUNT(se) FROM ScheduleEntry se WHERE se.schedule.semester.id = :semesterId")
    long countEntriesBySemesterId(@Param("semesterId") Long semesterId);

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

    @Query("SELECT AVG(s.fitnessScore) FROM Schedule s")
    Double findAverageFitnessScore();

    @Query("SELECT COALESCE(SUM(s.hardViolations), 0) FROM Schedule s")
    Long sumHardViolations();

    @Modifying
    @Query(value = "DELETE FROM schedule_entries WHERE schedule_id IN (SELECT id FROM schedules WHERE semester_id = :semesterId)", nativeQuery = true)
    void deleteEntriesBySemesterId(@Param("semesterId") Long semesterId);

    @Modifying
    @Query(value = "DELETE FROM schedules WHERE semester_id = :semesterId", nativeQuery = true)
    void deleteBySemesterId(@Param("semesterId") Long semesterId);
}