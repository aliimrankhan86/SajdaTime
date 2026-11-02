# Step 5 checks after merge (24 Oct 2026, evening)

Taken by the Haiku mechanic (dark-urdu-times retaken by the main session), judged by Fable: all 11 PASS, nothing to fix before 1.6 ships.
Emulator: no location, notification or exact alarm permission, so amber permission cards show.

| Picture | Setting | Verdict |
|---|---|---|
| urdu-rtl-times, arabic-rtl-times | light, 823dp | PASS: mark mirrored, Isha glyph at the left end |
| dark-urdu-times | dark, Urdu, night | PASS |
| text-1_5-times | font 1.5 | PASS: date wraps, mark shown |
| text-2_0-times | font 2.0 | PASS: mark hidden (rule hides it at 2.0 whatever the city length, deliberate); Allow under the bar is scroll position |
| tablet-times | 1600x2560 at 320dpi | PASS: 600dp column, sky edge to edge |
| landscape-times | rotated | Band on the left is Android's cutout letterbox, predates step 5 |
| anim-off-times | animator scales 0 | PASS: arc fully drawn |
| qibla-top, prayers-top, settings-top | light | PASS: same title height, nothing under the status bar |

Record only: landscape cutout band (`shortEdges` would change every screen's edge, Ali's call); tablet bottom bar spans full width while content is capped; My prayers says "Tracking is paused" (step 1 copy; CLAUDE.md bars "tracking" in usage counts copy, ruling for Ali).
