package com.example.timetable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
public class TimetableSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimetableSchedulerApplication.class, args);
    }
}
