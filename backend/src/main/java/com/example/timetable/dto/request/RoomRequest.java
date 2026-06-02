package com.example.timetable.dto.request;

import com.example.timetable.entity.enums.RoomType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RoomRequest(

        @NotBlank(message = "Building is required")
        String building,

        @NotBlank(message = "Room number is required")
        String roomNumber,

        @NotNull(message = "Capacity is required")
        @Min(value = 1, message = "Capacity must be at least 1")
        Integer capacity,

        @NotNull(message = "Room type is required")
        RoomType roomType
) {
}

