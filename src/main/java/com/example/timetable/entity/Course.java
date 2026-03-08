package com.example.timetable.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(
        name = "courses",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "code")
        }
)
@Getter
@Setter
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Course unique code (e.g. CS101)
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    // Course name (e.g. Data Structures)
    @Column(nullable = false, length = 150)
    private String name;

    // Number of credit hours
    @Column(nullable = false)
    private int creditHours;

    // Department that owns this course
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    // Sections of this course
    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Section> sections;
}
