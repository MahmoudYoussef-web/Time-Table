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

import java.util.List;
import java.util.NoSuchElementException;

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

        List<Section> sections = sectionRepository.findAll();
        List<Room> rooms = roomRepository.findAll();
        List<TimeSlot> slots = timeSlotRepository.findAll();

        Chromosome best =
                geneticAlgorithm.evolve(sections, rooms, slots);

        Schedule schedule = new Schedule();
        schedule.setSemester(semester);
        schedule.setFitnessScore(best.getFitness());
        schedule.setHardViolations(best.getHardViolations());
        schedule.setSoftViolations(best.getSoftViolations());

        for (Gene gene : best.getGenes()) {

            ScheduleEntry entry = new ScheduleEntry();

            entry.setSchedule(schedule);
            entry.setSection(gene.getSection());


            entry.setInstructor(
                    gene.getSection().getInstructor()
            );

            entry.setRoom(gene.getRoom());
            entry.setTimeSlot(gene.getTimeSlot());


            entry.setType("LECTURE");

            schedule.getEntries().add(entry);
        }

        return scheduleRepository.save(schedule);
    }
}