package com.example.timetable.service;

import com.example.timetable.model.Room;
import com.example.timetable.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    public Room save(Room room) {
        return roomRepository.save(room);
    }

    public Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Room not found with id: " + id));
    }

    public void deleteById(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new NoSuchElementException("Room not found with id: " + id);
        }
        roomRepository.deleteById(id);
    }
}
