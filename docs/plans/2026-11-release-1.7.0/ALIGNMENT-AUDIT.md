# Alignment and consistency audit: Ramadan card, Today strip widget, Times list

3 Nov 2026. Read only: nothing in the code was changed. Owner's complaint: "The cards and row widget have uneven
designs, and icon and text are not aligned. Cards are different sizes. These are basic mistakes."

**Sources.** Screenshots in `private/screenshots/1-7-0/` (1080 x 2400 at 420dpi, so 1dp = 2.625px; the images are shown
at 900 x 2000, so 1 display px = 1.2 real px = 0.457dp). I cropped them at 1:1 with `sips` to measure. Code at
`claude/1-7-0`, HEAD `b227f0f`, plus the uncommitted working tree.

**Important: the code moved while I audited.** `b227f0f` ("Ramadan tiles: same height and layout") landed after the
Ramadan screenshots were taken, and `RamadanCard.kt` has more uncommitted changes (divider, two-line fast text,
bold titleLarge countdown). So I checked the Ramadan findings against **current code**, and I mark the ones I saw only
in code, not on screen. The widget and Times-list findings match both the screenshots and the code.

Rules applied: DESIGN_SYSTEM §0 and §4.1, `docs/design-system/tokens/{spacing,shape}.json` (card radius 24, inner
tile radius **16**, `Space` gaps by relationship), Apple HIG layout and typography (one alignment axis per column, one
type size per role across siblings), ui-ux-pro-max (consistent spacing, no mixed sizes in the same role).

---

## 1. Today strip widget (`widget_strip.xml`, `WidgetRenderer.stripViews` / `fillColumns`)

### 1.1 One column's time is smaller than the other four (the worst defect)
- **Seen:** `widget-strip-2cell.png`, 1:1 crop. The cap height of "12:19 pm" (Asr) is about 19px against 22px for
  "2:09 am", "9:05 am", "2:44 pm" and "3:55 pm". That is about 10 to 10.5sp next to 12sp. The other five times sit on one
  baseline, but Asr's x-height is visibly lower.
- **Why:** every name and time TextView has its own `autoSizeTextType="uniform"` (`widget_strip.xml` lines 121-124,
  140-143, and the same in the other four columns: 20 views in all). Autosize fits each view **on its own**, so the
  longest string ("12:19 pm", 8 characters) is stepped down and its neighbours are not. On top of that, autosize on a
  `wrap_content` dimension is unsupported: Android's docs say not to use it there because results are unpredictable. The
  name views are `wrap_content` wide, and every view is `wrap_content` tall. Autosize cannot make sibling columns match
  by design.
- **Fix:** remove all `autoSize*` attributes from the 10 column TextViews. Then compute **one** size per size entry in
  `stripViews` and write it to all ten views (section 1.3).

### 1.2 Glyph beside the name puts the name off the column's centre line
- **Seen:** the glyph and name are centred **as a pair** (inner horizontal LinearLayout, `gravity="center"`), so the name
  is pushed right by half of (14dp glyph + 3dp gap). Measured in Fajr: the name's centre is 23px (8.8dp) right of the
  time's centre. Dhuhr, Asr and Isha show the same offset. Each column therefore has two centre lines. This is the
  owner's "icon and text are not aligned".
- **Seen:** in the highlighted Maghrib column, glyph + bold "Maghrib" fill 63 of the 66.6dp available. The pill (2dp
  padding, 12dp radius) touches the text at both ends, and that is what makes the pill look cramped.
- **Why it cannot be fixed in place:** at 300dp the column has 54.4dp inside it. Glyph 17dp + "Maghrib" at 12sp (44dp) =
  61dp, which does not fit. So option (b) always pushes Maghrib into a shrink or a cut at the common widths, and that is
  exactly what triggered autosize in 1.1.

### 1.3 Decision: (a) glyph above name above time, equal columns, fixed size. Recommended.

| | (a) glyph / name / time stacked | (b) glyph beside name (current) |
|---|---|---|
| Centre axis | One per column: glyph, name and time all on it | Two per column, name about 9dp off |
| Widest line at 12sp | "12:45 pm" bold about 51dp | glyph + "Maghrib" bold about 63dp |
| Fits 300dp (54.4dp column) at 12sp | Yes, about 3dp spare | No |
| Fits 400dp with town chip (53.4dp column) | Yes | No |
| Highlight pill | About 7dp air either side of the name | Touches the text |
| Height needed | 16 + 2 + 14 + 1 + 14 = 47dp in the column; 4 + 47 + 4 + 4 = 59dp with paddings, so it needs the **64dp** entry | 40dp |

(a) needs 64dp to show glyphs. In practice that covers one cell: on this emulator one cell tall is **104dp** (measured
from `widget-strip-before.png`: 228 display px = 104dp). Pixel and One UI cells are 70 to 100dp+. Only the theoretical
40dp entry loses the glyphs, and it shows names and times there as before.

**Owner check required (DESIGN_SYSTEM §0.2 item 3):** 1.7.0's comment at `WidgetRenderer.kt:116-118` records E2, "the
glyph now sits beside the name, so it fits one cell tall". (a) reverses that, so show him (a) and (b) side by side,
light and dark, before building. My recommendation is (a), because his complaint is exactly what (b) causes.

**Layout change (each of the 5 columns; ids unchanged, so `WidgetResourcesTest` still passes, and the glyph keeps its
layout tint for the tint test):**
```xml
<LinearLayout android:id="@+id/widget_row_fajr"
    android:layout_width="0dp" android:layout_height="wrap_content" android:layout_weight="1"
    android:background="@drawable/widget_row_plain" android:gravity="center_horizontal"
    android:orientation="vertical" android:paddingStart="2dp" android:paddingTop="2dp"
    android:paddingEnd="2dp" android:paddingBottom="2dp">
    <ImageView android:id="@+id/widget_glyph_fajr" android:layout_width="16dp" android:layout_height="16dp"
        android:layout_marginBottom="2dp" android:importantForAccessibility="no"
        android:src="@drawable/widget_glyph_fajr" android:tint="@color/widget_on_surface_variant" />
    <TextView android:id="@+id/widget_name_fajr" android:layout_width="match_parent"
        android:layout_height="wrap_content" android:ellipsize="end" android:importantForAccessibility="no"
        android:includeFontPadding="false" android:maxLines="1" android:textAlignment="center"
        android:textColor="@color/widget_on_surface" android:textSize="12sp" />
    <TextView android:id="@+id/widget_time_fajr" android:layout_width="match_parent"
        android:layout_height="wrap_content" android:layout_marginTop="1dp" android:ellipsize="end"
        android:fontFeatureSettings="tnum" android:importantForAccessibility="no"
        android:includeFontPadding="false" android:maxLines="1" android:textAlignment="center"
        android:textColor="@color/widget_on_surface" android:textSize="12sp" />
</LinearLayout>
```
Delete the inner horizontal `LinearLayout` and every `autoSize*` attribute. Update the header comment (lines 10-12)
that describes autosize. The `tools:ignore="UnusedAttribute"` on the root can stay for `fontFeatureSettings`.

**Renderer change:**
```kotlin
// stripViews: glyphs need the tall entry now that they sit above the name.
val glyphs = size.width >= STRIP_GLYPH_WIDTH_DP && size.height >= STRIP_TALL_HEIGHT_DP

// One size for all ten texts, so no column is ever smaller than its neighbours (autosize fitted each view alone).
// Measured against the widest of the ten strings, in bold with tabular figures (the highlighted column's face).
private const val STRIP_CHIP_RESERVE_DP = 105f // chip: 7 + 12 pin + 4 + 56 max text + 5 + 11 + 8 padding, + 2 margin

private fun stripTextSp(context: Context, widthDp: Float, chip: Boolean, texts: List<String>): Float {
    val inner = (widthDp - 8f - if (chip) STRIP_CHIP_RESERVE_DP else 0f) / 5f - 4f // root 4+4, column 2+2
    val scale = context.resources.configuration.fontScale
    val paint = android.text.TextPaint().apply { typeface = Typeface.DEFAULT_BOLD; fontFeatureSettings = "tnum" }
    var sp = 12f
    while (sp > 9f) {
        paint.textSize = sp * scale // in dp units, so measureText returns dp
        if (texts.all { paint.measureText(it) <= inner }) return sp
        sp -= 0.5f
    }
    return 9f // the owner's shrink-not-cut floor (Hijri line precedent, DESIGN_SYSTEM §9)
}
```
In `fillColumns`, collect the five headings and five times first, then call
`setTextViewTextSize(ids.name / ids.time, COMPLEX_UNIT_SP, sp)` on all ten. Write it on every render (renderer rule 2).
`chip` = the chip is visible at this entry (`size.width >= STRIP_CHIP_WIDTH_DP && !large && place != null`).
The PAINTS table is untouched: it holds colours, not sizes.

**Resulting fixed sizes (Roboto, "12:45 pm" bold about 4.2em, "Maghrib" bold about 3.75em; "12:45 PM"
(Bengali, Indonesian, Urdu) about 4.25em; Arabic "م"/"ص" and الإفطار / المغرب are narrower):**

| Strip width | Column inside | Size at 1.0 text | At 1.15 text |
|---|---|---|---|
| 250dp | 44.4dp | **10sp** (42dp) | 9sp |
| 300dp | 54.4dp | **12sp** (51dp) | 11sp |
| 330dp | 60.4dp | **12sp** | 12sp |
| 361dp (the screenshot) | 66.6dp | **12sp** | 12sp |
| 400dp + town chip | 53.4dp | **12sp** | 11sp |
| 400dp, no chip | 74.4dp | **12sp** | 12sp |

From 1.3 text the strip already shows the next prayer alone, so nothing above 1.3 matters. All five columns always
share one size. Height is checked in 1.3's table: at 1.15 the column is 16 + 2 + 16.2 + 1 + 16.2 = 51.4dp, inside the
64dp entry's 52dp.

### 1.4 Two-cell form: the hero line is crowded against the row, with empty space above and below
- **Seen** (`widget-strip-2cell.png`, widget 361 x 224dp): the "Maghrib 2:44 pm" descender ends about 5dp above the pill,
  and there is about 85dp of empty sky above and below the pair. `widget_strip_next` has `layout_marginBottom="10dp"`,
  but the 22sp hero keeps its font padding, so the visible gap is only about 5dp.
- **Seen:** the hero glyph is 20dp (`widget_glyph`, line 44) beside 22sp text. Its drawn mark is about 12.8dp tall
  against a cap height of about 16dp, so the icon reads small and light next to the bold name.
- **Fix:** `layout_marginBottom="16dp"` on `widget_strip_next` (a group gap: hero, then the list). In `stripViews`,
  when `sp == HERO_SP` on S+ (the only place this entry exists),
  `views.setViewLayoutWidth(R.id.widget_glyph, 26f, COMPLEX_UNIT_DIP)` and the same for its height, and write back
  20dp otherwise (rule 2). Keep the block centred vertically: once the gap is right, equal space above and below is
  correct.

### 1.5 Minor
- With (a), the highlight pill gets the same air as the Times list "Next" pill. No change needed beyond 1.3.
- The glyph artwork has different optical heights (Dhuhr and Asr marks run about 28px; Fajr and Isha about 23px at
  14dp). At 16dp in a stack this is less visible. No change unless the owner notices it.

---

## 2. Ramadan card (`RamadanCard.kt`, HEAD `b227f0f` + working tree)

### 2.1 Tiles of different heights. Fixed in `b227f0f`, verified in code only, not on screen
- **Seen in the screenshots** (before `b227f0f`): Suhoor tile 71dp tall, Iftar tile 98dp. The difference is the "Next"
  pill line (4dp spacer + 22dp pill). Iftar's bottom inset was 12dp against Suhoor's 16dp.
- **Now:** `Row(Modifier.height(IntrinsicSize.Min))` plus `fillMaxHeight()` on each tile, and the pill moved to the end
  of a fixed 30dp glyph row in both tiles. That is the right fix. **It still needs a light, dark, 2.0 text and Urdu
  screenshot**, because at 2.0 text "Suhoor ends" can wrap while "Iftar" does not. `IntrinsicSize.Min` keeps the boxes
  equal, but check that the times still share a baseline. If they do not, add `Modifier.weight(1f)` spacer above the
  time so both times sit on the bottom inset.

### 2.2 Tile corner radius is off-token
- `TimeColumn`: `RoundedCornerShape(18.dp)`. `tokens/shape.json` `radius.inner` = **16** ("A tile nested inside a card
  (Ramadan time columns)"). **Fix:** `RoundedCornerShape(16.dp)`.

### 2.3 Tile padding is uneven and off-scale
- `.padding(horizontal = 14.dp, vertical = 12.dp)`: 14 is on no scale here. **Fix:** `.padding(Space.Paragraph)`
  (12 all round), so the glyph's top and left insets match.

### 2.4 The same prayer has two different icons on one screen (ask the owner or Fable)
- The Suhoor tile uses `widget_glyph_fajr` (rays on a horizon), while the Today list's Fajr row and the card's own header
  use `Icons.Rounded.Brightness3` (crescent). The Iftar tile uses `widget_glyph_maghrib`, while the list's Maghrib uses
  `Brightness6`. One screen therefore uses two icon families for the same two prayers. This is a visual decision
  (§0.2 item 1), so do not change it on my say. Recommendation for Fable: one family for the five prayers across Times
  and widgets. The glyph set is the more distinctive one, and the list would adopt it.

### 2.5 Three icon sizes inside one card
- Header moon 20dp, tile glyphs 30dp (since `b227f0f`, the owner asked for bigger), (i) 24dp. The 30dp size is the
  owner's call and stays. Note only.

### 2.6 The (i) is 12dp inside the card's right content edge
- **Seen:** "Day 3 of 29" and both tiles end at x = 820 (display). The (i) glyph ends at about 790, because the 24dp icon is
  centred in a 48dp `IconButton`. **Fix:** `Modifier.size(48.dp).offset(x = 12.dp)` on the `IconButton`
  (`offset` mirrors in RTL). The target stays inside the card's 20dp padding.

### 2.7 Card padding is off-token (low)
- `.padding(horizontal = 20.dp, vertical = 16.dp)`. `spacing.json` gives card padding = `Space.Group` (16). The Next
  prayer card (`HomeScreen.kt:607`) uses the same 20/16, so the two cards match each other. Change both together or
  neither. Note only.

### 2.8 Working-tree divider uses a derived colour (code only)
- `HorizontalDivider(color = scheme.onPrimaryContainer.copy(alpha = 0.2f))`. It is not hex, but it is an untested derived
  colour. **Fix:** `HorizontalDivider(color = scheme.outlineVariant)`, or keep it and say so in the commit. Also tidy
  the fully qualified `androidx.compose...` names into imports.

### 2.9 Orphan word in the old fast line
- The screenshot shows "...1 minute longer than / yesterday". The working tree splits it into two lines, which fixes
  this. Verify at 360dp.

---

## 3. Times list (`HomeScreen.kt` `PrayerRow`, around line 1327)

### 3.1 Sunrise and Midnight names start 3dp left of the prayer names
- **Seen** (both Times screenshots): the "Sunrise" text starts at x = 145 and Fajr, Dhuhr, Asr and Maghrib at x = 151
  (display), a 2.7dp difference. **Why:** the icon is `size(if (dimmed) 18.dp else 21.dp)` and is followed by the same
  14dp spacer, so the name column moves by 3dp.
- **Fix:** keep the slot 21dp and shrink only the drawing:
  ```kotlin
  Box(Modifier.size(21.dp), contentAlignment = Alignment.Center) {
      Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(if (dimmed) 18.dp else 21.dp))
  }
  ```

### 3.2 Fajr with three lines is crammed against its dividers
- **Seen** (`ramadan-light.png`): "Fajr" starts about 3dp below the card top, and "May not ring on time" touches the
  divider. **Why:** `.padding(vertical = 1.dp)` with 24 + 16 + 16 = 56dp of text: the 50dp floor no longer gives any air.
- **Fix (smallest):**
  `.padding(vertical = if (note != null && warning != null) Space.Tight else 1.dp)`.
  This adds 6dp to that one row only, so the six-row budget note at the 50dp floor is unaffected on other days.

---

## 4. No-location Times and the Allow row (`PermissionAction.kt` `PermissionCard`)

Checked and **aligned**. "Makkah" and the Set location button share a centre line (y = 175). The Allow tile, title
block, Allow button and X all centre on y of about 941. All three compact buttons (Set location, Allow, Timetable) are
the same 40dp drawn height (85 display px).

Two low items:
- The X glyph sits 12dp inside the right gutter while the Timetable button below ends on it. This is standard for an
  `IconButton`. Leave it unless the owner points at it (it is kind D, always alone).
- The body wraps to leave "phone." alone on line 2 at 360dp. Optional: `style = bodySmall.copy(lineBreak = LineBreak.Paragraph)`
  (balanced on API 33+), or shorter copy (a copy change needs the owner).

---

## Not verified
- I took no new screenshots. The Ramadan items in section 2 are read against code newer than the screenshots.
- Text widths are computed from Roboto advance widths, not rendered. The `measureText` approach in 1.3 removes that
  gap at runtime.
- Urdu, Arabic and Bengali rendering of the strip is not checked. Their names are shorter than "Maghrib", but taller
  script needs the 64dp height check on a device.
