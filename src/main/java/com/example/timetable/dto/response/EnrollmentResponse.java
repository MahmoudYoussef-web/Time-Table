package com.example.timetable.dto.response;

public record EnrollmentResponse(

        Long id,

        // Student info
        String studentName,
        String studentEmail,

        // Section info
        String sectionName,
        String courseName,

        // Enrollment status
        String status,

        // Final grade
        String grade
) {}
