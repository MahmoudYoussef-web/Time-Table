package com.example.timetable.service;

import com.example.timetable.dto.response.ScheduleDTO;
import com.example.timetable.service.ScheduleRenderModel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ScheduleExcelService {

    public byte[] generateExcel(ScheduleDTO schedule) {
        return exportExcel(schedule, ColorTheme.NAVY);
    }

    public byte[] exportExcel(ScheduleDTO schedule, ColorTheme theme) {
        List<ScheduleRenderModel> models = ScheduleRenderModel.forAllYears(schedule, theme);
        return buildWorkbook(models, theme);
    }

    public byte[] exportExcel(ScheduleDTO schedule, String yearLevel, ColorTheme theme) {
        ScheduleRenderModel model = ScheduleRenderModel.forYear(schedule, yearLevel, theme);
        return buildWorkbook(List.of(model), theme);
    }

    private byte[] buildWorkbook(List<ScheduleRenderModel> models, ColorTheme theme) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            for (ScheduleRenderModel model : models) {
                if (model.isEmpty()) continue;
                buildSheet(workbook, model);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel", e);
        }
    }

    private void buildSheet(XSSFWorkbook workbook, ScheduleRenderModel model) {
        ColorTheme theme = model.getTheme();
        boolean isNavy = theme == ColorTheme.NAVY;
        byte[] themeRgb = isNavy ? new byte[]{0x0D, 0x1B, 0x4B} : new byte[]{0, 0, 0};
        byte[] whiteRgb = new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xFF};
        byte[] altBgRgb = isNavy ? new byte[]{(byte)0xFA, (byte)0xFB, (byte)0xFF} : null;
        byte[] breakBgRgb = new byte[]{(byte)0xF5, (byte)0xF5, (byte)0xF5};

        String sheetName = extractLevelName(model.getSubtitle());
        XSSFSheet sheet = workbook.createSheet(sheetName);
        sheet.createFreezePane(0, 4);
        sheet.setColumnWidth(0, 256 * 15);
        for (int c = 1; c < 7; c++) {
            sheet.setColumnWidth(c, 256 * 22);
        }

        XSSFCellStyle headerStyle = createCellStyle(workbook, themeRgb, whiteRgb, (short)13, true, false, false);
        XSSFCellStyle timeStyle = createCellStyle(workbook, themeRgb, null, (short)11, true, false, true);
        XSSFCellStyle courseStyle = createCellStyle(workbook, themeRgb, null, (short)10, false, false, true);
        XSSFCellStyle courseAltStyle = altBgRgb != null
                ? createCellStyle(workbook, themeRgb, altBgRgb, (short)10, false, false, true)
                : courseStyle;
        XSSFCellStyle breakStyle = createCellStyle(workbook, themeRgb, breakBgRgb, (short)12, true, false, false);
        XSSFCellStyle breakTimeStyle = createCellStyle(workbook, whiteRgb, themeRgb, (short)11, true, false, true);

        XSSFCellStyle titleStyle = createTextStyle(workbook, (short)28, true, HorizontalAlignment.CENTER, themeRgb);
        XSSFCellStyle subtitleStyle = createTextStyle(workbook, (short)16, true, HorizontalAlignment.CENTER, themeRgb);
        XSSFCellStyle dateStyle = createTextStyle(workbook, (short)11, false, HorizontalAlignment.CENTER, themeRgb);
        XSSFCellStyle footerStyle = createTextStyle(workbook, (short)9, false, HorizontalAlignment.CENTER, themeRgb);

        int rowIndex = 0;

        Row titleRow = sheet.createRow(rowIndex++);
        titleRow.setHeightInPoints(50);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(model.getTitle());
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

        Row subtitleRow = sheet.createRow(rowIndex++);
        subtitleRow.setHeightInPoints(28);
        Cell subtitleCell = subtitleRow.createCell(0);
        subtitleCell.setCellValue(model.getSubtitle());
        subtitleCell.setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

        Row dateRow = sheet.createRow(rowIndex++);
        dateRow.setHeightInPoints(22);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue(model.getDateRange());
        dateCell.setCellStyle(dateStyle);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 6));

        rowIndex++;

        Row headerRow = sheet.createRow(rowIndex++);
        headerRow.setHeightInPoints(ScheduleRenderModel.HEADER_ROW_HEIGHT);
        for (int i = 0; i < model.getColumns().size(); i++) {
            ColumnDef col = model.getColumns().get(i);
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(col.getLabel());
            cell.setCellStyle(headerStyle);
        }

        for (int ri = 0; ri < model.getRows().size(); ri++) {
            RowDef row = model.getRows().get(ri);
            boolean isAlt = isNavy && row.getIndex() % 2 == 1;

            if (row.getType() == RowType.BREAK) {
                Row excelRow = sheet.createRow(rowIndex++);
                excelRow.setHeightInPoints(ScheduleRenderModel.BREAK_ROW_HEIGHT);

                Cell timeCell = excelRow.createCell(0);
                timeCell.setCellValue(row.getTimeLabel() + "\n" + row.getTimeSubLabel());
                timeCell.setCellStyle(breakTimeStyle);

                for (int d = 0; d < 6; d++) {
                    Cell cell = excelRow.createCell(d + 1);
                    cell.setCellValue("Break");
                    cell.setCellStyle(breakStyle);
                }
                continue;
            }

            Row excelRow = sheet.createRow(rowIndex++);
            excelRow.setHeightInPoints(ScheduleRenderModel.COURSE_ROW_HEIGHT);

            Cell timeCell = excelRow.createCell(0);
            timeCell.setCellValue(row.getTimeLabel() + "\n" + row.getTimeSubLabel());
            timeCell.setCellStyle(timeStyle);

            for (int d = 0; d < row.getCells().size(); d++) {
                CellContent cc = row.getCells().get(d);
                Cell cell = excelRow.createCell(d + 1);

                if (cc.isEmpty()) {
                    cell.setCellValue("\u2014");
                    cell.setCellStyle(isAlt ? courseAltStyle : courseStyle);
                } else {
                    cell.setCellValue(cc.getCourseCode() + "\n"
                            + cc.getCourseName() + "\n"
                            + cc.getInstructor() + "\n"
                            + cc.getRoom());
                    cell.setCellStyle(isAlt ? courseAltStyle : courseStyle);
                }
            }
        }

        rowIndex++;
        Row footerRow = sheet.createRow(rowIndex);
        Cell footerCell = footerRow.createCell(0);
        footerCell.setCellValue(model.getFooter());
        footerCell.setCellStyle(footerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 6));
    }

    private XSSFCellStyle createCellStyle(XSSFWorkbook wb, byte[] fontRgb, byte[] fillRgb,
                                           short fontSize, boolean bold,
                                           boolean wrap, boolean center) {
        XSSFFont font = wb.createFont();
        font.setFontHeightInPoints(fontSize);
        font.setBold(bold);
        font.setColor(new XSSFColor(fontRgb));

        XSSFCellStyle style = wb.createCellStyle();
        style.setFont(font);
        if (center) {
            style.setAlignment(HorizontalAlignment.CENTER);
        }
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        if (wrap) {
            style.setWrapText(true);
        }
        if (fillRgb != null) {
            style.setFillForegroundColor(new XSSFColor(fillRgb));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private XSSFCellStyle createTextStyle(XSSFWorkbook wb, short fontSize, boolean bold,
                                           HorizontalAlignment align, byte[] fontRgb) {
        XSSFFont font = wb.createFont();
        font.setFontHeightInPoints(fontSize);
        font.setBold(bold);
        font.setColor(new XSSFColor(fontRgb));

        XSSFCellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setAlignment(align);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private String extractLevelName(String subtitle) {
        if (subtitle == null) return "Schedule";
        int end = subtitle.indexOf(" \u2014");
        return end > 0 ? subtitle.substring(0, end) : subtitle;
    }
}
