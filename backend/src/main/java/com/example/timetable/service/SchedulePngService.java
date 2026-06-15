package com.example.timetable.service;

import com.example.timetable.dto.response.ScheduleDTO;
import com.example.timetable.service.ScheduleRenderModel.*;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.ScreenshotType;
import com.microsoft.playwright.options.WaitUntilState;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class SchedulePngService {

    private static final int TARGET_WIDTH_PX = 2800;

    private final SvgRenderer svgRenderer;

    public SchedulePngService(SvgRenderer svgRenderer) {
        this.svgRenderer = svgRenderer;
    }

    public byte[] generatePng(ScheduleDTO schedule, String yearLevel, ColorTheme theme) {
        ScheduleRenderModel model = ScheduleRenderModel.forYear(schedule, yearLevel, theme);
        if (model.isEmpty()) {
            throw new RuntimeException("No entries found for year level: " + yearLevel);
        }
        return renderHtmlToPng(model);
    }

    public String generateHtml(ScheduleDTO schedule, String yearLevel, ColorTheme theme) {
        ScheduleRenderModel model = ScheduleRenderModel.forYear(schedule, yearLevel, theme);
        if (model.isEmpty()) {
            throw new RuntimeException("No entries found for year level: " + yearLevel);
        }
        return buildHtml(model);
    }

    private byte[] renderHtmlToPng(ScheduleRenderModel model) {
        String html = buildHtml(model);
        return renderHtmlToPng(html);
    }

    private byte[] renderHtmlToPng(String html) {
        try (Playwright playwright = Playwright.create()) {
            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setArgs(List.of(
                            "--no-sandbox",
                            "--disable-setuid-sandbox",
                            "--disable-dev-shm-usage",
                            "--force-device-scale-factor=1"
                    ));

            try (Browser browser = playwright.chromium().launch(launchOptions)) {
                Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                        .setViewportSize(TARGET_WIDTH_PX, 1080)
                        .setDeviceScaleFactor(1.0);

                try (BrowserContext context = browser.newContext(contextOptions)) {
                    Page page = context.newPage();

                    page.setContent(html, new Page.SetContentOptions()
                            .setWaitUntil(WaitUntilState.NETWORKIDLE));

                    page.waitForTimeout(800);

                    byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
                            .setFullPage(true)
                            .setType(ScreenshotType.PNG));

                    return screenshot;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to render PNG with Playwright", e);
        }
    }

    private String shieldBase64(ColorTheme theme) {
        String file = theme == ColorTheme.NAVY
                ? "assets/university_shield_navy.svg"
                : "assets/university_shield_black.svg";
        return svgRenderer.renderToBase64(file, 400, 400);
    }

    private String buildingBase64(ColorTheme theme) {
        String file = theme == ColorTheme.NAVY
                ? "assets/university_building_navy.svg"
                : "assets/university_building_black.svg";
        int w = theme == ColorTheme.NAVY ? 920 : 852;
        return svgRenderer.renderToBase64(file, w, 424);
    }

    private String dividerBase64(ColorTheme theme) {
        String file = theme == ColorTheme.NAVY
                ? "assets/divider_navy.svg"
                : "assets/divider_black.svg";
        return svgRenderer.renderToBase64(file, 700, 40);
    }

    private String buildHtml(ScheduleRenderModel model) {
        boolean isNavy = model.getTheme() == ColorTheme.NAVY;
        String titleColor    = isNavy ? "#0B1B4F" : "#000000";
        String headerBg      = isNavy ? "#13387a" : "#000000";
        String headerBorder  = isNavy ? "#0f2d6b" : "#333333";
        String outerBorder   = isNavy ? headerBg  : "#555555";
        String gridBorder    = isNavy ? "#e5e7eb" : "#cccccc";
        String timeBg     = isNavy ? "#F8FAFC" : "#f0f0f0";
        String subtitleColor = isNavy ? "#1E293B" : "#333333";
        String grayHex       = "#4f5d75";
        String altBgHex      = isNavy ? "#F8FAFC" : "#F5F5F5";
        String breakBg       = "#f5f5f5";
        String breakText     = "#4f5d75";
        String emptyColor    = "#94A3B8";

        return "<!DOCTYPE html>"
                + "<html><head>"
                + "<meta charset=\"UTF-8\"/>"
                + "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\"/>"
                + "<link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin=\"anonymous\"/>"
                + "<link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&display=swap\" rel=\"stylesheet\"/>"
                + "<style>"
                + buildCss(titleColor, headerBg, headerBorder, outerBorder, gridBorder,
                        subtitleColor, grayHex, altBgHex, isNavy, breakBg, breakText, emptyColor, timeBg)
                + "</style></head><body>"
                + buildHeaderHtml(model, titleColor, grayHex, isNavy)
                + buildTableHtml(model, isNavy, altBgHex)
                + buildLegendAndNoteHtml(headerBg)
                + buildFooterHtml(model, headerBg)
                + "</body></html>";
    }

    private String buildCss(String titleColor, String headerBg, String headerBorder,
                            String outerBorder, String gridBorder,
                            String subtitleColor, String grayHex,
                            String altBgHex, boolean isNavy, String breakBg,
                            String breakText, String emptyColor, String timeBg) {
        int h  = ScheduleRenderModel.HEADER_ROW_HEIGHT;
        int cr = ScheduleRenderModel.COURSE_ROW_HEIGHT;

        StringBuilder catCss = new StringBuilder();
        for (CourseCategory cat : CourseCategory.values()) {
            String cls = "cat-" + cat.name().toLowerCase().replace('_', '-');
            catCss.append(".").append(cls)
                    .append("{ background:").append(cat.bgHex).append(";")
                    .append(" border:1.5px solid ").append(cat.borderHex).append(";")
                    .append(" border-radius:16px; padding:10px 12px; margin:6px;")
                    .append(" box-shadow:0 2px 10px rgba(0,0,0,0.10), 0 1px 4px rgba(0,0,0,0.06);"
                            + " transition:none; }")
                    .append(".").append(cls).append(" .entry-code { color:")
                    .append(cat.codeHex).append("; }");
        }

        return "* { margin:0; padding:0; box-sizing:border-box; }"
                + "body { background:#FFFFFF; width:" + TARGET_WIDTH_PX + "px;"
                + " font-family:'Inter','Segoe UI',Arial,sans-serif;"
                + " padding-top:16px; padding-bottom:16px; margin:0; }"
                + ".wrapper { width:96%; max-width:2760px; margin:0 auto; }"
                + "table.header-outer { width:100%; table-layout:fixed; border-collapse:collapse; border:0; }"
                + "table.header-outer td { border:0; padding:0; vertical-align:middle; }"
                + "td.hdr-left  { width:11%; text-align:left;   vertical-align:bottom; padding:0; }"
                + "td.hdr-center{ width:58%; text-align:left; vertical-align:middle; padding:0 0 0 24px; }"
                + "td.hdr-right { width:31%; text-align:right;  vertical-align:bottom; padding:0; }"
                + "td.hdr-left  img { display:inline-block; }"
                + "td.hdr-right img { display:inline-block; }"
                + ".title { font-size:" + ScheduleRenderModel.TITLE_SIZE + "px; font-weight:900;"
                + " color:" + titleColor + "; letter-spacing:1px; white-space:nowrap;"
                + " line-height:1.0; margin-bottom:0px; }"
                + ".subtitle { font-size:" + ScheduleRenderModel.SUBTITLE_SIZE + "px; font-weight:600;"
                + " color:" + subtitleColor + "; margin-top:2px; margin-bottom:0px; }"
                + ".divider-svg { width:500px; height:30px; margin:4px 0; display:block; }"
                + ".date-line { font-size:" + ScheduleRenderModel.DATE_SIZE + "px;"
                + " color:" + grayHex + "; margin-top:2px; font-weight:500; }"
                + ".thick-divider { border:none; height:7px; background:" + headerBg
                + "; margin:10px 0 0 0; border-radius:0; }"
                + ".tbl { width:100%; table-layout:fixed; border-collapse:collapse;"
                + " border:3px solid " + outerBorder + "; }"
                + ".tbl th { background:" + headerBg + "; color:#FFFFFF;"
                + " font-size:" + ScheduleRenderModel.HEADER_SIZE + "px;"
                + " font-weight:700; text-align:center; padding:14px 6px;"
                + " height:" + h + "px;"
                + " letter-spacing:2.5px; border:1px solid " + headerBorder + "; }"
                + ".tbl td { border:1px solid " + gridBorder + "; height:" + cr + "px;"
                + " vertical-align:middle; padding:0; }"
                + ".time-cell, .time-header { width:160px; min-width:140px; max-width:160px; }"
                + ".time-cell { font-size:" + ScheduleRenderModel.TIME_SIZE + "px; font-weight:800;"
                + " color:" + titleColor + "; text-align:center; vertical-align:middle;"
                + " line-height:1.5; padding:6px 4px; background:" + timeBg + "; }"
                + ".time-sub { font-size:24px; font-weight:400; color:" + grayHex
                + "; display:block; }"
                + ".entry { text-align:center; line-height:1.4; }"
                + ".entry-code { font-size:" + ScheduleRenderModel.COURSE_CODE_SIZE + "px;"
                + " font-weight:800; letter-spacing:0.3px; display:block; margin-bottom:4px; }"
                + ".entry-name { font-size:" + ScheduleRenderModel.COURSE_NAME_SIZE + "px;"
                + " font-weight:700; color:#1e293b; display:block; margin-bottom:3px;"
                + " line-height:1.3; }"
                + ".entry-instructor { font-size:" + ScheduleRenderModel.INSTRUCTOR_SIZE + "px;"
                + " color:" + grayHex + "; display:block; margin-bottom:3px; }"
                + ".entry-room { font-size:" + ScheduleRenderModel.ROOM_SIZE + "px;"
                + " color:" + grayHex + "; display:block; }"
                + ".entry-conflict { border-left:6px solid #CC0000 !important;"
                + " background-color:#FFF0F0 !important; }"
                + ".entry-conflict .entry-code { color:#CC0000 !important; }"
                + ".empty-cell { text-align:center; vertical-align:middle; font-size:28px;"
                + " color:" + emptyColor + "; }"
                + ".break-row td { background:" + breakBg + "; height:" + ScheduleRenderModel.BREAK_ROW_HEIGHT + "px;"
                + " border-top:1px solid #e5e7eb; border-bottom:1px solid #e5e7eb; }"
                + ".break-cell { text-align:center; vertical-align:middle; font-size:28px;"
                + " font-weight:600; color:" + breakText + "; padding:8px; letter-spacing:0.5px; }"
                + ".break-icon { display:inline-block; width:36px; height:36px;"
                + " vertical-align:middle; margin-right:10px; }"
                + ".alt-row td { background:" + altBgHex + "; }"
                + ".bot-bar { display:table; width:100%; margin-top:8px;"
                + " border:1px solid #e2e8f0; border-radius:12px; padding:14px 0; background:#ffffff; }"
                + ".bot-bar .legends { display:table-cell; text-align:left; vertical-align:middle; padding-left:30px; }"
                + ".legend-item { display:inline-block; padding:0 28px 0 0; vertical-align:middle; }"
                + ".legend-dot { display:inline-block; width:22px; height:22px; border-radius:5px;"
                + " margin-right:8px; vertical-align:middle; }"
                + ".legend-label { font-size:28px; color:#1e293b; vertical-align:middle; font-weight:500; }"
                + ".bot-bar .note-cell { display:table-cell; width:440px; text-align:right;"
                + " vertical-align:middle; padding-right:24px; }"
                + ".note-inner { display:inline-block; border:1.5px solid #cbd5e1;"
                + " border-radius:10px; padding:10px 16px; text-align:left;"
                + " background:#ffffff; box-shadow:0 1px 4px rgba(0,0,0,0.06); }"
                + ".note-bold  { font-size:26px; font-weight:700; color:#1e293b; display:block; }"
                + ".note-title { font-size:23px; font-weight:400; color:" + grayHex + "; display:block;"
                + " max-width:360px; line-height:1.4; }"
                + ".footer { text-align:center; margin-top:14px; padding-top:0; }"
                + ".footer-rule-wrap { display:table; width:100%; border-collapse:collapse; table-layout:fixed; }"
                + ".footer-rule-wrap span { display:table-cell; vertical-align:middle; }"
                + ".footer-rule-line { border-top:2px solid #d1d5db; }"
                + ".footer-text { font-size:" + ScheduleRenderModel.FOOTER_SIZE + "px;"
                + " color:" + grayHex + "; white-space:nowrap; padding:0 8px; width:1px; }"
                + ".footer-diamonds { font-size:20px; color:" + headerBg
                + "; letter-spacing:6px; white-space:nowrap; padding:0 4px; width:1px; }"
                + catCss;
    }

    private String buildHeaderHtml(ScheduleRenderModel model, String titleColor, String grayHex, boolean isNavy) {
        String shieldB64   = shieldBase64(model.getTheme());
        String buildingB64 = buildingBase64(model.getTheme());
        String calIconB64  = svgRenderer.renderToBase64("assets/calendar_icon.svg", 36, 36);

        int buildingW = model.getTheme() == ColorTheme.NAVY ? 500 : 480;
        int buildingH = 240;

        return "<div class=\"wrapper\">"
                + "<table class=\"header-outer\"><tr>"
                + "<td class=\"hdr-left\">"
                + "<img src=\"" + shieldB64 + "\" width=\"220\" height=\"220\" alt=\"Shield\"/>"
                + "</td>"
                + "<td class=\"hdr-center\">"
                + "<div class=\"title\">" + esc(model.getTitle()) + "</div>"
                + "<div class=\"subtitle\">" + esc(model.getSubtitle()) + "</div>"
                + "<div style=\"display:flex; align-items:center; gap:8px; margin:8px 0 6px 0; width:500px;\">"
                + "<div style=\"flex:1; height:1px; background:" + titleColor + "; opacity:0.2;\"></div>"
                + "<span style=\"color:" + titleColor + "; font-size:8px; letter-spacing:8px; opacity:0.45;\">"
                + "&#9679;&nbsp;&#9679;&nbsp;&#9679;&nbsp;&#9679;&nbsp;&#9679;"
                + "</span>"
                + "<div style=\"flex:1; height:1px; background:" + titleColor + "; opacity:0.2;\"></div>"
                + "</div>"
                + "<div class=\"date-line\">"
                + "<img src=\"" + calIconB64 + "\" width=\"30\" height=\"30\" "
                + "style=\"vertical-align:middle; margin-right:8px;\" alt=\"\"/>"
                + "<span style=\"vertical-align:middle;\">For The Week: "
                + esc(model.getDateRange()) + "</span>"
                + "</div>"
                + "</td>"
                + "<td class=\"hdr-right\">"
                + "<div style=\"text-align:right; overflow:hidden;\">"
                + "<img src=\"" + buildingB64 + "\" width=\"" + buildingW + "\" height=\"" + buildingH + "\" alt=\"Building\"/>"
                + "</div></td>"
                + "</tr></table>"
                + "<div class=\"thick-divider\"></div>"
                + "</div>";
    }

    private String buildTableHtml(ScheduleRenderModel model, boolean isNavy, String altBgHex) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"wrapper\"><table class=\"tbl\"><colgroup>");
        sb.append("<col style=\"width:160px\"/>");
        for (int i = 1; i < model.getColumns().size(); i++)
            sb.append("<col style=\"width:auto\"/>");
        sb.append("</colgroup><thead><tr>");
        for (ColumnDef col : model.getColumns())
            sb.append("<th")
                    .append(col == model.getColumns().get(0) ? " class=\"time-header\"" : "")
                    .append(">").append(esc(col.getLabel())).append("</th>");
        sb.append("</tr></thead><tbody>");

        String coffeeB64 = svgRenderer.renderToBase64("assets/coffee_icon.svg", 36, 36);
        String pinIconB64 = svgRenderer.renderToBase64("assets/pin_icon.svg", 44, 44);

        for (RowDef row : model.getRows()) {
            boolean isAlt = isNavy && row.getIndex() % 2 == 1;

            if (row.getType() == RowType.BREAK) {
                sb.append("<tr class=\"break-row\">");
                sb.append("<td class=\"time-cell\">").append(esc(row.getTimeLabel()))
                        .append("<span class=\"time-sub\">").append(esc(row.getTimeSubLabel()))
                        .append("</span></td>");
                for (int d = 0; d < 6; d++)
                    sb.append("<td class=\"break-cell\">")
                            .append("<img src=\"").append(coffeeB64)
                            .append("\" width=\"36\" height=\"36\" class=\"break-icon\" alt=\"\"/>")
                            .append("Break</td>");
                sb.append("</tr>");
                continue;
            }

            sb.append(isAlt ? "<tr class=\"alt-row\">" : "<tr>");
            sb.append("<td class=\"time-cell\">").append(esc(row.getTimeLabel()))
                    .append("<span class=\"time-sub\">").append(esc(row.getTimeSubLabel()))
                    .append("</span></td>");

            for (CellContent cc : row.getCells()) {
                if (cc.isEmpty()) {
                    sb.append("<td class=\"empty-cell\">\u2014</td>");
                } else {
                    CourseCategory cat = cc.getCategory();
                    String catCls   = "cat-" + cat.name().toLowerCase().replace('_', '-');
                    String entryCls = cc.hasConflict() ? "entry entry-conflict" : "entry";
                    sb.append("<td>")
                            .append("<div class=\"").append(catCls).append("\">")
                            .append("<div class=\"").append(entryCls).append("\">")
                            .append("<span class=\"entry-code\">")
                            .append(esc(cc.getCourseCode())).append("</span>")
                            .append("<span class=\"entry-name\">")
                            .append(esc(cc.getCourseName())).append("</span>")
                            .append("<span class=\"entry-instructor\">")
                            .append(esc(cc.getInstructor())).append("</span>")
                            .append("<span class=\"entry-room\">")
                            .append("<img src=\"").append(pinIconB64)
                            .append("\" width=\"22\" height=\"22\" ")
                            .append("style=\"vertical-align:middle; margin-right:5px;\" alt=\"\"/>")
                            .append(esc(cc.getRoom()))
                            .append("</span>")
                            .append("</div></div></td>");
                }
            }
            sb.append("</tr>");
        }

        sb.append("</tbody></table></div>");
        return sb.toString();
    }

    private String buildLegendAndNoteHtml(String headerBg) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"wrapper\" style=\"margin-top:6px;\">");
        sb.append("<div class=\"bot-bar\">")
                .append("<div class=\"legends\">");

        for (CourseCategory cat : CourseCategory.legendOrder()) {
            sb.append("<span class=\"legend-item\">")
                    .append("<span class=\"legend-dot\" style=\"background:")
                    .append(cat.bgHex)
                    .append("; border:1.5px solid ").append(cat.borderHex).append(";\"></span>")
                    .append("<span class=\"legend-label\">").append(esc(cat.label)).append("</span>")
                    .append("</span>");
        }

        String infoIcon = "<span style=\"display:inline-block; width:28px; height:28px;"
                + " background:#1e293b; border-radius:50%; color:#fff;"
                + " font-size:20px; font-weight:700; text-align:center;"
                + " line-height:28px; margin-right:8px; vertical-align:middle;\">i</span>";

        sb.append("</div>")
                .append("<div class=\"note-cell\">")
                .append("<div class=\"note-inner\">")
                .append("<span class=\"note-bold\">")
                .append(infoIcon)
                .append("Note:</span>")
                .append("<span class=\"note-title\">Please check the classroom<br/>and time before each session.</span>")
                .append("</div></div></div>");

        sb.append("</div>");
        return sb.toString();
    }

    private String buildFooterHtml(ScheduleRenderModel model, String headerBg) {
        String triDiamond = "\u2666 \u2666 \u2666";
        String footerText = esc(model.getFooter());
        int fSize = ScheduleRenderModel.FOOTER_SIZE;

        return "<div class=\"wrapper\">"
                + "<div style=\"margin-top:14px; display:flex; align-items:center; gap:0;\">"
                + "<div style=\"flex:1; height:2px; background:#d1d5db;\"></div>"
                + "<div style=\"white-space:nowrap; padding:0 10px; color:" + headerBg
                + "; font-size:18px; letter-spacing:6px;\">" + triDiamond + "</div>"
                + "<div style=\"white-space:nowrap; padding:0 6px; color:#4f5d75;"
                + " font-size:" + fSize + "px;\">" + footerText + "</div>"
                + "<div style=\"white-space:nowrap; padding:0 10px; color:" + headerBg
                + "; font-size:18px; letter-spacing:6px;\">" + triDiamond + "</div>"
                + "<div style=\"flex:1; height:2px; background:#d1d5db;\"></div>"
                + "</div></div>";
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
