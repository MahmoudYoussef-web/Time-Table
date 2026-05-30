package com.example.timetable.repository;

import com.example.timetable.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email (used for login & initialization)
    Optional<User> findByEmail(String email);

    // Check if email already exists
    boolean existsByEmail(String email);
}
