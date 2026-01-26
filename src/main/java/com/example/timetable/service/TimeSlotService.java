package com.example.timetable.service;

import com.example.timetable.model.TimeSlot;
import com.example.timetable.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;

    public List<TimeSlot> findAll() {
        return timeSlotRepository.findAll();
    }

    public TimeSlot save(TimeSlot timeSlot) {
        return timeSlotRepository.save(timeSlot);
    }

    public TimeSlot findById(Long id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("TimeSlot not found with id: " + id));
    }

    public void deleteById(Long id) {
        if (!timeSlotRepository.existsById(id)) {
            throw new NoSuchElementException("TimeSlot not found with id: " + id);
        }
        timeSlotRepository.deleteById(id);
    }
}
