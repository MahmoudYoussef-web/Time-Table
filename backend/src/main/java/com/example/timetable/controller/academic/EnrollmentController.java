package com.example.timetable.controller.academic;

import com.example.timetable.dto.request.EnrollmentRequest;
import com.example.timetable.dto.response.EnrollmentResponse;
import com.example.timetable.entity.Enrollment;
import com.example.timetable.entity.Section;
import com.example.timetable.entity.Student;
import com.example.timetable.mapper.EnrollmentMapper;
import com.example.timetable.service.EnrollmentService;
import com.example.timetable.service.SectionService;
import com.example.timetable.service.StudentService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final StudentService studentService;
    private final SectionService sectionService;

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER','INSTRUCTOR')")
    @GetMapping
public ResponseEntity<List<EnrollmentResponse>> getAll() {
        return ResponseEntity.ok(
                enrollmentService.findAll()
                        .stream()
                        .map(EnrollmentMapper::toResponse)
                        .toList()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER','INSTRUCTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                EnrollmentMapper.toResponse(enrollmentService.findById(id))
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ApiResponse(responseCode = "201", description = "Enrollment created")
    public ResponseEntity<EnrollmentResponse> create(
            @Valid @RequestBody EnrollmentRequest request
    ) {
        Student student = studentService.findById(request.studentId());
        Section section = sectionService.findById(request.sectionId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        EnrollmentMapper.toResponse(
                                enrollmentService.save(
                                        EnrollmentMapper.toEntity(request, student, section)
                                )
                        )
                );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<EnrollmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EnrollmentRequest request
    ) {
        Enrollment existing = enrollmentService.findById(id);
        Student student = studentService.findById(request.studentId());
        Section section = sectionService.findById(request.sectionId());

        existing.setStudent(student);
        existing.setSection(section);
        existing.setStatus(request.status());

        return ResponseEntity.ok(
                EnrollmentMapper.toResponse(enrollmentService.save(existing))
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        enrollmentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}



