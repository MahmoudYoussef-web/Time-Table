package com.example.timetable.scheduling.algorithm.mutation;

import com.example.timetable.entity.Room;
import com.example.timetable.entity.Section;
import com.example.timetable.entity.TimeSlot;
import com.example.timetable.scheduling.algorithm.Chromosome;
import com.example.timetable.scheduling.algorithm.Gene;

import java.util.List;
import java.util.Random;
import java.util.function.BiPredicate;

public class RandomMutation implements MutationStrategy {

    private final Random random;

    public RandomMutation(Random random) {
        this.random = random;
    }

    @Override
    public void mutate(Chromosome chromosome,
                       List<Room> rooms,
                       List<TimeSlot> slots,
                       double mutationRate,
                       BiPredicate<Section, Room> roomFilter) {

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

                    Room selected;
                    if (roomFilter != null) {
                        List<Room> compatible = rooms.stream()
                                .filter(r -> roomFilter.test(gene.getSection(), r))
                                .toList();
                        selected = compatible.isEmpty()
                                ? gene.getRoom()
                                : compatible.get(random.nextInt(compatible.size()));
                    } else {
                        selected = rooms.get(random.nextInt(rooms.size()));
                    }
                    gene.setRoom(selected);
                }
            }
        }
    }
}