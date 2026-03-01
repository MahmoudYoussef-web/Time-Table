package com.example.timetable.service;

import com.example.timetable.dto.request.RoomRequest;
import com.example.timetable.dto.response.RoomResponse;

import java.util.List;

public interface RoomService {

    List<RoomResponse> findAll();

    RoomResponse findById(Long id);

    RoomResponse save(RoomRequest request);

    void deleteById(Long id);
}
