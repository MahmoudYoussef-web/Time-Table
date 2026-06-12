package com.example.timetable.scheduling.constraints.hard;

import com.example.timetable.entity.Room;
import com.example.timetable.entity.enums.RoomType;
import com.example.timetable.entity.enums.SessionType;
import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.Gene;
import com.example.timetable.scheduling.constraints.ConstraintViolation;
import com.example.timetable.scheduling.constraints.HardConstraint;
import com.example.timetable.scheduling.util.RoomTypeUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomTypeConstraint implements HardConstraint {

    private final Map<Long, SessionType> sectionSessionTypes = new ConcurrentHashMap<>();

    public void preload(Map<Long, SessionType> sectionSessionTypeMap) {
        sectionSessionTypes.clear();
        sectionSessionTypes.putAll(sectionSessionTypeMap);
    }

    @Override
    public String getName() {
        return "ROOM_TYPE";
    }

    @Override
    public int violations(Chromosome chromosome) {
        int count = 0;
        for (Gene gene : chromosome.getGenes()) {
            if (!isCompatible(gene)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public List<ConstraintViolation> explain(Chromosome chromosome) {
        List<ConstraintViolation> result = new ArrayList<>();
        for (Gene gene : chromosome.getGenes()) {
            if (!isCompatible(gene)) {
                result.add(new ConstraintViolation(
                        getName(),
                        gene.getSection().getId(),
                        gene.getSection().getName()
                                + " (" + gene.getSection().getSessionType()
                                + ") assigned to room "
                                + gene.getRoom().getRoomNumber()
                                + " (" + gene.getRoom().getRoomType()
                                + ")"
                ));
            }
        }
        return result;
    }

    private boolean isCompatible(Gene gene) {
        Room room = gene.getRoom();
        RoomType roomType = room.getRoomType();
        if (roomType == null) {
            return true;
        }

        SessionType sessionType = sectionSessionTypes.get(gene.getSection().getId());
        if (sessionType == null) {
            sessionType = gene.getSection().getSessionType();
            if (sessionType == null) {
                return true;
            }
        }

        return RoomTypeUtil.isCompatible(sessionType, roomType);
    }
}
