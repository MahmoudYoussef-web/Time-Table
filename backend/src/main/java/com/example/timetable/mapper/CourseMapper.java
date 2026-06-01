package com.example.timetable.mapper;

import com.example.timetable.dto.request.CourseRequest;
import com.example.timetable.dto.response.CourseResponse;
import com.example.timetable.entity.Course;
import com.example.timetable.entity.Department;

public class CourseMapper {

    // Convert request to entity
    public static Course toEntity(CourseRequest request, Department department) {

        Course course = new Course();

        // Set course code
        course.setCode(request.code());

        // Set course name
        course.setName(request.name());

        // Set credit hours
        course.setCreditHours(request.creditHours());

        // Set department
        course.setDepartment(department);

        return course;
    }

    // Convert entity to response
    public static CourseResponse toResponse(Course course) {

        return new CourseResponse(
                course.getId(),
                course.getCode(),
                course.getName(),
                course.getCreditHours(),
                course.getDepartment().getName()
        );
    }
}
