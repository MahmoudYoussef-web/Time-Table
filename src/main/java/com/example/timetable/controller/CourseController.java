package com.example.timetable.controller;

import com.example.timetable.dto.CourseRequest;
import com.example.timetable.dto.CourseResponse;
import com.example.timetable.mapper.CourseMapper;
import com.example.timetable.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER','INSTRUCTOR')")
    @GetMapping
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        return ResponseEntity.ok(
                courseService.findAll()
                        .stream()
                        .map(CourseMapper::toResponse)
                        .toList()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER','INSTRUCTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(
                CourseMapper.toResponse(courseService.findById(id))
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CourseRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        CourseMapper.toResponse(
                                courseService.save(
                                        CourseMapper.toEntity(request)
                                )
                        )
                );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
