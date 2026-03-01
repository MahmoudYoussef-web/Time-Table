package com.example.timetable.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "instructors")
@Getter
@Setter
public class Instructor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Linked user account
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Instructor specialization
    @Column(length = 150)
    private String specialization;

    // Department of the instructor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    // Sections taught by this instructor
    @OneToMany(mappedBy = "instructor", fetch = FetchType.LAZY)
    private List<Section> sections;

    // Schedule entries for this instructor
    @OneToMany(mappedBy = "instructor", fetch = FetchType.LAZY)
    private List<ScheduleEntry> scheduleEntries;
}
