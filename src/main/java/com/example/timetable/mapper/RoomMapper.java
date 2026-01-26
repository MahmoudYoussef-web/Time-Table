package com.example.timetable.mapper;

import com.example.timetable.dto.RoomRequest;
import com.example.timetable.dto.RoomResponse;
import com.example.timetable.model.Room;

public class RoomMapper {

    public static Room toEntity(RoomRequest request) {
        Room room = new Room();
        room.setRoomNumber(request.roomNumber());
        room.setCapacity(request.capacity());
        return room;
    }

    public static RoomResponse toResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getRoomNumber(),
                room.getCapacity()
        );
    }
}
