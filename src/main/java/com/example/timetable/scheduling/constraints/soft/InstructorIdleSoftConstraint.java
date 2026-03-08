package com.example.timetable.scheduling.constraints.soft;

import com.example.timetable.entity.Instructor;
import com.example.timetable.entity.TimeSlot;
import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.Gene;

import java.time.DayOfWeek;
import java.time.Duration;
import java.util.*;

public class InstructorIdleSoftConstraint implements SoftConstraint {

    private static final int DAILY_IDLE_THRESHOLD = 120; // ساعتين

    @Override
    public String name() {
        return "Instructor Idle Optimization";
    }

    @Override
    public double weight() {
        return 1.0; // غير مستخدم حالياً
    }

    @Override
    public int violations(Chromosome chromosome) {

        Map<Long, Map<DayOfWeek, List<TimeSlot>>> map = new HashMap<>();

        for (Gene gene : chromosome.getGenes()) {

            Instructor instructor = gene.getSection().getInstructor();
            TimeSlot slot = gene.getTimeSlot();

            if (instructor == null || slot == null) continue;

            map
                    .computeIfAbsent(instructor.getId(), k -> new HashMap<>())
                    .computeIfAbsent(slot.getDay(), k -> new ArrayList<>())
                    .add(slot);
        }

        int violations = 0;

        for (Map<DayOfWeek, List<TimeSlot>> instructorDays : map.values()) {

            for (List<TimeSlot> lectures : instructorDays.values()) {

                if (lectures.size() <= 1) continue;

                lectures.sort(Comparator.comparing(TimeSlot::getStartTime));

                int dailyIdle = 0;

                for (int i = 0; i < lectures.size() - 1; i++) {

                    int gap = (int) Duration.between(
                            lectures.get(i).getEndTime(),
                            lectures.get(i + 1).getStartTime()
                    ).toMinutes();

                    if (gap > 0) dailyIdle += gap;
                }

                if (dailyIdle > DAILY_IDLE_THRESHOLD) {
                    violations += (dailyIdle - DAILY_IDLE_THRESHOLD) / 30;
                }
            }
        }

        return violations;
    }
}