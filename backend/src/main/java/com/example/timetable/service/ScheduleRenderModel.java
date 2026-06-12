package com.example.timetable.service;

import com.example.timetable.dto.response.DayScheduleDTO;
import com.example.timetable.dto.response.ScheduleDTO;
import com.example.timetable.dto.response.ScheduleEntryDTO;
import com.example.timetable.dto.response.SlotDTO;
import com.example.timetable.dto.response.WeeklyScheduleDTO;
import com.example.timetable.mapper.WeeklyScheduleMapper;

import java.util.*;

public class ScheduleRenderModel {

    private final String title;
    private final String subtitle;
    private final String dateRange;
    private final String footer;
    private final ColorTheme theme;
    private final List<ColumnDef> columns;
    private final List<RowDef> rows;
    private final boolean isEmpty;

    private ScheduleRenderModel(Builder b) {
        this.title = b.title;
        this.subtitle = b.subtitle;
        this.dateRange = b.dateRange;
        this.footer = b.footer;
        this.theme = b.theme;
        this.columns = List.copyOf(b.columns);
        this.rows = List.copyOf(b.rows);
        this.isEmpty = b.isEmpty;
    }

    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getDateRange() { return dateRange; }
    public String getFooter() { return footer; }
    public ColorTheme getTheme() { return theme; }
    public List<ColumnDef> getColumns() { return columns; }
    public List<RowDef> getRows() { return rows; }
    public boolean isEmpty() { return isEmpty; }

    public static final List<String> DAYS = List.of(
            "SATURDAY", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY"
    );

    private static final String BREAK_THRESHOLD = "12:00";

    public static final int TITLE_SIZE = 36;
    public static final int SUBTITLE_SIZE = 20;
    public static final int DATE_SIZE = 13;
    public static final int HEADER_SIZE = 13;
    public static final int TIME_SIZE = 11;
    public static final int COURSE_CODE_SIZE = 16;
    public static final int COURSE_NAME_SIZE = 13;
    public static final int INSTRUCTOR_SIZE = 12;
    public static final int ROOM_SIZE = 12;
    public static final int FOOTER_SIZE = 9;

    public static final int HEADER_ROW_HEIGHT = 44;
    public static final int COURSE_ROW_HEIGHT = 90;
    public static final int BREAK_ROW_HEIGHT = 40;

    public static final float TIME_COLUMN_PCT = 0.15f;
    public static final float DAY_COLUMN_PCT = (1f - TIME_COLUMN_PCT) / 6f;

    public static ScheduleRenderModel forYear(ScheduleDTO schedule, String yearLevel, ColorTheme theme) {
        List<ScheduleEntryDTO> entries = filterEntriesForYear(schedule.getEntries(), yearLevel);
        String display = switch (yearLevel.toUpperCase()) {
            case "ALL" -> "All Years";
            case "1" -> "First Year";
            case "2" -> "Second Year";
            case "3" -> "Third Year";
            case "4" -> "Fourth Year";
            default -> formatYearDisplay(yearLevel);
        };
        return build(schedule, entries, display, theme);
    }

    private static List<ScheduleEntryDTO> filterEntriesForYear(List<ScheduleEntryDTO> entries, String yearLevel) {
        if ("ALL".equalsIgnoreCase(yearLevel)) return entries;
        return entries.stream()
                .filter(e -> e.yearLevel() != null
                        && e.yearLevel().toUpperCase().startsWith(yearLevel.toUpperCase()))
                .toList();
    }

    private static String formatYearDisplay(String yearLevel) {
        if ("ALL".equals(yearLevel)) return "All Years";
        return yearLevel.charAt(0) + yearLevel.substring(1).toLowerCase() + " Year";
    }

    private static String formatAmPm(String time24) {
        try {
            String t = time24.length() == 4 ? "0" + time24 : time24;
            return java.time.LocalTime.parse(t)
                    .format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"));
        } catch (Exception e) {
            return time24;
        }
    }

    private static String calcEndTime(String start) {
        try {
            java.time.LocalTime t = java.time.LocalTime.parse(start);
            return t.plusHours(2).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return start;
        }
    }

    public static List<ScheduleRenderModel> forAllYears(ScheduleDTO schedule, ColorTheme theme) {
        Map<String, WeeklyScheduleDTO> levelTables = WeeklyScheduleMapper.toLevelTables(schedule);
        List<ScheduleRenderModel> models = new ArrayList<>();
        List<String> ordered = List.of("First Year", "Second Year", "Third Year", "Fourth Year");

        for (String level : ordered) {
            WeeklyScheduleDTO weekly = levelTables.get(level);
            if (weekly == null) continue;

            Map<String, Map<String, SlotDTO>> dataMap = buildDataMap(weekly);
            List<String> timeSlots = extractTimeSlotsFromDataMap(dataMap);
            String breakTime = findBreakTime(timeSlots);
            List<RowDef> rows = buildRows(dataMap, timeSlots, breakTime);
            String subtitle = level + " \u2014 Semester: "
                    + (schedule.getSemesterName() != null ? schedule.getSemesterName() : "N/A");

            models.add(new ScheduleRenderModel.Builder()
                    .title("UNIVERSITY STUDY SCHEDULE")
                    .subtitle(subtitle)
                    .dateRange(buildDateRange(schedule))
                    .footer(buildFooter())
                    .theme(theme)
                    .columns(buildColumns())
                    .rows(rows)
                    .isEmpty(rows.isEmpty())
                    .build());
        }
        return models;
    }

    private static ScheduleRenderModel build(ScheduleDTO schedule,
                                              List<ScheduleEntryDTO> entries,
                                              String displayName,
                                              ColorTheme theme) {
        Map<String, List<ScheduleEntryDTO>> byDay = new LinkedHashMap<>();
        for (String day : DAYS) byDay.put(day, new ArrayList<>());
        for (ScheduleEntryDTO e : entries) {
            byDay.computeIfAbsent(e.dayOfWeek(), k -> new ArrayList<>()).add(e);
        }

        List<String> timeSlots = extractTimeSlots(entries);
        String breakTime = findBreakTime(timeSlots);

        Map<String, Map<String, SlotDTO>> dataMap = new LinkedHashMap<>();
        for (String day : DAYS) {
            Map<String, SlotDTO> slotMap = new LinkedHashMap<>();
            Map<String, ScheduleEntryDTO> seen = new LinkedHashMap<>();
            for (ScheduleEntryDTO e : byDay.get(day)) {
                seen.putIfAbsent(e.startTime(), e);
            }
            for (String time : timeSlots) {
                ScheduleEntryDTO entry = seen.get(time);
                slotMap.put(time, entry != null ? new SlotDTO(time, calcEndTime(time), entry) : null);
            }
            dataMap.put(day, slotMap);
        }

        List<RowDef> rows = buildRows(dataMap, timeSlots, breakTime);
        String subtitle = displayName + " \u2014 Semester: "
                + (schedule.getSemesterName() != null ? schedule.getSemesterName() : "N/A");

        return new ScheduleRenderModel.Builder()
                .title("UNIVERSITY STUDY SCHEDULE")
                .subtitle(subtitle)
                .dateRange(buildDateRange(schedule))
                .footer(buildFooter())
                .theme(theme)
                .columns(buildColumns())
                .rows(rows)
                .isEmpty(rows.isEmpty())
                .build();
    }

    private static String buildDateRange(ScheduleDTO s) {
        String start = s.getStartDate() != null ? s.getStartDate() : "N/A";
        String end = s.getEndDate() != null ? s.getEndDate() : "N/A";
        return start + " \u2014 " + end;
    }

    private static String buildFooter() {
        return "Generated by TimetableScheduler \u2014 "
                + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

    private static Map<String, Map<String, SlotDTO>> buildDataMap(WeeklyScheduleDTO weekly) {
        Map<String, Map<String, SlotDTO>> dataMap = new LinkedHashMap<>();
        for (DayScheduleDTO day : weekly.days()) {
            Map<String, SlotDTO> slotMap = new LinkedHashMap<>();
            for (SlotDTO slot : day.slots()) {
                slotMap.put(slot.startTime(), slot);
            }
            dataMap.put(day.day(), slotMap);
        }
        return dataMap;
    }

    private static List<ColumnDef> buildColumns() {
        List<ColumnDef> cols = new ArrayList<>();
        cols.add(new ColumnDef("TIME", TIME_COLUMN_PCT, HorizontalAlign.CENTER));
        for (String day : DAYS) {
            cols.add(new ColumnDef(day, DAY_COLUMN_PCT, HorizontalAlign.CENTER));
        }
        return cols;
    }

    private static List<RowDef> buildRows(Map<String, Map<String, SlotDTO>> dataMap,
                                          List<String> timeSlots,
                                          String breakTime) {
        List<RowDef> rows = new ArrayList<>();

        for (int i = 0; i < timeSlots.size(); i++) {
            String timeKey = timeSlots.get(i);
            boolean isBreak = timeKey.equals(breakTime);

            if (isBreak) {
                RowDef breakRow = new RowDef(RowType.BREAK, BREAK_ROW_HEIGHT);
                breakRow.setTimeLabel(formatAmPm(timeKey));
                breakRow.setTimeSubLabel("\u2013 " + formatAmPm(calcEndTime(timeKey)));
                rows.add(breakRow);
                continue;
            }

            RowDef courseRow = new RowDef(RowType.COURSE, COURSE_ROW_HEIGHT);
            courseRow.setTimeLabel(formatAmPm(timeKey));
            courseRow.setTimeSubLabel("\u2013 " + formatAmPm(calcEndTime(timeKey)));
            courseRow.setIndex(i);

            for (String day : DAYS) {
                SlotDTO slot = dataMap.getOrDefault(day, Map.of()).get(timeKey);
                if (slot != null && slot.entry() != null) {
                    ScheduleEntryDTO e = slot.entry();
                    String roomLabel = "LAB".equalsIgnoreCase(e.sessionType()) ? "Lab: " : "Rm: ";
                    courseRow.getCells().add(new CellContent(
                            e.courseCode(),
                            e.courseName(),
                            e.instructorName(),
                            roomLabel + e.roomNumber(),
                            e.sessionType() != null ? e.sessionType() : "LECTURE",
                            false,
                            e.hardViolations() > 0
                    ));
                } else {
                    courseRow.getCells().add(CellContent.EMPTY);
                }
            }
            rows.add(courseRow);
        }

        return rows;
    }

    private static List<String> extractTimeSlots(List<ScheduleEntryDTO> entries) {
        return entries.stream()
                .map(ScheduleEntryDTO::startTime)
                .distinct()
                .sorted()
                .toList();
    }

    private static List<String> extractTimeSlotsFromDataMap(Map<String, Map<String, SlotDTO>> dataMap) {
        return dataMap.values().stream()
                .flatMap(m -> m.keySet().stream())
                .distinct()
                .sorted()
                .toList();
    }

    private static String findBreakTime(List<String> timeSlots) {
        for (String time : timeSlots) {
            if (time.compareTo(BREAK_THRESHOLD) >= 0) {
                return time;
            }
        }
        return null;
    }

    public enum RowType { HEADER, COURSE, BREAK }
    public enum HorizontalAlign { LEFT, CENTER, RIGHT }

    public static class ColumnDef {
        private final String label;
        private final float widthPct;
        private final HorizontalAlign align;

        public ColumnDef(String label, float widthPct, HorizontalAlign align) {
            this.label = label;
            this.widthPct = widthPct;
            this.align = align;
        }

        public String getLabel() { return label; }
        public float getWidthPct() { return widthPct; }
        public HorizontalAlign getAlign() { return align; }
    }

    public static class RowDef {
        private final RowType type;
        private final int height;
        private String timeLabel;
        private String timeSubLabel;
        private int index;
        private final List<CellContent> cells = new ArrayList<>();

        public RowDef(RowType type, int height) {
            this.type = type;
            this.height = height;
        }

        public RowType getType() { return type; }
        public int getHeight() { return height; }
        public String getTimeLabel() { return timeLabel; }
        public void setTimeLabel(String v) { this.timeLabel = v; }
        public String getTimeSubLabel() { return timeSubLabel; }
        public void setTimeSubLabel(String v) { this.timeSubLabel = v; }
        public int getIndex() { return index; }
        public void setIndex(int v) { this.index = v; }
        public List<CellContent> getCells() { return cells; }
    }

    public static class CellContent {
        public static final CellContent EMPTY = new CellContent("", "", "", "", "", true, false);

        private final String courseCode;
        private final String courseName;
        private final String instructor;
        private final String room;
        private final String sessionType;
        private final boolean empty;
        private final boolean conflict;

        public CellContent(String courseCode, String courseName, String instructor, String room, String sessionType, boolean empty, boolean conflict) {
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.instructor = instructor;
            this.room = room;
            this.sessionType = sessionType;
            this.empty = empty;
            this.conflict = conflict;
        }

        public String getCourseCode() { return courseCode; }
        public String getCourseName() { return courseName; }
        public String getInstructor() { return instructor; }
        public String getRoom() { return room; }
        public String getSessionType() { return sessionType; }
        public boolean isEmpty() { return empty; }
        public boolean hasConflict() { return conflict; }
    }

    private static class Builder {
        private String title;
        private String subtitle;
        private String dateRange;
        private String footer;
        private ColorTheme theme = ColorTheme.NAVY;
        private final List<ColumnDef> columns = new ArrayList<>();
        private final List<RowDef> rows = new ArrayList<>();
        private boolean isEmpty;

        Builder title(String v) { this.title = v; return this; }
        Builder subtitle(String v) { this.subtitle = v; return this; }
        Builder dateRange(String v) { this.dateRange = v; return this; }
        Builder footer(String v) { this.footer = v; return this; }
        Builder theme(ColorTheme v) { this.theme = v; return this; }
        Builder columns(List<ColumnDef> v) { this.columns.addAll(v); return this; }
        Builder rows(List<RowDef> v) { this.rows.addAll(v); return this; }
        Builder isEmpty(boolean v) { this.isEmpty = v; return this; }

        ScheduleRenderModel build() {
            return new ScheduleRenderModel(this);
        }
    }
}
