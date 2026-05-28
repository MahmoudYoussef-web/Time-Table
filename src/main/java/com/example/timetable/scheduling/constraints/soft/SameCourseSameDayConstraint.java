package com.example.timetable.scheduling.constraints.soft;

import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.Gene;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SameCourseSameDayConstraint implements SoftConstraint {

    @Override
    public String name() {
        return "Same Course Same Day Preference";
    }

    @Override
    public double weight() {
        return 1.0;
    }

    @Override
    public int violations(Chromosome chromosome) {

        int violations = 0;
        List<Gene> genes = chromosome.getGenes();

        for (int i = 0; i < genes.size(); i++) {
            Gene g1 = genes.get(i);

            for (int j = i + 1; j < genes.size(); j++) {
                Gene g2 = genes.get(j);

                if (g1.getSection().getCourse()
                        .equals(g2.getSection().getCourse())
                        && g1.getTimeSlot().getDay()
                        .equals(g2.getTimeSlot().getDay())) {
                    violations++;
                }
            }
        }

        return violations;
    }
}
