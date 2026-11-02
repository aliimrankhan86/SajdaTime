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
| Next prayer | 180 × 110, 3 × 2 | Small (default, the only one below Android 12); wide from 270 × 150 | Glyph, name, time; the countdown; the town chip; five day bars. Wide adds the six rows. Body is the current sky |
| Today | 250 × 180, 4 × 4 | Compact; large from 250 × 280 (330 in Urdu and Arabic) | Six rows with glyphs; large adds the mark, date and Hijri date, and a countdown card |
| Today strip (new in 1.6) | 250 × 40, 4 × 1 | One layout, four sizes (250 or 380 wide, 40 or 64 tall) | Five columns, the next in a pill; glyphs from 64 tall; the chip from 380 wide |

At 1.3× text each widget keeps its first layout and the strip shows the next prayer alone.

**Look.** Paper widgets: a fixed three stop body (`widget_surface_top` → `widget_surface` →
`widget_surface_bottom`), a sheen (dark only, 7%), a 1dp rim; radius 16, or the launcher's own on
Android 12+. Next prayer: the sky instead of paper, the chip and highlighted row get a 1dp `primary`
rim, and "to come" bars become a 30% track. Countdown card (large Today): radius 18, highlight
gradient. Rows and pills radius 12. Day bars: done filled `primary`, next outlined `primary` (shape,
not only fill), to come `outlineVariant`.

**Type.** Countdown in the system light weight, tabular, sized in dp (36 small Next, 34 wide, 38 large
Today) because a large font setting pushed names off. Names 16 bold on Next, 14 on Today, 12 (to 10) on
the strip. Floor 12; named exceptions down to 9: the large Today Hijri line and the strip time at 250
wide. **Known gap:** labels and footers at 11 and strip names at 10 are not listed exceptions.

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
arch alone. The widget mark is the icon on a rounded tile, used only on the large Today widget.

**Known gap:** the Android 7 fallback icon and the watch icon still use `#14624B`.

**iPhone:** an App Store icon from the same artwork (1024pt, no transparency), with dark and tinted
variants; check it is distinct from other prayer apps (App Store Guideline 4.3).

## 8.6 Store pictures (`docs/store/`)

Generated by `tools/build-store-assets.sh`; never hand edited. English: eight phone pictures
(1080 × 1920); four per language for Arabic, Urdu, Turkish, Indonesian; four watch pictures (454 × 454);
a 512 icon; a 1024 × 500 feature graphic on the light hero gradient. **A visual change is not finished
until the affected pictures are retaken.**
