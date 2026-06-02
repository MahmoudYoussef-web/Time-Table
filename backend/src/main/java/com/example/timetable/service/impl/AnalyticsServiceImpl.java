package com.example.timetable.service.impl;

import com.example.timetable.dto.response.AnalyticsResponse;
import com.example.timetable.dto.response.InstructorWorkloadDTO;
import com.example.timetable.dto.response.RoomUtilizationDTO;
import com.example.timetable.entity.Instructor;
import com.example.timetable.entity.Room;
import com.example.timetable.repository.*;
import com.example.timetable.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ScheduleRepository scheduleRepository;
    private final InstructorRepository instructorRepository;
    private final RoomRepository roomRepository;
    private final CourseRepository courseRepository;
    private final ScheduleEntryRepository scheduleEntryRepository;

    @Override
    public AnalyticsResponse compute() {
        long totalSchedules = scheduleRepository.count();
        long totalInstructors = instructorRepository.count();
        long totalRooms = roomRepository.count();
        long totalCourses = courseRepository.count();

        Double avgFitness = scheduleRepository.findAverageFitnessScore();
        double averageFitnessScore = avgFitness != null ? avgFitness : 0.0;

        Long totalHard = scheduleRepository.sumHardViolations();
        long totalHardViolations = totalHard != null ? totalHard : 0;

        List<RoomUtilizationDTO> roomUtilization = buildRoomUtilization();
        List<InstructorWorkloadDTO> instructorWorkload = buildInstructorWorkload();

        return new AnalyticsResponse(
            totalSchedules, totalInstructors, totalRooms, totalCourses,
            averageFitnessScore, totalHardViolations,
            roomUtilization, instructorWorkload
        );
    }

    private List<RoomUtilizationDTO> buildRoomUtilization() {
        long totalTimeSlots = 50;
        return roomRepository.findAll().stream()
            .map(room -> {
                long count = scheduleEntryRepository.countByRoomId(room.getId());
                double pct = totalTimeSlots > 0 ? (count * 100.0 / totalTimeSlots) : 0;
                return new RoomUtilizationDTO(
                    room.getBuilding() + "-" + room.getRoomNumber(),
                    room.getCapacity(),
                    count,
                    Math.min(pct, 100.0)
                );
            })
            .toList();
    }

    private List<InstructorWorkloadDTO> buildInstructorWorkload() {
        return instructorRepository.findAll().stream()
            .map(instructor -> {
                long sectionCount = instructor.getSections() != null
                    ? instructor.getSections().size()
                    : 0;
                double estimatedHours = sectionCount * 3.0;
                String name = instructor.getUser() != null ? instructor.getUser().getFullName() : "Unknown";
                return new InstructorWorkloadDTO(
                    name,
                    sectionCount,
                    estimatedHours
                );
            })
            .toList();
    }
}

