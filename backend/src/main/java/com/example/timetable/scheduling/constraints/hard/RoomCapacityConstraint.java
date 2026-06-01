package com.example.timetable.scheduling.constraints.hard;

import com.example.timetable.entity.Room;
import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.Gene;
import com.example.timetable.scheduling.constraints.ConstraintViolation;
import com.example.timetable.scheduling.constraints.HardConstraint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomCapacityConstraint implements HardConstraint {

    private final Map<Long, Integer> sectionEnrollmentCount = new ConcurrentHashMap<>();

    public void preload(Map<Long, Integer> sectionEnrollmentCountMap) {
        sectionEnrollmentCount.clear();
        sectionEnrollmentCount.putAll(sectionEnrollmentCountMap);
    }

    @Override
    public String getName() {
        return "ROOM_CAPACITY";
    }

    @Override
    public int violations(Chromosome chromosome) {
        int count = 0;
        for (Gene gene : chromosome.getGenes()) {
            Room room = gene.getRoom();
            int needed = enrollmentCount(gene.getSection().getId());
            if (room.getCapacity() < needed) {
                count++;
            }
        }
        return count;
    }

    @Override
    public List<ConstraintViolation> explain(Chromosome chromosome) {
        List<ConstraintViolation> result = new ArrayList<>();
        for (Gene gene : chromosome.getGenes()) {
            Room room = gene.getRoom();
            int needed = enrollmentCount(gene.getSection().getId());
            if (room.getCapacity() < needed) {
                result.add(new ConstraintViolation(
                        getName(),
                        gene.getSection().getId(),
                        "Room " + room.getRoomNumber()
                                + " capacity (" + room.getCapacity()
                                + ") < needed (" + needed
                                + ") for " + gene.getSection().getName()
                ));
            }
        }
        return result;
    }

    private int enrollmentCount(Long sectionId) {
        return sectionEnrollmentCount.getOrDefault(sectionId, 0);
    }
}
