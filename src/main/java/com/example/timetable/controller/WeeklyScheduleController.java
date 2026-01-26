package com.example.timetable.controller;

import com.example.timetable.dto.WeeklyScheduleDTO;
import com.example.timetable.mapper.WeeklyScheduleMapper;
import com.example.timetable.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weekly-schedules")
@RequiredArgsConstructor
public class WeeklyScheduleController {

    private final ScheduleService scheduleService;

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER','INSTRUCTOR')")
    @GetMapping("/{scheduleId}")
    public ResponseEntity<WeeklyScheduleDTO> getWeeklySchedule(
            @PathVariable Long scheduleId
    ) {
        return ResponseEntity.ok(
                WeeklyScheduleMapper.toWeeklyTable(
                        scheduleService.getScheduleById(scheduleId)
                )
        );
    }
}
