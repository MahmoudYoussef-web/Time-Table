package com.example.timetable.controller;

import com.example.timetable.dto.RoomRequest;
import com.example.timetable.dto.RoomResponse;
import com.example.timetable.mapper.RoomMapper;
import com.example.timetable.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER','INSTRUCTOR')")
    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        return ResponseEntity.ok(
                roomService.findAll()
                        .stream()
                        .map(RoomMapper::toResponse)
                        .toList()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER','INSTRUCTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(
                RoomMapper.toResponse(roomService.findById(id))
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','SCHEDULER')")
    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(
            @Valid @RequestBody RoomRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        RoomMapper.toResponse(
                                roomService.save(RoomMapper.toEntity(request))
                        )
                );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
