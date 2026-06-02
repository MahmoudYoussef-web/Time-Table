package com.example.timetable.service;

import com.example.timetable.dto.response.*;
import com.example.timetable.mapper.WeeklyScheduleMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleExcelService {

    private static final List<String> ORDERED_DAYS = List.of(
            "SATURDAY", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY"
    );

    private static final List<String> ORDERED_LEVELS = List.of(
            "First Year", "Second Year", "Third Year", "Fourth Year"
    );

    private static final IndexedColors LECTURE_BG = IndexedColors.PALE_BLUE;
    private static final IndexedColors SECTION_BG = IndexedColors.BRIGHT_GREEN;
    private static final IndexedColors BREAK_BG   = IndexedColors.LIGHT_YELLOW;
    private static final IndexedColors HEADER_BG  = IndexedColors.DARK_BLUE;

    public byte[] exportExcel(ScheduleDTO schedule, ColorTheme theme) {
        return generateExcel(schedule, theme);
    }

    public byte[] generateExcel(ScheduleDTO schedule) {
        return generateExcel(schedule, ColorTheme.NAVY);
    }

    private byte[] generateExcel(ScheduleDTO schedule, ColorTheme theme) {
        try {
            Map<String, WeeklyScheduleDTO> levelTables =
                    WeeklyScheduleMapper.toLevelTables(schedule);

            Workbook workbook = new XSSFWorkbook();

            CellStyle headerStyle = createHeaderStyle(workbook, theme);
            CellStyle lectureStyle = createCellStyle(workbook, LECTURE_BG);
            CellStyle sectionStyle = createCellStyle(workbook, SECTION_BG);
            CellStyle breakStyle = createCellStyle(workbook, BREAK_BG);
            CellStyle defaultStyle = createCellStyle(workbook, null);

            for (String level : ORDERED_LEVELS) {
                WeeklyScheduleDTO weekly = levelTables.get(level);
                if (weekly == null) continue;

                Sheet sheet = workbook.createSheet(level);
                sheet.createFreezePane(1, 1);

                int rowIndex = 0;

                Row header = sheet.createRow(rowIndex++);
                header.setHeightInPoints(30);

                String[] headers = {"TIME", "SATURDAY", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = header.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                java.util.Set<String> allTimeKeys = weekly.days().stream()
                        .flatMap(d -> d.slots().stream())
                        .map(s -> s.startTime())
                        .collect(java.util.stream.Collectors.toCollection(java.util.TreeSet::new));
                java.util.List<String> orderedTimes = new java.util.ArrayList<>(allTimeKeys);

                Map<String, Map<String, SlotDTO>> dataMap = new java.util.HashMap<>();
                for (DayScheduleDTO day : weekly.days()) {
                    Map<String, SlotDTO> slotMap = new java.util.LinkedHashMap<>();
                    for (SlotDTO slot : day.slots()) {
                        slotMap.put(slot.startTime(), slot);
                    }
                    dataMap.put(day.day(), slotMap);
                }

                boolean breakAdded = false;
                boolean hasAfternoon = orderedTimes.stream().anyMatch(t -> t.compareTo("14:00") >= 0);

                for (int i = 0; i < orderedTimes.size(); i++) {
                    String timeKey = orderedTimes.get(i);

                    if (!breakAdded && hasAfternoon && timeKey.compareTo("12:00") >= 0) {
                        Row breakRow = sheet.createRow(rowIndex++);
                        breakRow.setHeightInPoints(22);
                        for (int c = 0; c < 7; c++) {
                            Cell cell = breakRow.createCell(c);
                            cell.setCellValue("BREAK");
                            cell.setCellStyle(breakStyle);
                        }
                        breakAdded = true;
                    }

                    Row row = sheet.createRow(rowIndex++);
                    row.setHeightInPoints(45);

                    Cell timeCell = row.createCell(0);
                    timeCell.setCellValue(formatTimeAmPm(timeKey));
                    timeCell.setCellStyle(defaultStyle);

                    for (int d = 0; d < ORDERED_DAYS.size(); d++) {
                        String day = ORDERED_DAYS.get(d);
                        SlotDTO slot = dataMap.getOrDefault(day, Map.of()).get(timeKey);
                        Cell cell = row.createCell(d + 1);

                        if (slot != null && slot.entry() != null) {
                            ScheduleEntryDTO e = slot.entry();
                            String sessionType = e.sessionType();
                            boolean isLecture = sessionType == null
                                    || (!sessionType.equalsIgnoreCase("LAB")
                                    && !sessionType.equalsIgnoreCase("SECTION")
                                    && !sessionType.equalsIgnoreCase("TUTORIAL"));

                            cell.setCellValue(e.courseCode() + " - " + e.courseName() + "\n"
                                    + e.instructorName() + " | " + e.roomNumber());
                            cell.setCellStyle(isLecture ? lectureStyle : sectionStyle);
                        } else {
                            cell.setCellStyle(defaultStyle);
                        }
                    }
                }

                for (int i = 0; i < 7; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel", e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook, ColorTheme theme) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 11);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(theme == ColorTheme.BLACK
                ? IndexedColors.BLACK.getIndex() : HEADER_BG.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createCellStyle(Workbook workbook, IndexedColors bg) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);

        if (bg != null) {
            style.setFillForegroundColor(bg.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private String formatTimeAmPm(String time24) {
        try {
            if (time24.length() <= 5) {
                time24 = time24.length() == 4 ? "0" + time24 : time24;
            }
            return java.time.LocalTime.parse(time24)
                    .format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"));
        } catch (Exception e) {
            return time24;
        }
    }
}
