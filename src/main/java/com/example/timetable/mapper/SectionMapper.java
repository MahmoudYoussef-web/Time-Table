package com.example.timetable.mapper;

import com.example.timetable.dto.request.SectionRequest;
import com.example.timetable.dto.response.SectionResponse;
import com.example.timetable.entity.Course;
import com.example.timetable.entity.Instructor;
import com.example.timetable.entity.Section;

public class SectionMapper {

    // Convert request to entity
    public static Section toEntity(
            SectionRequest request,
            Course course,
            Instructor instructor
    ) {

        Section section = new Section();

        section.setName(request.name());
        section.setCourse(course);
        section.setInstructor(instructor);
        section.setCapacity(request.capacity());

        return section;
    }

    // Convert entity to response
    public static SectionResponse toResponse(Section section) {

        String instructorName = null;

        if (section.getInstructor() != null
                && section.getInstructor().getUser() != null) {

            instructorName =
                    section.getInstructor()
                            .getUser()
                            .getFullName();
        }

        return new SectionResponse(
                section.getId(),
                section.getName(),
                section.getCourse().getCode(),
                section.getCourse().getName(),
                instructorName,
                section.getCapacity()
        );
    }
}