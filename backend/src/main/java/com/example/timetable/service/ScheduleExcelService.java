package com.example.timetable.service;

import com.example.timetable.dto.response.*;
import com.example.timetable.mapper.WeeklyScheduleMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
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

    private static final java.awt.Color NAVY_HEADER_BG = new java.awt.Color(26, 53, 96);
    private static final java.awt.Color NAVY_BREAK_BG = new java.awt.Color(243, 244, 246);
    private static final java.awt.Color NAVY_BREAK_FG = new java.awt.Color(107, 114, 128);
    private static final java.awt.Color NAVY_DEFAULT_FG = new java.awt.Color(30, 41, 59);

    private static final java.awt.Color BW_HEADER_BG = java.awt.Color.BLACK;
    private static final java.awt.Color BW_BREAK_BG = new java.awt.Color(180, 180, 180);
    private static final java.awt.Color BW_BREAK_FG = java.awt.Color.BLACK;
    private static final java.awt.Color BW_DEFAULT_FG = java.awt.Color.BLACK;

    private static final java.awt.Color WHITE = java.awt.Color.WHITE;
    private static final java.awt.Color ROW_EVEN_BG = new java.awt.Color(249, 250, 251);

    private static java.awt.Color toAwt(String hex) {
        String h = hex.replace("#", "");
        return new java.awt.Color(
                Integer.parseInt(h.substring(0, 2), 16),
                Integer.parseInt(h.substring(2, 4), 16),
                Integer.parseInt(h.substring(4, 6), 16)
        );
    }

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

            XSSFWorkbook workbook = new XSSFWorkbook();

            boolean isNavy = theme == ColorTheme.NAVY;
            CellStyle headerStyle = createHeaderStyle(workbook, isNavy);
            CellStyle breakStyle = createBreakStyle(workbook,
                    isNavy ? NAVY_BREAK_BG : BW_BREAK_BG,
                    isNavy ? NAVY_BREAK_FG : BW_BREAK_FG);
            java.awt.Color textColor = isNavy ? NAVY_DEFAULT_FG : BW_DEFAULT_FG;
            java.awt.Color codeColor = new java.awt.Color(100, 116, 139);
            java.awt.Color grayColor = new java.awt.Color(107, 114, 128);

            XSSFFont codeFont = (XSSFFont) workbook.createFont();
            codeFont.setBold(true);
            codeFont.setFontHeightInPoints((short) 9);
            codeFont.setColor(new XSSFColor(codeColor, null));

            XSSFFont boldFont = (XSSFFont) workbook.createFont();
            boldFont.setBold(true);
            boldFont.setFontHeightInPoints((short) 11);
            boldFont.setColor(new XSSFColor(textColor, null));

            XSSFFont normalFont = (XSSFFont) workbook.createFont();
            normalFont.setFontHeightInPoints((short) 10);
            normalFont.setColor(new XSSFColor(textColor, null));

            XSSFFont smallFont = (XSSFFont) workbook.createFont();
            smallFont.setFontHeightInPoints((short) 9);
            smallFont.setColor(new XSSFColor(grayColor, null));

            CellStyle entryEvenStyle = createEntryStyle(workbook, ROW_EVEN_BG, textColor);
            CellStyle entryOddStyle = createEntryStyle(workbook, WHITE, textColor);
            CellStyle timeStyle = createTimeStyle(workbook, isNavy);

            Map<java.awt.Color, CellStyle> categoryStyleCache = new java.util.HashMap<>();

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
                String breakThreshold = findBreakThreshold(orderedTimes);

                for (int i = 0; i < orderedTimes.size(); i++) {
                    String timeKey = orderedTimes.get(i);

                    if (!breakAdded && breakThreshold != null && timeKey.compareTo(breakThreshold) >= 0) {
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
                    row.setHeightInPoints(60);

                    Cell timeCell = row.createCell(0);
                    timeCell.setCellValue(formatTimeAmPm(timeKey));
                    timeCell.setCellStyle(timeStyle);

                    CellStyle entryStyle = (rowIndex % 2 == 0) ? entryEvenStyle : entryOddStyle;

                    for (int d = 0; d < ORDERED_DAYS.size(); d++) {
                        String day = ORDERED_DAYS.get(d);
                        SlotDTO slot = dataMap.getOrDefault(day, Map.of()).get(timeKey);
                        Cell cell = row.createCell(d + 1);

                        if (slot != null && slot.entry() != null) {
                            ScheduleEntryDTO e = slot.entry();
                            String line1 = e.courseCode();
                            String line2 = e.courseName();
                            String line3 = e.instructorName();
                            String line4 = "\u25CF " + e.roomNumber();
                            String full = line1 + "\n" + line2 + "\n" + line3 + "\n" + line4;

                            RichTextString rts = workbook.getCreationHelper().createRichTextString(full);
                            rts.applyFont(0, line1.length(), codeFont);
                            rts.applyFont(line1.length() + 1, line1.length() + 1 + line2.length(), boldFont);
                            int afterLine2 = line1.length() + 1 + line2.length() + 1;
                            rts.applyFont(afterLine2, afterLine2 + line3.length(), normalFont);
                            rts.applyFont(afterLine2 + line3.length() + 1, full.length(), smallFont);
                            cell.setCellValue(rts);

                            java.awt.Color catBg = toAwt(CourseCategory.fromCode(e.courseCode()).bgHex);
                            CellStyle catStyle = categoryStyleCache.computeIfAbsent(catBg,
                                    bg -> createEntryStyle(workbook, bg, textColor));
                            cell.setCellStyle(catStyle);
                        } else {
                            cell.setCellStyle(entryStyle);
                        }
                    }
                }

                sheet.setColumnWidth(0, 3000);
                for (int i = 1; i <= 6; i++) {
                    sheet.setColumnWidth(i, 6500);
                }

                java.awt.Color tabColor = switch (level) {
                    case "First Year"  -> new java.awt.Color(46, 125, 50);
                    case "Second Year" -> new java.awt.Color(21, 101, 192);
                    case "Third Year"  -> new java.awt.Color(106, 27, 154);
                    case "Fourth Year" -> new java.awt.Color(230, 81, 0);
                    default -> isNavy ? NAVY_HEADER_BG : BW_HEADER_BG;
                };
                ((XSSFSheet) sheet).setTabColor(new XSSFColor(tabColor, null));
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel", e);
        }
    }

    private static String findBreakThreshold(java.util.List<String> orderedTimes) {
        boolean hasAfternoon = orderedTimes.stream().anyMatch(t -> t.compareTo("14:00") >= 0);
        if (!hasAfternoon) return null;
        for (String t : orderedTimes) {
            if (t.compareTo("12:00") >= 0) {
                return t;
            }
        }
        return null;
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook, boolean isNavy) {
        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setBold(true);
        font.setColor(new XSSFColor(WHITE, null));
        font.setFontHeightInPoints((short) 11);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(new XSSFColor(isNavy ? NAVY_HEADER_BG : BW_HEADER_BG, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createEntryStyle(XSSFWorkbook workbook, java.awt.Color bg, java.awt.Color fg) {
        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setColor(new XSSFColor(fg, null));

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);

        style.setFillForegroundColor(new XSSFColor(bg, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createBreakStyle(XSSFWorkbook workbook, java.awt.Color bg, java.awt.Color fg) {
        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(new XSSFColor(fg, null));

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(new XSSFColor(bg, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFont(font);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createTimeStyle(XSSFWorkbook workbook, boolean isNavy) {
        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(new XSSFColor(isNavy ? NAVY_DEFAULT_FG : BW_DEFAULT_FG, null));

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(new XSSFColor(WHITE, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
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
