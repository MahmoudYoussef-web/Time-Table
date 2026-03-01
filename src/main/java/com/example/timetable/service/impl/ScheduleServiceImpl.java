package com.example.timetable.service.impl;

import com.example.timetable.dto.response.ScheduleDTO;
import com.example.timetable.entity.Schedule;
import com.example.timetable.entity.ScheduleEntry;
import com.example.timetable.mapper.ScheduleMapper;
import com.example.timetable.repository.ScheduleEntryRepository;
import com.example.timetable.repository.ScheduleRepository;
import com.example.timetable.service.ScheduleService;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleEntryRepository scheduleEntryRepository;

    // Generate schedule (handled by GA)
    @Override
    public Schedule generateSchedule() {
        throw new UnsupportedOperationException(
                "Handled in GeneticAlgorithmService"
        );
    }

    // Get schedule by id
    @Override
    public ScheduleDTO getScheduleById(Long id) {

        Schedule schedule =
                scheduleRepository.findById(id)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Schedule not found: " + id
                                )
                        );

        return ScheduleMapper.toDTO(schedule);
    }

    // Validate schedule
    @Override
    public ScheduleDTO validateSchedule(Long id) {

        Schedule schedule =
                scheduleRepository.findById(id)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Schedule not found: " + id
                                )
                        );

        return ScheduleMapper.toDTO(schedule);
    }

    // Get schedule for instructor
    @Override
    public ScheduleDTO getByInstructor(Long instructorId) {

        // Get latest schedule
        Schedule latest =
                scheduleRepository
                        .findTopByOrderByCreatedAtDesc()
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "No schedules found"
                                )
                        );

        // Filter entries
        List<ScheduleEntry> entries =
                scheduleEntryRepository
                        .findByScheduleIdAndSectionInstructorId(
                                latest.getId(),
                                instructorId
                        );

        latest.setEntries(entries);

        return ScheduleMapper.toDTO(latest);
    }
}
