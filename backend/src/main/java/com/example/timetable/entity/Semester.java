package com.example.timetable.entity;

import com.example.timetable.entity.enums.SemesterStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "semesters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SemesterStatus status = SemesterStatus.DRAFT;

    @OneToMany(mappedBy = "semester",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Schedule> schedules = new ArrayList<>();

    @PrePersist
    @PreUpdate
    private void validateDates() {

        if (startDate == null || endDate == null) {
            throw new IllegalStateException("Semester dates must not be null");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalStateException("Semester end date must be after start date");
        }
    }
}