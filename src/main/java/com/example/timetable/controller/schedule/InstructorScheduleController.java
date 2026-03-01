package com.example.timetable.controller.schedule;

import com.example.timetable.dto.response.WeeklyScheduleDTO;
import com.example.timetable.mapper.WeeklyScheduleMapper;
import com.example.timetable.service.InstructorService;
import com.example.timetable.service.ScheduleService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/instructor/schedule")
@RequiredArgsConstructor
public class InstructorScheduleController {

    private final ScheduleService scheduleService;
    private final InstructorService instructorService;

    // Get schedule for logged-in instructor
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/my")
    public ResponseEntity<WeeklyScheduleDTO> getMySchedule(
            Authentication authentication
    ) {

        // Email from JWT
        String email = authentication.getName();

        // Get instructor id
        Long instructorId =
                instructorService
                        .findByEmail(email)
                        .getId();

        return ResponseEntity.ok(
                WeeklyScheduleMapper.toWeeklyTable(
                        scheduleService.getByInstructor(instructorId)
                )
        );
    }
}
