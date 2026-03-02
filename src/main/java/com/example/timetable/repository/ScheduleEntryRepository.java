package com.example.timetable.repository;

import com.example.timetable.entity.ScheduleEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleEntryRepository
                extends JpaRepository<ScheduleEntry, Long> {

        List<ScheduleEntry> findBySectionId(Long sectionId);

        boolean existsByRoomIdAndTimeSlotId(Long roomId, Long timeSlotId);

        boolean existsBySectionIdAndTimeSlotId(Long sectionId, Long timeSlotId);

        List<ScheduleEntry> findByScheduleIdAndLockedTrue(Long scheduleId);

        // For instructor filtering
        List<ScheduleEntry> findByScheduleIdAndSectionInstructorId(
                        Long scheduleId,
                        Long instructorId);
}
