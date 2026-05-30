package com.example.timetable.service.impl;

import com.example.timetable.dto.response.ScheduleDTO;
import com.example.timetable.dto.response.WeeklyScheduleDTO;
import com.example.timetable.entity.Schedule;
import com.example.timetable.entity.ScheduleEntry;
import com.example.timetable.entity.enums.ScheduleStatus;
import com.example.timetable.mapper.ScheduleMapper;
import com.example.timetable.mapper.WeeklyScheduleMapper;
import com.example.timetable.repository.ScheduleEntryRepository;
import com.example.timetable.repository.ScheduleRepository;
import com.example.timetable.service.ScheduleFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleFilterServiceImpl implements ScheduleFilterService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleEntryRepository scheduleEntryRepository;

    @Override
    public WeeklyScheduleDTO getByInstructor(Long instructorId) {
        Schedule latest = scheduleRepository
                .findTopByStatusOrderByCreatedAtDesc(ScheduleStatus.PUBLISHED)
                .orElseThrow(() -> new NoSuchElementException("No published schedule found"));

        List<ScheduleEntry> entries = scheduleEntryRepository
                .findByScheduleIdAndInstructorIdWithDetails(latest.getId(), instructorId);

        latest.setEntries(entries);
        return WeeklyScheduleMapper.toWeeklyTable(ScheduleMapper.toDTO(latest));
    }

    @Override
    public WeeklyScheduleDTO getByDepartment(Long departmentId) {
        Schedule latest = scheduleRepository
                .findTopByStatusOrderByCreatedAtDesc(ScheduleStatus.PUBLISHED)
                .orElseThrow(() -> new NoSuchElementException("No published schedule found"));

        Schedule schedule = scheduleRepository.findByIdWithDetails(latest.getId())
                .orElseThrow(() -> new NoSuchElementException("Schedule not found"));

        List<ScheduleEntry> filtered = schedule.getEntries().stream()
                .filter(e -> e.getSection().getCourse().getDepartment().getId().equals(departmentId))
                .collect(Collectors.toList());

        schedule.setEntries(filtered);
        return WeeklyScheduleMapper.toWeeklyTable(ScheduleMapper.toDTO(schedule));
    }

    @Override
    public WeeklyScheduleDTO getByCourse(Long courseId) {
        Schedule latest = scheduleRepository
                .findTopByStatusOrderByCreatedAtDesc(ScheduleStatus.PUBLISHED)
                .orElseThrow(() -> new NoSuchElementException("No published schedule found"));

        Schedule schedule = scheduleRepository.findByIdWithDetails(latest.getId())
                .orElseThrow(() -> new NoSuchElementException("Schedule not found"));

        List<ScheduleEntry> filtered = schedule.getEntries().stream()
                .filter(e -> e.getSection().getCourse().getId().equals(courseId))
                .collect(Collectors.toList());

        schedule.setEntries(filtered);
        return WeeklyScheduleMapper.toWeeklyTable(ScheduleMapper.toDTO(schedule));
    }
}
