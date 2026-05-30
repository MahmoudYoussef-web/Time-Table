package com.example.timetable.service;

import com.example.timetable.entity.*;
import com.example.timetable.entity.enums.SessionType;
import com.example.timetable.repository.*;
import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.Gene;
import com.example.timetable.scheduling.algorithm.GeneticAlgorithm;
import com.example.timetable.scheduling.constraints.hard.InstructorAvailabilityConstraint;
import com.example.timetable.scheduling.constraints.hard.RoomCapacityConstraint;
import com.example.timetable.scheduling.constraints.hard.RoomTypeConstraint;
import com.example.timetable.scheduling.constraints.hard.StudentConflictConstraint;
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
    private final StudentConflictConstraint studentConflictConstraint;
    private final RoomCapacityConstraint roomCapacityConstraint;
    private final RoomTypeConstraint roomTypeConstraint;
    private final EnrollmentRepository enrollmentRepository;
    private final InstructorRepository instructorRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Schedule generate(Long semesterId) {

        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new NoSuchElementException("Semester not found"));

        // Delete previous DRAFT schedules for this semester to avoid global UK conflicts
        List<Schedule> oldDrafts = scheduleRepository.findBySemesterIdAndStatus(
                semesterId, com.example.timetable.entity.enums.ScheduleStatus.DRAFT);
        if (!oldDrafts.isEmpty()) {
            scheduleRepository.deleteAll(oldDrafts);
            scheduleRepository.flush();
        }

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

        // Preload student-enrollment map for StudentConflictConstraint
        List<Long> sectionIds = sections.stream()
                .map(Section::getId)
                .collect(Collectors.toList());
        Map<Long, Set<Long>> sectionStudents = enrollmentRepository
                .findBySectionIdIn(sectionIds)
                .stream()
                .filter(e -> "ACTIVE".equals(e.getStatus()))
                .collect(Collectors.groupingBy(
                        e -> e.getSection().getId(),
                        Collectors.mapping(
                                e -> e.getStudent().getId(),
                                Collectors.toSet()
                        )
                ));
        studentConflictConstraint.preload(sectionStudents);

        Map<Long, Integer> enrollmentCounts = sectionStudents.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().size()
                ));
        roomCapacityConstraint.preload(enrollmentCounts);

        Map<Long, SessionType> sectionTypes = sections.stream()
                .collect(Collectors.toMap(
                        Section::getId,
                        s -> s.getSessionType() != null
                                ? s.getSessionType()
                                : SessionType.LECTURE
                ));
        roomTypeConstraint.preload(sectionTypes);

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
            entry.setType(determineSessionType(section));

            schedule.getEntries().add(entry);
        }

        Schedule saved = scheduleRepository.save(schedule);

        // Save unscheduled section IDs
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

        saved.setUnscheduledSectionIds(missingSections);
        scheduleRepository.save(saved);

        return saved;
    }

    private SessionType determineSessionType(Section section) {
        return section.getSessionType() != null
                ? section.getSessionType()
                : SessionType.LECTURE;
    }
}