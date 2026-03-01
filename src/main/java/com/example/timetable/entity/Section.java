package com.example.timetable.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "sections")
@Getter
@Setter
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Section name (e.g. A, B, C, Lab1)
    @Column(nullable = false, length = 50)
    private String name;

    // Course of this section
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // Instructor of this section
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id", nullable = false)
    private Instructor instructor;

    // Maximum number of students
    @Column(nullable = false)
    private int capacity;

    // Enrolled students
    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY)
    private List<Enrollment> enrollments;

    // Schedule entries of this section
    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY)
    private List<ScheduleEntry> scheduleEntries;

    // Exams of this section
    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY)
    private List<Exam> exams;

    // Announcements of this section
    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY)
    private List<Announcement> announcements;
}
