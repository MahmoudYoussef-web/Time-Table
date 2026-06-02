package com.example.timetable.dto.response;

public record RoomUtilizationDTO(
    String roomLabel,
    int capacity,
    long entriesCount,
    double utilizationPercent
) {}
