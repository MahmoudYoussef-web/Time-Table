package com.example.timetable.service;

import com.example.timetable.entity.TimeSlot;

import java.util.List;

public interface TimeSlotService {

    List<TimeSlot> findAll();

    TimeSlot findById(Long id);

    TimeSlot save(TimeSlot timeSlot);

    void deleteById(Long id);
}
