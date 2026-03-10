package com.example.timetable.service;

import com.example.timetable.dto.response.DayScheduleDTO;
import com.example.timetable.dto.response.ScheduleDTO;
import com.example.timetable.dto.response.SlotDTO;
import com.example.timetable.dto.response.WeeklyScheduleDTO;
import com.example.timetable.mapper.WeeklyScheduleMapper;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
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
                    WeeklyScheduleMapper.toDepartmentTables(schedule);

            Document document = new Document(PageSize.A4.rotate());

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            PdfWriter.getInstance(document, out);

            document.open();

            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD);
            Font sectionFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font cellFont = new Font(Font.HELVETICA, 10);

            Paragraph title =
                    new Paragraph("University Weekly Timetable", titleFont);

            title.setAlignment(Element.ALIGN_CENTER);

            document.add(title);
            document.add(Chunk.NEWLINE);

            for (var entry : departmentTables.entrySet()) {

                String department = entry.getKey();
                WeeklyScheduleDTO weekly = entry.getValue();

                Paragraph sectionTitle =
                        new Paragraph(
                                department + " (SEM 1)",
                                sectionFont
                        );

                sectionTitle.setSpacingBefore(15);
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

        Set<String> timeRanges = new TreeSet<>();

        for (DayScheduleDTO day : weekly.days()) {

            for (SlotDTO slot : day.slots()) {

                String range =
                        slot.startTime() + "-" + slot.endTime();

                timeRanges.add(range);
            }
        }

        List<String> orderedTimes =
                new ArrayList<>(timeRanges);

        PdfPTable table =
                new PdfPTable(orderedTimes.size() + 1);

        table.setWidthPercentage(100);

        table.addCell(header("Day", headerFont));

        for (String time : orderedTimes) {

            table.addCell(header(time, headerFont));
        }

        for (DayScheduleDTO day : weekly.days()) {

            table.addCell(header(day.day(), headerFont));

            Map<String, SlotDTO> slotMap = new HashMap<>();

            for (SlotDTO slot : day.slots()) {

                String key =
                        slot.startTime() + "-" + slot.endTime();

                slotMap.put(key, slot);
            }

            for (String time : orderedTimes) {

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
    }private PdfPCell header(String text, Font font) {

        PdfPCell cell = new PdfPCell(new Phrase(text, font));

        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8);

        cell.setBackgroundColor(Color.LIGHT_GRAY);

        return cell;
    }


    private PdfPCell cell(String text, Font font) {

        PdfPCell cell = new PdfPCell(new Phrase(text, font));

        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);

        return cell;
    }
}