package com.example.timetable.mapper;

import com.example.timetable.dto.request.RoomRequest;
import com.example.timetable.dto.response.RoomResponse;
import com.example.timetable.entity.Room;

public class RoomMapper {

    // Convert request DTO to entity
    public static Room toEntity(RoomRequest request) {

        Room room = new Room();

        room.setBuilding(request.building());
        room.setRoomNumber(request.roomNumber());
        room.setCapacity(request.capacity());
        room.setRoomType(request.roomType());

        return room;
    }

    // Convert entity to response DTO
    public static RoomResponse toResponse(Room room) {

        return new RoomResponse(
                room.getId(),
                room.getBuilding(),
                room.getRoomNumber(),
                room.getCapacity(),
                room.getRoomType()
        );
    }
}