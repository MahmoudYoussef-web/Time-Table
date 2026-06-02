package com.example.timetable.controller.instructor;

import com.example.timetable.dto.response.TimeSlotResponse;
import com.example.timetable.service.InstructorAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instructors/{instructorId}/unavailable-slots")
@RequiredArgsConstructor
public class InstructorAvailabilityController {

    private final InstructorAvailabilityService service;

    @GetMapping
    public ResponseEntity<List<TimeSlotResponse>> getUnavailable(@PathVariable Long instructorId) {
        return ResponseEntity.ok(service.getUnavailableSlots(instructorId));
    }

    @PostMapping("/{slotId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<Void> addUnavailable(
            @PathVariable Long instructorId,
            @PathVariable Long slotId) {
        service.addUnavailableSlot(instructorId, slotId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{slotId}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<Void> removeUnavailable(
            @PathVariable Long instructorId,
            @PathVariable Long slotId) {
        service.removeUnavailableSlot(instructorId, slotId);
        return ResponseEntity.noContent().build();
    }
}
