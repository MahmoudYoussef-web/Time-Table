package com.example.timetable.mapper;

import com.example.timetable.dto.TimeSlotRequest;
import com.example.timetable.dto.TimeSlotResponse;
import com.example.timetable.model.TimeSlot;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class TimeSlotMapper {

    public static TimeSlot toEntity(TimeSlotRequest request) {
        TimeSlot timeSlot = new TimeSlot();

        timeSlot.setDayOfWeek(
                DayOfWeek.valueOf(request.dayOfWeek().toUpperCase())
        );

        timeSlot.setStartTime(LocalTime.parse(request.startTime()));
        timeSlot.setEndTime(LocalTime.parse(request.endTime()));

        return timeSlot;
    }

    public static TimeSlotResponse toResponse(TimeSlot timeSlot) {
        return new TimeSlotResponse(
                timeSlot.getId(),
                timeSlot.getDayOfWeek().name(),
                timeSlot.getStartTime().toString(),
                timeSlot.getEndTime().toString()
        );
    }
}
