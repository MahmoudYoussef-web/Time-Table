package com.example.timetable.service.impl;

import com.example.timetable.entity.TimeSlot;
import com.example.timetable.repository.TimeSlotRepository;
import com.example.timetable.service.TimeSlotService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TimeSlotServiceImpl implements TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;

    @Override
    public List<TimeSlot> findAll() {
        return timeSlotRepository.findAll().stream()
                .sorted(Comparator.comparingInt((TimeSlot slot) -> switch (slot.getDay()) {
                    case SUNDAY -> 1;
                    case MONDAY -> 2;
                    case TUESDAY -> 3;
                    case WEDNESDAY -> 4;
                    case THURSDAY -> 5;
                    case FRIDAY -> 6;
                    case SATURDAY -> 7;
                }).thenComparing(TimeSlot::getStartTime))
                .toList();
    }

    @Override
    public TimeSlot findById(Long id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("TimeSlot not found with id: " + id));
    }

    @Override
    public TimeSlot save(TimeSlot timeSlot) {

        try {
            return timeSlotRepository.save(timeSlot);
        } catch (DataIntegrityViolationException ex) {

            throw new IllegalArgumentException(
                    "This time slot already exists: "
                            + timeSlot.getDay()
                            + " "
                            + timeSlot.getStartTime()
                            + " - "
                            + timeSlot.getEndTime()
            );
        }
    }

    @Override
    public void deleteById(Long id) {

        if (!timeSlotRepository.existsById(id)) {
            throw new NoSuchElementException("TimeSlot not found with id: " + id);
        }

        timeSlotRepository.deleteById(id);
    }
}
