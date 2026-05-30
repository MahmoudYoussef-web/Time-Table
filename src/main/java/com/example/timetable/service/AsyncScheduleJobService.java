package com.example.timetable.service;

import com.example.timetable.entity.Schedule;
import com.example.timetable.entity.ScheduleGenerationJob;
import com.example.timetable.entity.enums.JobStatus;
import com.example.timetable.repository.ScheduleGenerationJobRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AsyncScheduleJobService {

    private final GeneticScheduleService geneticScheduleService;
    private final ScheduleGenerationJobRepository jobRepository;

    private static final Logger log = LoggerFactory.getLogger(AsyncScheduleJobService.class);

    @Async
    public void generate(UUID jobId, Long semesterId) {
        try {
            Schedule schedule = geneticScheduleService.generate(semesterId);

            ScheduleGenerationJob job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new IllegalStateException("Job not found: " + jobId));
            job.setScheduleId(schedule.getId());
            job.setStatus(JobStatus.COMPLETED);
            jobRepository.save(job);

            log.info("Schedule generation completed. jobId={}, scheduleId={}", jobId, schedule.getId());

        } catch (Exception e) {
            log.error("Schedule generation failed. jobId={}", jobId, e);

            ScheduleGenerationJob job = jobRepository.findById(jobId).orElse(null);
            if (job != null) {
                job.setStatus(JobStatus.FAILED);
                jobRepository.save(job);
            }
        }
    }
}
