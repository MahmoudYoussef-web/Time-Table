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

        // Set section name
        section.setName(request.name());

        // Set course
        section.setCourse(course);

        // Set instructor
        section.setInstructor(instructor);

        // Set capacity
        section.setCapacity(request.capacity());

        return section;
    }

    // Convert entity to response
    public static SectionResponse toResponse(Section section) {

        return new SectionResponse(
                section.getId(),
                section.getName(),
                section.getCourse().getCode(),
                section.getCourse().getName(),
                section.getInstructor().getUser().getFullName(),
                section.getCapacity()
        );
    }
}
