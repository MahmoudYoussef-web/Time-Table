package com.example.timetable.dto.request;

public record InstructorRequest(

        String name,
        String email,
        String password,
        Long departmentId

) {}