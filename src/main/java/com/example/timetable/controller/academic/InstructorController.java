package com.example.timetable.controller.academic;

import com.example.timetable.dto.request.InstructorRequest;
import com.example.timetable.dto.response.InstructorResponse;
import com.example.timetable.entity.Department;
import com.example.timetable.entity.Instructor;
import com.example.timetable.mapper.InstructorMapper;
import com.example.timetable.service.DepartmentService;
import com.example.timetable.service.InstructorService;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instructors")
@RequiredArgsConstructor
public class InstructorController {

    private final InstructorService instructorService;
    private final DepartmentService departmentService;
    private final PasswordEncoder passwordEncoder;

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER','INSTRUCTOR')")
    @GetMapping
    public ResponseEntity<List<InstructorResponse>> getAll() {
        return ResponseEntity.ok(
                instructorService.findAll()
                        .stream()
                        .map(InstructorMapper::toResponse)
                        .toList()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ApiResponse(responseCode = "201", description = "Instructor created")
    public ResponseEntity<InstructorResponse> create(
            @Valid @RequestBody InstructorRequest request
    ) {
        Department department =
                departmentService.findById(request.departmentId());

        Instructor instructor =
                InstructorMapper.toEntity(request, department, passwordEncoder);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        InstructorMapper.toResponse(
                                instructorService.save(instructor)
                        )
                );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<InstructorResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody InstructorRequest request
    ) {
        Instructor existing = instructorService.findById(id);
        Department department = departmentService.findById(request.departmentId());

        existing.getUser().setFullName(request.name());
        existing.getUser().setEmail(request.email());
        existing.setDepartment(department);

        if (request.password() != null && !request.password().isBlank()) {
            existing.getUser().setPassword(passwordEncoder.encode(request.password()));
        }

        return ResponseEntity.ok(
                InstructorMapper.toResponse(instructorService.save(existing))
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        instructorService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}