package com.example.timetable.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private boolean success;
    private String message;
    private String token;
    private UserResponse user;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserResponse {
        private Long id;
        private String email;
    }
}
