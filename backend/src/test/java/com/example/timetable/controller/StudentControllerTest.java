package com.example.timetable.controller;

import com.example.timetable.controller.academic.StudentController;
import com.example.timetable.dto.request.StudentRequest;
import com.example.timetable.entity.Department;
import com.example.timetable.entity.Student;
import com.example.timetable.entity.User;
import com.example.timetable.mapper.StudentMapper;
import com.example.timetable.repository.UserRepository;
import com.example.timetable.service.DepartmentService;
import com.example.timetable.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private DepartmentService departmentService;

    @Test
    void getAllReturnsStudentList() throws Exception {
        User user = new User();
        user.setFullName("Test Student");
        user.setEmail("test@test.com");

        Department dept = new Department();
        dept.setName("CS");

        Student student = new Student();
        student.setId(1L);
        student.setUser(user);
        student.setAcademicYear("2025/2026");
        student.setLevel(3);
        student.setDepartment(dept);

        when(studentService.findAll()).thenReturn(List.of(student));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("Test Student"))
                .andExpect(jsonPath("$[0].academicYear").value("2025/2026"));
    }

    @Test
    void getByIdReturnsStudent() throws Exception {
        User user = new User();
        user.setFullName("Alice");
        user.setEmail("alice@test.com");

        Department dept = new Department();
        dept.setName("Math");

        Student student = new Student();
        student.setId(1L);
        student.setUser(user);
        student.setAcademicYear("2025/2026");
        student.setLevel(2);
        student.setDepartment(dept);

        when(studentService.findById(1L)).thenReturn(student);

        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Alice"))
                .andExpect(jsonPath("$.level").value(2));
    }

    @Test
    void createReturns201() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setFullName("Bob");
        user.setEmail("bob@test.com");

        Department dept = new Department();
        dept.setId(1L);
        dept.setName("CS");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(departmentService.findById(1L)).thenReturn(dept);

        Student student = StudentMapper.toEntity(
                new StudentRequest(1L, "2025/2026", 3, 1L), user, dept
        );
        student.setId(1L);

        when(studentService.save(any())).thenReturn(student);

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": 1,
                                    "academicYear": "2025/2026",
                                    "level": 3,
                                    "departmentId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Bob"));
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/students/1"))
                .andExpect(status().isNoContent());
    }
}
