package com.example.timetable.service;

import com.example.timetable.dto.response.*;
import com.example.timetable.mapper.WeeklyScheduleMapper;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;

import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SchedulePdfService {

    private static final Color HEADER_BG  = new Color(26, 53, 96);
    private static final Color HEADER_FG  = Color.WHITE;
    private static final Color LECTURE_BG = new Color(219, 234, 254);
    private static final Color LECTURE_FG = new Color(29, 78, 216);
    private static final Color SECTION_BG = new Color(220, 252, 231);
    private static final Color SECTION_FG = new Color(21, 128, 61);
    private static final Color BREAK_BG   = new Color(254, 243, 199);
    private static final Color BREAK_FG   = new Color(146, 64, 14);
    private static final Color ROW_EVEN   = new Color(249, 250, 251);
    private static final Color ROW_ODD    = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(209, 213, 219);
    private static final Color TIME_FG    = new Color(55, 65, 81);
    private static final Color GRAY_TEXT  = new Color(107, 114, 128);

    private static final List<String> ORDERED_DAYS = List.of(
            "SATURDAY", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY"
    );

    public byte[] generatePdf(ScheduleDTO schedule) {
        try {
            Map<String, WeeklyScheduleDTO> levelTables =
                    WeeklyScheduleMapper.toLevelTables(schedule);

            Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, out);

            writer.setPageEvent(new FooterPageEvent());

            document.open();

            addSummaryPage(document, schedule);

            List<String> levels = List.of("First Year", "Second Year", "Third Year", "Fourth Year");

            for (String level : levels) {
                WeeklyScheduleDTO weekly = levelTables.get(level);
                if (weekly == null) continue;

                document.newPage();
                addPageHeader(document, level, schedule);
                PdfPTable table = buildScheduleTable(weekly);
                document.add(table);

                document.add(Chunk.NEWLINE);
                document.add(buildLegend());
            }

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate schedule PDF", e);
        }
    }

    private void addSummaryPage(Document doc, ScheduleDTO schedule) throws DocumentException {
        Font titleFont = new Font(Font.HELVETICA, 24, Font.BOLD, HEADER_BG);
        Font subtitleFont = new Font(Font.HELVETICA, 14, Font.NORMAL, GRAY_TEXT);
        Font sectionFont = new Font(Font.HELVETICA, 13, Font.BOLD, new Color(192, 57, 43));
        Font dataFont = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(55, 65, 81));
        Font labelFont = new Font(Font.HELVETICA, 11, Font.BOLD, HEADER_BG);

        Paragraph title = new Paragraph("DEPARTMENT WEEKLY SCHEDULE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(4);
        doc.add(title);

        Paragraph subtitle = new Paragraph("— OVERVIEW —", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        doc.add(subtitle);

        String semesterName = schedule.getSemesterName() != null ? schedule.getSemesterName() : "N/A";
        String dateRange = (schedule.getStartDate() != null ? schedule.getStartDate() : "N/A")
                + " — " + (schedule.getEndDate() != null ? schedule.getEndDate() : "N/A");

        Paragraph semester = new Paragraph("Semester: " + semesterName, sectionFont);
        semester.setAlignment(Element.ALIGN_CENTER);
        semester.setSpacingAfter(2);
        doc.add(semester);

        Paragraph dates = new Paragraph(dateRange, dataFont);
        dates.setAlignment(Element.ALIGN_CENTER);
        dates.setSpacingAfter(6);
        doc.add(dates);

        Paragraph generated = new Paragraph(
                "Generated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                subtitleFont
        );
        generated.setAlignment(Element.ALIGN_CENTER);
        generated.setSpacingAfter(30);
        doc.add(generated);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(60);
        infoTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        infoTable.setSpacingBefore(10);

        Map<String, Long> levelCount = schedule.getEntries().stream()
                .collect(Collectors.groupingBy(
                        e -> e.yearLevel() != null ? e.yearLevel() : "Other",
                        Collectors.counting()
                ));

        addInfoRow(infoTable, "Year Levels Included", String.join(", ", levelCount.keySet()), labelFont, dataFont);
        addInfoRow(infoTable, "Total Sections", String.valueOf(levelCount.values().stream().mapToLong(Long::longValue).sum()), labelFont, dataFont);
        addInfoRow(infoTable, "Total Schedule Entries", String.valueOf(schedule.getEntries().size()), labelFont, dataFont);
        addInfoRow(infoTable, "Hard Violations", String.valueOf(schedule.getHardViolations()), labelFont, dataFont);
        addInfoRow(infoTable, "Soft Violations", String.valueOf(schedule.getSoftViolations()), labelFont, dataFont);
        addInfoRow(infoTable, "Fitness Score", String.format("%.4f", schedule.getFitnessScore()), labelFont, dataFont);

        doc.add(infoTable);
    }

    private void addInfoRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(PdfPCell.NO_BORDER);
        labelCell.setPadding(6);
        labelCell.setMinimumHeight(24);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(PdfPCell.NO_BORDER);
        valueCell.setPadding(6);
        valueCell.setMinimumHeight(24);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private void addPageHeader(Document doc, String level, ScheduleDTO schedule) throws DocumentException {
        Font mainTitleFont = new Font(Font.HELVETICA, 22, Font.BOLD, HEADER_BG);
        Font deptFont = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(30, 64, 110));
        Font levelFont = new Font(Font.HELVETICA, 13, Font.BOLD, new Color(192, 57, 43));
        Font dateFont = new Font(Font.HELVETICA, 10, Font.NORMAL, GRAY_TEXT);

        Paragraph mainTitle = new Paragraph("UNIVERSITY STUDY SCHEDULE", mainTitleFont);
        mainTitle.setAlignment(Element.ALIGN_CENTER);
        mainTitle.setSpacingAfter(2);
        doc.add(mainTitle);

        Paragraph separator = new Paragraph("____________________________________________________",
                new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(156, 163, 175)));
        separator.setAlignment(Element.ALIGN_CENTER);
        separator.setSpacingAfter(6);
        doc.add(separator);

        Paragraph deptLine = new Paragraph("DEPARTMENT WEEKLY SCHEDULE", deptFont);
        deptLine.setAlignment(Element.ALIGN_CENTER);
        deptLine.setSpacingAfter(2);
        doc.add(deptLine);

        String semesterInfo = level + " — Semester: " + (schedule.getSemesterName() != null ? schedule.getSemesterName() : "N/A");
        Paragraph levelLine = new Paragraph(semesterInfo, levelFont);
        levelLine.setAlignment(Element.ALIGN_CENTER);
        levelLine.setSpacingAfter(2);
        doc.add(levelLine);

        String weekInfo = "For The Week: "
                + (schedule.getStartDate() != null ? schedule.getStartDate() : "N/A")
                + " — "
                + (schedule.getEndDate() != null ? schedule.getEndDate() : "N/A");
        Paragraph weekLine = new Paragraph(weekInfo, dateFont);
        weekLine.setAlignment(Element.ALIGN_CENTER);
        weekLine.setSpacingAfter(12);
        doc.add(weekLine);
    }

    private PdfPTable buildScheduleTable(WeeklyScheduleDTO weekly) throws DocumentException {
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2.0f, 3.2f, 3.2f, 3.2f, 3.2f, 3.2f, 3.2f});

        addHeaderCell(table, "TIME");
        for (String day : ORDERED_DAYS) {
            addHeaderCell(table, day);
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

            if (!breakAdded && hasAfternoonSlots && timeKey.compareTo("12:00") >= 0) {
                addBreakRow(table);
                breakAdded = true;
            }

            String displayTime = formatTimeAmPm(timeKey);
            PdfPCell timeCell = buildTimeCell(displayTime, i);
            table.addCell(timeCell);

            for (String day : ORDERED_DAYS) {
                SlotDTO slot = dataMap.getOrDefault(day, Map.of()).get(timeKey);
                table.addCell(buildEntryCell(slot, i));
            }
        }

        return table;
    }

    private void addHeaderCell(PdfPTable table, String text) {
        Font f = new Font(Font.HELVETICA, 11, Font.BOLD, HEADER_FG);
        PdfPCell cell = new PdfPCell(new Phrase(text, f));
        cell.setBackgroundColor(HEADER_BG);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8);
        cell.setMinimumHeight(38);
        cell.setBorderColor(new Color(71, 85, 105));
        cell.setBorderWidth(1);
        table.addCell(cell);
    }

    private PdfPCell buildTimeCell(String text, int rowIndex) {
        Font f = new Font(Font.HELVETICA, 9, Font.BOLD, TIME_FG);
        Color bg = rowIndex % 2 == 0 ? ROW_EVEN : ROW_ODD;
        PdfPCell cell = new PdfPCell(new Phrase(text, f));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        cell.setMinimumHeight(55);
        cell.setBorderColor(BORDER_COLOR);
        return cell;
    }

    private PdfPCell buildEntryCell(SlotDTO slot, int rowIndex) {
        if (slot == null || slot.entry() == null) {
            PdfPCell empty = new PdfPCell(new Phrase(""));
            empty.setBackgroundColor(rowIndex % 2 == 0 ? ROW_EVEN : ROW_ODD);
            empty.setMinimumHeight(55);
            empty.setBorderColor(BORDER_COLOR);
            return empty;
        }

        ScheduleEntryDTO e = slot.entry();

        String sessionType = e.sessionType();
        boolean isLecture = sessionType == null
                || (!sessionType.equalsIgnoreCase("LAB")
                && !sessionType.equalsIgnoreCase("SECTION")
                && !sessionType.equalsIgnoreCase("TUTORIAL"));

        Color bg = isLecture ? LECTURE_BG : SECTION_BG;
        Color fg = isLecture ? LECTURE_FG : SECTION_FG;
        Color border = isLecture ? new Color(147, 197, 253) : new Color(134, 239, 172);

        Font courseFont  = new Font(Font.HELVETICA, 9,  Font.BOLD,   fg);
        Font nameFont    = new Font(Font.HELVETICA, 8,  Font.NORMAL, fg);
        Font roomFont    = new Font(Font.HELVETICA, 7,  Font.ITALIC, GRAY_TEXT);
        Font typeFont    = new Font(Font.HELVETICA, 7,  Font.NORMAL, GRAY_TEXT);

        Phrase content = new Phrase();
        content.add(new Chunk(e.courseCode() + " - " + e.courseName() + "\n", courseFont));

        String instructorDisplay = e.instructorName();
        content.add(new Chunk(instructorDisplay + "\n", nameFont));

        String hallLabel = isLecture ? "Hall: " : "Lab: ";
        content.add(new Chunk(hallLabel + e.roomNumber() + "\n", roomFont));

        String sessionLabel;
        if ("LAB".equalsIgnoreCase(sessionType)) {
            sessionLabel = "(Lab)";
        } else if ("SECTION".equalsIgnoreCase(sessionType) || "TUTORIAL".equalsIgnoreCase(sessionType)) {
            sessionLabel = "(Section)";
        } else {
            sessionLabel = "(Lecture)";
        }
        content.add(new Chunk(sessionLabel, typeFont));

        PdfPCell cell = new PdfPCell(content);
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setMinimumHeight(55);
        cell.setBorderColor(border);
        cell.setBorderWidth(1);
        return cell;
    }

    private void addBreakRow(PdfPTable table) {
        Font f = new Font(Font.HELVETICA, 11, Font.BOLD, BREAK_FG);

        PdfPCell timeCell = new PdfPCell(new Phrase("BREAK", f));
        timeCell.setBackgroundColor(BREAK_BG);
        timeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        timeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        timeCell.setPadding(6);
        timeCell.setMinimumHeight(28);
        timeCell.setBorderColor(BORDER_COLOR);
        table.addCell(timeCell);

        for (int i = 0; i < 6; i++) {
            PdfPCell cell = new PdfPCell(new Phrase("BREAK", f));
            cell.setBackgroundColor(BREAK_BG);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(6);
            cell.setMinimumHeight(28);
            cell.setBorderColor(BORDER_COLOR);
            table.addCell(cell);
        }
    }

    private PdfPTable buildLegend() throws DocumentException {
        Font notesFont = new Font(Font.HELVETICA, 9, Font.NORMAL, GRAY_TEXT);
        Font titleFont = new Font(Font.HELVETICA, 9, Font.BOLD, HEADER_BG);

        Paragraph notes = new Paragraph();
        notes.add(new Chunk("Notes:\n", titleFont));
        notes.add(new Chunk("  \u2022 Lectures are held in Main Hall.\n", notesFont));
        notes.add(new Chunk("  \u2022 Sections (Sakan) are led by Teaching Assistants.\n\n", notesFont));

        PdfPTable legend = new PdfPTable(6);
        legend.setWidthPercentage(65);
        legend.setHorizontalAlignment(Element.ALIGN_LEFT);
        legend.setWidths(new float[]{1f, 4f, 1f, 4f, 1f, 3f});
        legend.setSpacingBefore(4);

        addLegendBox(legend, LECTURE_BG);
        addLegendText(legend, "Lecture (Main Hall)", notesFont);

        addLegendBox(legend, SECTION_BG);
        addLegendText(legend, "Section (Sakan)", notesFont);

        addLegendBox(legend, BREAK_BG);
        addLegendText(legend, "Break", notesFont);

        Paragraph container = new Paragraph();
        container.add(notes);
        container.add(legend);

        PdfPTable wrapper = new PdfPTable(1);
        wrapper.setWidthPercentage(100);
        PdfPCell wrapperCell = new PdfPCell(container);
        wrapperCell.setBorder(PdfPCell.NO_BORDER);
        wrapper.addCell(wrapperCell);

        return wrapper;
    }

    private void addLegendBox(PdfPTable table, Color color) {
        PdfPCell cell = new PdfPCell(new Phrase(" "));
        cell.setBackgroundColor(color);
        cell.setFixedHeight(16);
        cell.setBorderColor(new Color(180, 180, 180));
        table.addCell(cell);
    }

    private void addLegendText(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase("  " + text, font));
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(PdfPCell.NO_BORDER);
        table.addCell(cell);
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
                Font f = new Font(Font.HELVETICA, 8, Font.ITALIC, new Color(156, 163, 175));
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
