package com.example.timetable.mapper;

import com.example.timetable.dto.request.SectionRequest;
import com.example.timetable.dto.response.SectionResponse;
import com.example.timetable.entity.Course;
import com.example.timetable.entity.Instructor;
import com.example.timetable.entity.Section;
import com.example.timetable.entity.Semester;

public class SectionMapper {

    public static Section toEntity(
            SectionRequest request,
            Course course,
            Instructor instructor,
            Semester semester
    ) {
        Section section = new Section();
        section.setName(request.name());
        section.setCourse(course);
        section.setInstructor(instructor);
        section.setSemester(semester);
        section.setCapacity(request.capacity());
        section.setYearLevel(request.yearLevel());
        section.setSessionType(request.sessionType());
        return section;
    }

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
                section.getCapacity(),
                section.getYearLevel(),
                section.getSessionType()
        );
    }
}