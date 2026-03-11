package com.example.timetable.service;

import com.example.timetable.dto.response.*;
import com.example.timetable.mapper.WeeklyScheduleMapper;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;

import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.List;

@Service
public class SchedulePdfService {

    public byte[] generatePdf(ScheduleDTO schedule) {

        try {

            Map<String, WeeklyScheduleDTO> departmentTables =
                    WeeklyScheduleMapper.toLevelTables(schedule);

            Document document = new Document(PageSize.A4.rotate());

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            PdfWriter.getInstance(document, out);

            document.open();

            Font titleFont = new Font(Font.HELVETICA, 22, Font.BOLD);
            Font sectionFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font cellFont = new Font(Font.HELVETICA, 11);

            Paragraph title =
                    new Paragraph("University Weekly Timetable", titleFont);

            title.setAlignment(Element.ALIGN_CENTER);

            document.add(title);
            document.add(Chunk.NEWLINE);

            List<String> orderedLevels = List.of(
                    "First Year",
                    "Second Year",
                    "Third Year",
                    "Fourth Year"
            );

            boolean firstPage = true;

            for (String level : orderedLevels) {

                WeeklyScheduleDTO weekly = departmentTables.get(level);

                if (weekly == null) continue;

                if (!firstPage) {
                    document.newPage();
                }

                firstPage = false;

                Paragraph sectionTitle =
                        new Paragraph(level + " (SEM 1)", sectionFont);

                sectionTitle.setSpacingBefore(10);
                sectionTitle.setSpacingAfter(10);

                document.add(sectionTitle);

                PdfPTable table =
                        buildTable(weekly, headerFont, cellFont);

                document.add(table);
            }

            document.close();

            return out.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException("Failed to generate schedule PDF", e);
        }
    }


    private PdfPTable buildTable(
            WeeklyScheduleDTO weekly,
            Font headerFont,
            Font cellFont
    ) {

        List<String> orderedTimes = List.of(
                "09:00-10:00",
                "10:00-11:00",
                "11:00-12:00",
                "BREAK",
                "13:00-14:00",
                "14:00-15:00",
                "15:00-16:00"
        );

        List<String> orderedDays = List.of(
                "MONDAY",
                "TUESDAY",
                "WEDNESDAY",
                "THURSDAY",
                "FRIDAY",
                "SATURDAY",
                "SUNDAY"
        );

        PdfPTable table = new PdfPTable(orderedTimes.size() + 1);

        table.setWidthPercentage(100);

        float[] columnWidths = new float[orderedTimes.size() + 1];

        columnWidths[0] = 4f;

        for (int i = 1; i < columnWidths.length; i++) {
            columnWidths[i] = 3f;
        }

        table.setWidths(columnWidths);

        table.addCell(header("Day", headerFont));

        for (String time : orderedTimes) {

            table.addCell(header(time, headerFont));
        }

        Map<String, Map<String, SlotDTO>> data = new HashMap<>();

        for (DayScheduleDTO day : weekly.days()) {

            Map<String, SlotDTO> slotMap = new HashMap<>();

            for (SlotDTO slot : day.slots()) {

                String column = normalizeTime(slot.startTime());

                slotMap.put(column, slot);
            }

            data.put(day.day(), slotMap);
        }

        for (String day : orderedDays) {

            PdfPCell dayCell = header(day, headerFont);
            dayCell.setNoWrap(true);

            table.addCell(dayCell);

            Map<String, SlotDTO> slotMap =
                    data.getOrDefault(day, new HashMap<>());

            for (String time : orderedTimes) {

                if (time.equals("BREAK")) {

                    PdfPCell breakCell = new PdfPCell(new Phrase(""));
                    breakCell.setMinimumHeight(45);
                    breakCell.setBackgroundColor(new Color(230,230,230));

                    table.addCell(breakCell);

                    continue;
                }

                SlotDTO slot = slotMap.get(time);

                if (slot == null || slot.entry() == null) {

                    table.addCell(cell("", cellFont));

                } else {

                    String text =
                            slot.entry().courseCode()
                                    + "\n"
                                    + slot.entry().instructorName()
                                    + "\n"
                                    + slot.entry().roomNumber();

                    table.addCell(cell(text, cellFont));
                }
            }
        }

        return table;
    }


    private PdfPCell header(String text, Font font) {

        PdfPCell cell = new PdfPCell(new Phrase(text, font));

        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        cell.setPadding(10);

        cell.setBackgroundColor(new Color(200,200,200));

        return cell;
    }


    private PdfPCell cell(String text, Font font) {

        PdfPCell cell = new PdfPCell(new Phrase(text, font));

        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        cell.setPadding(8);

        cell.setMinimumHeight(45);

        return cell;
    }


    private String normalizeTime(String start) {

        if (start.startsWith("08") || start.startsWith("09"))
            return "09:00-10:00";

        if (start.startsWith("10"))
            return "10:00-11:00";

        if (start.startsWith("11") || start.startsWith("12"))
            return "11:00-12:00";

        if (start.startsWith("13"))
            return "13:00-14:00";

        if (start.startsWith("14"))
            return "14:00-15:00";

        return "15:00-16:00";
    }
}