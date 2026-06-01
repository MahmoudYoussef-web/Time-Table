# Flying Saucer PNG Rendering Audit

Audit of `SchedulePngService.java` against Flying Saucer 9.3.1 rendering capabilities.

---

## 1. Rendered HTML Snippets

### Header

```html
<div class="page header">
  <table class="header-table">
    <tr>
      <td class="header-left-cell">
        <svg width="120" height="120" viewBox="0 0 120 120" xmlns="http://www.w3.org/2000/svg">
          <polygon points="60,10 120,45 60,80 0,45" fill="none" stroke="#123A8C" stroke-width="2.5"/>
          <polygon points="60,80 120,45 120,55 60,90 0,55 0,45" fill="none" stroke="#123A8C" stroke-width="2"/>
          <rect x="44" y="75" width="32" height="28" rx="3" fill="none" stroke="#123A8C" stroke-width="2"/>
          <line x1="60" y1="75" x2="60" y2="103" stroke="#123A8C" stroke-width="2"/>
          <circle cx="60" cy="112" r="6" fill="none" stroke="#123A8C" stroke-width="2"/>
          <line x1="60" y1="118" x2="60" y2="120" stroke="#123A8C" stroke-width="2"/>
          <path d="M30 38 Q45 20 60 20 Q75 20 90 38" fill="none" stroke="#123A8C" stroke-width="1.5"/>
          <path d="M20 50 Q10 38 18 30" fill="none" stroke="#123A8C" stroke-width="1.5"/>
          <path d="M100 50 Q110 38 102 30" fill="none" stroke="#123A8C" stroke-width="1.5"/>
        </svg>
      </td>
      <td class="header-center-cell">
        <div class="title">University Study Schedule</div>
        <div class="subtitle">First Year &#8212; Semester: Fall 2026</div>
        <div class="date-line">
          <svg width="16" height="16" viewBox="0 0 14 14" style="display:inline;margin-right:4px">
            <rect x="2" y="3" width="10" height="10" rx="1" fill="none" stroke="#123A8C" stroke-width="1.2"/>
            <line x1="2" y1="7" x2="12" y2="7" stroke="#123A8C" stroke-width="1.2"/>
            <line x1="5" y1="1" x2="5" y2="4" stroke="#123A8C" stroke-width="1.2"/>
            <line x1="9" y1="1" x2="9" y2="4" stroke="#123A8C" stroke-width="1.2"/>
          </svg>
          For The Week: 2026-05-25 &#8212; 2026-06-01
        </div>
      </td>
      <td class="header-right-cell">
        <svg width="200" height="110" viewBox="0 0 200 110" xmlns="http://www.w3.org/2000/svg">
          <rect x="55" y="25" width="90" height="78" fill="none" stroke="#123A8C" stroke-width="1.8"/>
          <polygon points="30,25 100,0 170,25" fill="none" stroke="#123A8C" stroke-width="1.8"/>
          <line x1="75" y1="25" x2="75" y2="103" stroke="#123A8C" stroke-width="1.3"/>
          <line x1="100" y1="25" x2="100" y2="103" stroke="#123A8C" stroke-width="1.3"/>
          <line x1="125" y1="25" x2="125" y2="103" stroke="#123A8C" stroke-width="1.3"/>
          <rect x="110" y="50" width="14" height="20" fill="none" stroke="#123A8C" stroke-width="1.2"/>
          <rect x="76" y="50" width="14" height="20" fill="none" stroke="#123A8C" stroke-width="1.2"/>
          <circle cx="20" cy="58" r="18" fill="none" stroke="#123A8C" stroke-width="1.3"/>
          <line x1="20" y1="76" x2="20" y2="103" stroke="#123A8C" stroke-width="1.3"/>
          <circle cx="180" cy="58" r="18" fill="none" stroke="#123A8C" stroke-width="1.3"/>
          <line x1="180" y1="76" x2="180" y2="103" stroke="#123A8C" stroke-width="1.3"/>
          <path d="M5 103 L195 103" stroke="#123A8C" stroke-width="1.3"/>
          <circle cx="14" cy="22" r="6" fill="none" stroke="#123A8C" stroke-width="1.2"/>
          <path d="M14 28 C14 52 38 52 38 38" fill="none" stroke="#123A8C" stroke-width="1.2"/>
          <circle cx="186" cy="22" r="6" fill="none" stroke="#123A8C" stroke-width="1.2"/>
          <path d="M186 28 C186 52 162 52 162 38" fill="none" stroke="#123A8C" stroke-width="1.2"/>
        </svg>
      </td>
    </tr>
  </table>
</div>
<div class="page"><hr class="divider"/></div>
```

### Logo SVG

```html
<svg width="120" height="120" viewBox="0 0 120 120" xmlns="http://www.w3.org/2000/svg">
  <polygon points="60,10 120,45 60,80 0,45" fill="none" stroke="#123A8C" stroke-width="2.5"/>
  <polygon points="60,80 120,45 120,55 60,90 0,55 0,45" fill="none" stroke="#123A8C" stroke-width="2"/>
  <rect x="44" y="75" width="32" height="28" rx="3" fill="none" stroke="#123A8C" stroke-width="2"/>
  <line x1="60" y1="75" x2="60" y2="103" stroke="#123A8C" stroke-width="2"/>
  <circle cx="60" cy="112" r="6" fill="none" stroke="#123A8C" stroke-width="2"/>
  <line x1="60" y1="118" x2="60" y2="120" stroke="#123A8C" stroke-width="2"/>
  <path d="M30 38 Q45 20 60 20 Q75 20 90 38" fill="none" stroke="#123A8C" stroke-width="1.5"/>
  <path d="M20 50 Q10 38 18 30" fill="none" stroke="#123A8C" stroke-width="1.5"/>
  <path d="M100 50 Q110 38 102 30" fill="none" stroke="#123A8C" stroke-width="1.5"/>
</svg>
```

### Building SVG

```html
<svg width="200" height="110" viewBox="0 0 200 110" xmlns="http://www.w3.org/2000/svg">
  <rect x="55" y="25" width="90" height="78" fill="none" stroke="#123A8C" stroke-width="1.8"/>
  <polygon points="30,25 100,0 170,25" fill="none" stroke="#123A8C" stroke-width="1.8"/>
  <line x1="75" y1="25" x2="75" y2="103" stroke="#123A8C" stroke-width="1.3"/>
  <line x1="100" y1="25" x2="100" y2="103" stroke="#123A8C" stroke-width="1.3"/>
  <line x1="125" y1="25" x2="125" y2="103" stroke="#123A8C" stroke-width="1.3"/>
  <rect x="110" y="50" width="14" height="20" fill="none" stroke="#123A8C" stroke-width="1.2"/>
  <rect x="76" y="50" width="14" height="20" fill="none" stroke="#123A8C" stroke-width="1.2"/>
  <circle cx="20" cy="58" r="18" fill="none" stroke="#123A8C" stroke-width="1.3"/>
  <line x1="20" y1="76" x2="20" y2="103" stroke="#123A8C" stroke-width="1.3"/>
  <circle cx="180" cy="58" r="18" fill="none" stroke="#123A8C" stroke-width="1.3"/>
  <line x1="180" y1="76" x2="180" y2="103" stroke="#123A8C" stroke-width="1.3"/>
  <path d="M5 103 L195 103" stroke="#123A8C" stroke-width="1.3"/>
  <circle cx="14" cy="22" r="6" fill="none" stroke="#123A8C" stroke-width="1.2"/>
  <path d="M14 28 C14 52 38 52 38 38" fill="none" stroke="#123A8C" stroke-width="1.2"/>
  <circle cx="186" cy="22" r="6" fill="none" stroke="#123A8C" stroke-width="1.2"/>
  <path d="M186 28 C186 52 162 52 162 38" fill="none" stroke="#123A8C" stroke-width="1.2"/>
</svg>
```

### Calendar Icon

```html
<svg width="16" height="16" viewBox="0 0 14 14" style="display:inline;margin-right:4px">
  <rect x="2" y="3" width="10" height="10" rx="1" fill="none" stroke="#123A8C" stroke-width="1.2"/>
  <line x1="2" y1="7" x2="12" y2="7" stroke="#123A8C" stroke-width="1.2"/>
  <line x1="5" y1="1" x2="5" y2="4" stroke="#123A8C" stroke-width="1.2"/>
  <line x1="9" y1="1" x2="9" y2="4" stroke="#123A8C" stroke-width="1.2"/>
</svg>
```

### Time Column

```html
<td class="time-cell">
  9:00 AM
  <span class="time-sub">&#8211; 11:00 AM</span>
</td>
```

### CS303 Cell

```html
<td>
  <div class="entry">
    <div class="entry-code">CS303</div>
    <div class="entry-name">Data Structures</div>
    <div class="entry-instructor">Dr. Smith</div>
    <div class="entry-room">Rm: 201</div>
  </div>
</td>
```

---

## 2. Static Verification Checks

### Check 1 — Logo SVG inside rendered DOM?
**YES.** Concatenated at `SchedulePngService.java:148`, placed inside `<td class="header-left-cell">` at line 183.

### Check 2 — `width="120" height="120"` directly on `<svg>` tag?
**YES.** Line 148:
```
"<svg width=\"120\" height=\"120\" viewBox=\"0 0 120 120\" xmlns=\"http://www.w3.org/2000/svg\">"
```

### Check 3 — Building SVG inside rendered DOM?
**YES.** Concatenated at line 159, placed inside `<td class="header-right-cell">` at line 195.

### Check 4 — `width="200" height="110"` directly on `<svg>` tag?
**YES.** Line 159:
```
"<svg width=\"200\" height=\"110\" viewBox=\"0 0 200 110\" xmlns=\"http://www.w3.org/2000/svg\">"
```

### Check 5 — Any CSS rule containing `.entry-conflict`, `.conflict`, `color:red`, `#cc0000`?
**NONE.** Zero matches in `buildCss()` (lines 104-145). The `conflictSectionIds` parameter flows through every method but is **never consumed** in `buildEntryHtml()`.

### Check 6 — Any CSS rule containing `.break-time` with `background:#123A8C`?
**NONE.** `.break-time` at lines 141-142 contains only:
```css
.break-time { text-align: center; font-size: 15px; font-weight: 700; color: #222222; vertical-align: middle; padding: 10px; }
```
No `background` property.

### Check 7 — Any element with `class="entry-conflict"`?
**NO.** `buildEntryHtml()` at lines 251-257 generates:
```html
<td>
  <div class="entry">
    <div class="entry-code">...</div>
    <div class="entry-name">...</div>
    <div class="entry-instructor">...</div>
    <div class="entry-room">...</div>
  </div>
</td>
```
The class `entry-conflict` is never assigned anywhere in the class. Conflict data is accepted but discarded.

---

## 3. Full Feature Rendering Audit

### Will Render Correctly

| Feature | Source | Details |
|---|---|---|
| Blue header bar on table | `.tbl th { background: #123A8C; }` line 123 | Flying Saucer supports `background` on table cells |
| White header text | `.tbl th { color: #FFFFFF; }` line 123 | Simple text color |
| Day name headers | `<th>` with capitalized names, line 209 | Plain table header cells |
| Time column (12h format) | `class="time-cell"` lines 230-231 | Text + `<span>` with `display:block` |
| Course code (bold) | `class="entry-code"` line 253 | `font-weight: 700` supported |
| Course name | `class="entry-name"` line 254 | Simple styled `<div>` |
| Instructor name | `class="entry-instructor"` line 255 | Simple styled `<div>` |
| Room number | `class="entry-room"` line 256 | Simple styled `<div>` |
| Break row | `class="break-time"` + `class="break-cell"` lines 218-225 | Standard table row |
| Footer with date | `class="footer-text"` lines 262-264 | Simple text span |
| `<hr>` divider | `class="divider"` line 197 | Flying Saucer renders `<hr>` |
| Title "University Study Schedule" | `class="title"` line 185 | `font-size: 48px`, `font-weight: 900` |
| Semester subtitle | `class="subtitle"` line 186 | Simple text |
| Date line text | `class="date-line"` line 187 | Simple text |
| Logo SVG shapes | Lines 148-157 | `<polygon>`, `<rect>`, `<line>`, `<circle>`, `<path>` with stroke |
| Building SVG shapes | Lines 159-175 | Same basic SVG support |
| Empty cells with dash | `class="empty-cell"` line 238 | `&#8212;` entity renders as em dash |
| Light blue borders | `.tbl td { border: 1px solid #B7CAE8; }` line 126 | Standard CSS border |

### Will Render Incorrectly or Not at All

| Issue | Location | Root Cause | Impact |
|---|---|---|---|
| Calendar icon breaks text flow | Lines 188-193 | `style="display:inline;margin-right:4px"` on `<svg>` — Flying Saucer does **not** process CSS on SVG elements. Batik treats inline SVGs as block-level replaced elements. | SVG renders at 16x16 but breaks line layout; text after it flows to next line. |
| `rx="3"` on logo `<rect>` | Line 151 | Flying Saucer Batik ignores `rx` attribute on `<rect>` elements. | Renders as sharp-cornered rectangle instead of rounded. |
| `rx="1"` on calendar `<rect>` | Line 189 | Same as above. | Renders as sharp corner. |
| `border-radius: 8px` on table | Line 122 | Flying Saucer does not support CSS `border-radius`. | Table corners remain square. |
| `overflow: hidden` on table | Line 122 | Flying Saucer does not support CSS `overflow`. | No clipping applied (no visual effect since border-radius is also ignored). |
| Conflict highlighting | `conflictSectionIds` unused | The parameter is parsed, passed through 3 methods, but never checked in `buildEntryHtml()`. | No visual conflict indication in PNG output (unlike PDF and Excel services which do highlight conflicts). |

---

## 4. Dead Code: `conflictSectionIds` Flow

```
generatePng (line 33)  →  buildHtml (line 91)
                              →  buildTableHtml (line 203)
                                   →  buildEntryHtml (line 248) — PARAMETER RECEIVED BUT NEVER READ
```

The `Set<Long> conflictSectionIds` is passed through the entire call chain but **never referenced** inside `buildEntryHtml()`. The method signature accepts it, the parameter is unused. The PNG path has no conflict styling at all.

Compare with `SchedulePdfService.java:217` which does use it:
```java
boolean hasConflict = e.sectionId() != null && conflictSectionIds != null && conflictSectionIds.contains(e.sectionId());
```
