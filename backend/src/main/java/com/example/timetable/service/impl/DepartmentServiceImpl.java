package com.example.timetable.service.impl;

import com.example.timetable.entity.Department;
import com.example.timetable.repository.DepartmentRepository;
import com.example.timetable.service.DepartmentService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    @Override
    public Department findById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Department not found with id: " + id
                        )
                );
    }

    @Override
    public Department save(Department department) {
        return departmentRepository.save(department);
    }

    @Override
    public void deleteById(Long id) {

        if (!departmentRepository.existsById(id)) {
            throw new NoSuchElementException(
                    "Department not found with id: " + id
            );
        }

        departmentRepository.deleteById(id);
    }
}
