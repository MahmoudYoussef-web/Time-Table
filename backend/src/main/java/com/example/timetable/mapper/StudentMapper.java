package com.example.timetable.mapper;

import com.example.timetable.dto.request.StudentRequest;
import com.example.timetable.dto.response.StudentResponse;
import com.example.timetable.entity.Department;
import com.example.timetable.entity.Student;
import com.example.timetable.entity.User;
import com.example.timetable.entity.enums.UserRole;

public class StudentMapper {

    public static User toUser(StudentRequest request) {
        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setRole(UserRole.STUDENT);
        user.setEnabled(true);
        return user;
    }

    public static Student toEntity(
            StudentRequest request,
            User user,
            Department department
    ) {
        Student student = new Student();
        student.setUser(user);
        student.setAcademicYear(request.academicYear());
        student.setLevel(request.level());
        student.setDepartment(department);
        return student;
    }

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
