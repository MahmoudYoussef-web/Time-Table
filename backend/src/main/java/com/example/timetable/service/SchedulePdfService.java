package com.example.timetable.service;

import com.example.timetable.dto.response.*;
import com.example.timetable.mapper.WeeklyScheduleMapper;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SchedulePdfService {

    private static final Color NAVY        = new Color(26, 53, 96);
    private static final Color WHITE       = Color.WHITE;
    private static final Color BLACK       = Color.BLACK;
    private static final Color BREAK_BG    = new Color(243, 244, 246);
    private static final Color BREAK_FG    = new Color(107, 114, 128);
    private static final Color ROW_EVEN    = new Color(249, 250, 251);
    private static final Color ROW_ODD     = WHITE;
    private static final Color BORDER      = new Color(209, 213, 219);
    private static final String BREAK_THRESHOLD = "12:00";
    private static final Color GRAY_TEXT   = new Color(107, 114, 128);

    private static java.awt.Color toAwt(String hex) {
        String h = hex.replace("#", "");
        return new java.awt.Color(
                Integer.parseInt(h.substring(0, 2), 16),
                Integer.parseInt(h.substring(2, 4), 16),
                Integer.parseInt(h.substring(4, 6), 16)
        );
    }

    private static final List<String> ORDERED_DAYS = List.of(
            "SATURDAY", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY"
    );

    public byte[] generatePdf(ScheduleDTO schedule) {
        return generatePdf(schedule, PdfColorScheme.COLOR, null);
    }

    public byte[] exportPdf(ScheduleDTO schedule, ColorTheme theme) {
        return generatePdf(schedule, toScheme(theme), null);
    }

    public byte[] exportPdf(ScheduleDTO schedule, String year, ColorTheme theme) {
        return generatePdf(schedule, toScheme(theme), year);
    }

    private static String mapYearFilter(String yearFilter) {
        if (yearFilter == null) return null;
        return switch (yearFilter.toUpperCase()) {
            case "FIRST", "1" -> "First Year";
            case "SECOND", "2" -> "Second Year";
            case "THIRD", "3" -> "Third Year";
            case "FOURTH", "4" -> "Fourth Year";
            default -> yearFilter;
        };
    }

    private byte[] generatePdf(ScheduleDTO schedule, PdfColorScheme scheme, String yearFilter) {
        try {
            Map<String, WeeklyScheduleDTO> levelTables =
                    WeeklyScheduleMapper.toLevelTables(schedule);

            Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new FooterPageEvent());

            document.open();

            String mappedFilter = mapYearFilter(yearFilter);
            List<String> levels = mappedFilter != null
                    ? List.of(mappedFilter)
                    : List.of("First Year", "Second Year", "Third Year", "Fourth Year");
            for (String level : levels) {
                WeeklyScheduleDTO weekly = levelTables.get(level);
                if (weekly == null) continue;

                document.newPage();
                addPageHeader(document, level, schedule, scheme);
                PdfPTable table = buildScheduleTable(weekly, scheme);
                document.add(table);
            }

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate schedule PDF for schedule ID={}", schedule.getId(), e);
            throw new RuntimeException("Failed to generate schedule PDF", e);
        }
    }

    private static PdfColorScheme toScheme(ColorTheme theme) {
        return switch (theme) {
            case BLACK -> PdfColorScheme.BW;
            default -> PdfColorScheme.COLOR;
        };
    }

    private void addPageHeader(Document doc, String level, ScheduleDTO schedule, PdfColorScheme scheme) throws DocumentException {
        Font mainTitleFont = new Font(Font.HELVETICA, 22, Font.BOLD, scheme.headerBg());
        Paragraph mainTitle = new Paragraph("UNIVERSITY STUDY SCHEDULE", mainTitleFont);
        mainTitle.setAlignment(Element.ALIGN_LEFT);
        mainTitle.setSpacingAfter(2);
        doc.add(mainTitle);

        String semesterInfo = level + " \u2014 Semester: "
                + (schedule.getSemesterName() != null ? schedule.getSemesterName() : "N/A");
        Font subtitleFont = new Font(Font.HELVETICA, 12, Font.BOLD, GRAY_TEXT);
        Paragraph subtitle = new Paragraph(semesterInfo, subtitleFont);
        subtitle.setAlignment(Element.ALIGN_LEFT);
        subtitle.setSpacingAfter(2);
        doc.add(subtitle);

        Font dotFont = new Font(Font.HELVETICA, 7, Font.NORMAL, scheme.headerBg());
        Paragraph dots = new Paragraph("\u2022  \u2022  \u2022  \u2022  \u2022", dotFont);
        dots.setAlignment(Element.ALIGN_LEFT);
        dots.setSpacingAfter(3);
        doc.add(dots);

        String weekInfo = "For The Week: "
                + (schedule.getStartDate() != null ? schedule.getStartDate() : "N/A")
                + " \u2014 "
                + (schedule.getEndDate() != null ? schedule.getEndDate() : "N/A");
        Font dateFont = new Font(Font.HELVETICA, 9, Font.NORMAL, GRAY_TEXT);
        Paragraph weekLine = new Paragraph(weekInfo, dateFont);
        weekLine.setAlignment(Element.ALIGN_LEFT);
        weekLine.setSpacingAfter(10);
        doc.add(weekLine);
    }

    private PdfPTable buildScheduleTable(WeeklyScheduleDTO weekly, PdfColorScheme scheme) throws DocumentException {
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.6f, 3.3f, 3.3f, 3.3f, 3.3f, 3.3f, 3.3f});

        addHeaderCell(table, "Time", scheme);
        for (String day : ORDERED_DAYS) {
            addHeaderCell(table, day, scheme);
        }

        Set<String> allTimeKeys = weekly.days().stream()
                .flatMap(d -> d.slots().stream())
                .map(s -> s.startTime())
                .collect(Collectors.toCollection(TreeSet::new));
        List<String> orderedTimes = new ArrayList<>(allTimeKeys);

        Map<String, Map<String, SlotDTO>> dataMap = new HashMap<>();
        for (DayScheduleDTO day : weekly.days()) {
            Map<String, SlotDTO> slotMap = new LinkedHashMap<>();
            for (SlotDTO slot : day.slots()) {
                slotMap.put(slot.startTime(), slot);
            }
            dataMap.put(day.day(), slotMap);
        }

        boolean breakAdded = false;
        boolean hasAfternoonSlots = orderedTimes.stream()
                .anyMatch(t -> t.compareTo("14:00") >= 0);

        for (int i = 0; i < orderedTimes.size(); i++) {
            String timeKey = orderedTimes.get(i);

            if (!breakAdded && hasAfternoonSlots && timeKey.compareTo(BREAK_THRESHOLD) >= 0) {
                addBreakRow(table, scheme);
                breakAdded = true;
            }

            String endTime = getEndTime(dataMap, timeKey);
            String displayTime = formatTimeAmPm(timeKey) + "\n\u2013 " + formatTimeAmPm(endTime);
            PdfPCell timeCell = buildTimeCell(displayTime, i, scheme);
            table.addCell(timeCell);

            for (String day : ORDERED_DAYS) {
                SlotDTO slot = dataMap.getOrDefault(day, Map.of()).get(timeKey);
                table.addCell(buildEntryCell(slot, i));
            }
        }

        return table;
    }

    private void addHeaderCell(PdfPTable table, String text, PdfColorScheme scheme) {
        Font f = new Font(Font.HELVETICA, 11, Font.BOLD, scheme.headerFg());
        PdfPCell cell = new PdfPCell(new Phrase(text, f));
        cell.setBackgroundColor(scheme.headerBg());
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8);
        cell.setMinimumHeight(38);
        cell.setBorderColor(new Color(71, 85, 105));
        cell.setBorderWidth(1);
        table.addCell(cell);
    }

    private PdfPCell buildTimeCell(String text, int rowIndex, PdfColorScheme scheme) {
        Font mainF = new Font(Font.HELVETICA, 9, Font.BOLD, scheme.headerBg());
        PdfPCell cell = new PdfPCell(new Phrase(text, mainF));
        cell.setBackgroundColor(scheme == PdfColorScheme.COLOR
                ? new Color(248, 250, 252) : new Color(240, 240, 240));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        cell.setMinimumHeight(75);
        cell.setBorderColor(BORDER);
        return cell;
    }

    private PdfPCell buildEntryCell(SlotDTO slot, int rowIndex) {
        if (slot == null || slot.entry() == null) {
            PdfPCell empty = new PdfPCell(new Phrase("\u2014",
                    new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(148, 163, 184))));
            empty.setBackgroundColor(rowIndex % 2 == 0 ? ROW_EVEN : ROW_ODD);
            empty.setHorizontalAlignment(Element.ALIGN_CENTER);
            empty.setVerticalAlignment(Element.ALIGN_MIDDLE);
            empty.setMinimumHeight(70);
            empty.setBorderColor(BORDER);
            return empty;
        }

        ScheduleEntryDTO e = slot.entry();

        CourseCategory cat = CourseCategory.fromCode(e.courseCode());
        Color bgColor    = toAwt(cat.bgHex);
        Color codeColor  = toAwt(cat.codeHex);
        Color borderColor = codeColor;

        Font codeFont   = new Font(Font.HELVETICA, 10, Font.BOLD, codeColor);
        Font nameFont   = new Font(Font.HELVETICA, 9,  Font.BOLD, new Color(30, 41, 59));
        Font instrFont  = new Font(Font.HELVETICA, 8,  Font.NORMAL, GRAY_TEXT);
        Font roomFont   = new Font(Font.HELVETICA, 8,  Font.NORMAL, GRAY_TEXT);

        Paragraph content = new Paragraph();
        content.setAlignment(Element.ALIGN_CENTER);
        content.setLeading(13f);

        content.add(new Chunk(e.courseCode() + "\n", codeFont));
        content.add(new Chunk(e.courseName() + "\n", nameFont));
        content.add(new Chunk(e.instructorName() + "\n", instrFont));
        content.add(new Chunk("\u25CF " + e.roomNumber(), roomFont));

        PdfPCell cell = new PdfPCell(content);
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(6);
        cell.setPaddingBottom(6);
        cell.setPaddingLeft(5);
        cell.setPaddingRight(5);
        cell.setMinimumHeight(80);
        cell.setBorderColor(BORDER);
        cell.setBorderWidth(0.5f);
        cell.setBorderWidthLeft(3f);
        cell.setBorderColorLeft(borderColor);

        return cell;
    }

    private void addBreakRow(PdfPTable table, PdfColorScheme scheme) {
        Font timeF = new Font(Font.HELVETICA, 8, Font.BOLD, WHITE);
        PdfPCell timeCell = new PdfPCell(new Phrase("BREAK", timeF));
        timeCell.setBackgroundColor(scheme.headerBg());
        timeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        timeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        timeCell.setPadding(5);
        timeCell.setMinimumHeight(24);
        timeCell.setBorderColor(BORDER);
        table.addCell(timeCell);

        Font f = new Font(Font.HELVETICA, 8, Font.BOLD, BREAK_FG);
        for (int i = 0; i < 6; i++) {
            PdfPCell cell = new PdfPCell(new Phrase("\u2615  Break", f));
            cell.setBackgroundColor(BREAK_BG);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(5);
            cell.setMinimumHeight(24);
            cell.setBorderColor(BORDER);
            table.addCell(cell);
        }
    }

    private static String getEndTime(Map<String, Map<String, SlotDTO>> dataMap, String timeKey) {
        return dataMap.values().stream()
                .flatMap(m -> m.entrySet().stream())
                .filter(e -> e.getKey().equals(timeKey) && e.getValue() != null && e.getValue().endTime() != null)
                .map(e -> e.getValue().endTime())
                .findFirst()
                .orElseGet(() -> {
                    try {
                        String t = timeKey.length() == 4 ? "0" + timeKey : timeKey;
                        return java.time.LocalTime.parse(t).plusHours(2)
                                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
                    } catch (Exception ex) {
                        return timeKey;
                    }
                });
    }

    private String formatTimeAmPm(String time24) {
        try {
            if (time24.length() <= 5) {
                time24 = time24.length() == 4 ? "0" + time24 : time24;
            }
            LocalTime t = LocalTime.parse(time24);
            return t.format(DateTimeFormatter.ofPattern("h:mm a"));
        } catch (Exception e) {
            return time24;
        }
    }

    private static class FooterPageEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                Font f = new Font(Font.HELVETICA, 8, Font.ITALIC, GRAY_TEXT);
                String footer = "Generated by TimetableScheduler \u2014 "
                        + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                        + "    Page " + writer.getPageNumber();
                Phrase p = new Phrase(footer, f);
                PdfContentByte cb = writer.getDirectContent();
                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, p,
                        (document.left() + document.right()) / 2,
                        document.bottom() - 10, 0);
            } catch (Exception ignored) {}
        }
    }
}
