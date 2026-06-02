package com.example.timetable.controller;

import com.example.timetable.controller.academic.EnrollmentController;
import com.example.timetable.dto.request.EnrollmentRequest;
import com.example.timetable.entity.*;
import com.example.timetable.mapper.EnrollmentMapper;
import com.example.timetable.service.EnrollmentService;
import com.example.timetable.service.SectionService;
import com.example.timetable.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EnrollmentController.class)
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnrollmentService enrollmentService;

    @MockBean
    private StudentService studentService;

    @MockBean
    private SectionService sectionService;

    private Student makeStudent(String name) {
        User user = new User();
        user.setFullName(name);
        user.setEmail(name.toLowerCase() + "@test.com");

        Department dept = new Department();
        dept.setName("CS");

        Student student = new Student();
        student.setId(1L);
        student.setUser(user);
        student.setDepartment(dept);
        return student;
    }

    private Section makeSection(String name) {
        Course course = new Course();
        course.setName("Algorithms");

        Section section = new Section();
        section.setId(1L);
        section.setName(name);
        section.setCourse(course);
        return section;
    }

    @Test
    void getAllReturnsEnrollments() throws Exception {
        Student student = makeStudent("Alice");
        Section section = makeSection("SEC-01");

        Enrollment enrollment = new Enrollment();
        enrollment.setId(1L);
        enrollment.setStudent(student);
        enrollment.setSection(section);
        enrollment.setStatus("ACTIVE");

        when(enrollmentService.findAll()).thenReturn(List.of(enrollment));

        mockMvc.perform(get("/api/enrollments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentName").value("Alice"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void createReturns201() throws Exception {
        Student student = makeStudent("Bob");
        Section section = makeSection("SEC-02");

        when(studentService.findById(1L)).thenReturn(student);
        when(sectionService.findById(1L)).thenReturn(section);

        Enrollment enrollment = EnrollmentMapper.toEntity(
                new EnrollmentRequest(1L, 1L, "ACTIVE"), student, section
        );
        enrollment.setId(1L);

        when(enrollmentService.save(any())).thenReturn(enrollment);

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "studentId": 1,
                                    "sectionId": 1,
                                    "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentName").value("Bob"));
    }
}
