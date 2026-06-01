package com.example.timetable.controller.academic;

import com.example.timetable.dto.request.TimeSlotRequest;
import com.example.timetable.dto.response.TimeSlotResponse;
import com.example.timetable.entity.TimeSlot;
import com.example.timetable.mapper.TimeSlotMapper;
import com.example.timetable.service.TimeSlotService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/timeslots")
@RequiredArgsConstructor
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @GetMapping
    public ResponseEntity<List<TimeSlotResponse>> getAll() {
        List<TimeSlotResponse> response =
                timeSlotService.findAll()
                        .stream()
                        .map(TimeSlotMapper::toResponse)
                        .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TimeSlotResponse> create(
            @Valid @RequestBody TimeSlotRequest request
    ) {
        TimeSlot timeSlot = TimeSlotMapper.toEntity(request);
        TimeSlot saved = timeSlotService.save(timeSlot);
        return ResponseEntity.ok(TimeSlotMapper.toResponse(saved));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TimeSlotResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TimeSlotRequest request
    ) {
        TimeSlot existing = timeSlotService.findById(id);
        existing.setDay(java.time.DayOfWeek.valueOf(request.day().toUpperCase()));
        existing.setStartTime(java.time.LocalTime.parse(request.startTime()));
        existing.setEndTime(java.time.LocalTime.parse(request.endTime()));
        return ResponseEntity.ok(
                TimeSlotMapper.toResponse(timeSlotService.save(existing))
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        timeSlotService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}