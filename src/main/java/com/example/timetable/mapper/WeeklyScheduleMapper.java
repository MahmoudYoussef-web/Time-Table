package com.example.timetable.mapper;

import com.example.timetable.dto.response.*;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

public class WeeklyScheduleMapper {

    public static WeeklyScheduleDTO toWeeklyTable(ScheduleDTO schedule) {

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


    public static Map<String, WeeklyScheduleDTO> toLevelTables(
            ScheduleDTO schedule
    ) {

        Map<String, List<ScheduleEntryDTO>> byLevel =
                schedule.getEntries()
                        .stream()
                        .collect(Collectors.groupingBy(e -> {

                            String section = e.sectionName();

                            if (section.startsWith("L1"))
                                return "First Year";

                            if (section.startsWith("L2"))
                                return "Second Year";

                            if (section.startsWith("L3"))
                                return "Third Year";

                            if (section.startsWith("L4"))
                                return "Fourth Year";

                            return "Other";
                        }));


        List<String> orderedLevels = List.of(
                "First Year",
                "Second Year",
                "Third Year",
                "Fourth Year"
        );

        Map<String, WeeklyScheduleDTO> result =
                new LinkedHashMap<>();

        for (String level : orderedLevels) {

            List<ScheduleEntryDTO> entries = byLevel.get(level);

            if (entries == null || entries.isEmpty())
                continue;

            ScheduleDTO subSchedule =
                    new ScheduleDTO(
                            schedule.getId(),
                            schedule.getFitnessScore(),
                            schedule.getHardViolations(),
                            schedule.getSoftViolations(),
                            schedule.getStatus(),
                            schedule.getCreatedAt(),
                            entries
                    );

            result.put(level, toWeeklyTable(subSchedule));
        }

        return result;
    }
}