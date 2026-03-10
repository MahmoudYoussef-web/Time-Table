package com.example.timetable.mapper;

import com.example.timetable.dto.response.*;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

public class WeeklyScheduleMapper {

    public static WeeklyScheduleDTO toWeeklyTable(ScheduleDTO schedule) {

        // Collect ALL time ranges
        Set<String> timeRanges =
                schedule.getEntries()
                        .stream()
                        .map(e -> e.startTime() + "-" + e.endTime())
                        .collect(Collectors.toCollection(TreeSet::new));

        List<String> orderedTimes =
                new ArrayList<>(timeRanges);

        Map<String, List<ScheduleEntryDTO>> groupedByDay =
                schedule.getEntries()
                        .stream()
                        .collect(Collectors.groupingBy(
                                ScheduleEntryDTO::dayOfWeek
                        ));

        List<DayScheduleDTO> days = new ArrayList<>();

        for (DayOfWeek day : DayOfWeek.values()) {

            String dayName = day.name();

            List<ScheduleEntryDTO> entries =
                    groupedByDay.getOrDefault(dayName, List.of());

            Map<String, ScheduleEntryDTO> entryMap =
                    new HashMap<>();

            for (ScheduleEntryDTO e : entries) {

                String key =
                        e.startTime() + "-" + e.endTime();

                entryMap.putIfAbsent(key, e);
            }

            List<SlotDTO> slots = new ArrayList<>();

            for (String time : orderedTimes) {

                String start = time.split("-")[0];
                String end = time.split("-")[1];

                slots.add(
                        new SlotDTO(
                                start,
                                end,
                                entryMap.get(time)
                        )
                );
            }

            days.add(new DayScheduleDTO(dayName, slots));
        }

        return new WeeklyScheduleDTO(days);
    }
    public static Map<String, WeeklyScheduleDTO> toDepartmentTables(
            ScheduleDTO schedule
    ) {

        Map<String, List<ScheduleEntryDTO>> byDepartment =
                schedule.getEntries()
                        .stream()
                        .collect(Collectors.groupingBy(
                                ScheduleEntryDTO::departmentName
                        ));

        Map<String, WeeklyScheduleDTO> result =
                new LinkedHashMap<>();

        for (var entry : byDepartment.entrySet()) {

            ScheduleDTO subSchedule =
                    new ScheduleDTO(
                            schedule.getId(),
                            schedule.getFitnessScore(),
                            schedule.getHardViolations(),
                            schedule.getSoftViolations(),
                            schedule.getStatus(),
                            schedule.getCreatedAt(),
                            entry.getValue()
                    );

            result.put(
                    entry.getKey(),
                    toWeeklyTable(subSchedule)
            );
        }

        return result;
    }
}