package com.example.timetable.repository;

import com.example.timetable.model.ScheduleEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, Long> {

    List<ScheduleEntry> findByScheduleId(Long scheduleId);

    boolean existsByRoomIdAndTimeSlotId(Long roomId, Long timeSlotId);

    boolean existsByClassSectionIdAndTimeSlotId(Long classSectionId, Long timeSlotId);
}
