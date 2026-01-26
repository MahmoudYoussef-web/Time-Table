package com.example.timetable.mapper;

import com.example.timetable.dto.CourseRequest;
import com.example.timetable.dto.CourseResponse;
import com.example.timetable.model.Course;

public class CourseMapper {

    public static Course toEntity(CourseRequest request) {
        Course course = new Course();
        course.setCode(request.code());
        course.setName(request.name());
        return course;
    }

    public static CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getCode(),
                course.getName()
        );
    }
}
