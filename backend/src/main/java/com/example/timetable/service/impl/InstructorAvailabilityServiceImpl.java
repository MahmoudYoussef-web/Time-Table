package com.example.timetable.service.impl;

import com.example.timetable.dto.response.TimeSlotResponse;
import com.example.timetable.entity.Instructor;
import com.example.timetable.entity.InstructorAvailability;
import com.example.timetable.entity.TimeSlot;
import com.example.timetable.repository.InstructorRepository;
import com.example.timetable.repository.TimeSlotRepository;
import com.example.timetable.service.InstructorAvailabilityService;
import com.example.timetable.service.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class InstructorAvailabilityServiceImpl implements InstructorAvailabilityService {

    private final InstructorRepository instructorRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final SecurityService securityService;

    @Override
    public List<TimeSlotResponse> getUnavailableSlots(Long instructorId) {
        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new NoSuchElementException("Instructor not found with id: " + instructorId));
        return instructor.getUnavailableSlots().stream()
                .map(ia -> new TimeSlotResponse(
                        ia.getTimeSlot().getId(),
                        ia.getTimeSlot().getDay().name(),
                        ia.getTimeSlot().getStartTime().toString(),
                        ia.getTimeSlot().getEndTime().toString()))
                .toList();
    }

    @Override
    @Transactional
    public void addUnavailableSlot(Long instructorId, Long slotId) {
        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new NoSuchElementException("Instructor not found with id: " + instructorId));
        checkAccess(instructor);

        TimeSlot timeSlot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new NoSuchElementException("TimeSlot not found with id: " + slotId));

        boolean alreadyExists = instructor.getUnavailableSlots().stream()
                .anyMatch(ia -> ia.getTimeSlot().getId().equals(slotId));

        if (alreadyExists) {
            throw new IllegalStateException("Slot " + slotId + " is already marked as unavailable");
        }

        InstructorAvailability availability = new InstructorAvailability();
        availability.setInstructor(instructor);
        availability.setTimeSlot(timeSlot);
        instructor.getUnavailableSlots().add(availability);
        instructorRepository.save(instructor);
    }

    @Override
    @Transactional
    public void removeUnavailableSlot(Long instructorId, Long slotId) {
        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new NoSuchElementException("Instructor not found with id: " + instructorId));
        checkAccess(instructor);

        InstructorAvailability toRemove = instructor.getUnavailableSlots().stream()
                .filter(ia -> ia.getTimeSlot().getId().equals(slotId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Slot " + slotId + " is not in unavailable list"));

        instructor.getUnavailableSlots().remove(toRemove);
        instructorRepository.save(instructor);
    }

    private void checkAccess(Instructor instructor) {
        securityService.checkInstructorAccess(instructor.getUser());
    }
}
