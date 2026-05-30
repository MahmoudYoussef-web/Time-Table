package com.example.timetable.controller.academic;

import com.example.timetable.dto.request.SemesterRequest;
import com.example.timetable.dto.response.SemesterResponse;
import com.example.timetable.entity.Semester;
import com.example.timetable.mapper.SemesterMapper;
import com.example.timetable.service.SemesterService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/semesters")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterService semesterService;

    @GetMapping
    public ResponseEntity<List<SemesterResponse>> getAll() {
        List<SemesterResponse> response =
                semesterService.findAll()
                        .stream()
                        .map(SemesterMapper::toResponse)
                        .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<SemesterResponse> create(
            @Valid @RequestBody SemesterRequest request
    ) {
        Semester semester = SemesterMapper.toEntity(request);
        Semester saved = semesterService.save(semester);
        return ResponseEntity.ok(SemesterMapper.toResponse(saved));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<SemesterResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SemesterRequest request
    ) {
        return ResponseEntity.ok(
                SemesterMapper.toResponse(semesterService.update(id, request))
        );
    }
}