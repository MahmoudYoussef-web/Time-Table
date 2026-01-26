package com.example.timetable.mapper;

import com.example.timetable.dto.ClassSectionRequest;
import com.example.timetable.dto.ClassSectionResponse;
import com.example.timetable.model.ClassSection;
import com.example.timetable.model.Course;
import com.example.timetable.model.Instructor;

public class ClassSectionMapper {

    public static ClassSection toEntity(
            ClassSectionRequest request,
            Course course,
            Instructor instructor
    ) {
        ClassSection section = new ClassSection();
        section.setCourse(course);
        section.setInstructor(instructor);
        return section;
    }

    public static ClassSectionResponse toResponse(ClassSection section) {
        return new ClassSectionResponse(
                section.getId(),
                section.getCourse().getCode(),
                section.getCourse().getName(),
                section.getInstructor().getName()
        );
    }
}
