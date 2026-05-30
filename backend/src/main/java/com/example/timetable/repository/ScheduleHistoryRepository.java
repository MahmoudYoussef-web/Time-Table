package com.example.timetable.repository;



import com.example.timetable.entity.ScheduleHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleHistoryRepository
        extends JpaRepository<ScheduleHistory, Long> {

    // Get history for schedule
    List<ScheduleHistory> findByScheduleIdOrderByCreatedAtDesc(
            Long scheduleId
    );

    // Get history by user
    List<ScheduleHistory> findByCreatedByIdOrderByCreatedAtDesc(
            Long userId
    );
}

