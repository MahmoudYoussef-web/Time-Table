package com.example.timetable.auth.service;

import com.example.timetable.auth.dto.AuthResponse;
import com.example.timetable.auth.dto.LoginRequest;
import com.example.timetable.auth.dto.RegisterRequest;
import com.example.timetable.auth.model.Role;
import com.example.timetable.auth.model.User;
import com.example.timetable.auth.repository.UserRepository;
import com.example.timetable.auth.security.JwtUtil;
import com.example.timetable.exception.InvalidCredentialsException;
import com.example.timetable.exception.UserAlreadyExistsException;
import com.example.timetable.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.INSTRUCTOR); // default role
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        String token = jwtUtil.generateToken(
                savedUser.getEmail(),
                savedUser.getRole().name()
        );

        AuthResponse.UserResponse userResponse =
                new AuthResponse.UserResponse(
                        savedUser.getId(),
                        savedUser.getEmail()
                );

        return new AuthResponse(
                true,
                "Registration successful",
                token,
                userResponse
        );
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new UserNotFoundException("Invalid email or password")
                );

        if (!user.isEnabled()) {
            throw new InvalidCredentialsException("Account is disabled");
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        AuthResponse.UserResponse userResponse =
                new AuthResponse.UserResponse(
                        user.getId(),
                        user.getEmail()
                );

        return new AuthResponse(
                true,
                "Login successful",
                token,
                userResponse
        );
    }
}
