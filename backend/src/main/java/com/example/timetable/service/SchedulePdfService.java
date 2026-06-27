package com.example.timetable.service;

import com.example.timetable.dto.response.*;
import com.example.timetable.mapper.WeeklyScheduleMapper;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SchedulePdfService {

    private static final List<String> ORDERED_DAYS =
            List.of("SATURDAY", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY");

    // ── Fonts ────────────────────────────────────────────────────────────────
    private BaseFont interRegular, interMedium, interSemiBold,
            interBold, interExtraBold, interItalic;
    private BaseFont montBlack, montBold, montSemiBold, montMedium, montRegular;
    private BaseFont archivoBlack;

    @PostConstruct
    private void initFonts() {
        interRegular   = tryFont("fonts/Inter-Regular.ttf");
        interMedium    = tryFont("fonts/Inter-Medium.ttf");
        interSemiBold  = tryFont("fonts/Inter-SemiBold.ttf");
        interBold      = tryFont("fonts/Inter-Bold.ttf");
        interExtraBold = tryFont("fonts/Inter-ExtraBold.ttf");
        interItalic    = tryFont("fonts/Inter-Italic.ttf");
        montBlack      = tryFont("fonts/Montserrat-Black.ttf");
        montBold       = tryFont("fonts/Montserrat-Bold.ttf");
        montSemiBold   = tryFont("fonts/Montserrat-SemiBold.ttf");
        montMedium     = tryFont("fonts/Montserrat-Medium.ttf");
        montRegular    = tryFont("fonts/Montserrat-Regular.ttf");
        archivoBlack   = tryFont("fonts/Archivo-Black.ttf");
    }

    private static BaseFont tryFont(String path) {
        try (InputStream is = SchedulePdfService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) return null;
            byte[] bytes = is.readAllBytes();
            return BaseFont.createFont(path, BaseFont.WINANSI, BaseFont.EMBEDDED, true, bytes, null);
        } catch (Exception e) { return null; }
    }

    private Font f(BaseFont bf, float size, Color c) {
        return bf != null
                ? new Font(bf, size, Font.NORMAL, c)
                : new Font(Font.HELVETICA, size, Font.NORMAL, c);
    }

    // ── Constructor ──────────────────────────────────────────────────────────
    private final SvgRenderer svgRenderer;

    public SchedulePdfService(SvgRenderer svgRenderer) {
        this.svgRenderer = svgRenderer;
    }

    // ── Public API ───────────────────────────────────────────────────────────
    public byte[] generatePdf(ScheduleDTO schedule) {
        return generate(schedule, PdfColorScheme.COLOR, null);
    }

    public byte[] exportPdf(ScheduleDTO schedule, ColorTheme theme) {
        return generate(schedule, toScheme(theme), null);
    }

    public byte[] exportPdf(ScheduleDTO schedule, String year, ColorTheme theme) {
        return generate(schedule, toScheme(theme), year);
    }

    private static PdfColorScheme toScheme(ColorTheme theme) {
        return theme == ColorTheme.BLACK ? PdfColorScheme.BW : PdfColorScheme.COLOR;
    }

    // ── Core generator ───────────────────────────────────────────────────────
    private byte[] generate(ScheduleDTO schedule, PdfColorScheme scheme, String yearFilter) {
        try {
            Map<String, WeeklyScheduleDTO> levelTables =
                    WeeklyScheduleMapper.toLevelTables(schedule);

            Document doc = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            writer.setPageEvent(new FooterPageEvent(scheme, interItalic));
            doc.open();

            String mappedFilter = mapYear(yearFilter);
            List<String> levels = mappedFilter != null
                    ? List.of(mappedFilter)
                    : List.of("First Year", "Second Year", "Third Year", "Fourth Year");

            boolean first = true;
            for (String level : levels) {
                WeeklyScheduleDTO weekly = levelTables.get(level);
                if (weekly == null) continue;
                if (!first) doc.newPage();
                first = false;

                addHeader(doc, level, schedule, scheme);
                doc.add(buildTable(weekly, scheme));
                addLegendAndNote(doc, scheme);
            }

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            log.error("PDF generation failed for schedule {}", schedule.getId(), e);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private static String mapYear(String y) {
        if (y == null) return null;
        return switch (y.toUpperCase()) {
            case "FIRST",  "1" -> "First Year";
            case "SECOND", "2" -> "Second Year";
            case "THIRD",  "3" -> "Third Year";
            case "FOURTH", "4" -> "Fourth Year";
            default -> y;
        };
    }

    // ── Header ───────────────────────────────────────────────────────────────
    private void addHeader(Document doc, String level, ScheduleDTO schedule,
                           PdfColorScheme scheme) throws Exception {

        boolean isColor = scheme == PdfColorScheme.COLOR;

        PdfPTable tbl = new PdfPTable(new float[]{1.0f, 5.8f, 3.2f});
        tbl.setWidthPercentage(100);
        tbl.setSpacingAfter(0f);

        // Left — shield
        try {
            String path = isColor
                    ? "assets/university_shield_navy.svg"
                    : "assets/university_shield_black.svg";
            Image img = svgImage(path, 270, 270);
            img.scaleToFit(90, 90);
            PdfPCell c = new PdfPCell(img, false);
            c.setBorder(Rectangle.NO_BORDER);
            c.setVerticalAlignment(Element.ALIGN_BOTTOM);
            tbl.addCell(c);
        } catch (Exception ex) {
            tbl.addCell(emptyCell());
        }

        // Center — title block
        Color titleColor = isColor ? hex("#0B1B4F") : Color.BLACK;
        Color grayColor  = scheme.grayText();

        PdfPCell center = new PdfPCell();
        center.setBorder(Rectangle.NO_BORDER);
        center.setPaddingLeft(20f);
        center.setPaddingBottom(4f);
        center.setVerticalAlignment(Element.ALIGN_BOTTOM);

        Paragraph title = new Paragraph("UNIVERSITY STUDY SCHEDULE",
                f(archivoBlack, 26, titleColor));
        title.setSpacingAfter(2f);
        center.addElement(title);

        String semInfo = level + " \u2014 Semester: "
                + (schedule.getSemesterName() != null ? schedule.getSemesterName() : "N/A");
        Paragraph subtitle = new Paragraph(semInfo, f(archivoBlack, 11, titleColor));
        subtitle.setSpacingAfter(5f);
        center.addElement(subtitle);

        try {
            String divPath = isColor
                    ? "assets/divider_navy.svg"
                    : "assets/divider_black.svg";
            Image divider = svgImage(divPath, 380, 24);
            divider.scaleToFit(160, 10);
            Paragraph divPara = new Paragraph();
            divPara.add(new Chunk(divider, 0, 0, true));
            divPara.setSpacingAfter(4f);
            center.addElement(divPara);
        } catch (Exception ignored) {
            Paragraph dots = new Paragraph("\u2022  \u2022  \u2022  \u2022  \u2022",
                    f(montRegular, 7, hex("#94A3B8")));
            dots.setSpacingAfter(4f);
            center.addElement(dots);
        }

        Paragraph dateLine = new Paragraph();
        dateLine.setLeading(14f);
        try {
            Image cal = svgImage("assets/calendar_icon.svg", 30, 30);
            cal.scaleToFit(11, 11);
            dateLine.add(new Chunk(cal, 0, -2, true));
        } catch (Exception ignored) {}
        String weekInfo = "  For The Week: "
                + nvl(schedule.getStartDate()) + " \u2014 " + nvl(schedule.getEndDate());
        dateLine.add(new Chunk(weekInfo, f(montMedium, 9, grayColor)));
        center.addElement(dateLine);

        tbl.addCell(center);

        // Right — building
        try {
            String path = isColor
                    ? "assets/university_building_navy.svg"
                    : "assets/university_building_black.svg";
            int bw = isColor ? 820 : 940;
            int bh = isColor ? 325 : 370;
            Image img = svgImage(path, bw, bh);
            img.scaleToFit(isColor ? 195 : 215, 88);
            PdfPCell c = new PdfPCell(img, false);
            c.setBorder(Rectangle.NO_BORDER);
            c.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c.setVerticalAlignment(Element.ALIGN_BOTTOM);
            c.setPadding(0);
            tbl.addCell(c);
        } catch (Exception ex) {
            tbl.addCell(emptyCell());
        }

        doc.add(tbl);

        // Thick divider bar
        PdfPTable bar = new PdfPTable(1);
        bar.setWidthPercentage(100);
        PdfPCell barCell = new PdfPCell();
        barCell.setBackgroundColor(scheme.headerBg());
        barCell.setFixedHeight(8f);
        barCell.setBorder(Rectangle.NO_BORDER);
        bar.addCell(barCell);
        bar.setSpacingBefore(5f);
        bar.setSpacingAfter(7f);
        doc.add(bar);
    }

    // ── Schedule table ───────────────────────────────────────────────────────
    private PdfPTable buildTable(WeeklyScheduleDTO weekly, PdfColorScheme scheme) throws Exception {

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.6f, 3.3f, 3.3f, 3.3f, 3.3f, 3.3f, 3.3f});

        // Header row
        addHeaderCell(table, "TIME", scheme);
        for (String day : ORDERED_DAYS) addHeaderCell(table, day, scheme);

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

        // Data rows
        for (int i = 0; i < times.size(); i++) {
            String tk = times.get(i);

            // Gap-based break detection
            if (i > 0) {
                String prevEnd = getEndTime(data, times.get(i - 1));
                try {
                    LocalTime pe = LocalTime.parse(pad(prevEnd));
                    LocalTime cs = LocalTime.parse(pad(tk));
                    if (java.time.Duration.between(pe, cs).toMinutes() >= 30)
                        addBreakRow(table, scheme);
                } catch (Exception ignored) {}
            }

            String endTime = getEndTime(data, tk);
            table.addCell(buildTimeCell(fmt(tk), fmt(endTime), scheme));
            for (String day : ORDERED_DAYS)
                table.addCell(buildEntryCell(data.getOrDefault(day, Map.of()).get(tk), i));
        }

        // Outer border wrapper
        PdfPTable wrapper = new PdfPTable(1);
        wrapper.setWidthPercentage(100);
        PdfPCell wc = new PdfPCell(table);
        wc.setPadding(0);
        wc.setBorderColor(scheme.outerBorder());
        wc.setBorderWidth(1.5f);
        wrapper.addCell(wc);
        return wrapper;
    }

    private void addHeaderCell(PdfPTable table, String text, PdfColorScheme scheme) {
        PdfPCell cell = new PdfPCell(new Phrase(text, f(montBold, 10, scheme.headerFg())));
        cell.setBackgroundColor(scheme.headerBg());
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(12);
        cell.setPaddingBottom(12);
        cell.setMinimumHeight(46);
        cell.setBorderColor(scheme.headerBg());
        cell.setBorderWidth(0.5f);
        table.addCell(cell);
    }

    private PdfPCell buildTimeCell(String start, String end, PdfColorScheme scheme) {
        Paragraph p = new Paragraph();
        p.setAlignment(Element.ALIGN_CENTER);
        p.setLeading(14f);
        p.add(new Chunk(start + "\n", f(interBold, 9, scheme.text())));
        p.add(new Chunk("\u2013 " + end, f(interRegular, 8, scheme.grayText())));

        PdfPCell cell = new PdfPCell(p);
        cell.setBackgroundColor(scheme.timeBg());
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        cell.setMinimumHeight(80);
        cell.setBorderColor(scheme.gridBorder());
        cell.setBorderWidth(0.5f);
        cell.setBorderWidthRight(1f);
        return cell;
    }

    private PdfPCell buildEntryCell(SlotDTO slot, int rowIndex) {
        if (slot == null || slot.entry() == null) {
            PdfPCell cell = new PdfPCell(new Phrase("\u2014",
                    f(interRegular, 10, hex("#94A3B8"))));
            cell.setBackgroundColor(rowIndex % 2 == 0 ? hex("#F8FAFC") : Color.WHITE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setMinimumHeight(75);
            cell.setBorderColor(hex("#E5E7EB"));
            cell.setBorderWidth(0.5f);
            return cell;
        }

        ScheduleEntryDTO e = slot.entry();
        CourseCategory cat = CourseCategory.fromCode(e.courseCode());
        Color bgColor    = hex(cat.bgHex);
        Color codeColor  = hex(cat.codeHex);

        Paragraph content = new Paragraph();
        content.setAlignment(Element.ALIGN_CENTER);
        content.setLeading(16f);
        content.add(new Chunk(e.courseCode() + "\n",    f(interExtraBold, 11, codeColor)));
        content.add(new Chunk(e.courseName() + "\n",    f(interBold,       9, hex("#1E293B"))));
        content.add(new Chunk(e.instructorName() + "\n",f(interRegular,    7, hex("#4F5D75"))));
        content.add(new Chunk("\u29BE " + e.roomNumber(), f(interRegular,  7, hex(cat.codeHex))));

        PdfPCell cell = new PdfPCell(content);
        cell.setBackgroundColor(bgColor);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(7);
        cell.setPaddingBottom(7);
        cell.setPaddingLeft(5);
        cell.setPaddingRight(5);
        cell.setMinimumHeight(80);
        cell.setBorderWidth(0.5f);
        cell.setBorderColor(hex("#E5E7EB"));
        cell.setBorderWidthLeft(2.5f);
        cell.setBorderColorLeft(codeColor);

        if (e.hardViolations() > 0) {
            cell.setBorderWidthLeft(4f);
            cell.setBorderColorLeft(new Color(204, 0, 0));
            cell.setBackgroundColor(new Color(255, 240, 240));
        }
        return cell;
    }

    private void addBreakRow(PdfPTable table, PdfColorScheme scheme) throws Exception {
        // Time cell
        PdfPCell timeCell = new PdfPCell(
                new Phrase("Break", f(interBold, 8, scheme.breakFg())));
        timeCell.setBackgroundColor(scheme.breakBg());
        timeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        timeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        timeCell.setPadding(5);
        timeCell.setMinimumHeight(26);
        timeCell.setBorderColor(scheme.gridBorder());
        timeCell.setBorderWidth(0.5f);
        table.addCell(timeCell);

        // Day cells
        for (int i = 0; i < 6; i++) {
            Paragraph p = new Paragraph();
            p.setAlignment(Element.ALIGN_CENTER);
            try {
                Image coffee = svgImage("assets/coffee_icon.svg", 36, 36);
                coffee.scaleToFit(11, 11);
                p.add(new Chunk(coffee, 0, -2));
                p.add(new Chunk("  Break", f(interBold, 8, scheme.breakFg())));
            } catch (Exception ex) {
                p.add(new Chunk("Break", f(interBold, 8, scheme.breakFg())));
            }
            PdfPCell cell = new PdfPCell(p);
            cell.setBackgroundColor(scheme.breakBg());
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(5);
            cell.setMinimumHeight(26);
            cell.setBorderColor(scheme.gridBorder());
            cell.setBorderWidth(0.5f);
            table.addCell(cell);
        }
    }

    // ── Legend & Note ────────────────────────────────────────────────────────
    private void addLegendAndNote(Document doc, PdfColorScheme scheme) throws DocumentException {
        PdfPTable bar = new PdfPTable(2);
        bar.setWidthPercentage(100);
        bar.setWidths(new float[]{7f, 2f});
        bar.setSpacingBefore(6f);

        // Legend
        PdfPCell legendCell = new PdfPCell();
        legendCell.setBorder(PdfPCell.NO_BORDER);
        legendCell.setPaddingTop(6);
        legendCell.setPaddingBottom(6);
        Paragraph legends = new Paragraph();
        legends.setLeading(16f);
        for (CourseCategory cat : CourseCategory.legendOrder()) {
            Color dotColor = hex(cat.codeHex);
            legends.add(new Chunk("\u25A0 ", f(interBold, 10, dotColor)));
            legends.add(new Chunk(cat.label + "      ", f(interRegular, 8, hex("#1E293B"))));
        }
        legendCell.addElement(legends);
        bar.addCell(legendCell);

        // Note
        Paragraph note = new Paragraph();
        note.setLeading(12f);
        note.add(new Chunk("\u24D8 Note:\n",   f(interBold,    8, hex("#1E293B"))));
        note.add(new Chunk("Please check the classroom\nand time before each session.",
                f(interRegular, 7, scheme.grayText())));
        PdfPCell noteCell = new PdfPCell(note);
        noteCell.setBorderColor(hex("#D1D5DB"));
        noteCell.setBorderWidth(1f);
        noteCell.setPadding(8);
        noteCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        bar.addCell(noteCell);

        doc.add(bar);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private Image svgImage(String path, int w, int h) throws Exception {
        BufferedImage bi = svgRenderer.render(path, w, h);
        return Image.getInstance(bi, null);
    }

    private static PdfPCell emptyCell() {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    private static Color hex(String h) {
        String s = h.replace("#", "");
        return new Color(
                Integer.parseInt(s.substring(0, 2), 16),
                Integer.parseInt(s.substring(2, 4), 16),
                Integer.parseInt(s.substring(4, 6), 16));
    }

    private static String nvl(Object o) { return o != null ? o.toString() : "N/A"; }

    private static String pad(String t) { return t.length() == 4 ? "0" + t : t; }

    private String fmt(String t) {
        try {
            return LocalTime.parse(pad(t)).format(DateTimeFormatter.ofPattern("h:mm a"));
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
                        return LocalTime.parse(pad(tk)).plusHours(2)
                                .format(DateTimeFormatter.ofPattern("HH:mm"));
                    } catch (Exception ex) { return tk; }
                });
    }

    // ── Footer ───────────────────────────────────────────────────────────────
    private static class FooterPageEvent extends PdfPageEventHelper {
        private final PdfColorScheme scheme;
        private final BaseFont font;

        FooterPageEvent(PdfColorScheme scheme, BaseFont font) {
            this.scheme = scheme;
            this.font   = font;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte cb = writer.getDirectContent();
                float y     = document.bottom() - 6;
                float left  = document.left();
                float right = document.right();

                String date   = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
                String text   = "\u25C6  \u25C6  \u25C6   Generated by TimetableScheduler \u2014 "
                        + date + "   \u2022   Page " + writer.getPageNumber()
                        + "   \u25C6  \u25C6  \u25C6";

                Font f = font != null
                        ? new Font(font, 8, Font.NORMAL, scheme.grayText())
                        : new Font(Font.HELVETICA, 8, Font.ITALIC, scheme.grayText());

                float w     = f.getCalculatedBaseFont(false).getWidthPoint(text, 8);
                float cx    = (left + right) / 2f;
                float start = cx - w / 2f;

                cb.setColorStroke(scheme.footerRule());
                cb.setLineWidth(0.75f);
                cb.moveTo(left,        y); cb.lineTo(start - 6, y); cb.stroke();
                cb.moveTo(start + w + 6, y); cb.lineTo(right,  y); cb.stroke();

                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                        new Phrase(text, f), cx, y - 3, 0);
            } catch (Exception ignored) {}
        }
    }
}