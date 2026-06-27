package com.example.timetable.service;

import com.example.timetable.dto.response.*;
import com.example.timetable.mapper.WeeklyScheduleMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleExcelService {

    private static final List<String> ORDERED_DAYS =
            List.of("SATURDAY", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY");

    private static final List<String> ORDERED_LEVELS =
            List.of("First Year", "Second Year", "Third Year", "Fourth Year");

    private static Color hex(String h) {
        String s = h.replace("#", "");
        return new Color(
                Integer.parseInt(s.substring(0, 2), 16),
                Integer.parseInt(s.substring(2, 4), 16),
                Integer.parseInt(s.substring(4, 6), 16));
    }

    // ── Public API ───────────────────────────────────────────────────────────
    public byte[] generateExcel(ScheduleDTO schedule) {
        return generate(schedule, ColorTheme.NAVY);
    }

    public byte[] exportExcel(ScheduleDTO schedule, ColorTheme theme) {
        return generate(schedule, theme);
    }

    // ── Core generator ───────────────────────────────────────────────────────
    private byte[] generate(ScheduleDTO schedule, ColorTheme theme) {
        try {
            boolean isNavy = theme != ColorTheme.BLACK;
            Map<String, WeeklyScheduleDTO> levelTables =
                    WeeklyScheduleMapper.toLevelTables(schedule);

            XSSFWorkbook wb = new XSSFWorkbook();

            // ── Shared colors (from PdfColorScheme for consistency) ──────────
            Color headerBg  = isNavy ? hex("#13387A") : Color.BLACK;
            Color headerFg  = Color.WHITE;
            Color gridBg    = isNavy ? hex("#F8FAFC") : hex("#F5F5F5");
            Color breakBg   = isNavy ? hex("#F3F4F6") : hex("#E0E0E0");
            Color breakFg   = isNavy ? hex("#6B7280") : hex("#444444");
            Color textColor = isNavy ? hex("#1E293B") : Color.BLACK;
            Color grayColor = isNavy ? hex("#4F5D75") : hex("#555555");

            // ── Shared fonts ─────────────────────────────────────────────────
            XSSFFont headerFont = xFont(wb, headerFg,  true,  11);
            XSSFFont codeFont   = xFont(wb, isNavy ? hex("#1565C0") : Color.BLACK, true, 9);
            XSSFFont boldFont   = xFont(wb, textColor, true,  11);
            XSSFFont normalFont = xFont(wb, textColor, false, 10);
            XSSFFont smallFont  = xFont(wb, grayColor, false,  9);
            XSSFFont timeFont   = xFont(wb, textColor, true,  10);
            XSSFFont breakFont  = xFont(wb, breakFg,   true,  10);

            // ── Shared styles ─────────────────────────────────────────────────
            CellStyle headerStyle = headerStyle(wb, headerFont, headerBg);
            CellStyle breakStyle  = breakStyle(wb, breakFont, breakBg);
            CellStyle timeStyle   = timeStyle(wb, timeFont);
            CellStyle evenStyle   = entryStyle(wb, normalFont, gridBg);
            CellStyle oddStyle    = entryStyle(wb, normalFont, Color.WHITE);

            Map<Color, CellStyle> catStyleCache = new HashMap<>();
            Map<Color, XSSFFont> catFontCache = new HashMap<>();

            // ── Per-level sheets ──────────────────────────────────────────────
            for (String level : ORDERED_LEVELS) {
                WeeklyScheduleDTO weekly = levelTables.get(level);
                if (weekly == null) continue;

                XSSFSheet sheet = (XSSFSheet) wb.createSheet(level);
                sheet.setTabColor(new XSSFColor(tabColor(level, isNavy), null));

                // ── Title header rows ─────────────────────────────────────────
                Row titleRow = sheet.createRow(0);
                titleRow.setHeightInPoints(28);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("UNIVERSITY STUDY SCHEDULE");
                CellStyle titleStyle = wb.createCellStyle();
                XSSFFont titleFont = xFont(wb, isNavy ? hex("#0B1B4F") : Color.BLACK, true, 16);
                titleStyle.setFont(titleFont);
                titleStyle.setFillForegroundColor(new XSSFColor(isNavy ? hex("#EEF2FF") : hex("#F5F5F5"), null));
                titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                titleStyle.setAlignment(HorizontalAlignment.CENTER);
                titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                setBorders(titleStyle, BorderStyle.NONE);
                titleCell.setCellStyle(titleStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

                Row subtitleRow = sheet.createRow(1);
                subtitleRow.setHeightInPoints(20);
                Cell subtitleCell = subtitleRow.createCell(0);
                subtitleCell.setCellValue(level + "  \u2014  Semester: "
                        + (schedule.getSemesterName() != null ? schedule.getSemesterName() : "N/A"));
                CellStyle subtitleStyle = wb.createCellStyle();
                XSSFFont subtitleFont = xFont(wb, isNavy ? hex("#1E293B") : Color.BLACK, false, 11);
                subtitleStyle.setFont(subtitleFont);
                subtitleStyle.setFillForegroundColor(new XSSFColor(isNavy ? hex("#EEF2FF") : hex("#F5F5F5"), null));
                subtitleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                subtitleStyle.setAlignment(HorizontalAlignment.CENTER);
                subtitleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                setBorders(subtitleStyle, BorderStyle.NONE);
                subtitleCell.setCellStyle(subtitleStyle);
                sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

                Row dateRow = sheet.createRow(2);
                dateRow.setHeightInPoints(16);
                Cell dateCell = dateRow.createCell(0);
                dateCell.setCellValue("For The Week:  "
                        + (schedule.getStartDate() != null ? schedule.getStartDate() : "N/A")
                        + "  \u2014  "
                        + (schedule.getEndDate() != null ? schedule.getEndDate() : "N/A")
                        + "      Generated: "
                        + java.time.LocalDate.now()
                                .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")));
                CellStyle dateStyle = wb.createCellStyle();
                XSSFFont dateFont = xFont(wb, hex("#4F5D75"), false, 9);
                dateStyle.setFont(dateFont);
                dateStyle.setFillForegroundColor(new XSSFColor(isNavy ? hex("#EEF2FF") : hex("#F5F5F5"), null));
                dateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                dateStyle.setAlignment(HorizontalAlignment.CENTER);
                dateStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                setBorders(dateStyle, BorderStyle.NONE);
                dateCell.setCellStyle(dateStyle);
                sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 6));

                Row spacerRow = sheet.createRow(3);
                spacerRow.setHeightInPoints(6);
                for (int i = 0; i < 7; i++) {
                    Cell sc = spacerRow.createCell(i);
                    CellStyle spacerStyle = wb.createCellStyle();
                    spacerStyle.setFillForegroundColor(new XSSFColor(headerBg, null));
                    spacerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    setBorders(spacerStyle, BorderStyle.NONE);
                    sc.setCellStyle(spacerStyle);
                }

                // ── Column headers row (row 4) ────────────────────────────────
                Row headerRow = sheet.createRow(4);
                headerRow.setHeightInPoints(30);
                String[] headers = {"TIME","SATURDAY","SUNDAY","MONDAY","TUESDAY","WEDNESDAY","THURSDAY"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }
                sheet.createFreezePane(1, 5);

                // ── Print setup ───────────────────────────────────────────────
                sheet.getPrintSetup().setLandscape(true);
                sheet.getPrintSetup().setPaperSize(org.apache.poi.ss.usermodel.PrintSetup.A4_PAPERSIZE);
                sheet.setFitToPage(true);
                sheet.getPrintSetup().setFitWidth((short) 1);
                sheet.getPrintSetup().setFitHeight((short) 0);

                // Build data map
                Set<String> timeKeys = weekly.days().stream()
                        .flatMap(d -> d.slots().stream())
                        .map(SlotDTO::startTime)
                        .collect(Collectors.toCollection(TreeSet::new));
                List<String> times = new ArrayList<>(timeKeys);

                Map<String, Map<String, SlotDTO>> data = new HashMap<>();
                for (DayScheduleDTO day : weekly.days()) {
                    Map<String, SlotDTO> m = new LinkedHashMap<>();
                    for (SlotDTO s : day.slots()) m.put(s.startTime(), s);
                    data.put(day.day(), m);
                }

                // Data rows with gap-based break detection
                int rowIdx = 5;
                for (int i = 0; i < times.size(); i++) {
                    String tk = times.get(i);

                    // Break row on gap ≥ 30 min
                    if (i > 0) {
                        String prevEnd = getEndTime(data, times.get(i - 1));
                        try {
                            long gap = java.time.Duration.between(
                                    java.time.LocalTime.parse(pad(prevEnd)),
                                    java.time.LocalTime.parse(pad(tk))).toMinutes();
                            if (gap >= 30) {
                                Row br = sheet.createRow(rowIdx++);
                                br.setHeightInPoints(22);
                                Cell timeBreak = br.createCell(0);
                                timeBreak.setCellValue("Break");
                                timeBreak.setCellStyle(breakStyle);
                                for (int c = 1; c < 7; c++) {
                                    Cell cell = br.createCell(c);
                                    cell.setCellValue("\u2615  Break");
                                    cell.setCellStyle(breakStyle);
                                }
                            }
                        } catch (Exception ignored) {}
                    }

                    Row row = sheet.createRow(rowIdx++);
                    row.setHeightInPoints(80);

                    // Time cell
                    Cell timeCell = row.createCell(0);
                    String endTk = getEndTime(data, tk);
                    timeCell.setCellValue(fmt(tk) + "\n\u2013 " + fmt(endTk));
                    timeCell.setCellStyle(timeStyle);

                    // Entry cells
                    CellStyle defaultStyle = (rowIdx % 2 == 0) ? evenStyle : oddStyle;
                    for (int d = 0; d < ORDERED_DAYS.size(); d++) {
                        SlotDTO slot = data.getOrDefault(ORDERED_DAYS.get(d), Map.of()).get(tk);
                        Cell cell = row.createCell(d + 1);

                        if (slot != null && slot.entry() != null) {
                            ScheduleEntryDTO e = slot.entry();
                            CourseCategory cat = CourseCategory.fromCode(e.courseCode());
                            Color catBg = hex(cat.bgHex);
                            Color catCode = hex(cat.codeHex);

                            // Rich text
                            XSSFFont catCodeFont = catFontCache.computeIfAbsent(catCode,
                                    c -> xFont(wb, c, true, 9));
                            String line1 = e.courseCode();
                            String line2 = e.courseName();
                            String line3 = e.instructorName();
                            String line4 = "\u25CF " + e.roomNumber();
                            String full  = line1 + "\n" + line2 + "\n" + line3 + "\n" + line4;

                            RichTextString rts = wb.getCreationHelper().createRichTextString(full);
                            int p1 = line1.length();
                            int p2 = p1 + 1 + line2.length();
                            int p3 = p2 + 1 + line3.length();
                            rts.applyFont(0,      p1,        catCodeFont);
                            rts.applyFont(p1 + 1, p2,        boldFont);
                            rts.applyFont(p2 + 1, p3,        normalFont);
                            rts.applyFont(p3 + 1, full.length(), smallFont);
                            cell.setCellValue(rts);

                            CellStyle catStyle = catStyleCache.computeIfAbsent(catBg,
                                    bg -> entryStyle(wb, normalFont, bg));
                            cell.setCellStyle(catStyle);
                        } else {
                            cell.setCellStyle(defaultStyle);
                        }
                    }
                }

                // Column widths
                sheet.setColumnWidth(0, 4000);
                for (int i = 1; i <= 6; i++) sheet.setColumnWidth(i, 6500);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            wb.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel", e);
        }
    }

    // ── Style builders ───────────────────────────────────────────────────────
    private static XSSFFont xFont(XSSFWorkbook wb, Color color, boolean bold, int size) {
        XSSFFont f = (XSSFFont) wb.createFont();
        f.setBold(bold);
        f.setFontHeightInPoints((short) size);
        f.setColor(new XSSFColor(color, null));
        return f;
    }

    private static CellStyle headerStyle(XSSFWorkbook wb, XSSFFont font, Color bg) {
        CellStyle s = wb.createCellStyle();
        s.setFont(font);
        s.setFillForegroundColor(new XSSFColor(bg, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(s, BorderStyle.THIN);
        return s;
    }

    private static CellStyle entryStyle(XSSFWorkbook wb, XSSFFont font, Color bg) {
        CellStyle s = wb.createCellStyle();
        s.setFont(font);
        s.setFillForegroundColor(new XSSFColor(bg, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setWrapText(true);
        setBorders(s, BorderStyle.THIN);
        return s;
    }

    private static CellStyle breakStyle(XSSFWorkbook wb, XSSFFont font, Color bg) {
        CellStyle s = wb.createCellStyle();
        s.setFont(font);
        s.setFillForegroundColor(new XSSFColor(bg, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(s, BorderStyle.THIN);
        return s;
    }

    private static CellStyle timeStyle(XSSFWorkbook wb, XSSFFont font) {
        CellStyle s = wb.createCellStyle();
        s.setFont(font);
        s.setFillForegroundColor(new XSSFColor(hex("#F8FAFC"), null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setWrapText(true);
        setBorders(s, BorderStyle.THIN);
        return s;
    }

    private static void setBorders(CellStyle s, BorderStyle style) {
        s.setBorderTop(style);
        s.setBorderBottom(style);
        s.setBorderLeft(style);
        s.setBorderRight(style);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private static Color tabColor(String level, boolean isNavy) {
        return switch (level) {
            case "First Year"  -> hex("#2E7D32");
            case "Second Year" -> hex("#1565C0");
            case "Third Year"  -> hex("#6A1B9A");
            case "Fourth Year" -> hex("#E65100");
            default -> isNavy ? hex("#13387A") : Color.BLACK;
        };
    }

    private static String pad(String t) { return t.length() == 4 ? "0" + t : t; }

    private static String fmt(String t) {
        try {
            return java.time.LocalTime.parse(pad(t))
                    .format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"));
        } catch (Exception e) { return t; }
    }

    private static String getEndTime(Map<String, Map<String, SlotDTO>> data, String tk) {
        return data.values().stream()
                .flatMap(m -> m.entrySet().stream())
                .filter(e -> e.getKey().equals(tk)
                        && e.getValue() != null
                        && e.getValue().endTime() != null)
                .map(e -> e.getValue().endTime())
                .findFirst()
                .orElseGet(() -> {
                    try {
                        return java.time.LocalTime.parse(pad(tk)).plusHours(2)
                                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
                    } catch (Exception ex) { return tk; }
                });
    }


}