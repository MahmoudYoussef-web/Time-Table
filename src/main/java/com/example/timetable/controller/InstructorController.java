package com.example.timetable.controller;

import com.example.timetable.dto.InstructorRequest;
import com.example.timetable.dto.InstructorResponse;
import com.example.timetable.mapper.InstructorMapper;
import com.example.timetable.service.InstructorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instructors")
@RequiredArgsConstructor
public class InstructorController {

    private final InstructorService instructorService;

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER','INSTRUCTOR')")
    @GetMapping
    public ResponseEntity<List<InstructorResponse>> getAllInstructors() {
        return ResponseEntity.ok(
                instructorService.findAll()
                        .stream()
                        .map(InstructorMapper::toResponse)
                        .toList()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER','INSTRUCTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<InstructorResponse> getInstructorById(@PathVariable Long id) {
        return ResponseEntity.ok(
                InstructorMapper.toResponse(instructorService.findById(id))
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @PostMapping
    public ResponseEntity<InstructorResponse> createInstructor(
            @Valid @RequestBody InstructorRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        InstructorMapper.toResponse(
                                instructorService.save(
                                        InstructorMapper.toEntity(request)
                                )
                        )
                );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInstructor(@PathVariable Long id) {
        instructorService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
