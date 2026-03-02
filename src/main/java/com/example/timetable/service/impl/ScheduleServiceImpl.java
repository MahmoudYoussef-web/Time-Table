package com.example.timetable.service.impl;

import com.example.timetable.dto.response.ScheduleDTO;
import com.example.timetable.entity.*;
import com.example.timetable.mapper.ScheduleMapper;
import com.example.timetable.repository.ScheduleEntryRepository;
import com.example.timetable.repository.ScheduleRepository;
import com.example.timetable.repository.SemesterRepository;
import com.example.timetable.service.GeneticScheduleService;
import com.example.timetable.service.ScheduleService;
import com.example.timetable.entity.enums.ScheduleStatus;
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
        private final SemesterRepository semesterRepository;
        private final GeneticScheduleService geneticScheduleService;

        @Override
        public Schedule generateSchedule(Long semesterId) {
                return geneticScheduleService.generate(semesterId);
        }

        @Override
        public ScheduleDTO getScheduleById(Long id) {
                Schedule schedule = getScheduleEntity(id);
                return ScheduleMapper.toDTO(schedule);
        }

        @Override
        public ScheduleDTO validateSchedule(Long id) {

                Schedule schedule = getScheduleEntity(id);

                if (schedule.getHardViolations() > 0) {
                        throw new IllegalStateException("Cannot validate schedule with hard violations");
                }

                schedule.setStatus(ScheduleStatus.VALIDATED);

                return ScheduleMapper.toDTO(schedule);
        }

        @Override
        public ScheduleDTO publishSchedule(Long id) {

                Schedule schedule = getScheduleEntity(id);

                if (schedule.getStatus() != ScheduleStatus.VALIDATED) {
                        throw new IllegalStateException("Schedule must be validated before publishing");
                }

                schedule.setStatus(ScheduleStatus.PUBLISHED);

                return ScheduleMapper.toDTO(schedule);
        }

        @Override
        public ScheduleDTO lockSchedule(Long id) {

                Schedule schedule = getScheduleEntity(id);

                if (schedule.getStatus() != ScheduleStatus.PUBLISHED) {
                        throw new IllegalStateException("Only published schedule can be locked");
                }

                schedule.setStatus(ScheduleStatus.LOCKED);

                return ScheduleMapper.toDTO(schedule);
        }

        @Override
        public ScheduleDTO getByInstructor(Long instructorId) {

                Schedule latest = scheduleRepository
                                .findTopByOrderByCreatedAtDesc()
                                .orElseThrow(() -> new NoSuchElementException("No schedules found"));

                List<ScheduleEntry> entries = scheduleEntryRepository
                                .findByScheduleIdAndSectionInstructorId(
                                                latest.getId(),
                                                instructorId);

                latest.setEntries(entries);

                return ScheduleMapper.toDTO(latest);
        }

        @Override
        public void lockEntry(Long scheduleId, Long entryId) {

                ScheduleEntry entry = scheduleEntryRepository.findById(entryId)
                                .orElseThrow(() -> new NoSuchElementException("Entry not found"));

                if (!entry.getSchedule().getId().equals(scheduleId)) {
                        throw new IllegalArgumentException("Entry does not belong to schedule");
                }

                entry.setLocked(true);
        }

        private Schedule getScheduleEntity(Long id) {
                return scheduleRepository.findById(id)
                                .orElseThrow(() -> new NoSuchElementException("Schedule not found: " + id));
        }
}