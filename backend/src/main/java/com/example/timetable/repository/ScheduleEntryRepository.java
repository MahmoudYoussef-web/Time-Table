package com.example.timetable.repository;

import com.example.timetable.entity.ScheduleEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleEntryRepository
                extends JpaRepository<ScheduleEntry, Long> {

        List<ScheduleEntry> findBySectionId(Long sectionId);

        boolean existsByRoomIdAndTimeSlotId(Long roomId, Long timeSlotId);

        boolean existsBySectionIdAndTimeSlotId(Long sectionId, Long timeSlotId);

        List<ScheduleEntry> findByScheduleIdAndLockedTrue(Long scheduleId);

        List<ScheduleEntry> findByScheduleIdAndSectionInstructorId(
                        Long scheduleId,
                        Long instructorId);

        @Query("""
            SELECT e FROM ScheduleEntry e
            JOIN FETCH e.section s
            JOIN FETCH s.course c
            JOIN FETCH c.department d
            JOIN FETCH s.instructor i
            JOIN FETCH i.user u
            JOIN FETCH e.room r
            JOIN FETCH e.timeSlot t
            WHERE e.schedule.id = :scheduleId
            AND s.instructor.id = :instructorId
            ORDER BY t.day, t.startTime
            """)
        List<ScheduleEntry> findByScheduleIdAndInstructorIdWithDetails(
                @Param("scheduleId") Long scheduleId,
                @Param("instructorId") Long instructorId);
}