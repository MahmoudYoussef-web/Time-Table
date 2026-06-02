package com.example.timetable.auth.service;

import com.example.timetable.auth.dto.AuthResponse;
import com.example.timetable.auth.dto.LoginRequest;
import com.example.timetable.auth.dto.PasswordChangeRequest;
import com.example.timetable.auth.dto.ProfileUpdateRequest;
import com.example.timetable.auth.dto.RegisterRequest;
import com.example.timetable.auth.jwt.JwtUtil;
import com.example.timetable.entity.User;
import com.example.timetable.entity.enums.UserRole;
import com.example.timetable.exception.InvalidCredentialsException;
import com.example.timetable.exception.UserAlreadyExistsException;
import com.example.timetable.exception.UserNotFoundException;
import com.example.timetable.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // =========================
    // Register
    // =========================
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.INSTRUCTOR);
        user.setEnabled(true);
        user.setLastLoginAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(
                savedUser.getEmail(),
                savedUser.getRole().name()
        );

        return new AuthResponse(
                true,
                "Registration successful",
                token,
                new AuthResponse.UserResponse(
                        savedUser.getId(),
                        savedUser.getEmail()
                )
        );
    }

    // =========================
    // Update Profile
    // =========================
    public void updateProfile(ProfileUpdateRequest request) {
        User user = getCurrentUser();
        user.setFullName(request.getFullName());
        userRepository.save(user);
    }

    // =========================
    // Change Password
    // =========================
    public void changePassword(PasswordChangeRequest request) {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    // =========================
    // Login
    // =========================
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new UserNotFoundException("Invalid email or password")
                );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return new AuthResponse(
                true,
                "Login successful",
                token,
                new AuthResponse.UserResponse(
                        user.getId(),
                        user.getEmail()
                )
        );
    }
}
