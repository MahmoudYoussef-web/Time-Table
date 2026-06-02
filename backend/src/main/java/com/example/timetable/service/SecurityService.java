package com.example.timetable.service;

import com.example.timetable.entity.User;
import org.springframework.security.access.AccessDeniedException;

public interface SecurityService {

    String getCurrentUserEmail();

    boolean isCurrentUserAdmin();

    void checkInstructorAccess(User instructorUser);

    void requireRole(String... roles);
}