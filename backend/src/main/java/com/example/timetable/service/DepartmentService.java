package com.example.timetable.service;

import com.example.timetable.entity.Department;

import java.util.List;

public interface DepartmentService {

    List<Department> findAll();

    Department findById(Long id);

    Department save(Department department);

    void deleteById(Long id);
}
