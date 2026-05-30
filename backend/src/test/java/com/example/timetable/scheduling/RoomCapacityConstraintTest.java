package com.example.timetable.scheduling;

import com.example.timetable.entity.Room;
import com.example.timetable.entity.Section;
import com.example.timetable.entity.TimeSlot;
import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.Gene;
import com.example.timetable.scheduling.constraints.ConstraintViolation;
import com.example.timetable.scheduling.constraints.hard.RoomCapacityConstraint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RoomCapacityConstraintTest {

    private RoomCapacityConstraint constraint;

    @BeforeEach
    void setUp() {
        constraint = new RoomCapacityConstraint();
    }

    @Test
    void noPreloadReturnsZeroViolations() {
        Room room = room(1L, "A", "101", 30);
        Section section = section(1L, "CS101", 200);
        Gene gene = new Gene(section, room, timeSlot(1L));
        Chromosome chromosome = new Chromosome(List.of(gene));

        int violations = constraint.violations(chromosome);

        assertThat(violations).isZero();
    }

    @Test
    void insufficientRoomCapacityDetected() {
        constraint.preload(Map.of(1L, 50));

        Room room = room(1L, "A", "101", 30);
        Section section = section(1L, "CS101", 200);
        Gene gene = new Gene(section, room, timeSlot(1L));
        Chromosome chromosome = new Chromosome(List.of(gene));

        int violations = constraint.violations(chromosome);

        assertThat(violations).isEqualTo(1);
    }

    @Test
    void sufficientRoomCapacityPasses() {
        constraint.preload(Map.of(1L, 25));

        Room room = room(1L, "A", "101", 30);
        Section section = section(1L, "CS101", 200);
        Gene gene = new Gene(section, room, timeSlot(1L));
        Chromosome chromosome = new Chromosome(List.of(gene));

        int violations = constraint.violations(chromosome);

        assertThat(violations).isZero();
    }

    @Test
    void explainReturnsViolationsWithDetails() {
        constraint.preload(Map.of(1L, 50));

        Room room = room(1L, "A", "101", 30);
        Section section = section(1L, "CS101", 200);
        Gene gene = new Gene(section, room, timeSlot(1L));
        Chromosome chromosome = new Chromosome(List.of(gene));

        List<ConstraintViolation> violations = constraint.explain(chromosome);

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).getConstraintName()).isEqualTo("ROOM_CAPACITY");
        assertThat(violations.get(0).getSectionId()).isEqualTo(1L);
        assertThat(violations.get(0).getMessage())
                .contains("capacity (30)")
                .contains("needed (50)");
    }

    @Test
    void multipleGenesCheckedIndependently() {
        constraint.preload(Map.of(1L, 50, 2L, 10));

        Room smallRoom = room(1L, "A", "101", 30);
        Room bigRoom = room(2L, "A", "102", 100);
        Section lecture = section(1L, "CS101", 200);
        Section tutorial = section(2L, "CS101-TUT", 30);

        Gene g1 = new Gene(lecture, smallRoom, timeSlot(1L));
        Gene g2 = new Gene(tutorial, bigRoom, timeSlot(2L));
        Chromosome chromosome = new Chromosome(List.of(g1, g2));

        int violations = constraint.violations(chromosome);

        assertThat(violations).isEqualTo(1);
    }

    private static Room room(Long id, String building, String roomNumber, int capacity) {
        Room r = new Room();
        r.setId(id);
        r.setBuilding(building);
        r.setRoomNumber(roomNumber);
        r.setCapacity(capacity);
        return r;
    }

    private static Section section(Long id, String name, int capacity) {
        Section s = new Section();
        s.setId(id);
        s.setName(name);
        s.setCapacity(capacity);
        return s;
    }

    private static TimeSlot timeSlot(Long id) {
        TimeSlot t = new TimeSlot();
        t.setId(id);
        t.setDay(DayOfWeek.MONDAY);
        t.setStartTime(LocalTime.of(9, 0));
        t.setEndTime(LocalTime.of(10, 0));
        return t;
    }
}
