package com.example.timetable.service.impl;

import com.example.timetable.entity.TimeSlot;
import com.example.timetable.repository.TimeSlotRepository;
import com.example.timetable.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class TimeSlotServiceImpl implements TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;

    @Override
    public List<TimeSlot> findAll() {
        return timeSlotRepository.findAll();
    }

    @Override
    public TimeSlot findById(Long id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("TimeSlot not found with id: " + id));
    }

    @Override
    public TimeSlot save(TimeSlot timeSlot) {
        return timeSlotRepository.save(timeSlot);
    }

    @Override
    public void deleteById(Long id) {

        if (!timeSlotRepository.existsById(id)) {
            throw new NoSuchElementException("TimeSlot not found with id: " + id);
        }

        timeSlotRepository.deleteById(id);
    }
}
