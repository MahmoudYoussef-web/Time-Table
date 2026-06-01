package com.example.timetable.mapper;

import com.example.timetable.dto.response.ScheduleDTO;
import com.example.timetable.dto.response.ScheduleEntryDTO;
import com.example.timetable.entity.Schedule;
import com.example.timetable.entity.ScheduleEntry;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class ScheduleMapper {

    public static ScheduleDTO toDTO(Schedule schedule) {

        List<ScheduleEntryDTO> entries =
                schedule.getEntries()
                        .stream()
                        .sorted(
                                Comparator
                                        .comparing(
                                                (ScheduleEntry e)
                                                        -> e.getTimeSlot().getDay()
                                        )
                                        .thenComparing(
                                                e -> e.getTimeSlot().getStartTime()
                                        )
                        )
                        .map(ScheduleMapper::toEntryDTO)
                        .toList();

        ScheduleDTO dto = new ScheduleDTO(
                schedule.getId(),
                schedule.getFitnessScore(),
                schedule.getHardViolations(),
                schedule.getSoftViolations(),
                schedule.getStatus().name(),
                schedule.getCreatedAt(),
                entries
        );

        if (schedule.getUnscheduledSectionIds() != null) {
            dto.setUnscheduledSectionIds(schedule.getUnscheduledSectionIds());
        }

        if (schedule.getSemester() != null) {
            dto.setSemesterName(schedule.getSemester().getName());
            dto.setStartDate(schedule.getSemester().getStartDate() != null
                    ? schedule.getSemester().getStartDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                    : null);
            dto.setEndDate(schedule.getSemester().getEndDate() != null
                    ? schedule.getSemester().getEndDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                    : null);
        }

        return dto;
    }

    private static ScheduleEntryDTO toEntryDTO(
            ScheduleEntry entry) {

        String yearLevel = entry.getSection().getYearLevel() != null
                ? entry.getSection().getYearLevel().getDisplayName()
                : null;

        String sessionType = entry.getType() != null
                ? entry.getType().name()
                : null;

        return new ScheduleEntryDTO(

                entry.getSection().getId(),

                entry.getSection().getName(),

                entry.getSection().getCourse().getCode(),

                entry.getSection().getCourse().getName(),

                entry.getSection()
                        .getInstructor()
                        .getUser()
                        .getFullName(),

                entry.getRoom().getRoomNumber(),

                entry.getSection()
                        .getCourse()
                        .getDepartment()
                        .getName(),

                entry.getTimeSlot()
                        .getDay()
                        .name(),

                entry.getTimeSlot()
                        .getStartTime()
                        .toString(),

                entry.getTimeSlot()
                        .getEndTime()
                        .toString(),

                yearLevel,

                sessionType,

                0,
                0
        );
    }
}