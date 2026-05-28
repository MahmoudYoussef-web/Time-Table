package com.example.timetable.controller.academic;

import com.example.timetable.dto.request.SectionRequest;
import com.example.timetable.dto.response.SectionResponse;
import com.example.timetable.entity.Course;
import com.example.timetable.entity.Instructor;
import com.example.timetable.entity.Section;
import com.example.timetable.entity.Semester;
import com.example.timetable.mapper.SectionMapper;
import com.example.timetable.service.CourseService;
import com.example.timetable.service.InstructorService;
import com.example.timetable.service.SectionService;
import com.example.timetable.service.SemesterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;
    private final CourseService courseService;
    private final InstructorService instructorService;
    private final SemesterService semesterService;

    @GetMapping
    public ResponseEntity<List<SectionResponse>> getAll() {
        List<SectionResponse> response =
                sectionService.findAll()
                        .stream()
                        .map(SectionMapper::toResponse)
                        .toList();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @PostMapping
    public ResponseEntity<SectionResponse> create(
            @Valid @RequestBody SectionRequest request
    ) {
        Course course = courseService.findById(request.courseId());
        Instructor instructor = instructorService.findById(request.instructorId());
        Semester semester = semesterService.findById(request.semesterId());

        Section section = SectionMapper.toEntity(request, course, instructor, semester);
        Section saved = sectionService.save(section);
        return ResponseEntity.ok(SectionMapper.toResponse(saved));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @PutMapping("/{id}")
    public ResponseEntity<SectionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SectionRequest request
    ) {
        Section existing = sectionService.findById(id);
        Course course = courseService.findById(request.courseId());
        Instructor instructor = instructorService.findById(request.instructorId());
        Semester semester = semesterService.findById(request.semesterId());

        existing.setName(request.name());
        existing.setCourse(course);
        existing.setInstructor(instructor);
        existing.setSemester(semester);
        existing.setCapacity(request.capacity());
        existing.setYearLevel(request.yearLevel());

        return ResponseEntity.ok(
                SectionMapper.toResponse(sectionService.save(existing))
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sectionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}