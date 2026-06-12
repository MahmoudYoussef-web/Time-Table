package com.example.timetable.service;

import com.example.timetable.dto.response.ScheduleDTO;
import com.example.timetable.service.ScheduleRenderModel.*;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.swing.Java2DRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class SchedulePngService {

    private static final int TARGET_WIDTH_PX = 2800;

    public byte[] generatePng(ScheduleDTO schedule, String yearLevel, ColorTheme theme) {
        ScheduleRenderModel model = ScheduleRenderModel.forYear(schedule, yearLevel, theme);
        if (model.isEmpty()) {
            throw new RuntimeException("No entries found for year level: " + yearLevel);
        }
        String html = buildHtml(model);
        return renderHtmlToPng(html);
    }

    private byte[] renderHtmlToPng(String html) {
        try {
            Document doc = XMLResource.load(
                    new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8))
            ).getDocument();

            Java2DRenderer renderer = new Java2DRenderer(doc, TARGET_WIDTH_PX);
            BufferedImage image = renderer.getImage();

            BufferedImage canvas = new BufferedImage(TARGET_WIDTH_PX, image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            canvas.getGraphics().drawImage(image, 0, 0, null);

            image = cropWhitespace(canvas, 20);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to render PNG from HTML", e);
        }
    }

    private BufferedImage cropWhitespace(BufferedImage img, int paddingPx) {
        if (img.getWidth() <= paddingPx * 2 || img.getHeight() <= paddingPx * 2)
            return img;

        int minX = img.getWidth();
        int maxX = 0;
        int maxY = img.getHeight() / 3;

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                if ((img.getRGB(x, y) & 0xFFFFFF) != 0xFFFFFF) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }

        if (minX >= maxX || maxY <= 0) return img;

        int cropX = Math.max(0, minX - paddingPx);
        int cropY = 0;
        int cropW = Math.min(img.getWidth() - cropX, maxX - minX + 2 * paddingPx);
        int cropH = Math.min(img.getHeight() - cropY, maxY + paddingPx);

        return img.getSubimage(cropX, cropY, cropW, cropH);
    }

    private String buildHtml(ScheduleRenderModel model) {
        boolean isNavy = model.getTheme() == ColorTheme.NAVY;
        String headerBg = isNavy ? "#274472" : "#000000";
        String headerBorder = isNavy ? "#1E3A5F" : "#333333";
        String outerBorder = isNavy ? "#334155" : "#555555";
        String gridBorder = isNavy ? "#CBD5E1" : "#CCCCCC";
        String timeBg = "#F8FAFC";
        String timeText = "#000000";
        String subtitleColor = isNavy ? "#1E293B" : "#333333";
        String grayHex = "#64748B";
        String altBgHex = isNavy ? "#F8FAFC" : "#F5F5F5";
        String breakBg = isNavy ? "#EEF2FF" : "#E8E8E8";
        String breakText = isNavy ? "#334155" : "#444444";
        String emptyColor = "#94A3B8";

        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" "
                + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>"
                + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>"
                + "<style type=\"text/css\">"
                + buildCss(headerBg, headerBorder, outerBorder, gridBorder, timeBg, timeText, subtitleColor, grayHex, altBgHex, isNavy, breakBg, breakText, emptyColor)
                + "</style></head><body>"
                + buildHeaderHtml(model, grayHex)
                + buildTableHtml(model, headerBg, headerBorder, outerBorder, gridBorder, timeBg, timeText, grayHex, isNavy, altBgHex, breakBg, breakText, emptyColor)
                + buildFooterHtml(model, grayHex)
                + "</body></html>";
    }

    private String buildCss(String headerBg, String headerBorder, String outerBorder,
                            String gridBorder, String timeBg, String timeText,
                            String subtitleColor, String grayHex, String altBgHex,
                            boolean isNavy, String breakBg, String breakText, String emptyColor) {
        int h = ScheduleRenderModel.HEADER_ROW_HEIGHT;
        int cr = ScheduleRenderModel.COURSE_ROW_HEIGHT;

        return "* { margin: 0; padding: 0; box-sizing: border-box; }"
                + "body { background: #FFFFFF; width: " + TARGET_WIDTH_PX + "px;"
                + " font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }"
                + ".wrapper { width: 90%; max-width: 1800px; margin: 20px auto; }"
                + ".header { text-align: center; margin-bottom: 8px; }"
                + ".title { font-size: " + ScheduleRenderModel.TITLE_SIZE + "px; font-weight: 800;"
                + " color: " + headerBg + "; letter-spacing: 1px; line-height: 1.2; }"
                + ".subtitle { font-size: " + ScheduleRenderModel.SUBTITLE_SIZE + "px; font-weight: 600;"
                + " color: " + subtitleColor + "; margin-top: 4px; }"
                + ".date-line { font-size: " + ScheduleRenderModel.DATE_SIZE + "px; font-weight: 400;"
                + " color: " + grayHex + "; margin-top: 6px; }"
                + ".divider { border: none; border-top: 2px solid " + headerBg + "; margin: 8px 0 14px 0; }"
                + ".tbl { width: 100%; border-collapse: collapse;"
                + " border: 2px solid " + outerBorder + "; }"
                + ".tbl th { background: " + headerBg + "; color: #FFFFFF;"
                + " font-size: " + ScheduleRenderModel.HEADER_SIZE + "px; font-weight: 700;"
                + " text-align: center; padding: 8px; height: " + h + "px;"
                + " letter-spacing: 0.5px; border: 1px solid " + headerBorder + "; }"
                + ".tbl td { border: 1px solid " + gridBorder + "; padding: 6px 4px;"
                + " height: " + cr + "px; vertical-align: middle; }"
                + ".time-cell { font-size: " + ScheduleRenderModel.TIME_SIZE + "px; font-weight: 700;"
                + " color: " + timeText + "; text-align: center; vertical-align: middle;"
                + " line-height: 1.3; padding: 8px 4px; background: " + timeBg + "; }"
                + ".time-sub { font-size: 9px; font-weight: 400; color: " + grayHex + "; display: block; }"
                + ".entry { text-align: center; line-height: 1.4; padding: 2px; }"
                + ".entry-conflict { border-left: 4px solid #CC0000; background-color: #FFF0F0; }"
                + ".entry-conflict .entry-code { color: #CC0000; }"
                + ".entry-code { font-size: " + ScheduleRenderModel.COURSE_CODE_SIZE + "px; font-weight: 700;"
                + " color: " + headerBg + "; display: block; margin-bottom: 4px; }"
                + ".entry-name { font-size: " + ScheduleRenderModel.COURSE_NAME_SIZE + "px; font-weight: 500;"
                + " color: #1E293B; display: block; margin-bottom: 3px; }"
                + ".entry-instructor { font-size: " + ScheduleRenderModel.INSTRUCTOR_SIZE + "px;"
                + " color: " + grayHex + "; display: block; margin-bottom: 2px; }"
                + ".entry-room { font-size: " + ScheduleRenderModel.ROOM_SIZE + "px;"
                + " color: " + grayHex + "; display: block; }"
                + ".empty-cell { text-align: center; vertical-align: middle;"
                + " font-size: 13px; color: " + emptyColor + "; }"
                + ".break-row td { background: " + breakBg + "; }"
                + ".break-cell { text-align: center; vertical-align: middle;"
                + " font-size: 12px; font-weight: 600; color: " + breakText + "; }"
                + ".alt-row td { background: " + altBgHex + "; }"
                + ".date-label { font-weight: 700; color: " + grayHex + "; margin-right: 6px; }"
                + ".footer { text-align: center; margin-top: 12px; }"
                + ".footer-text { font-size: " + ScheduleRenderModel.FOOTER_SIZE + "px; color: " + grayHex + "; }";
    }

    private String buildHeaderHtml(ScheduleRenderModel model, String grayHex) {
        return "<div class=\"wrapper header\">"
                + "<div class=\"title\">" + esc(model.getTitle()) + "</div>"
                + "<div class=\"subtitle\">" + esc(model.getSubtitle()) + "</div>"
                + "<div class=\"date-line\">"
                + "<span class=\"date-label\">Period:</span> "
                + esc(model.getDateRange())
                + "</div>"
                + "</div>"
                + "<div class=\"wrapper\"><hr class=\"divider\"/></div>";
    }

    private String buildTableHtml(ScheduleRenderModel model, String headerBg,
                                   String headerBorder, String outerBorder,
                                   String gridBorder, String timeBg, String timeText,
                                   String grayHex, boolean isNavy, String altBgHex,
                                   String breakBg, String breakText, String emptyColor) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"wrapper\"><table class=\"tbl\">");
        sb.append("<thead><tr>");
        for (ColumnDef col : model.getColumns()) {
            sb.append("<th>").append(esc(col.getLabel())).append("</th>");
        }
        sb.append("</tr></thead><tbody>");

        for (int ri = 0; ri < model.getRows().size(); ri++) {
            RowDef row = model.getRows().get(ri);
            boolean isAlt = isNavy && row.getIndex() % 2 == 1;

            if (row.getType() == RowType.BREAK) {
                sb.append("<tr class=\"break-row\">");
                sb.append("<td class=\"time-cell\">").append(esc(row.getTimeLabel()))
                        .append("<span class=\"time-sub\">").append(esc(row.getTimeSubLabel())).append("</span></td>");
                for (int d = 0; d < 6; d++) {
                    sb.append("<td class=\"break-cell\">Break</td>");
                }
                sb.append("</tr>");
                continue;
            }

            sb.append(isAlt ? "<tr class=\"alt-row\">" : "<tr>");
            sb.append("<td class=\"time-cell\">").append(esc(row.getTimeLabel()))
                    .append("<span class=\"time-sub\">").append(esc(row.getTimeSubLabel())).append("</span></td>");

            for (CellContent cc : row.getCells()) {
                if (cc.isEmpty()) {
                    sb.append("<td class=\"empty-cell\">\u2014</td>");
                } else {
                    String entryClass = cc.hasConflict() ? "entry entry-conflict" : "entry";
                    sb.append("<td><div class=\"").append(entryClass).append("\">")
                            .append("<span class=\"entry-code\">").append(esc(cc.getCourseCode())).append("</span>")
                            .append("<span class=\"entry-name\">").append(esc(cc.getCourseName())).append("</span>")
                            .append("<span class=\"entry-instructor\">").append(esc(cc.getInstructor())).append("</span>")
                            .append("<span class=\"entry-room\">").append(esc(cc.getRoom())).append("</span>")
                            .append("</div></td>");
                }
            }
            sb.append("</tr>");
        }

        sb.append("</tbody></table></div>");
        return sb.toString();
    }

    private String buildFooterHtml(ScheduleRenderModel model, String grayHex) {
        return "<div class=\"wrapper\"><div class=\"footer\">"
                + "<span class=\"footer-text\">" + esc(model.getFooter()) + "</span>"
                + "</div></div>";
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
