package com.example.timetable.controller;

import com.example.timetable.dto.ScheduleDTO;
import com.example.timetable.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @PostMapping("/generate")
    public ResponseEntity<ScheduleDTO> generateSchedule() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        scheduleService.getScheduleById(
                                scheduleService.generateSchedule().getId()
                        )
                );
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER','INSTRUCTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ScheduleDTO> getSchedule(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.getScheduleById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @GetMapping("/{id}/validate")
    public ResponseEntity<ScheduleDTO> validateSchedule(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleService.validateSchedule(id));
    }
}
