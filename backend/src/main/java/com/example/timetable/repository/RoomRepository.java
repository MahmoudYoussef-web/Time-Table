package com.example.timetable.repository;

import com.example.timetable.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByRoomNumber(String roomNumber);

    Optional<Room> findByBuildingAndRoomNumber(String building, String roomNumber);
}
