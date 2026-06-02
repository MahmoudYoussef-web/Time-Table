package com.example.timetable.service.impl;

import com.example.timetable.dto.response.ScheduleDTO;
import com.example.timetable.dto.response.ScheduleSummaryResponse;
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
import com.example.timetable.service.AsyncScheduleJobService;
import com.example.timetable.service.ConflictEvaluationService;
import com.example.timetable.service.GeneticScheduleService;
import com.example.timetable.service.ScheduleService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleEntryRepository scheduleEntryRepository;
    private final SemesterRepository semesterRepository;
    private final GeneticScheduleService geneticScheduleService;
    private final AsyncScheduleJobService asyncScheduleJobService;
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

        asyncScheduleJobService.generate(job.getId(), semesterId);

        return job.getId();
    }

    @Override
    public ScheduleGenerationJob getJob(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new NoSuchElementException("Job not found"));
    }

    @Override
    public List<ScheduleSummaryResponse> findAll() {
        return scheduleRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(s -> new ScheduleSummaryResponse(
                        s.getId(),
                        s.getSemester().getName(),
                        s.getStatus().name(),
                        s.getFitnessScore(),
                        s.getHardViolations(),
                        s.getSoftViolations(),
                        s.getCreatedAt().toString()
                ))
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        Schedule schedule = getScheduleEntity(id);
        if (schedule.getStatus() != ScheduleStatus.DRAFT)
            throw new IllegalStateException("Cannot delete a published or locked schedule");
        scheduleRepository.delete(schedule);
    }

    @Override
    public ScheduleDTO getScheduleById(Long id) {
        return ScheduleMapper.toDTO(getScheduleWithDetails(id));
    }

    @Override
    public ScheduleDTO validateSchedule(Long id) {

        Schedule schedule = getScheduleEntity(id);

        if (schedule.getStatus() != ScheduleStatus.DRAFT)
            throw new IllegalStateException("Only draft schedules can be validated");

        if (schedule.getHardViolations() > 0)
            throw new IllegalStateException("Cannot validate schedule with hard violations");

        schedule.setStatus(ScheduleStatus.VALIDATED);

        return ScheduleMapper.toDTO(scheduleRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NoSuchElementException("Schedule not found")));
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

        return ScheduleMapper.toDTO(scheduleRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NoSuchElementException("Schedule not found")));
    }

    @Override
    public ScheduleDTO lockSchedule(Long id) {

        Schedule schedule = getScheduleEntity(id);

        if (schedule.getStatus() != ScheduleStatus.PUBLISHED)
            throw new IllegalStateException("Only published schedule can be locked");

        schedule.setStatus(ScheduleStatus.LOCKED);

        return ScheduleMapper.toDTO(scheduleRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NoSuchElementException("Schedule not found")));
    }

    @Override
    public ScheduleDTO getByInstructor(Long instructorId) {

        Schedule latest = scheduleRepository
                .findTopByStatusOrderByCreatedAtDesc(ScheduleStatus.PUBLISHED)
                .orElseThrow(() -> new NoSuchElementException("No published schedule found"));

        List<ScheduleEntry> entries =
                scheduleEntryRepository
                        .findByScheduleIdAndInstructorIdWithDetails(
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
                        .orElseThrow(() -> new NoSuchElementException("Entry not found"));

        if (!entry.getSchedule().getId().equals(scheduleId))
            throw new IllegalArgumentException("Invalid entry");

        entry.setLocked(true);
    }

    private Schedule getScheduleEntity(Long id) {

        return scheduleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Schedule not found"));
    }

    private Schedule getScheduleWithDetails(Long id) {

        return scheduleRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NoSuchElementException("Schedule not found"));
    }

    @Override
    public List<ConstraintViolation> getConflicts(Long scheduleId) {

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NoSuchElementException("Schedule not found"));

        List<ConstraintViolation> violations = conflictEvaluationService.explain(schedule);

        if (schedule.getUnscheduledSectionIds() != null
                && !schedule.getUnscheduledSectionIds().isEmpty()) {

            for (Long sectionId : schedule.getUnscheduledSectionIds()) {
                violations.add(new ConstraintViolation(
                        "UnscheduledSection",
                        sectionId,
                        "Section could not be scheduled due to no available room/time slot"
                ));
            }
        }

        return violations;
    }
}
