package com.example.timetable.controller;

import com.example.timetable.dto.TimeSlotRequest;
import com.example.timetable.dto.TimeSlotResponse;
import com.example.timetable.mapper.TimeSlotMapper;
import com.example.timetable.service.TimeSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timeslots")
@RequiredArgsConstructor
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER','INSTRUCTOR')")
    @GetMapping
    public ResponseEntity<List<TimeSlotResponse>> getAllTimeSlots() {
        return ResponseEntity.ok(
                timeSlotService.findAll()
                        .stream()
                        .map(TimeSlotMapper::toResponse)
                        .toList()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER','INSTRUCTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<TimeSlotResponse> getTimeSlotById(@PathVariable Long id) {
        return ResponseEntity.ok(
                TimeSlotMapper.toResponse(timeSlotService.findById(id))
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @PostMapping
    public ResponseEntity<TimeSlotResponse> createTimeSlot(
            @Valid @RequestBody TimeSlotRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        TimeSlotMapper.toResponse(
                                timeSlotService.save(
                                        TimeSlotMapper.toEntity(request)
                                )
                        )
                );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimeSlot(@PathVariable Long id) {
        timeSlotService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
