package com.example.timetable.mapper;

import com.example.timetable.dto.request.TimeSlotRequest;
import com.example.timetable.dto.response.TimeSlotResponse;
import com.example.timetable.entity.TimeSlot;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class TimeSlotMapper {

    // Convert request to entity
    public static TimeSlot toEntity(TimeSlotRequest request) {

        TimeSlot timeSlot = new TimeSlot();

        // Set day of week
        timeSlot.setDay(
                DayOfWeek.valueOf(request.day().toUpperCase())
        );

        // Set start time
        timeSlot.setStartTime(
                LocalTime.parse(request.startTime())
        );

        // Set end time
        timeSlot.setEndTime(
                LocalTime.parse(request.endTime())
        );

        return timeSlot;
    }

    // Convert entity to response
    public static TimeSlotResponse toResponse(TimeSlot timeSlot) {

        return new TimeSlotResponse(
                timeSlot.getId(),
                timeSlot.getDay().name(),
                timeSlot.getStartTime().toString(),
                timeSlot.getEndTime().toString()
        );
    }
}
