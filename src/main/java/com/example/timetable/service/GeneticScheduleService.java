package com.example.timetable.service;

import com.example.timetable.entity.*;
import com.example.timetable.repository.*;
import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.Gene;
import com.example.timetable.scheduling.algorithm.GeneticAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.*;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class GeneticScheduleService {

    private final GeneticAlgorithm geneticAlgorithm;
    private final SectionRepository sectionRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ScheduleRepository scheduleRepository;
    private final SemesterRepository semesterRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Schedule generate(Long semesterId) {

        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new NoSuchElementException("Semester not found"));

        List<Section> sections = sectionRepository.findBySemester_Id(semesterId);

        if (sections.isEmpty()) {
            throw new IllegalStateException("No sections found for this semester");
        }

        List<Room> rooms = roomRepository.findAll();
        List<TimeSlot> slots = timeSlotRepository.findAll();

        Chromosome best = geneticAlgorithm.evolve(sections, rooms, slots);

        Schedule schedule = new Schedule();
        schedule.setSemester(semester);
        schedule.setFitnessScore(best.getFitness());
        schedule.setHardViolations(best.getHardViolations());
        schedule.setSoftViolations(best.getSoftViolations());

        schedule.setEntries(new ArrayList<>());

        Set<String> instructorSlot = new HashSet<>();
        Set<String> roomSlot = new HashSet<>();
        Set<String> sectionSlot = new HashSet<>();

        for (Gene gene : best.getGenes()) {

            Instructor instructor = gene.getSection().getInstructor();
            Room room = gene.getRoom();
            TimeSlot slot = gene.getTimeSlot();
            Section section = gene.getSection();

            String instructorKey = instructor.getId() + "-" + slot.getId();
            String roomKey = room.getId() + "-" + slot.getId();
            String sectionKey = section.getId() + "-" + slot.getId();

            if (instructorSlot.contains(instructorKey)) {
                continue;
            }

            if (roomSlot.contains(roomKey)) {
                continue;
            }

            if (sectionSlot.contains(sectionKey)) {
                continue;
            }

            instructorSlot.add(instructorKey);
            roomSlot.add(roomKey);
            sectionSlot.add(sectionKey);

            ScheduleEntry entry = new ScheduleEntry();

            entry.setSchedule(schedule);
            entry.setSection(section);
            entry.setInstructor(instructor);
            entry.setRoom(room);
            entry.setTimeSlot(slot);
            entry.setType("LECTURE");

            schedule.getEntries().add(entry);
        }

        return scheduleRepository.save(schedule);
    }
}