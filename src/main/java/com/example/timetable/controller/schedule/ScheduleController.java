package com.example.timetable.controller.schedule;

import com.example.timetable.dto.response.ScheduleDTO;
import com.example.timetable.entity.Schedule;
import com.example.timetable.mapper.ScheduleMapper;
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
        @PostMapping("/generate/{semesterId}")
        public ResponseEntity<ScheduleDTO> generate(
                        @PathVariable Long semesterId) {

                Schedule schedule = scheduleService.generateSchedule(semesterId);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ScheduleMapper.toDTO(schedule));
        }

        // Get schedule by id
        @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER','INSTRUCTOR')")
        @GetMapping("/{id}")
        public ResponseEntity<ScheduleDTO> getById(
                        @PathVariable Long id) {
                return ResponseEntity.ok(
                                scheduleService.getScheduleById(id));
        }

        // Validate schedule
        @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
        @GetMapping("/{id}/validate")
        public ResponseEntity<ScheduleDTO> validate(
                        @PathVariable Long id) {
                return ResponseEntity.ok(
                                scheduleService.validateSchedule(id));
        }

        @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
        @PatchMapping("/{scheduleId}/entries/{entryId}/lock")
        public ResponseEntity<Void> lockEntry(
                        @PathVariable Long scheduleId,
                        @PathVariable Long entryId) {

                scheduleService.lockEntry(scheduleId, entryId);
                return ResponseEntity.noContent().build();
        }
}
