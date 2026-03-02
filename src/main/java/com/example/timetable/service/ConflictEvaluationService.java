package com.example.timetable.service;

import com.example.timetable.entity.Schedule;
import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.Gene;
import com.example.timetable.scheduling.constraints.ConstraintViolation;
import com.example.timetable.scheduling.constraints.HardConstraint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConflictEvaluationService {

    private final List<HardConstraint> hardConstraints;

    public List<ConstraintViolation> explain(Schedule schedule) {

        List<Gene> genes = schedule.getEntries()
                .stream()
                .map(entry ->
                        new Gene(
                                entry.getSection(),
                                entry.getRoom(),
                                entry.getTimeSlot()
                        )
                )
                .toList();

        Chromosome chromosome = new Chromosome(genes);

        List<ConstraintViolation> violations = new ArrayList<>();

        for (HardConstraint constraint : hardConstraints) {
            violations.addAll(constraint.explain(chromosome));
        }

        return violations;
    }
}