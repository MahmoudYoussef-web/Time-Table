package com.example.timetable.repository;

import com.example.timetable.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    Optional<TimeSlot> findByDayAndStartTimeAndEndTime(DayOfWeek day, LocalTime startTime, LocalTime endTime);

    List<TimeSlot> findAllByOrderByStartTimeAsc();
}
