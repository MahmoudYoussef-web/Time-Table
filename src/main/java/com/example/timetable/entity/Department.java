package com.example.timetable.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(
        name = "departments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "code")
        }
)
@Getter
@Setter
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // مثال: CS, IT, IS
    @Column(nullable = false, unique = true, length = 20)
    private String code;

    // مثال: Computer Science
    @Column(nullable = false, length = 150)
    private String name;

    // علاقة مع الطلاب
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<Student> students;

    // علاقة مع الدكاترة
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<Instructor> instructors;

    // علاقة مع المواد
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<Course> courses;
}
