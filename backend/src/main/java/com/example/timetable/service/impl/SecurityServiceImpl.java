package com.example.timetable.service.impl;

import com.example.timetable.entity.User;
import com.example.timetable.service.SecurityService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityServiceImpl implements SecurityService {

    @Override
    public String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Override
    public boolean isCurrentUserAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @Override
    public void checkInstructorAccess(User instructorUser) {
        if (!isCurrentUserAdmin() && (instructorUser == null || !getCurrentUserEmail().equals(instructorUser.getEmail()))) {
            throw new AccessDeniedException("You can only manage your own data");
        }
    }

    @Override
    public void requireRole(String... roles) {
        var authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        for (String role : roles) {
            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role))) {
                return;
            }
        }
        throw new AccessDeniedException("Requires one of roles: " + String.join(", ", roles));
    }
}