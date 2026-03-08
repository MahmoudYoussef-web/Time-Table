package com.example.timetable.controller.academic;

import com.example.timetable.entity.TimeSlot;
import com.example.timetable.service.TimeSlotService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timeslots")
@RequiredArgsConstructor
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @GetMapping
    public ResponseEntity<List<TimeSlot>> getAll() {
        return ResponseEntity.ok(timeSlotService.findAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TimeSlot> create(
            @RequestBody TimeSlot timeSlot
    ) {
        return ResponseEntity.ok(
                timeSlotService.save(timeSlot)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        timeSlotService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}