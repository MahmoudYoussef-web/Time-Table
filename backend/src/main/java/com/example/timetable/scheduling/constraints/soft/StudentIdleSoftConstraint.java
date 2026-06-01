package com.example.timetable.scheduling.constraints.soft;

import com.example.timetable.entity.*;
import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.Gene;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Duration;
import java.util.*;

@Component
public class StudentIdleSoftConstraint implements SoftConstraint {

    private static final int DAILY_IDLE_THRESHOLD = 120; // ساعتين

    @Override
    public String name() {
        return "Student Idle Optimization";
    }

    @Override
    public double weight() {
        return 1.0; // غير مستخدم حالياً
    }

    @Override
    public int violations(Chromosome chromosome) {

        Map<Long, Map<DayOfWeek, List<TimeSlot>>> map = new HashMap<>();

        for (Gene gene : chromosome.getGenes()) {

            TimeSlot slot = gene.getTimeSlot();
            if (slot == null) continue;

            Section section = gene.getSection();
            if (section.getEnrollments() == null) continue;

            for (Enrollment enrollment : section.getEnrollments()) {

                Student student = enrollment.getStudent();
                if (student == null) continue;

                map
                        .computeIfAbsent(student.getId(), k -> new HashMap<>())
                        .computeIfAbsent(slot.getDay(), k -> new ArrayList<>())
                        .add(slot);
            }
        }

        int violations = 0;

        for (Map<DayOfWeek, List<TimeSlot>> studentDays : map.values()) {

            for (List<TimeSlot> lectures : studentDays.values()) {

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