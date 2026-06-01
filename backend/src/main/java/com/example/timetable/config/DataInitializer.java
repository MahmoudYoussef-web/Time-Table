package com.example.timetable.config;

import com.example.timetable.entity.User;
import com.example.timetable.entity.enums.UserRole;
import com.example.timetable.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // Create default admin if not exists
        if (userRepository.findByEmail("admin@system.com").isEmpty()) {

            User admin = new User();

            // Set admin full name
            admin.setFullName("System Admin");

            // Set admin email
            admin.setEmail("admin@system.com");

            // Encode password
            admin.setPassword(
                    passwordEncoder.encode("admin123")
            );

            // Set role
            admin.setRole(UserRole.ADMIN);

            // Enable account
            admin.setEnabled(true);

            userRepository.save(admin);
        }
    }
}
