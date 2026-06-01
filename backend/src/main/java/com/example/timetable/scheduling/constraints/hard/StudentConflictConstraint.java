package com.example.timetable.scheduling.constraints.hard;

import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.Gene;
import com.example.timetable.scheduling.constraints.ConstraintViolation;
import com.example.timetable.scheduling.constraints.HardConstraint;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StudentConflictConstraint implements HardConstraint {

    private final Map<Long, Set<Long>> sectionStudents = new ConcurrentHashMap<>();

    public void preload(Map<Long, Set<Long>> sectionStudentsMap) {
        sectionStudents.clear();
        sectionStudents.putAll(sectionStudentsMap);
    }

    @Override
    public String getName() {
        return "STUDENT_CONFLICT";
    }

    @Override
    public int violations(Chromosome chromosome) {
        List<Gene> genes = chromosome.getGenes();
        int count = 0;

        for (int i = 0; i < genes.size(); i++) {
            Gene g1 = genes.get(i);
            if (g1.getTimeSlot() == null) continue;

            for (int j = i + 1; j < genes.size(); j++) {
                Gene g2 = genes.get(j);
                if (g2.getTimeSlot() == null) continue;

                if (!sameDay(g1, g2)) continue;
                if (!overlap(g1, g2)) continue;
                if (shareStudents(g1.getSection().getId(), g2.getSection().getId())) {
                    count++;
                }
            }
        }

        return count;
    }

    @Override
    public List<ConstraintViolation> explain(Chromosome chromosome) {
        List<ConstraintViolation> result = new ArrayList<>();
        List<Gene> genes = chromosome.getGenes();

        for (int i = 0; i < genes.size(); i++) {
            Gene g1 = genes.get(i);
            if (g1.getTimeSlot() == null) continue;

            for (int j = i + 1; j < genes.size(); j++) {
                Gene g2 = genes.get(j);
                if (g2.getTimeSlot() == null) continue;

                if (!sameDay(g1, g2)) continue;
                if (!overlap(g1, g2)) continue;

                Set<Long> s1 = sectionStudents.get(g1.getSection().getId());
                Set<Long> s2 = sectionStudents.get(g2.getSection().getId());
                if (s1 == null || s2 == null) continue;

                Set<Long> intersection = new HashSet<>(s1);
                intersection.retainAll(s2);
                if (!intersection.isEmpty()) {
                    result.add(new ConstraintViolation(
                            getName(),
                            g1.getSection().getId(),
                            g1.getSection().getName() + " & " + g2.getSection().getName()
                                    + " share " + intersection.size() + " student(s) at "
                                    + g1.getTimeSlot().getDay() + " "
                                    + g1.getTimeSlot().getStartTime()
                    ));
                }
            }
        }

        return result;
    }

    private boolean sameDay(Gene a, Gene b) {
        return a.getTimeSlot().getDay().equals(b.getTimeSlot().getDay());
    }

    private boolean overlap(Gene a, Gene b) {
        LocalTime s1 = a.getTimeSlot().getStartTime();
        LocalTime e1 = a.getTimeSlot().getEndTime();
        LocalTime s2 = b.getTimeSlot().getStartTime();
        LocalTime e2 = b.getTimeSlot().getEndTime();
        return s1.isBefore(e2) && s2.isBefore(e1);
    }

    private boolean shareStudents(Long sectionId1, Long sectionId2) {
        Set<Long> set1 = sectionStudents.get(sectionId1);
        Set<Long> set2 = sectionStudents.get(sectionId2);
        if (set1 == null || set2 == null) return false;

        if (set1.size() <= set2.size()) {
            for (Long sid : set1) {
                if (set2.contains(sid)) return true;
            }
        } else {
            for (Long sid : set2) {
                if (set1.contains(sid)) return true;
            }
        }
        return false;
    }
}
