package com.example.timetable.service;

import com.example.timetable.dto.response.ScheduleDTO;
import com.example.timetable.service.ScheduleRenderModel.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class SchedulePdfService {

    private static final List<String> ORDERED_LEVELS = List.of(
            "First Year", "Second Year", "Third Year", "Fourth Year"
    );

    private static final float MARGIN = 24f;

    public byte[] generatePdf(ScheduleDTO schedule) {
        return exportAll(schedule, ColorTheme.NAVY);
    }

    public byte[] exportPdf(ScheduleDTO schedule, ColorTheme theme) {
        return exportAll(schedule, theme);
    }

    public byte[] exportPdf(ScheduleDTO schedule, String yearLevel, ColorTheme theme) {
        return exportSingle(schedule, yearLevel, theme);
    }

    private byte[] exportAll(ScheduleDTO schedule, ColorTheme theme) {
        try {
            Document document = new Document(PageSize.A3.rotate(), MARGIN, MARGIN, 48, 48);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new FooterEvent(theme));

            document.open();

            List<ScheduleRenderModel> models = ScheduleRenderModel.forAllYears(schedule, theme);
            boolean first = true;
            for (ScheduleRenderModel model : models) {
                if (model.isEmpty()) continue;
                if (!first) document.newPage();
                first = false;
                renderModel(document, model);
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate schedule PDF", e);
        }
    }

    private byte[] exportSingle(ScheduleDTO schedule, String yearLevel, ColorTheme theme) {
        try {
            Document document = new Document(PageSize.A3.rotate(), MARGIN, MARGIN, 48, 48);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new FooterEvent(theme));

            document.open();

            if (yearLevel == null) {
                List<ScheduleRenderModel> models = ScheduleRenderModel.forAllYears(schedule, theme);
                if (!models.isEmpty()) {
                    renderModel(document, models.get(0));
                }
            } else {
                ScheduleRenderModel model = ScheduleRenderModel.forYear(schedule, yearLevel, theme);
                if (!model.isEmpty()) {
                    renderModel(document, model);
                }
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate schedule PDF", e);
        }
    }

    private void renderModel(Document doc, ScheduleRenderModel model) throws DocumentException {
        Color textColor = model.getTheme() == ColorTheme.NAVY ? new Color(0x0D, 0x1B, 0x4B) : Color.BLACK;

        Font titleFont = new Font(Font.HELVETICA, ScheduleRenderModel.TITLE_SIZE, Font.BOLD, textColor);
        Paragraph title = new Paragraph(model.getTitle(), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(4);
        doc.add(title);

        Font subtitleFont = new Font(Font.HELVETICA, ScheduleRenderModel.SUBTITLE_SIZE, Font.BOLD, textColor);
        Paragraph subtitle = new Paragraph(model.getSubtitle(), subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(2);
        doc.add(subtitle);

        Font dateFont = new Font(Font.HELVETICA, ScheduleRenderModel.DATE_SIZE, Font.NORMAL, textColor);
        Paragraph dateLine = new Paragraph(model.getDateRange(), dateFont);
        dateLine.setAlignment(Element.ALIGN_CENTER);
        dateLine.setSpacingAfter(10);
        doc.add(dateLine);

        PdfPTable sepTable = new PdfPTable(1);
        sepTable.setWidthPercentage(100);
        PdfPCell sepCell = new PdfPCell();
        sepCell.setFixedHeight(2f);
        sepCell.setBorder(Rectangle.TOP);
        sepCell.setBorderWidth(2);
        sepCell.setBorderColor(textColor);
        sepTable.addCell(sepCell);
        sepTable.setSpacingAfter(14);
        doc.add(sepTable);

        doc.add(buildTable(model));
    }

    private PdfPTable buildTable(ScheduleRenderModel model) {
        ColorTheme theme = model.getTheme();
        Color headerBg = theme == ColorTheme.NAVY ? new Color(0x0D, 0x1B, 0x4B) : Color.BLACK;
        Color textColor = theme == ColorTheme.NAVY ? new Color(0x0D, 0x1B, 0x4B) : Color.BLACK;
        Color breakBg = new Color(0xF5, 0xF5, 0xF5);
        Color borderColor = theme == ColorTheme.NAVY ? new Color(0x0D, 0x1B, 0x4B) : Color.BLACK;
        Color altBg = theme == ColorTheme.NAVY ? new Color(0xFA, 0xFB, 0xFF) : Color.WHITE;

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{15f, 14.17f, 14.17f, 14.17f, 14.17f, 14.17f, 14.17f});

        Font headerFont = new Font(Font.HELVETICA, ScheduleRenderModel.HEADER_SIZE, Font.BOLD, Color.WHITE);
        for (ColumnDef col : model.getColumns()) {
            PdfPCell hc = new PdfPCell(new Phrase(col.getLabel(), headerFont));
            hc.setBackgroundColor(headerBg);
            hc.setHorizontalAlignment(Element.ALIGN_CENTER);
            hc.setVerticalAlignment(Element.ALIGN_MIDDLE);
            hc.setPadding(8);
            hc.setMinimumHeight(ScheduleRenderModel.HEADER_ROW_HEIGHT);
            hc.setBorderWidth(2);
            hc.setBorderColor(borderColor);
            table.addCell(hc);
        }

        for (int ri = 0; ri < model.getRows().size(); ri++) {
            RowDef row = model.getRows().get(ri);
            boolean isAlt = theme == ColorTheme.NAVY && row.getIndex() % 2 == 1;

            if (row.getType() == RowType.BREAK) {
                addPdfBreakCell(table, row, textColor, headerBg, breakBg, borderColor);
            } else {
                addPdfTimeCell(table, row, textColor, borderColor, isAlt ? altBg : Color.WHITE);

                for (CellContent cell : row.getCells()) {
                    addPdfCourseCell(table, cell, theme, borderColor, isAlt ? altBg : Color.WHITE);
                }
            }
        }

        return table;
    }

    private void addPdfTimeCell(PdfPTable table, RowDef row, Color textColor,
                                 Color borderColor, Color bg) {
        Font timeFont = new Font(Font.HELVETICA, ScheduleRenderModel.TIME_SIZE, Font.BOLD, textColor);
        Font timeSubFont = new Font(Font.HELVETICA, 9, Font.BOLD, textColor);

        Phrase content = new Phrase();
        content.add(new Chunk(row.getTimeLabel() + "\n", timeFont));
        content.add(new Chunk(row.getTimeSubLabel(), timeSubFont));

        PdfPCell cell = new PdfPCell(content);
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4);
        cell.setMinimumHeight(ScheduleRenderModel.COURSE_ROW_HEIGHT);
        cell.setBorderWidth(1);
        cell.setBorderColor(borderColor);
        table.addCell(cell);
    }

    private void addPdfCourseCell(PdfPTable table, CellContent cc, ColorTheme theme,
                                   Color borderColor, Color bg) {
        if (cc.isEmpty()) {
            Font f = new Font(Font.HELVETICA, 11, Font.NORMAL,
                    theme == ColorTheme.NAVY ? new Color(0x0D, 0x1B, 0x4B) : Color.BLACK);
            PdfPCell cell = new PdfPCell(new Phrase("\u2014", f));
            cell.setBackgroundColor(bg);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setMinimumHeight(ScheduleRenderModel.COURSE_ROW_HEIGHT);
            cell.setPadding(4);
            cell.setBorderWidth(1);
            cell.setBorderColor(borderColor);
            table.addCell(cell);
            return;
        }

        Color textColor = theme == ColorTheme.NAVY ? new Color(0x0D, 0x1B, 0x4B) : Color.BLACK;
        Font codeFont = new Font(Font.HELVETICA, ScheduleRenderModel.COURSE_CODE_SIZE, Font.BOLD, textColor);
        Font nameFont = new Font(Font.HELVETICA, ScheduleRenderModel.COURSE_NAME_SIZE, Font.NORMAL, textColor);
        Font smallFont = new Font(Font.HELVETICA, ScheduleRenderModel.INSTRUCTOR_SIZE, Font.NORMAL, textColor);

        Phrase content = new Phrase();
        content.add(new Chunk(cc.getCourseCode() + "\n", codeFont));
        content.add(new Chunk(cc.getCourseName() + "\n", nameFont));
        content.add(new Chunk(cc.getInstructor() + "\n", smallFont));
        content.add(new Chunk(cc.getRoom(), smallFont));

        PdfPCell cell = new PdfPCell(content);
        cell.setBackgroundColor(bg);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setMinimumHeight(ScheduleRenderModel.COURSE_ROW_HEIGHT);
        cell.setPadding(6);
        cell.setBorderWidth(1);
        cell.setBorderColor(borderColor);
        table.addCell(cell);
    }

    private void addPdfBreakCell(PdfPTable table, RowDef row, Color textColor,
                                  Color headerBg, Color breakBg, Color borderColor) {
        Font timeFont = new Font(Font.HELVETICA, ScheduleRenderModel.TIME_SIZE, Font.BOLD, textColor);
        Font timeSubFont = new Font(Font.HELVETICA, 9, Font.BOLD, textColor);

        Phrase content = new Phrase();
        content.add(new Chunk(row.getTimeLabel() + "\n", timeFont));
        content.add(new Chunk(row.getTimeSubLabel(), timeSubFont));

        PdfPCell timeCell = new PdfPCell(content);
        timeCell.setBackgroundColor(headerBg);
        timeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        timeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        timeCell.setPadding(4);
        timeCell.setMinimumHeight(ScheduleRenderModel.BREAK_ROW_HEIGHT);
        timeCell.setBorderWidth(1);
        timeCell.setBorderColor(borderColor);
        table.addCell(timeCell);

        Font breakFont = new Font(Font.HELVETICA, 12, Font.BOLD, textColor);
        for (int i = 0; i < 6; i++) {
            PdfPCell cell = new PdfPCell(new Phrase("Break", breakFont));
            cell.setBackgroundColor(breakBg);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(3);
            cell.setMinimumHeight(ScheduleRenderModel.BREAK_ROW_HEIGHT);
            cell.setBorderWidth(1);
            cell.setBorderColor(borderColor);
            table.addCell(cell);
        }
    }

    private static class FooterEvent extends PdfPageEventHelper {
        private final ColorTheme theme;

        FooterEvent(ColorTheme theme) {
            this.theme = theme;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                Color textColor = theme == ColorTheme.NAVY ? new Color(0x0D, 0x1B, 0x4B) : Color.BLACK;
                Font f = new Font(Font.HELVETICA, ScheduleRenderModel.FOOTER_SIZE, Font.NORMAL, textColor);
                String footer = "Generated by TimetableScheduler \u2014 "
                        + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"));
                Phrase p = new Phrase(footer, f);
                PdfContentByte cb = writer.getDirectContent();
                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, p,
                        (document.left() + document.right()) / 2,
                        document.bottom() - 10, 0);
            } catch (Exception ignored) {}
        }
    }
}
