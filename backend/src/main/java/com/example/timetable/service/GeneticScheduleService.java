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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeneticScheduleService {

    private static final Logger log = LoggerFactory.getLogger(GeneticScheduleService.class);

    private final ConcurrentHashMap<Long, Object> semesterLocks = new ConcurrentHashMap<>();

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
    private final ScheduleEntryRepository scheduleEntryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Schedule generate(Long semesterId) {
        synchronized (semesterLocks.computeIfAbsent(semesterId, k -> new Object())) {

            Semester semester = semesterRepository.findById(semesterId)
                    .orElseThrow(() -> new NoSuchElementException("Semester not found"));
    
        // Debug: count entries before delete
        long before = scheduleRepository.countEntriesBySemesterId(semesterId);
        log.info("Schedule entries BEFORE delete for semester {}: {}", semesterId, before);

        // Delete all existing schedules and entries for this semester using native SQL
        // to avoid JPA cascade/orphan issues with H2 MySQL mode
        scheduleRepository.deleteEntriesBySemesterId(semesterId);
        scheduleRepository.deleteBySemesterId(semesterId);

        long after = scheduleRepository.countEntriesBySemesterId(semesterId);
        log.info("Schedule entries AFTER delete for semester {}: {}", semesterId, after);

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

            // Dedup genes into unique entries (local list, not attached to schedule yet)
            List<ScheduleEntry> preparedEntries = new ArrayList<>();
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

                entry.setSection(section);
                entry.setInstructor(instructor);
                entry.setRoom(room);
                entry.setTimeSlot(slot);
                entry.setType(determineSessionType(section));

                preparedEntries.add(entry);
            }

            // Debug: check for duplicate room+slot in genes
            List<Gene> genes = best.getGenes();
            long totalGenes = genes.size();
            long uniqueRoomSlots = genes.stream()
                    .map(g -> g.getRoom().getId() + "-" + g.getTimeSlot().getId())
                    .distinct()
                    .count();
            log.info("Total genes: {}, Unique room+slot combos in genes: {}, Final entries after dedup: {}",
                    totalGenes, uniqueRoomSlots, preparedEntries.size());

            // Compute unscheduled sections from prepared entries
            Set<Long> scheduledSectionIds = preparedEntries.stream()
                    .map(e -> e.getSection().getId())
                    .collect(Collectors.toSet());
            List<Long> missingSections = sections.stream()
                    .map(Section::getId)
                    .filter(id -> !scheduledSectionIds.contains(id))
                    .collect(Collectors.toList());
            if (!missingSections.isEmpty()) {
                log.warn("Sections not scheduled (duplicate gene conflict): {}", missingSections);
            }

            // Log all entries with their room+slot before saving
            for (int i = 0; i < preparedEntries.size(); i++) {
                ScheduleEntry e = preparedEntries.get(i);
                log.info("preparedEntry[{}]: room={}, slot={}, section={}, instructor={}",
                        i, e.getRoom().getId(), e.getTimeSlot().getId(),
                        e.getSection().getId(), e.getInstructor().getId());
            }

            // Save schedule first (without entries to avoid cascade issues)
            Schedule schedule = new Schedule();
            schedule.setSemester(semester);
            schedule.setFitnessScore(best.getFitness());
            schedule.setHardViolations(best.getHardViolations());
            schedule.setSoftViolations(best.getSoftViolations());
            schedule.setUnscheduledSectionIds(missingSections);
            schedule.setEntries(new ArrayList<>());

            Schedule saved = scheduleRepository.save(schedule);

            // Save each entry individually with FK to the saved schedule
            // NOTE: do NOT add to saved.getEntries() — CascadeType.ALL on Schedule.entries
            // would cause a duplicate INSERT on flush/commit
            for (ScheduleEntry entry : preparedEntries) {
                entry.setSchedule(saved);
                scheduleEntryRepository.save(entry);
            }

            return saved;
        }
    }

    private SessionType determineSessionType(Section section) {
        return section.getSessionType() != null
                ? section.getSessionType()
                : SessionType.LECTURE;
    }
}
