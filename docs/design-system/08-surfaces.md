# 8. Surfaces outside the app screens

Home screen widgets, Wear OS, notifications, the PDF, the launcher icon and the store pictures. They
use the same tokens; where they differ, it is recorded here with the reason. All are **Android only**
today; the iPhone equivalent is named for each.

---

## 8.1 Home screen widgets (`app/src/main/java/com/sajdatime/app/widget/`)

Three widgets, five layouts (plus a `_fixed` twin each for a chosen Light or Dark theme). Drawn by
`WidgetRenderer.kt` from saved state; no periodic updates: one alarm redraws at the next prayer,
sunrise or midnight.

| Widget | Min size, cells | Layouts | Shows |
|---|---|---|---|
| Next prayer | 180 × 110, 3 × 2 (under 140 tall, 150 in Urdu, Arabic and Bengali, the day bars are hidden, with a smaller countdown and less padding so the chip fits; below Android 12 only this short form exists, so no bars there) | Small (default, the only one below Android 12); wide from 270 × 158, or 170 (192 from 1.1x text) in Urdu, Arabic and Bengali (heights measured 3 Nov 2026: small wants 131.8dp, 134.9 at 1.15x, 140.6 at 1.3x and 161.5 at 2.0; wide 149 and 156 at 1.15x) | Glyph, name, time; the countdown; the town chip; five day bars. Wide adds the six rows. Body is the current sky |
| Today | 250 × 204, 4 × 4 | Compact; large from 250 × 300, or 324 from 1.1x text (346, and 374 from 1.1x text, in Urdu, Arabic and Bengali, measured); measured 296dp and 322dp wanted | Six rows with glyphs; large adds the mark, date and Hijri date, and a countdown card |
| Today strip (new in 1.6) | 250 × 40, 4 × 1 | One layout, ten size entries: 250 × 40, 110 × 40; 300 and 400 wide each at 40, 78 (86 in Urdu, Arabic, Bengali), 148 and 152 tall (measured 3 Nov 2026: glyph row wants 72.8dp, hero tier 124.6dp, big hero 129.5, all +5 at 1.15x and about +15 in Urdu and Arabic; they were 64, 110 and 150 and cut the times and underline off) | Five columns; the next prayer, only when still today, has the logo in place of its glyph, bold text and an underline, without a pill; glyphs require at least 86 tall and 300 wide; the chip from 400 wide |

At 1.3× text each widget keeps its first layout and the strip shows the next prayer alone.

**Look.** Paper widgets: a fixed three stop body (`widget_surface_top` → `widget_surface` →
`widget_surface_bottom`), a sheen (dark only, 7%), a 1dp rim; radius 16, or the launcher's own on
Android 12+. Next prayer and Today (1.7.0): the strip's body instead of paper, the town chip gets a 1dp `primary`
rim, the current row carries the app mark in place of its glyph, bold name and time, and on Next prayer "to come" bars become a 30% track. Countdown card (large Today): radius 18, highlight
gradient. Rows and pills radius 12. Day bars: done filled `primary`, next outlined `primary` (shape,
not only fill), to come `outlineVariant`. All three use the period sky in light and the app's dark green
in dark (`widget_strip_*`); the paper body remains only in the picker previews.

**Current prayer marker, by size (Ali, 3 Nov 2026, from mockups).** Larger forms (Today with the countdown card, about 4x3 and up;
Next prayer wide) put a highlight band (the rimmed `widget_pill_sky`, because the plain `widget_highlight` fill is 1.0:1 against the sky and would not show) behind the current row: logo in place of the glyph, bold, no
underline. Smaller forms (the strip, compact Today 3x3) use a 4dp underline (`widget_underline`, chosen over 3dp for reading at a
glance, no added row height on the strip, whose bar margin dropped from 3dp to 2dp; on the compact Today the marked row has 4dp of its own bottom padding for the bar, so it is 3dp taller and the other rows lose their 1dp, fixed 4 Nov 2026 after the bar cut the text on a real Redmi) with the same
logo and bold. Launchers below Android 12 always get the compact Today (underline, glyph kept). The exact size cut-off is confirmed in the widget space pass (task F). Ali also asked for a small app logo in the top
start corner of the small widgets (top left, top right in right to left) so people remember the app, a separate small mark (Ali chose it over swapping the sun glyph).
Built only on the strip's 148dp and 152dp entries at normal text size (3 Nov 2026), not yet seen on a screen. The thin and
78dp entries have no room, and a 90dp entry was tried and dropped: the Opus review put its height over budget from 1.15x
text and the hero line over 300dp wide. Next prayer, compact Today and the other small forms have no corner logo yet.

**Type.** Countdown in the system light weight, tabular, sized in dp (36 small Next, 34 wide, 38 large
Today) because a large font setting pushed names off. Names 16 bold on Next (rows 13), 15 on the large Today, 14 on the compact Today, 12 (to 9) on
the strip. Floor 12; named exceptions down to 9: the large Today Hijri line and the strip time at 250
wide. Labels and footers are 12 (raised from 11 on 3 Nov 2026, 1.7.0). **Known gaps:** strip names also shrink to 9, and
the am/pm after a time is drawn at 0.7 of the time (about 10), neither listed as an exception; compact Today
rows and wide Next rows stay 14 and 13 until their fit is measured at 1.15x and 2x (a 15 row clips at 180dp tall).

**Glyphs** show where the sun is, so they read without language: Fajr rays over an empty horizon,
Sunrise a rising half sun, Dhuhr the sun over a post with no shadow, Asr a shadow equal to the post,
Maghrib a setting half sun, Isha a crescent and star. Drawn white, tinted per theme; never mirrored.

**States.** No location: "Open SajdaTime to set your location". Exact alarms denied: the time replaces
the countdown. After Isha: "Tomorrow". Polar: "Approximate time". Ramadan: "Suhoor ends", "Iftar".

**Accessibility.** Text views are hidden; the widget speaks one sentence ("Next prayer Asr at 3:42 pm,
in 1h 14m"). The town chip is its own button ("Update location, London"); it opens the app, which
updates the location.

**Colours** are in `tokens/color.json` → `widget` (tested), held to the app by `WidgetColorsTest` and
`WidgetThemeTest`. The mark is the icon's own green and sand, not theme roles.

Resource names listed literally in `WidgetResourcesTest`, `WidgetThemeTest` and `WidgetDeviceTest`:

| Drawable resources | Use |
|---|---|
| `widget_background` | Paper body |
| `widget_chip` | Town chip |
| `widget_divider`, `widget_divider_sky` | Dividers on paper and sky |
| `widget_hero_background` | Large Today countdown card |
| `widget_pill_sky` | Town chip and the large forms' current row band |
| `widget_preview_next`, `widget_preview_strip`, `widget_preview_today` | Widget picker previews |
| `widget_row_highlight`, `widget_row_plain` | Rows (the plain fill is no longer the band: see widget_pill_sky) |
| `widget_seg_done`, `widget_seg_next`, `widget_seg_todo`, `widget_seg_todo_sky` | Day bars on paper and sky |
| `widget_sky_dawn`, `widget_sky_day`, `widget_sky_evening`, `widget_sky_night` | Next prayer body by sky period (unused since 1.7.0) |
| `widget_strip_dawn`, `widget_strip_day`, `widget_strip_evening`, `widget_strip_night` | Strip body by sky period |
| `widget_underline` | Current prayer underline: the strip column and the compact Today row (4dp) |

| Fixed colour resources | Use |
|---|---|
| `widget_light_on_surface` | Main text ink in a chosen Light theme |
| `widget_dark_on_surface` | Main text ink in a chosen Dark theme |

The 60 fixed colours remain covered by the existing `widget_light_*` and `widget_dark_*` wildcard prose in `tokens/color.json`.

**iPhone:** WidgetKit. Next prayer → `systemSmall` and `systemMedium`; Today → `systemLarge` and
`systemMedium`; strip → `systemMedium` or `accessoryRectangular` on the lock screen. Countdown →
`Text(date, style: .timer)`. Same colours from the token file.

## 8.2 Wear OS (`wear/`)

One scheme, always dark, independent of the phone's theme choice. Every colour equals the phone's dark
role except the background, **pure black** for OLED (`tokens/color.json` → `wear`, now tested). Wear
Material 3 default type, floor 12. Everything inside a 10% circular inset; lists end with 20% padding.

| Screen | Content |
|---|---|
| Times | Next prayer name (`primary`), countdown, clock; five prayers (no Sunrise), next in `primary`; location button; the disclaimer line; school button last |
| Qibla | Dial on `surfaceContainer` (`primaryContainer` when aligned), remaining turn arc, needle, the Kaaba at 0.66 of the radius (phone 0.70) and 0.30 across (phone 0.22); "Facing the Kaaba" or the bearing; a buzz on arrival |
| School, Location | Full screen choices with chips and OK |
| Tile | Label 11, prayer name 24 bold, detail 14; one entry per minute |

**Known gaps:** the tile's three colours do not match the palette (`wearTile`, held by test so they
cannot move silently; aligning them is the owner's call); the tile preview shows the notification arch;
tapping the tile does nothing; next is shown by colour alone; the watch icon uses the old green
`#14624B` and is not adaptive.

The owner tested the watch himself (1 Sep 2026). **Never ask him to test the watch** (`CLAUDE.md`).

**iPhone:** a watchOS SwiftUI app with a paged `TabView` (Times, Qibla); the tile becomes a WidgetKit
complication or Smart Stack widget.

## 8.3 Notifications (`app/src/main/java/com/sajdatime/app/notify/`)

| Type | Title | Body | Channel, importance |
|---|---|---|---|
| Prayer alert | Time for Asr | Asr begins at 3:42 pm | Prayer times (high), or Prayer alarm (high, alarm sound, may bypass Do Not Disturb) |
| Reminder before prayer | Asr at 3:42 pm | Asr begins in 10m | Reminder before prayer (default, no sound) |
| Suhoor reminder | Suhoor ends at 5:10 am | Fajr begins in 60m | Suhoor reminder (high) |
| Next prayer badge | Asr at 3:42 pm | System countdown | Next prayer badge (low, silent, ongoing) |

Small icon: the mihrab arch, white (`ic_notification`). No colour, actions, large icon or expanded style
are set. Channel names follow the app language. "Approximate time" as sub text on polar days.

**iPhone:** `UNNotificationRequest` with a calendar trigger; alarm style → Time Sensitive interruption
level (Critical Alerts need Apple's entitlement); the badge → a Live Activity.

## 8.4 PDF (`app/src/main/java/com/sajdatime/app/pdf/PrayerPdfExporter.kt`)

A4 at 72dpi (595 × 842pt), 36pt margins, rows 19pt with 0.4pt `#AAAAAA` hairlines, system sans. Header:
logo tile and wordmark in the icon green `#0E6B4F`, tagline 8.5pt `#6B6B6B`, the place 15pt bold, a 1pt
rule, "Approximate area · month" 9pt `#555555`. Table header 10 bold, cells 10. Ramadan: #, Day, Date,
six times, Fajr and Maghrib bold (emphasis by weight, so it prints in black and white). Footer 8pt
`#777777` "Calculated offline on your device. SajdaTime is free, forever." RTL mirrors the table.

**iPhone:** `UIGraphicsPDFRenderer` with the same point metrics.

## 8.5 Launcher icon and marks

Adaptive icon: background `#0E6B4F`, a sand `#F0D69A` mihrab and two minarets inside the safe zone
(4.57:1), the same foreground as the monochrome layer for themed icons. The icon green is one step
from `primary` on purpose; both move together only in an icon refresh. The notification icon is the
arch alone. The widget mark (`widget_mark`) is the icon on a rounded tile, used on the large Today
widget and in place of the highlighted prayer's glyph on the strip, Next prayer and Today.

**Known gap:** the Android 7 fallback icon and the watch icon still use `#14624B`.

**iPhone:** an App Store icon from the same artwork (1024pt, no transparency), with dark and tinted
variants; check it is distinct from other prayer apps (App Store Guideline 4.3).

## 8.6 Store pictures (`docs/store/`)

Generated by `tools/build-store-assets.sh`; never hand edited. English: eight phone pictures
(1080 × 1920); four per language for Arabic, Urdu, Turkish, Indonesian; four watch pictures (454 × 454);
a 512 icon; a 1024 × 500 feature graphic on the light hero gradient. **A visual change is not finished
until the affected pictures are retaken.**
