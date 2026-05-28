package com.example.timetable.service;

import com.example.timetable.entity.*;
import com.example.timetable.entity.enums.SessionType;
import com.example.timetable.repository.*;
import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.Gene;
import com.example.timetable.scheduling.algorithm.GeneticAlgorithm;
import com.example.timetable.scheduling.constraints.hard.InstructorAvailabilityConstraint;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeneticScheduleService {

    private static final Logger log = LoggerFactory.getLogger(GeneticScheduleService.class);

    private final GeneticAlgorithm geneticAlgorithm;
    private final SectionRepository sectionRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ScheduleRepository scheduleRepository;
    private final SemesterRepository semesterRepository;
    private final InstructorAvailabilityConstraint availabilityConstraint;
    private final InstructorRepository instructorRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Schedule generate(Long semesterId) {

        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new NoSuchElementException("Semester not found"));

        List<Section> sections = sectionRepository.findBySemesterId(semesterId);

        if (sections.isEmpty()) {
            throw new IllegalStateException("No sections found for this semester");
        }

        List<Room> rooms = roomRepository.findAll();
        List<TimeSlot> slots = timeSlotRepository.findAll();

        // Preload instructor availability constraint
        Set<InstructorAvailability> allUnavailable = sections.stream()
                .map(Section::getInstructor)
                .distinct()
                .flatMap(i -> i.getUnavailableSlots().stream())
                .collect(Collectors.toSet());
        availabilityConstraint.preload(allUnavailable);

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
            entry.setType(SessionType.LECTURE);

            schedule.getEntries().add(entry);
        }

        Schedule saved = scheduleRepository.save(schedule);

        // Warn about any sections that lost their slot due to duplicate filtering
        Set<Long> scheduledSectionIds = saved.getEntries().stream()
                .map(e -> e.getSection().getId())
                .collect(Collectors.toSet());

        List<Long> missingSections = sections.stream()
                .map(Section::getId)
                .filter(id -> !scheduledSectionIds.contains(id))
                .collect(Collectors.toList());

        if (!missingSections.isEmpty()) {
            log.warn("Sections not scheduled (duplicate gene conflict): {}", missingSections);
        }

        return saved;
    }
}