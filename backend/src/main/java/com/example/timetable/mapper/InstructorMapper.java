package com.example.timetable.mapper;

import com.example.timetable.dto.request.InstructorRequest;
import com.example.timetable.dto.response.InstructorResponse;
import com.example.timetable.entity.Department;
import com.example.timetable.entity.Instructor;
import com.example.timetable.entity.User;
import com.example.timetable.entity.enums.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;

public class InstructorMapper {

    public static Instructor toEntity(
            InstructorRequest request,
            Department department,
            PasswordEncoder passwordEncoder
    ) {
        User user = new User();
        user.setFullName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.INSTRUCTOR);
        user.setEnabled(true);

        Instructor instructor = new Instructor();
        instructor.setUser(user);
        instructor.setDepartment(department);

        user.setInstructor(instructor);

        return instructor;
    }

    public static InstructorResponse toResponse(Instructor instructor) {
        String departmentName = null;
        if (instructor.getDepartment() != null) {
            departmentName = instructor.getDepartment().getName();
        }

        return new InstructorResponse(
                instructor.getId(),
                instructor.getUser().getFullName(),
                instructor.getUser().getEmail(),
                departmentName
        );
    }
}