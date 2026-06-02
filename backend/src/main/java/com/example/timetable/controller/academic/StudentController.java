package com.example.timetable.controller.academic;

import com.example.timetable.dto.request.StudentRequest;
import com.example.timetable.dto.response.StudentResponse;
import com.example.timetable.entity.Department;
import com.example.timetable.entity.Student;
import com.example.timetable.entity.User;
import com.example.timetable.mapper.StudentMapper;
import com.example.timetable.repository.UserRepository;
import com.example.timetable.service.DepartmentService;
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
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final UserRepository userRepository;
    private final DepartmentService departmentService;

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER','INSTRUCTOR')")
    @GetMapping
public ResponseEntity<List<StudentResponse>> getAll() {
        return ResponseEntity.ok(
                studentService.findAll()
                        .stream()
                        .map(StudentMapper::toResponse)
                        .toList()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER','INSTRUCTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                StudentMapper.toResponse(studentService.findById(id))
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ApiResponse(responseCode = "201", description = "Student created")
    public ResponseEntity<StudentResponse> create(
            @Valid @RequestBody StudentRequest request
    ) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new java.util.NoSuchElementException("User not found with id: " + request.userId()));
        Department department = departmentService.findById(request.departmentId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        StudentMapper.toResponse(
                                studentService.save(
                                        StudentMapper.toEntity(request, user, department)
                                )
                        )
                );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequest request
    ) {
        Student existing = studentService.findById(id);
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new java.util.NoSuchElementException("User not found with id: " + request.userId()));
        Department department = departmentService.findById(request.departmentId());

        existing.setUser(user);
        existing.setAcademicYear(request.academicYear());
        existing.setLevel(request.level());
        existing.setDepartment(department);

        return ResponseEntity.ok(
                StudentMapper.toResponse(studentService.save(existing))
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}



