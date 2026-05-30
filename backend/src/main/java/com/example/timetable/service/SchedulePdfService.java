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

    private static final Color NAVY        = new Color(26, 53, 96);
    private static final Color WHITE       = Color.WHITE;
    private static final Color LECTURE_BG  = new Color(219, 234, 254);
    private static final Color LECTURE_FG  = new Color(29, 78, 216);
    private static final Color LECTURE_BORDER = new Color(37, 99, 235);
    private static final Color SECTION_BG  = new Color(220, 252, 231);
    private static final Color SECTION_FG  = new Color(21, 128, 61);
    private static final Color SECTION_BORDER = new Color(34, 197, 94);
    private static final Color LAB_BG      = new Color(254, 243, 199);
    private static final Color LAB_FG      = new Color(146, 64, 14);
    private static final Color LAB_BORDER  = new Color(234, 179, 8);
    private static final Color BREAK_BG    = new Color(243, 244, 246);
    private static final Color BREAK_FG    = new Color(107, 114, 128);
    private static final Color ROW_EVEN    = new Color(249, 250, 251);
    private static final Color ROW_ODD     = WHITE;
    private static final Color BORDER      = new Color(209, 213, 219);
    private static final Color GRAY_TEXT   = new Color(107, 114, 128);
    private static final Color ACCENT_LINE = new Color(37, 99, 235);

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
            }

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate schedule PDF", e);
        }
    }

    private void addSummaryPage(Document doc, ScheduleDTO schedule) throws DocumentException {
        Font titleFont   = new Font(Font.HELVETICA, 26, Font.BOLD, NAVY);
        Font deptFont    = new Font(Font.HELVETICA, 12, Font.NORMAL, GRAY_TEXT);
        Font sectionFont = new Font(Font.HELVETICA, 14, Font.BOLD, new Color(192, 57, 43));
        Font dataFont    = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(55, 65, 81));
        Font labelFont   = new Font(Font.HELVETICA, 11, Font.BOLD, NAVY);
        Font statValFont  = new Font(Font.HELVETICA, 18, Font.BOLD, NAVY);
        Font statLabelFont = new Font(Font.HELVETICA, 9, Font.NORMAL, GRAY_TEXT);

        Paragraph title = new Paragraph("DEPARTMENT WEEKLY SCHEDULE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(2);
        doc.add(title);

        Paragraph accentLine = new Paragraph("____________________________________________________",
                new Font(Font.HELVETICA, 8, Font.NORMAL, ACCENT_LINE));
        accentLine.setAlignment(Element.ALIGN_CENTER);
        accentLine.setSpacingAfter(16);
        doc.add(accentLine);

        String semesterName = schedule.getSemesterName() != null ? schedule.getSemesterName() : "N/A";
        String dateRange = (schedule.getStartDate() != null ? schedule.getStartDate() : "N/A")
                + " — " + (schedule.getEndDate() != null ? schedule.getEndDate() : "N/A");

        Paragraph semester = new Paragraph("Semester: " + semesterName, sectionFont);
        semester.setAlignment(Element.ALIGN_CENTER);
        semester.setSpacingAfter(2);
        doc.add(semester);

        Paragraph dates = new Paragraph(dateRange, dataFont);
        dates.setAlignment(Element.ALIGN_CENTER);
        dates.setSpacingAfter(4);
        doc.add(dates);

        Paragraph generated = new Paragraph(
                "Generated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                deptFont
        );
        generated.setAlignment(Element.ALIGN_CENTER);
        generated.setSpacingAfter(24);
        doc.add(generated);

        Map<String, Long> levelCount = schedule.getEntries().stream()
                .collect(Collectors.groupingBy(
                        e -> e.yearLevel() != null ? e.yearLevel() : "Other",
                        Collectors.counting()
                ));

        long totalEntries = levelCount.values().stream().mapToLong(Long::longValue).sum();
        long totalSections = schedule.getEntries().stream()
                .map(ScheduleEntryDTO::sectionId)
                .distinct()
                .count();

        long lectureCount = schedule.getEntries().stream()
                .filter(e -> e.sessionType() == null || "LECTURE".equalsIgnoreCase(e.sessionType()))
                .count();
        long labCount = schedule.getEntries().stream()
                .filter(e -> "LAB".equalsIgnoreCase(e.sessionType()))
                .count();
        long sectionCount = schedule.getEntries().stream()
                .filter(e -> "SECTION".equalsIgnoreCase(e.sessionType()) || "TUTORIAL".equalsIgnoreCase(e.sessionType()))
                .count();

        PdfPTable statsRow = new PdfPTable(4);
        statsRow.setWidthPercentage(70);
        statsRow.setHorizontalAlignment(Element.ALIGN_CENTER);
        statsRow.setSpacingBefore(4);
        statsRow.setSpacingAfter(20);
        statsRow.setWidths(new float[]{1, 1, 1, 1});

        addStatCell(statsRow, String.valueOf(totalSections), "Total Sections", statValFont, statLabelFont);
        addStatCell(statsRow, String.valueOf(lectureCount), "Lectures", statValFont, statLabelFont);
        addStatCell(statsRow, String.valueOf(labCount), "Labs", statValFont, statLabelFont);
        addStatCell(statsRow, String.valueOf(sectionCount), "Sections/Tuts", statValFont, statLabelFont);

        doc.add(statsRow);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(60);
        infoTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        infoTable.setSpacingBefore(4);

        PdfPCell infoHeaderCell = new PdfPCell(new Phrase("Schedule Overview", new Font(Font.HELVETICA, 12, Font.BOLD, NAVY)));
        infoHeaderCell.setColspan(2);
        infoHeaderCell.setBorder(PdfPCell.NO_BORDER);
        infoHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        infoHeaderCell.setPadding(6);
        infoTable.addCell(infoHeaderCell);

        addInfoRow(infoTable, "Year Levels Included", String.join(", ", levelCount.keySet()), labelFont, dataFont);
        addInfoRow(infoTable, "Total Schedule Entries", String.valueOf(totalEntries), labelFont, dataFont);
        addInfoRow(infoTable, "Hard Violations", String.valueOf(schedule.getHardViolations()), labelFont, dataFont);
        addInfoRow(infoTable, "Soft Violations", String.valueOf(schedule.getSoftViolations()), labelFont, dataFont);
        addInfoRow(infoTable, "Fitness Score", String.format("%.4f", schedule.getFitnessScore()), labelFont, dataFont);

        doc.add(infoTable);

        if (schedule.getUnscheduledSectionIds() != null && !schedule.getUnscheduledSectionIds().isEmpty()) {
            Paragraph warnTitle = new Paragraph("\nUnscheduled Sections",
                    new Font(Font.HELVETICA, 11, Font.BOLD, new Color(192, 57, 43)));
            warnTitle.setSpacingBefore(16);
            warnTitle.setSpacingAfter(4);
            doc.add(warnTitle);

            Paragraph warnBody = new Paragraph(
                    schedule.getUnscheduledSectionIds().size()
                            + " section(s) could not be scheduled due to no available room or time slot.",
                    dataFont
            );
            doc.add(warnBody);
        }
    }

    private void addStatCell(PdfPTable table, String value, String label, Font valFont, Font labelFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);

        Paragraph valP = new Paragraph(value, valFont);
        valP.setAlignment(Element.ALIGN_CENTER);
        valP.setSpacingAfter(2);
        cell.addElement(valP);

        Paragraph labelP = new Paragraph(label, labelFont);
        labelP.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(labelP);

        table.addCell(cell);
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
        Font mainTitleFont = new Font(Font.HELVETICA, 20, Font.BOLD, NAVY);
        Font levelFont     = new Font(Font.HELVETICA, 13, Font.BOLD, new Color(192, 57, 43));
        Font dateFont      = new Font(Font.HELVETICA, 10, Font.NORMAL, GRAY_TEXT);

        Paragraph mainTitle = new Paragraph("UNIVERSITY STUDY SCHEDULE", mainTitleFont);
        mainTitle.setAlignment(Element.ALIGN_CENTER);
        mainTitle.setSpacingAfter(2);
        doc.add(mainTitle);

        Paragraph accentLine = new Paragraph("____________________________________________________",
                new Font(Font.HELVETICA, 8, Font.NORMAL, ACCENT_LINE));
        accentLine.setAlignment(Element.ALIGN_CENTER);
        accentLine.setSpacingAfter(6);
        doc.add(accentLine);

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
        weekLine.setSpacingAfter(10);
        doc.add(weekLine);
    }

    private PdfPTable buildScheduleTable(WeeklyScheduleDTO weekly) throws DocumentException {
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2.0f, 3.2f, 3.2f, 3.2f, 3.2f, 3.2f, 3.2f});

        addHeaderCell(table, "Time");
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
        Font f = new Font(Font.HELVETICA, 11, Font.BOLD, WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, f));
        cell.setBackgroundColor(NAVY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8);
        cell.setMinimumHeight(38);
        cell.setBorderColor(new Color(71, 85, 105));
        cell.setBorderWidth(1);
        table.addCell(cell);
    }

    private PdfPCell buildTimeCell(String text, int rowIndex) {
        Font f = new Font(Font.HELVETICA, 9, Font.BOLD, WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, f));
        cell.setBackgroundColor(NAVY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        cell.setMinimumHeight(70);
        cell.setBorderColor(BORDER);
        return cell;
    }

    private PdfPCell buildEntryCell(SlotDTO slot, int rowIndex) {
        if (slot == null || slot.entry() == null) {
            PdfPCell empty = new PdfPCell(new Phrase(""));
            empty.setBackgroundColor(rowIndex % 2 == 0 ? ROW_EVEN : ROW_ODD);
            empty.setMinimumHeight(70);
            empty.setBorderColor(BORDER);
            return empty;
        }

        ScheduleEntryDTO e = slot.entry();
        String sessionType = e.sessionType();

        boolean isLecture = sessionType == null || "LECTURE".equalsIgnoreCase(sessionType);
        boolean isLab = "LAB".equalsIgnoreCase(sessionType);
        boolean isSection = "SECTION".equalsIgnoreCase(sessionType) || "TUTORIAL".equalsIgnoreCase(sessionType);

        Color bg, fg, leftBorder;
        if (isLab) {
            bg = LAB_BG; fg = LAB_FG; leftBorder = LAB_BORDER;
        } else if (isSection) {
            bg = SECTION_BG; fg = SECTION_FG; leftBorder = SECTION_BORDER;
        } else {
            bg = LECTURE_BG; fg = LECTURE_FG; leftBorder = LECTURE_BORDER;
        }

        Font courseFont = new Font(Font.HELVETICA, 9, Font.BOLD, fg);
        Font nameFont   = new Font(Font.HELVETICA, 8, Font.NORMAL, fg);
        Font roomFont   = new Font(Font.HELVETICA, 7, Font.ITALIC, GRAY_TEXT);
        Font timeFont   = new Font(Font.HELVETICA, 7, Font.NORMAL, GRAY_TEXT);

        Phrase content = new Phrase();
        content.add(new Chunk(e.courseCode() + " - " + e.courseName() + "\n", courseFont));

        String instructorDisplay = e.instructorName();
        if (instructorDisplay.length() > 20) {
            instructorDisplay = instructorDisplay.substring(0, 18) + "..";
        }
        content.add(new Chunk(instructorDisplay + "\n", nameFont));

        String roomLabel = isLab ? "Lab: " : "Rm: ";
        content.add(new Chunk(roomLabel + e.roomNumber() + "\n", roomFont));

        String timeRange = e.startTime() + " - " + e.endTime();
        content.add(new Chunk(timeRange, timeFont));

        PdfPCell cell = new PdfPCell(content);
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setMinimumHeight(70);
        cell.setBorderColor(BORDER);
        cell.setBorderWidth(1);

        cell.setBorderWidthLeft(4);
        cell.setBorderColorLeft(leftBorder);

        return cell;
    }

    private void addBreakRow(PdfPTable table) {
        Font f = new Font(Font.HELVETICA, 9, Font.BOLD, BREAK_FG);

        PdfPCell timeCell = new PdfPCell(new Phrase("BREAK", f));
        timeCell.setBackgroundColor(NAVY);
        timeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        timeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        timeCell.setPadding(4);
        timeCell.setMinimumHeight(28);
        timeCell.setBorderColor(BORDER);
        table.addCell(timeCell);

        for (int i = 0; i < 6; i++) {
            PdfPCell cell = new PdfPCell(new Phrase("BREAK", f));
            cell.setBackgroundColor(BREAK_BG);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(4);
            cell.setMinimumHeight(28);
            cell.setBorderColor(BORDER);
            table.addCell(cell);
        }
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
