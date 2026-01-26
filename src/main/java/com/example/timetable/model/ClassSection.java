package com.example.timetable.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "class_sections",
        indexes = {
                @Index(name = "idx_course", columnList = "course_id"),
                @Index(name = "idx_instructor", columnList = "instructor_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"course", "instructor"})
public class ClassSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instructor_id", nullable = false)
    private Instructor instructor;

    @Column(name = "number_of_students", nullable = false)
    private int numberOfStudents;
}
