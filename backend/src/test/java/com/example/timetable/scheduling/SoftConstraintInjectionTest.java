package com.example.timetable.scheduling;

import com.example.timetable.scheduling.constraints.HardConstraint;
import com.example.timetable.scheduling.constraints.hard.InstructorAvailabilityConstraint;
import com.example.timetable.scheduling.constraints.hard.InstructorConflictConstraint;
import com.example.timetable.scheduling.constraints.hard.RoomCapacityConstraint;
import com.example.timetable.scheduling.constraints.hard.RoomConflictConstraint;
import com.example.timetable.scheduling.constraints.hard.RoomTypeConstraint;
import com.example.timetable.scheduling.constraints.hard.StudentConflictConstraint;
import com.example.timetable.scheduling.constraints.hard.TimeOverlapConstraint;
import com.example.timetable.scheduling.constraints.soft.InstructorBackToBackConstraint;
import com.example.timetable.scheduling.constraints.soft.InstructorGapPreferenceConstraint;
import com.example.timetable.scheduling.constraints.soft.InstructorIdleSoftConstraint;
import com.example.timetable.scheduling.constraints.soft.SameCourseSameDayConstraint;
import com.example.timetable.scheduling.constraints.soft.SoftConstraint;
import com.example.timetable.scheduling.constraints.soft.StudentIdleSoftConstraint;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SoftConstraintInjectionTest {

    private final List<SoftConstraint> softConstraints = List.of(
            new InstructorBackToBackConstraint(),
            new InstructorGapPreferenceConstraint(),
            new InstructorIdleSoftConstraint(),
            new SameCourseSameDayConstraint(),
            new StudentIdleSoftConstraint()
    );

    private final List<HardConstraint> hardConstraints = List.of(
            new InstructorAvailabilityConstraint(),
            new InstructorConflictConstraint(),
            new RoomCapacityConstraint(),
            new RoomConflictConstraint(),
            new RoomTypeConstraint(),
            new StudentConflictConstraint(),
            new TimeOverlapConstraint()
    );

    @Test
    void allSoftConstraintsAreInjected() {
        assertThat(softConstraints).hasSize(5);
    }

    @Test
    void allHardConstraintsAreInjected() {
        assertThat(hardConstraints).hasSize(7);
    }

    @Test
    void allSoftConstraintsHavePositiveWeight() {
        for (SoftConstraint sc : softConstraints) {
            assertThat(sc.weight())
                    .as("Soft constraint '%s' should have positive weight", sc.name())
                    .isPositive();
        }
    }

    @Test
    void allSoftConstraintsHaveName() {
        for (SoftConstraint sc : softConstraints) {
            assertThat(sc.name())
                    .as("Soft constraint should have a non-blank name")
                    .isNotBlank();
        }
    }
}