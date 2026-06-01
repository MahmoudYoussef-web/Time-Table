package com.example.timetable.controller.schedule;

import com.example.timetable.dto.response.WeeklyScheduleDTO;
import com.example.timetable.mapper.WeeklyScheduleMapper;
import com.example.timetable.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weekly-schedules")
@RequiredArgsConstructor
public class WeeklyScheduleController {

    private final ScheduleService scheduleService;

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER','INSTRUCTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<WeeklyScheduleDTO> getWeekly(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                WeeklyScheduleMapper.toWeeklyTable(
                        scheduleService.getScheduleById(id)
                )
        );
    }
}
