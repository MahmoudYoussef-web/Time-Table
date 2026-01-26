package com.example.timetable.controller;

import com.example.timetable.dto.ClassSectionRequest;
import com.example.timetable.dto.ClassSectionResponse;
import com.example.timetable.mapper.ClassSectionMapper;
import com.example.timetable.model.Course;
import com.example.timetable.model.Instructor;
import com.example.timetable.service.ClassSectionService;
import com.example.timetable.service.CourseService;
import com.example.timetable.service.InstructorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/class-sections")
@RequiredArgsConstructor
public class ClassSectionController {

    private final ClassSectionService classSectionService;
    private final CourseService courseService;
    private final InstructorService instructorService;

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER','INSTRUCTOR')")
    @GetMapping
    public ResponseEntity<List<ClassSectionResponse>> getAllSections() {
        return ResponseEntity.ok(
                classSectionService.findAll()
                        .stream()
                        .map(ClassSectionMapper::toResponse)
                        .toList()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER','INSTRUCTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ClassSectionResponse> getSectionById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ClassSectionMapper.toResponse(
                        classSectionService.findById(id)
                )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @PostMapping
    public ResponseEntity<ClassSectionResponse> createSection(
            @Valid @RequestBody ClassSectionRequest request
    ) {
        Course course = courseService.findById(request.courseId());
        Instructor instructor = instructorService.findById(request.instructorId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ClassSectionMapper.toResponse(
                                classSectionService.save(
                                        ClassSectionMapper.toEntity(
                                                request, course, instructor
                                        )
                                )
                        )
                );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSection(@PathVariable Long id) {
        classSectionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
