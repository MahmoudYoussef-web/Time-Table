package com.example.timetable.mapper;

import com.example.timetable.dto.InstructorRequest;
import com.example.timetable.dto.InstructorResponse;
import com.example.timetable.model.Instructor;

public class InstructorMapper {

    public static Instructor toEntity(InstructorRequest request) {
        Instructor instructor = new Instructor();
        instructor.setName(request.name());
        return instructor;
    }

    public static InstructorResponse toResponse(Instructor instructor) {
        return new InstructorResponse(
                instructor.getId(),
                instructor.getName()
        );
    }
}
