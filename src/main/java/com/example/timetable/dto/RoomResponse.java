package com.example.timetable.dto;

public record RoomResponse(
        Long id,
        String roomNumber,
        Integer capacity
) {}
