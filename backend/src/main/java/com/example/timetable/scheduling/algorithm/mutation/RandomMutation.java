package com.example.timetable.scheduling.algorithm.mutation;

import com.example.timetable.entity.Room;
import com.example.timetable.entity.TimeSlot;
import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.Gene;

import java.util.List;
import java.util.Random;

public class RandomMutation implements MutationStrategy {

    private final Random random;

    public RandomMutation(Random random) {
        this.random = random;
    }

    @Override
    public void mutate(Chromosome chromosome,
                       List<Room> rooms,
                       List<TimeSlot> slots,
                       double mutationRate) {

        for (Gene gene : chromosome.getGenes()) {

            if (gene.isLocked()) {
                continue;
            }

            if (random.nextDouble() < mutationRate) {

                // 70% mutate timeslot
                if (random.nextDouble() < 0.7) {

                    gene.setTimeSlot(
                            slots.get(random.nextInt(slots.size()))
                    );

                } else {

                    gene.setRoom(
                            rooms.get(random.nextInt(rooms.size()))
                    );
                }
            }
        }
    }
}