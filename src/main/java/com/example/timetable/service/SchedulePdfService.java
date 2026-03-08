package com.example.timetable.service;

import com.example.timetable.dto.response.DayScheduleDTO;
import com.example.timetable.dto.response.ScheduleDTO;
import com.example.timetable.dto.response.SlotDTO;
import com.example.timetable.dto.response.WeeklyScheduleDTO;
import com.example.timetable.mapper.WeeklyScheduleMapper;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class SchedulePdfService {

    public byte[] generatePdf(ScheduleDTO schedule) {

        try {

            WeeklyScheduleDTO weekly =
                    WeeklyScheduleMapper.toWeeklyTable(schedule);

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4.rotate());

            PdfWriter.getInstance(document, out);

            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font infoFont = new Font(Font.HELVETICA, 12);
            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font cellFont = new Font(Font.HELVETICA, 11);

            Paragraph title =
                    new Paragraph("University Weekly Timetable", titleFont);

            title.setAlignment(Element.ALIGN_CENTER);

            document.add(title);
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Schedule ID: " + schedule.getId(), infoFont));
            document.add(new Paragraph("Created At: " + schedule.getCreatedAt(), infoFont));
            document.add(new Paragraph("Hard Violations: " + schedule.getHardViolations(), infoFont));
            document.add(new Paragraph("Soft Violations: " + schedule.getSoftViolations(), infoFont));
            document.add(new Paragraph("Fitness Score: " + schedule.getFitnessScore(), infoFont));

            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(5);

            table.setWidthPercentage(100);

            table.addCell(header("Day", headerFont));
            table.addCell(header("Course", headerFont));
            table.addCell(header("Instructor", headerFont));
            table.addCell(header("Room", headerFont));
            table.addCell(header("Time", headerFont));

            for (DayScheduleDTO day : weekly.days()) {

                for (SlotDTO slot : day.slots()) {

                    if (slot.entry() == null) continue;

                    table.addCell(cell(day.day(), cellFont));

                    table.addCell(cell(
                            slot.entry().courseCode() +
                                    " - " +
                                    slot.entry().courseName(),
                            cellFont
                    ));

                    table.addCell(cell(
                            slot.entry().instructorName(),
                            cellFont
                    ));

                    table.addCell(cell(
                            slot.entry().roomNumber(),
                            cellFont
                    ));

                    String time =
                            slot.startTime() +
                                    " - " +
                                    slot.endTime();

                    table.addCell(cell(time, cellFont));
                }
            }

            document.add(table);

            document.close();

            return out.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException("Failed to generate schedule PDF", e);

        }
    }

    private PdfPCell header(String text, Font font) {

        PdfPCell cell = new PdfPCell(new Phrase(text, font));

        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8);

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