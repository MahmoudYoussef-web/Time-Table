package com.example.timetable.service.impl;

import com.example.timetable.dto.request.RoomRequest;
import com.example.timetable.dto.response.RoomResponse;
import com.example.timetable.entity.Room;
import com.example.timetable.mapper.RoomMapper;
import com.example.timetable.repository.RoomRepository;
import com.example.timetable.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    // Get all rooms
    @Override
    public List<RoomResponse> findAll() {

        return roomRepository.findAll()
                .stream()
                .map(RoomMapper::toResponse)
                .toList();
    }

    // Get room by id
    @Override
    public RoomResponse findById(Long id) {

        Room room = roomRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Room not found with id: " + id
                        ));

        return RoomMapper.toResponse(room);
    }

    // Create room
    @Override
    public RoomResponse save(RoomRequest request) {

        Room room = RoomMapper.toEntity(request);

        Room saved = roomRepository.save(room);

        return RoomMapper.toResponse(saved);
    }

    @Override
    public RoomResponse update(Long id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Room not found with id: " + id
                        ));

        room.setBuilding(request.building());
        room.setRoomNumber(request.roomNumber());
        room.setCapacity(request.capacity());

        return RoomMapper.toResponse(roomRepository.save(room));
    }

    // Delete room
    @Override
    public void deleteById(Long id) {

        if (!roomRepository.existsById(id)) {
            throw new NoSuchElementException(
                    "Room not found with id: " + id
            );
        }

        roomRepository.deleteById(id);
    }
}
