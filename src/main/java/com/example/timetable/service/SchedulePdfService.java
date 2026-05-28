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

    private static final Color HEADER_BG  = new Color(26, 58, 108);
    private static final Color HEADER_FG  = Color.WHITE;
    private static final Color LECTURE_BG = new Color(210, 228, 250);
    private static final Color LECTURE_FG = new Color(26, 58, 108);
    private static final Color LAB_BG     = new Color(208, 240, 192);
    private static final Color LAB_FG     = new Color(30, 100, 30);
    private static final Color BREAK_BG   = new Color(253, 235, 208);
    private static final Color BREAK_FG   = new Color(180, 80, 20);
    private static final Color ZEBRA_BG   = new Color(248, 249, 250);
    private static final Color GRAY_LINE  = new Color(200, 200, 200);

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

            List<String> levels = List.of("First Year", "Second Year", "Third Year", "Fourth Year");
            boolean firstPage = true;

            for (String level : levels) {
                WeeklyScheduleDTO weekly = levelTables.get(level);
                if (weekly == null) continue;

                if (!firstPage) document.newPage();
                firstPage = false;

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

    private void addPageHeader(Document doc, String level, ScheduleDTO schedule) throws DocumentException {
        Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(26, 58, 108));
        Font subFont   = new Font(Font.HELVETICA, 14, Font.BOLD, new Color(192, 57, 43));
        Font dateFont  = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(100, 100, 100));

        Paragraph title = new Paragraph("DEPARTMENT WEEKLY SCHEDULE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        String semesterInfo = level + " — Semester: " + schedule.getStatus();
        Paragraph sub = new Paragraph(semesterInfo, subFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingBefore(2);
        doc.add(sub);

        Paragraph dateLine = new Paragraph(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")), dateFont);
        dateLine.setAlignment(Element.ALIGN_CENTER);
        dateLine.setSpacingBefore(2);
        dateLine.setSpacingAfter(6);
        doc.add(dateLine);

        Paragraph separator = new Paragraph("_______________________________________________________________________________",
                new Font(Font.HELVETICA, 8, Font.NORMAL, GRAY_LINE));
        separator.setAlignment(Element.ALIGN_CENTER);
        separator.setSpacingAfter(10);
        doc.add(separator);
    }

    private PdfPTable buildScheduleTable(WeeklyScheduleDTO weekly) throws DocumentException {
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 3f, 3f, 3f, 3f, 3f, 3f});

        addHeaderCell(table, "TIME");
        for (String day : ORDERED_DAYS) {
            addHeaderCell(table, day.substring(0, 3));
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

        boolean isBreakAdded = false;

        for (int i = 0; i < orderedTimes.size(); i++) {
            String timeKey = orderedTimes.get(i);

            if (!isBreakAdded && timeKey.compareTo("12:00") >= 0) {
                addBreakRow(table);
                isBreakAdded = true;
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
        cell.setPadding(10);
        cell.setMinimumHeight(35);
        cell.setBorderColor(new Color(180, 190, 200));
        table.addCell(cell);
    }

    private PdfPCell buildTimeCell(String text, int rowIndex) {
        Font f = new Font(Font.HELVETICA, 9, Font.BOLD, new Color(60, 60, 60));
        Color bg = rowIndex % 2 == 0 ? new Color(240, 244, 248) : new Color(230, 237, 245);
        PdfPCell cell = new PdfPCell(new Phrase(text, f));
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        cell.setMinimumHeight(50);
        cell.setBorderColor(new Color(220, 220, 220));
        return cell;
    }

    private PdfPCell buildEntryCell(SlotDTO slot, int rowIndex) {
        if (slot == null || slot.entry() == null) {
            PdfPCell empty = new PdfPCell(new Phrase(""));
            empty.setBackgroundColor(rowIndex % 2 == 0 ? Color.WHITE : ZEBRA_BG);
            empty.setMinimumHeight(50);
            empty.setBorderColor(new Color(220, 220, 220));
            return empty;
        }

        ScheduleEntryDTO e = slot.entry();

        String sessionType = e.sessionType();
        boolean isLecture = sessionType == null || !sessionType.equals("LAB");
        Color bg = isLecture ? LECTURE_BG : LAB_BG;
        Color fg = isLecture ? LECTURE_FG : LAB_FG;

        Font courseFont  = new Font(Font.HELVETICA, 9,  Font.BOLD,   fg);
        Font nameFont    = new Font(Font.HELVETICA, 8,  Font.NORMAL, fg);
        Font roomFont    = new Font(Font.HELVETICA, 7,  Font.ITALIC, new Color(100, 100, 100));

        Phrase content = new Phrase();
        content.add(new Chunk(e.courseCode() + " - " + e.courseName() + "\n", courseFont));
        content.add(new Chunk("Dr. " + e.instructorName() + "\n", nameFont));
        String hallLabel = isLecture ? "Hall: " : "Lab: ";
        content.add(new Chunk(hallLabel + e.roomNumber(), roomFont));

        PdfPCell cell = new PdfPCell(content);
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setMinimumHeight(50);
        cell.setBorderColor(new Color(200, 210, 220));
        return cell;
    }

    private void addBreakRow(PdfPTable table) {
        Font f = new Font(Font.HELVETICA, 10, Font.BOLD, BREAK_FG);

        PdfPCell timeCell = new PdfPCell(new Phrase("BREAK", f));
        timeCell.setBackgroundColor(BREAK_BG);
        timeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        timeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        timeCell.setPadding(6);
        timeCell.setMinimumHeight(30);
        timeCell.setBorderColor(new Color(220, 220, 220));
        table.addCell(timeCell);

        for (int i = 0; i < 6; i++) {
            PdfPCell cell = new PdfPCell(new Phrase("BREAK", f));
            cell.setBackgroundColor(BREAK_BG);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(6);
            cell.setMinimumHeight(30);
            cell.setBorderColor(new Color(220, 220, 220));
            table.addCell(cell);
        }
    }

    private PdfPTable buildLegend() throws DocumentException {
        PdfPTable legend = new PdfPTable(6);
        legend.setWidthPercentage(65);
        legend.setHorizontalAlignment(Element.ALIGN_LEFT);
        legend.setWidths(new float[]{1f, 3f, 1f, 3f, 1f, 2f});

        Font legendFont = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(50, 50, 50));

        addLegendBox(legend, LECTURE_BG);
        addLegendText(legend, "Lecture (Main Hall)", legendFont);

        addLegendBox(legend, LAB_BG);
        addLegendText(legend, "Section / Lab", legendFont);

        addLegendBox(legend, BREAK_BG);
        addLegendText(legend, "Break", legendFont);

        return legend;
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
                Font f = new Font(Font.HELVETICA, 8, Font.ITALIC, new Color(150, 150, 150));
                String footer = "Generated by TimetableScheduler — "
                        + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
                Phrase p = new Phrase(footer, f);
                PdfContentByte cb = writer.getDirectContent();
                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, p,
                        (document.left() + document.right()) / 2,
                        document.bottom() - 10, 0);
            } catch (Exception ignored) {}
        }
    }
}
