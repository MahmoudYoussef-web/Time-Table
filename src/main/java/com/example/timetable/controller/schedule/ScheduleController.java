package com.example.timetable.controller.schedule;

import com.example.timetable.entity.ScheduleGenerationJob;
import com.example.timetable.scheduling.constraints.ConstraintViolation;
import com.example.timetable.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @PostMapping("/generate/{semesterId}")
    public ResponseEntity<UUID> generateAsync(
            @PathVariable Long semesterId) {

        UUID jobId =
                scheduleService.generateScheduleAsync(semesterId);

        return ResponseEntity.ok(jobId);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<ScheduleGenerationJob> getJob(
            @PathVariable UUID jobId) {

        return ResponseEntity.ok(
                scheduleService.getJob(jobId));
    }
    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @GetMapping("/{id}/conflicts")
    public ResponseEntity<List<ConstraintViolation>> conflicts(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                scheduleService.getConflicts(id)
        );
    }
    }
