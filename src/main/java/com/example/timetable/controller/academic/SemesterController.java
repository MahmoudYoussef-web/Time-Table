package com.example.timetable.controller.academic;

import com.example.timetable.entity.Semester;
import com.example.timetable.repository.SemesterRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/semesters")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterRepository semesterRepository;

    @GetMapping
    public ResponseEntity<List<Semester>> getAll() {
        return ResponseEntity.ok(
                semesterRepository.findAll()
        );
    }

    @PostMapping
    public ResponseEntity<Semester> create(
            @RequestBody Semester semester
    ) {
        return ResponseEntity.ok(
                semesterRepository.save(semester)
        );
    }
}