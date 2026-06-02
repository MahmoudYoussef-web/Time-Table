package com.example.timetable.service;

import com.example.timetable.dto.response.TimeSlotResponse;

import java.util.List;

public interface InstructorAvailabilityService {
    List<TimeSlotResponse> getUnavailableSlots(Long instructorId);
    void addUnavailableSlot(Long instructorId, Long slotId);
    void removeUnavailableSlot(Long instructorId, Long slotId);
}
