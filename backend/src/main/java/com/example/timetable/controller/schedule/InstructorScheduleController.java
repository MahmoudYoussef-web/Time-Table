package com.example.timetable.controller.schedule;

import com.example.timetable.dto.response.WeeklyScheduleDTO;
import com.example.timetable.mapper.WeeklyScheduleMapper;
import com.example.timetable.service.InstructorService;
import com.example.timetable.service.ScheduleService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/instructor/schedule")
@RequiredArgsConstructor
public class InstructorScheduleController {

    private final ScheduleService scheduleService;
    private final InstructorService instructorService;

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @GetMapping("/my")
    public ResponseEntity<?> getMySchedule(
            Authentication authentication
    ) {
        String email = authentication.getName();

        try {
            Long instructorId = instructorService.findByEmail(email).getId();
            return ResponseEntity.ok(
                    WeeklyScheduleMapper.toWeeklyTable(
                            scheduleService.getByInstructor(instructorId)
                    )
            );
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of(
                            "success", false,
                            "message", "No instructor profile found for this account"
                    ));
        }
    }
}
