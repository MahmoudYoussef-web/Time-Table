package com.example.timetable.mapper;

import com.example.timetable.dto.request.StudentRequest;
import com.example.timetable.dto.response.StudentResponse;
import com.example.timetable.entity.Department;
import com.example.timetable.entity.Student;
import com.example.timetable.entity.User;

public class StudentMapper {

    // Convert request to entity
    public static Student toEntity(
            StudentRequest request,
            User user,
            Department department
    ) {

        Student student = new Student();

        // Set linked user
        student.setUser(user);

        // Set academic year
        student.setAcademicYear(request.academicYear());

        // Set study level
        student.setLevel(request.level());

        // Set department
        student.setDepartment(department);

        return student;
    }

    // Convert entity to response
    public static StudentResponse toResponse(Student student) {

        return new StudentResponse(
                student.getId(),
                student.getUser().getFullName(),
                student.getUser().getEmail(),
                student.getAcademicYear(),
                student.getLevel(),
                student.getDepartment().getName()
        );
    }
}
