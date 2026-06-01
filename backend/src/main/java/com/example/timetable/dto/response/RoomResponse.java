package com.example.timetable.dto.response;


import com.example.timetable.entity.enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoomResponse {

    private Long id;
    private String building;
    private String roomNumber;
    private int capacity;
    private RoomType roomType;
}