package com.example.timetable.controller.academic;

import com.example.timetable.dto.request.DepartmentRequest;
import com.example.timetable.dto.response.DepartmentResponse;
import com.example.timetable.entity.Department;
import com.example.timetable.mapper.DepartmentMapper;
import com.example.timetable.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAll() {
        return ResponseEntity.ok(
                departmentService.findAll()
                        .stream()
                        .map(DepartmentMapper::toResponse)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                DepartmentMapper.toResponse(departmentService.findById(id))
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<DepartmentResponse> create(
            @Valid @RequestBody DepartmentRequest request
    ) {
        Department saved = departmentService.save(DepartmentMapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DepartmentMapper.toResponse(saved));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request
    ) {
        Department existing = departmentService.findById(id);
        existing.setCode(request.code());
        existing.setName(request.name());
        return ResponseEntity.ok(
                DepartmentMapper.toResponse(departmentService.save(existing))
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}