package com.example.timetable.mapper;

import com.example.timetable.dto.request.EnrollmentRequest;
import com.example.timetable.dto.response.EnrollmentResponse;
import com.example.timetable.entity.Enrollment;
import com.example.timetable.entity.Section;
import com.example.timetable.entity.Student;

public class EnrollmentMapper {

    // Convert request to entity
    public static Enrollment toEntity(
            EnrollmentRequest request,
            Student student,
            Section section
    ) {

        Enrollment enrollment = new Enrollment();

        // Set student
        enrollment.setStudent(student);

        // Set section
        enrollment.setSection(section);

        // Set status
        enrollment.setStatus(request.status());

        return enrollment;
    }

    // Convert entity to response
    public static EnrollmentResponse toResponse(Enrollment enrollment) {

        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getStudent().getUser().getFullName(),
                enrollment.getStudent().getUser().getEmail(),
                enrollment.getSection().getName(),
                enrollment.getSection().getCourse().getName(),
                enrollment.getStatus(),
                enrollment.getGrade()
        );
    }
}
