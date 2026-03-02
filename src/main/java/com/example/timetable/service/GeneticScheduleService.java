package com.example.timetable.service;

import com.example.timetable.entity.*;
import com.example.timetable.entity.enums.ScheduleStatus;

import com.example.timetable.repository.ScheduleRepository;
import com.example.timetable.repository.ScheduleEntryRepository;
import com.example.timetable.repository.SectionRepository;
import com.example.timetable.repository.RoomRepository;
import com.example.timetable.repository.TimeSlotRepository;
import com.example.timetable.repository.SemesterRepository;

import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.Gene;
import com.example.timetable.scheduling.algorithm.GeneticAlgorithm;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class GeneticScheduleService {

    private final GeneticAlgorithm geneticAlgorithm;
    private final SectionRepository sectionRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleEntryRepository scheduleEntryRepository;
    private final SemesterRepository semesterRepository;

    public Schedule generate(Long semesterId) {

        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new NoSuchElementException("Semester not found: " + semesterId));

        Optional<Schedule> previousOpt = scheduleRepository.findTopBySemesterOrderByCreatedAtDesc(semester);

        Map<Long, ScheduleEntry> lockedEntries = new HashMap<>();

        if (previousOpt.isPresent()) {

            Schedule previous = previousOpt.get();

            List<ScheduleEntry> lockedList = scheduleEntryRepository.findByScheduleIdAndLockedTrue(previous.getId());

            for (ScheduleEntry entry : lockedList) {
                lockedEntries.put(entry.getSection().getId(), entry);
            }
        }

        List<Section> sections = sectionRepository.findAll();
        List<Room> rooms = roomRepository.findAll();
        List<TimeSlot> slots = timeSlotRepository.findAll();

        Chromosome best = geneticAlgorithm.evolveWithLocks(
                sections,
                rooms,
                slots,
                lockedEntries);

        Schedule schedule = new Schedule();
        schedule.setSemester(semester);
        schedule.setStatus(ScheduleStatus.DRAFT);
        schedule.setFitnessScore(best.getFitness());
        schedule.setHardViolations(best.getHardViolations());
        schedule.setSoftViolations(best.getSoftViolations());

        for (Gene gene : best.getGenes()) {

            ScheduleEntry entry = new ScheduleEntry();
            entry.setSchedule(schedule);
            entry.setSection(gene.getSection());
            entry.setRoom(gene.getRoom());
            entry.setTimeSlot(gene.getTimeSlot());
            entry.setLocked(gene.isLocked());

            schedule.getEntries().add(entry);
        }

        return scheduleRepository.save(schedule);
    }
}