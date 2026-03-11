package com.example.timetable.service.impl;

import com.example.timetable.dto.response.ScheduleDTO;
import com.example.timetable.entity.Schedule;
import com.example.timetable.entity.ScheduleEntry;
import com.example.timetable.entity.ScheduleGenerationJob;
import com.example.timetable.entity.Semester;
import com.example.timetable.entity.enums.JobStatus;
import com.example.timetable.entity.enums.ScheduleStatus;
import com.example.timetable.entity.enums.SemesterStatus;
import com.example.timetable.mapper.ScheduleMapper;
import com.example.timetable.repository.ScheduleEntryRepository;
import com.example.timetable.repository.ScheduleGenerationJobRepository;
import com.example.timetable.repository.ScheduleRepository;
import com.example.timetable.repository.SemesterRepository;
import com.example.timetable.scheduling.constraints.ConstraintViolation;
import com.example.timetable.service.ConflictEvaluationService;
import com.example.timetable.service.GeneticScheduleService;
import com.example.timetable.service.ScheduleService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleEntryRepository scheduleEntryRepository;
    private final SemesterRepository semesterRepository;
    private final GeneticScheduleService geneticScheduleService;
    private final ScheduleGenerationJobRepository jobRepository;
    private final ConflictEvaluationService conflictEvaluationService;

    private static final Logger log =
            LoggerFactory.getLogger(ScheduleServiceImpl.class);

    @Override
    public UUID generateScheduleAsync(Long semesterId) {

        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new NoSuchElementException("Semester not found"));

        if (semester.getStatus() == SemesterStatus.CLOSED)
            throw new IllegalStateException("Cannot generate schedule for closed semester");

        ScheduleGenerationJob job = new ScheduleGenerationJob();
        job.setId(UUID.randomUUID());
        job.setStatus(JobStatus.RUNNING);

        jobRepository.save(job);

        try {

            Schedule schedule =
                    geneticScheduleService.generate(semesterId);

            job.setScheduleId(schedule.getId());
            job.setStatus(JobStatus.COMPLETED);

        } catch (Exception e) {

            log.error("Schedule generation failed", e);
            e.printStackTrace();
            job.setStatus(JobStatus.FAILED);
        }

        jobRepository.save(job);

        return job.getId();
    }

    @Override
    public ScheduleGenerationJob getJob(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new NoSuchElementException("Job not found"));
    }

    @Override
    public ScheduleDTO getScheduleById(Long id) {
        return ScheduleMapper.toDTO(getScheduleEntity(id));
    }

    @Override
    public ScheduleDTO validateSchedule(Long id) {

        Schedule schedule = getScheduleEntity(id);

        if (schedule.getStatus() != ScheduleStatus.DRAFT)
            throw new IllegalStateException("Only draft schedules can be validated");

        if (schedule.getHardViolations() > 0)
            throw new IllegalStateException("Cannot validate schedule with hard violations");

        schedule.setStatus(ScheduleStatus.VALIDATED);

        return ScheduleMapper.toDTO(schedule);
    }

    @Override
    public ScheduleDTO publishSchedule(Long id) {

        Schedule schedule = getScheduleEntity(id);

        if (schedule.getStatus() != ScheduleStatus.VALIDATED)
            throw new IllegalStateException("Schedule must be validated before publishing");

        boolean exists = scheduleRepository
                .existsBySemesterIdAndStatus(
                        schedule.getSemester().getId(),
                        ScheduleStatus.PUBLISHED
                );

        if (exists)
            throw new IllegalStateException("Another schedule already published");

        schedule.setStatus(ScheduleStatus.PUBLISHED);

        return ScheduleMapper.toDTO(schedule);
    }

    @Override
    public ScheduleDTO lockSchedule(Long id) {

        Schedule schedule = getScheduleEntity(id);

        if (schedule.getStatus() != ScheduleStatus.PUBLISHED)
            throw new IllegalStateException("Only published schedule can be locked");

        schedule.setStatus(ScheduleStatus.LOCKED);

        return ScheduleMapper.toDTO(schedule);
    }

    @Override
    public ScheduleDTO getByInstructor(Long instructorId) {

        Schedule latest = scheduleRepository
                .findTopByStatusOrderByCreatedAtDesc(ScheduleStatus.PUBLISHED)
                .orElseThrow();

        List<ScheduleEntry> entries =
                scheduleEntryRepository
                        .findByScheduleIdAndSectionInstructorId(
                                latest.getId(),
                                instructorId
                        );

        latest.setEntries(entries);

        return ScheduleMapper.toDTO(latest);
    }

    @Override
    public void lockEntry(Long scheduleId, Long entryId) {

        ScheduleEntry entry =
                scheduleEntryRepository.findById(entryId)
                        .orElseThrow();

        if (!entry.getSchedule().getId().equals(scheduleId))
            throw new IllegalArgumentException("Invalid entry");

        entry.setLocked(true);
    }

    private Schedule getScheduleEntity(Long id) {

        return scheduleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Schedule not found"));
    }

    @Override
    public List<ConstraintViolation> getConflicts(Long scheduleId) {

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NoSuchElementException("Schedule not found"));

        return conflictEvaluationService.explain(schedule);
    }
}