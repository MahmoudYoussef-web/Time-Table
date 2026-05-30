package com.example.timetable.entity;

import com.example.timetable.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email")
        }
)
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // الاسم الحقيقي
    @Column(nullable = false, length = 150)
    private String fullName;

    // للإيميل (Login)
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // Password مش Raw (لازم Hash)
    @Column(nullable = false)
    private String password;

    // دور المستخدم
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    // مفعل ولا لا
    @Column(nullable = false)
    private Boolean enabled = true;

    // تاريخ الإنشاء
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // آخر Login
    private LocalDateTime lastLoginAt;

    // علاقة مع Student
    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private Student student;

    // علاقة مع Instructor
    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private Instructor instructor;

    // Auto timestamp
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

