package com.example.timetable.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "courses",
        indexes = {
                @Index(name = "idx_course_code", columnList = "code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "credit_hours", nullable = false)
    private int creditHours;
}
