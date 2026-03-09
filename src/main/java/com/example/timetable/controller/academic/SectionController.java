package com.example.timetable.controller.academic;

import com.example.timetable.dto.request.SectionRequest;
import com.example.timetable.dto.response.SectionResponse;
import com.example.timetable.entity.Course;
import com.example.timetable.entity.Instructor;
import com.example.timetable.entity.Section;
import com.example.timetable.mapper.SectionMapper;
import com.example.timetable.service.CourseService;
import com.example.timetable.service.InstructorService;
import com.example.timetable.service.SectionService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;
    private final CourseService courseService;
    private final InstructorService instructorService;

    @GetMapping
    public ResponseEntity<List<SectionResponse>> getAll() {

        List<SectionResponse> response =
                sectionService.findAll()
                        .stream()
                        .map(SectionMapper::toResponse)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<SectionResponse> create(
            @RequestBody SectionRequest request
    ) {

        Course course = courseService.findById(request.courseId());
        Instructor instructor = instructorService.findById(request.instructorId());

        Section section =
                SectionMapper.toEntity(request, course, instructor);

        Section saved = sectionService.save(section);

        return ResponseEntity.ok(
                SectionMapper.toResponse(saved)
        );
    }
}