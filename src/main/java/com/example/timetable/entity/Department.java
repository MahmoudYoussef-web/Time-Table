package com.example.timetable.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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


    @Column(nullable = false, unique = true, length = 20)
    private String code;


    @Column(nullable = false, length = 150)
    private String name;


    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<Student> students;


    @OneToMany(mappedBy = "department")
    @JsonIgnore
    private List<Course> courses;

    @OneToMany(mappedBy = "department")
    @JsonIgnore
    private List<Instructor> instructors;
}
