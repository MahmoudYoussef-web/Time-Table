package com.example.timetable.mapper;

import com.example.timetable.dto.request.DepartmentRequest;
import com.example.timetable.dto.response.DepartmentResponse;
import com.example.timetable.entity.Department;

public class DepartmentMapper {

    public static Department toEntity(DepartmentRequest request) {
        Department department = new Department();
        department.setCode(request.code());
        department.setName(request.name());
        return department;
    }

    public static DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getCode(),
                department.getName()
        );
    }
}