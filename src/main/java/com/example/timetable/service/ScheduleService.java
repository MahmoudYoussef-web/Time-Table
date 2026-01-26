package com.example.timetable.service;

import com.example.timetable.dto.ScheduleDTO;
import com.example.timetable.mapper.ScheduleMapper;
import com.example.timetable.model.*;
import com.example.timetable.repository.*;
import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.GeneticAlgorithm;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private static final Logger log =
            LoggerFactory.getLogger(ScheduleService.class);

    private final GeneticAlgorithm geneticAlgorithm;

    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ClassSectionRepository classSectionRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleEntryRepository scheduleEntryRepository;

    /**
     * Generates a new schedule using Genetic Algorithm
     */
    public Schedule generateSchedule() {

        List<ClassSection> sections = classSectionRepository.findAll();
        List<Room> rooms = roomRepository.findAll();
        List<TimeSlot> timeSlots = timeSlotRepository.findAll();

        if (sections.isEmpty() || rooms.isEmpty() || timeSlots.isEmpty()) {
            throw new IllegalStateException("Not enough data to generate schedule");
        }

        Chromosome bestChromosome =
                geneticAlgorithm.evolve(sections, rooms, timeSlots);

        Schedule schedule = new Schedule();
        schedule.setFitnessScore(bestChromosome.getFitness());
        schedule.setHardViolations(bestChromosome.getHardViolations());
        schedule.setSoftViolations(bestChromosome.getSoftViolations());

        Schedule savedSchedule = scheduleRepository.save(schedule);

        bestChromosome.getGenes().forEach(gene -> {
            ScheduleEntry entry = new ScheduleEntry();
            entry.setSchedule(savedSchedule);
            entry.setClassSection(gene.getClassSection());
            entry.setRoom(gene.getRoom());
            entry.setTimeSlot(gene.getTimeSlot());
            scheduleEntryRepository.save(entry);
        });

        return savedSchedule;
    }

    /**
     * Fetch schedule as DTO
     */
    public ScheduleDTO getScheduleById(Long id) {

        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Schedule not found with id: " + id));

        schedule.getEntries().sort(
                Comparator.comparing(
                        ScheduleEntry::getTimeSlot,
                        Comparator.comparing(TimeSlot::getDayOfWeek)
                                .thenComparing(TimeSlot::getStartTime)
                )
        );

        return ScheduleMapper.toDTO(schedule);
    }

    /**
     * ✅ Validate existing schedule (Explainable Validation)
     */
    public ScheduleDTO validateSchedule(Long id) {

        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Schedule not found with id: " + id));

        // Validation here is simply exposing stored fitness breakdown
        return ScheduleMapper.toDTO(schedule);
    }
}
