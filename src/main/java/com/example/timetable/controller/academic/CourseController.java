package com.example.timetable.controller.academic;

import com.example.timetable.dto.request.CourseRequest;
import com.example.timetable.dto.response.CourseResponse;
import com.example.timetable.entity.Department;
import com.example.timetable.mapper.CourseMapper;
import com.example.timetable.service.CourseService;
import com.example.timetable.service.DepartmentService;
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
    private final DepartmentService departmentService;

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER','INSTRUCTOR')")
    @GetMapping
    public ResponseEntity<List<CourseResponse>> getAll() {
        return ResponseEntity.ok(
                courseService.findAll()
                        .stream()
                        .map(CourseMapper::toResponse)
                        .toList()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER','INSTRUCTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                CourseMapper.toResponse(
                        courseService.findById(id)
                )
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CourseResponse> create(
            @Valid @RequestBody CourseRequest request
    ) {

        Department department =
                departmentService.findById(
                        request.departmentId()
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        CourseMapper.toResponse(
                                courseService.save(
                                        CourseMapper.toEntity(
                                                request,
                                                department
                                        )
                                )
                        )
                );
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
