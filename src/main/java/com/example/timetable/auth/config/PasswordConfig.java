package com.example.timetable.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    // BCrypt encoder for hashing passwords

    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
