# Step 8: Widgets, design (Fable `ux-lead`, 25 Oct 2026)

> **READING ORDER:** as `STEP-1-DESIGN.md`: `BUILD-INSTRUCTIONS.md` first, then this file for step 8 only. Where `UX.md:30` says "the widgets do not change in 1.6.0" it is superseded by BUILD-INSTRUCTIONS §2 item 8 and §5 row 8 (reading order). Where `DETAILED-PLAN.md` §5 and 8.11 differ from this file, this file wins once `plan-verifier` has checked it.

Written before any step 8 code. Skills: none invoked. DESIGN_SYSTEM §1 (the four skies, the gloss), §4 Home screen widgets, §9 (18 Oct widget entries, 24 Oct skies entry), §11, BUILD-INSTRUCTIONS §3 Q10 and Q14, DETAILED-PLAN §5 and 8.11, GAP-REVIEW-1 §11, STEP-5-DESIGN §4 and §5, HANDOVER lessons 130, 131, 142 and 169 decide nearly everything. Apple HIG applied by hand (§11). Contrast figures were computed with the WCAG formula `WidgetColorsTest` uses; the test is the judge.

**Status: a recommendation. Q1 reverses a §9 reasoning and is decided only from Ali's pictures (§13).**

Decided before this file and not reopened: four fixed skies, the same sixteen stops as the phone (`Color.kt` `LightSky*`, `DarkSky*`, DESIGN_SYSTEM §1), dark versions kept deep (Q10, 24 Oct); widget drawings are the glyph family (24 Oct); no jama'ah on widgets (Q14); nothing on a widget animates, no "now" dot, no bitmap drawn per refresh (lesson 130, §9 18 Oct); the town on every widget (owner, 18 Oct) as far as a 4 by 1 has room; the countdown only with exact alarms (`countdownAllowed`); colours resolved by the launcher under Theme "System", pinned on a `_fixed` layout under Light or Dark (19 Oct); nothing below 12sp on a widget except the owner's one 9sp Hijri exception; RemoteViews only (no Glance: verified, no `glance` in `gradle/libs.versions.toml` or `app/build.gradle.kts`).

## 1. Recommendation

Two things, both small in what the user sees.

1. **A third widget, "Today strip", 4 cells by 1.** Five equal columns, Fajr to Isha (no sunrise: it is not a prayer and the row has no room), each with the prayer's glyph over its name over its time. The next prayer's column sits in the highlight pill with bold name and time, exactly as the next row is marked on the Today widgets. Paper body (the approved gloss surface), not a sky: the strip is "today", the sky means "now". The town chip appears at the end only when the launcher gives 380dp or more of width (five cells on most phones). It is one tap target that opens the app, and one TalkBack sentence.
2. **The sky on the Next prayer widget.** The small and the wide Next prayer widget swap their grey gloss body for the sky of the current period, the same three stops the phone uses (top, mid at 55 percent, foot), under the same sheen and rim. Nothing on the widget moves, changes size or is added: glyph, name, time, countdown, chip, bars and the wide layout's six rows stay where they are. Two containers that would vanish on a mint sky gain a 1dp green rim (the chip and the highlighted row), and the "to come" bars become a soft translucent track. The Today widgets (compact and large) do not change: a list is paper, and the green countdown card is the object Ali asked to keep consistent.

**Rejected, with reasons so they are not re-proposed:** the sky on the Today widgets (the large one has the green countdown card, and Ali ruled on 23 Oct that the sky does not replace that card; two tinted bodies on one home screen would also make the green card look like a mistake); the sky on the strip (five pills of text on a tinted band is the busiest thing on a home screen, and the strip has no "now" object to justify the time of day colour); the sun arc with dots on the Next widget (DETAILED-PLAN §5: ten drawables to show what the five bars already show; two progress objects on a 180 by 110dp widget is clutter); a period word ("Evening") on the widget (the phone has none, §9 24 Oct); a green tint on the next column's glyph under Theme "System" (a colour set in code is baked in and goes stale, lesson 130; the Today rows keep the grey glyph when highlighted for the same reason, verified `ROW_PAINTS`); a "Now" marker (the Times rule, next only); fading passed columns (BUILD-INSTRUCTIONS §2 "faded past rows" rejected); a sixth column for the town at every width (at 250dp five columns already have to shrink); 12 hour times without "am" and "pm" on the strip (the plan's wording pass keeps `TimeFormat.clock` everywhere; a time that reads differently on the widget and the screen is the kind of thing a user reports as a bug); a separate `widget_strip_name` string (DETAILED-PLAN 6.2 lists it; the manifest label does that job and the picker shows only label and description); any new artwork (`docs/ASSETS.md` untouched: the glyphs on the strip are the six widget drawings that exist, and the skies are colour).

## 2. The journeys, numbered

Words in quotes are on screen.

1. **Adding the strip.** Long press the home screen, Widgets, SajdaTime: three previews. "SajdaTime: next prayer", "SajdaTime: today's prayers" and the new "SajdaTime: five prayers in a row" with "All five prayers in one row, the next one marked." Dragged out, it lands at 4 by 1. A second later the first update applies the app's theme (HANDOVER, accepted). It reads: Fajr 6:01 am · Dhuhr 12:45 pm (in the pill, bold) · Asr 3:17 pm · Maghrib 6:10 pm · Isha 7:15 pm, each under its glyph.
2. **A glance at 4 pm.** The pill has moved to Maghrib. Nothing else changed. The strip was redrawn by the same alarm that moves the Today widgets (prayer alarm or the weak refresh alarm).
3. **After Isha.** No pill: the next prayer is tomorrow's, which the Today rule never highlights (`isHighlighted`, verified). Five plain columns. At midnight the times become tomorrow's, as the Today widget does.
4. **Ramadan.** The Fajr column's name reads "Suhoor" and Maghrib's "Iftar" (Q3), the times unchanged. TalkBack hears "Fajr · Suhoor ends 5:16 am".
5. **Five cells wide (or a wide phone).** The town chip "London" with its pin and arrow sits after Isha's column. Pressing it opens the app, which refreshes the location on resume, as every widget chip does today.
6. **No location.** One line across the strip, "Open SajdaTime to set your location", at 14sp, columns hidden. A tap opens the app.
7. **Large text, 130 percent or more.** The strip shows one row: the next prayer's glyph, name in bold and time ("Dhuhr  12:45 pm"), with "Tomorrow" after Isha. All five are in the spoken sentence. It is the same rule the two existing widgets follow at 130 percent (`LARGE_TEXT = 1.3`, verified): keep the one fact at the user's size rather than cut five.
8. **Urdu and Arabic.** Fajr's column is at the right edge, Isha's at the left, the chip at the far left; glyphs are not mirrored; digits Western. `applyDirection` already forces this from the app's language (verified).
9. **The Next prayer widget at dusk.** The widget Ali placed on 18 Oct now sits on the olive dusk sky (dark) or the sand sky (light). At Isha's minute it becomes navy or slate. Chip and bars in their places; the chip and the highlighted row carry a thin green rim.
10. **The Next prayer widget with no location.** "Open SajdaTime to set your location" on the day sky (the phone shows the day sky with no times too, STEP-5 §5).
11. **TalkBack on the strip.** One node: "London · Today's prayer times. Next prayer Dhuhr at 12:45 pm, in 2 hours 44 minutes. Fajr 6:01 am. Dhuhr 12:45 pm. Asr 3:17 pm. Maghrib 6:10 pm. Isha 7:15 pm", then the chip, "Update location, London, button". Reuses `widget_a11y_today`, `widget_a11y_row`, `withPlace` and `nextSentence`, with sunrise left out because it is not shown.

## 3. The strip: what shows at each size

One layout XML, `widget_strip.xml` (and `widget_strip_fixed.xml`, the frame-and-include twin, as the other four have), with every variant made by visibility written both ways at render time. On Android 12 and later the renderer hands the launcher a map of four sizes built from the same layout; below 12 it hands the first.

| Launcher gives | Entry (`SizeF`) | Columns show | Chip | Who gets this |
|---|---|---|---|---|
| 250 to 379dp wide, 40 to 63dp tall | `250 × 40` | name over time, no glyph | no | dense grids, old launchers, the 320dp phone |
| 250 to 379dp wide, 64dp or taller | `250 × 64` | glyph, name, time | no | most 4 by 1 placements |
| 380dp or wider, 40 to 63dp tall | `380 × 40` | name over time | yes | five cells on a dense grid |
| 380dp or wider, 64dp or taller | `380 × 64` | glyph, name, time | yes | five cells, wide phones, tablets |

`widget_strip_info.xml`: `targetCellWidth 4`, `targetCellHeight 1`, `minWidth 250dp`, `minHeight 40dp`, `minResizeWidth 250dp`, `minResizeHeight 40dp`, `resizeMode horizontal|vertical`, `previewLayout @layout/widget_strip`, `previewImage @drawable/widget_preview_strip`, `description @string/widget_strip_desc`, `updatePeriodMillis 0`, `widgetCategory home_screen`. **Why `minHeight` is 40dp and not the plan's 60dp:** launchers that size by the 70n minus 30 formula round a 60dp minimum up to two cells, so the plan's "250 by 60dp" would land as 4 by 2 on them. 40dp is one cell everywhere; the layout survives 40dp (next row).

**Anatomy, 1.0 text.** Root: horizontal, `widget_background` (gloss, unchanged), padding 6dp sides and 4dp top and bottom, `gravity center_vertical`, `setLayoutDirection` written both ways. Five columns: vertical `LinearLayout`, `0dp` width, weight 1, 1dp side margins, padding 3dp, `gravity center`, background `widget_row_plain` or `widget_row_highlight` switched by resource id. In each: glyph `ImageView` 16dp tinted `widget_on_surface_variant` (the six existing drawings; sunrise's unused); name `TextView` 12sp `widget_on_surface`, `maxLines 1`, `match_parent` width, centred, `autoSizeTextType uniform` from 10sp to 12sp; time `TextView` 13sp `tnum`, same autosize 11sp to 13sp. Heights: thin variant 14 + 16 + 6 + 8 = 44dp of content inside 40dp only if the root's vertical padding drops to 2dp at that entry; so the thin entry uses 2dp top and bottom (written at render, `setViewPadding`), which gives 40dp exactly. Glyph variant: 16 + 2 + 14 + 16 + 6 + 8 = 62dp, inside 64. After the columns, the chip (`widget_place_chip` and its three children, as the other layouts), `GONE` under 380dp. Also in the root, both `GONE` unless needed: `widget_message` (no location, 14sp, 2 lines) and `widget_strip_next` (the large text row: glyph 20dp tinted `widget_on_surface_variant`, name 14sp bold, time 14sp `tnum`, sublabel 12sp for "Tomorrow", all centred).

**Why autosize, and its floor.** At 300dp (a common 4 cell width) a column's inner width is about 48dp. "Maghrib" at 12sp is about 45dp and "12:45 pm" at 13sp about 52dp, so the time shrinks one step to 12sp and nothing else moves. At the 250dp floor "Maghrib" shrinks to 10sp and the time to 11sp. This is the Hijri line's precedent (§9 18 Oct: shrink rather than cut, below the floor only when it would otherwise be cut, approved by the owner from screenshots), and it is uneven across columns only at widths most launchers never give. Android 7 ignores autosize and ellipsises (as the Hijri line does); accepted and recorded. (Corrected by review, 25 Oct 2026: `TimeFormat.clock` follows the phone's 12 or 24 hour setting, not the language, so any language can reach autosize; and the time floor was lowered to 9sp in the build so "pm" is not cut at 250dp.)

**The next column.** `widget_row_highlight` pill, name and time through `bold()`, glyph unchanged. No "Next" word (no room; bold plus pill plus position says it, and the sentence says it). After Isha nothing is marked.

**Ramadan (Q3).** On a Ramadan day the Fajr column's name is `widget_strip_suhoor` "Suhoor" and Maghrib's is `widget_iftar` "Iftar"; glyphs and times unchanged; the spoken row keeps the full "Fajr · Suhoor ends". Every printed Ramadan timetable heads the column "Suhoor" or "Sehri" with the end time under it, which is how a Ramadan user reads it daily; the longer "Suhoor ends" stays everywhere there is room. Follows `ramadanToday` and the Ramadan card switch, as the Today rows do.

**Friday.** "Dhuhr", as the Today widget shows it (no Friday logic in `WidgetRenderer`, verified). Jumu'ah stays off widgets with jama'ah (Q14).

**Polar, approximated day.** No room for the "Approximate time" footer in one row; the spoken sentence appends `notif_approximate` after the five rows. Visible marker: none. A known gap, written in HANDOVER, not hidden.

**Large text (130 percent and above, any size).** The five columns and chip rule stay `GONE`; `widget_strip_next` is `VISIBLE` with the next prayer, the chip after it under the same 380dp rule. This replaces DETAILED-PLAN 8.11's two thresholds (130 hides glyphs, 160 hides names). Arithmetic: at 1.5 on a 340dp widget a column's inner width is about 58dp and "12:45 pm" at the 11sp floor is about 66dp, so names and times both cut at 1.5 before 1.6 is reached; one threshold, shared with the two existing widgets, is the honest rule.

## 4. The strip: every state

| State | What shows | Spoken |
|---|---|---|
| Normal, afternoon | five columns, Maghrib in the pill | town · today sentence · five rows |
| After Isha, before midnight | five columns, no pill | "Next prayer tomorrow, Fajr at …" then five rows |
| Midnight to Fajr | tomorrow's five (now today's), Fajr in the pill | as normal |
| No location | "Open SajdaTime to set your location" | the same sentence |
| Makkah fallback | columns as normal; chip "Makkah" in the app's language when wide | "Makkah · …" |
| Ramadan | "Suhoor" and "Iftar" as names (Q3) | "Fajr · Suhoor ends 5:16 am", "Maghrib · Iftar 6:10 pm" |
| Approximated day | as normal, no marker | "… Approximate time" appended |
| Jama'ah set, alerts off, bell off | no change (widgets never show alert state) | |
| Theme System | launcher resolves `values` or `values-night` | |
| Theme Light or Dark | `widget_strip_fixed`, every colour pinned (`PAINTS` entry, `WidgetThemeTest`) | |
| 1.3 and 2.0 text | one row, next prayer only; chip if wide | five rows still spoken |
| 250dp width | names to 10sp, times to 11sp at worst; chip hidden | |
| 320dp phone (about 290dp widget) | times at 12sp, names 12sp; picture 7 | |
| Urdu, Arabic | columns mirrored, chip at the far left, glyphs unmirrored, Western digits | `bidiIsolated` town |
| Launcher below Android 12 | the `250 × 40` build (thin) at every size | |
| Resized to 2 rows | the glyph variant (height passes 64dp) | |
| Remove animations | nothing animates on any widget | |

## 5. The sky on the Next prayer widget

**Drawables.** `widget_sky_dawn.xml`, `widget_sky_day.xml`, `widget_sky_evening.xml`, `widget_sky_night.xml`: a copy of `widget_background.xml` with the body gradient's three colours replaced (`startColor` the sky top, `centerColor` the sky mid with `centerY 0.55`, `endColor` the sky foot), the sheen and the rim unchanged, `@dimen/widget_corner_radius` unchanged. Colours `widget_sky_dawn_top`, `widget_sky_dawn_mid`, … (8) and `widget_sky_foot` (1) in `values/colors.xml` with `values-night` twins, each equal to its `Color.kt` twin (`LightSkyDawnTop` … `DarkSkyFoot`, which exist). Fixed copies `widget_light_sky_*`, `widget_dark_sky_*` (18 colours) and `widget_sky_*_light`, `widget_sky_*_dark` (8 drawables). Twelve sky drawables in all, mechanical; `WidgetThemeTest` holds the fixed copies equal to the phone resolved ones.

**Chosen at render** by `state.period`, a new `WidgetState` field: `Sky.periodOf(PrayerEngine.currentPrayer(coordinates, prefs, now, zone))` (`:core`, verified both exist); `SkyPeriod.DAY` with no location. `nextViews` writes `setInt(R.id.widget_root, "setBackgroundResource", skin(state, skyDrawable(period)))` on every render, both layouts. `FIXED_DRAWABLES` gains the four sky entries and the two below. `todayViews` and the strip keep `widget_background`.

**Inks** stay exactly as the layouts declare them: `widget_on_surface` (name, countdown, time), `widget_on_surface_variant` (small time, sublabel, row glyphs), `widget_primary` (hero glyph, chip). Computed minima on the sky stops: light `on_surface_variant` 4.57 (night top), `primary` 5.83, `on_surface` 11.6; dark `on_surface_variant` 6.22 (day top), `primary` 6.19, `on_surface` 11.1; under the dark sheen (7 percent white over the day top) `on_surface_variant` about 5.4. All clear 4.5.

**Two containers fix, one track change,** because `widget_highlight` (`#D7EAE0`) against the light day top (`#D6E8DC`, the same mint) is 1.0:1 and the chip and the highlighted row would vanish:
- `widget_pill_sky.xml` (+ `_light`, `_dark`): `widget_highlight` fill, 1dp `widget_primary` stroke, 12dp radius. Used on sky layouts for the chip background and the highlighted row (the row is about 20dp tall, so it is a capsule either way). On paper widgets the pills stay as approved. `primary` against every stop is already in the 72 pairs, so the rim is visible at 3:1 everywhere.
- `widget_seg_todo_sky.xml` (+ `_light`, `_dark`): the "to come" bar as `widget_sky_track`, `widget_on_surface_variant` at 30 percent (`#4D5A605C` light, `#4DA9B2AE` dark), composited in the test over each stop at a 1.3:1 floor (a track, not text; the hairline's 1.2 precedent). `widget_outline_variant` on the day top is 1.15:1 and would disappear. Done and next bars are `primary` and unchanged.

**The wide layout's six rows** sit on the lower half of the sky, where it has faded to the page: row inks hold on all three stops; the highlighted row uses the rimmed pill; the divider `widget_divider` (`outline_variant`) is 1.1 to 1.4 against the mid stops and becomes `widget_sky_track` too on the sky layouts (one more `Paint` entry; no new drawable if the divider is an `ImageView` with a colour, else one).

**What the sky never does on a widget:** change between refreshes (fixed stops, chosen at the prayer, sunrise and midnight boundaries), sit under any text at a lower ratio than the paper did, carry a period word, or appear on a Today widget or the strip. DESIGN_SYSTEM §1's sentence "never on a widget's text" was written on 24 Oct for the phone and contradicts Ali's 23 Oct "sky-style current-prayer widget with the time left"; it is corrected in §10 below once Ali answers Q1.

## 6. Refresh moments

The widgets are redrawn by `WidgetUpdater.refreshAll` from: the app opening and every recalculation (`SajdaViewModel.kt:681, 721, 733`), each prayer alarm (`PrayerAlarmReceiver.kt:61, 99, 137`), the daily reschedule worker, system events (boot, time or zone change), and the weak refresh alarm (`WidgetRefreshScheduler`, request code 20,000, verified). The sky changes at Fajr, sunrise, Asr and Isha; three of those are prayer boundaries already covered. **Sunrise is not** (`WidgetState.kt:83` "Sunrise is never next", and no prayer alarm fires at sunrise), so a dawn sky would stay until Dhuhr. Change:

`nextRefreshAt(nextPrayerAt: Instant, sunriseAt: Instant?, now: Instant, zone: ZoneId)`: the earliest of the next prayer, local midnight and sunrise when sunrise is after `now`, plus the 2 second slack. `scheduleNext` passes `state.today?.get(PrayerSlot.SUNRISE)`. Nothing else in the chain changes: every redraw still lays the next one. `WidgetRefreshSchedulerTest` gains: sunrise before Dhuhr wins at 06:00; sunrise already passed at 13:00 is ignored (Asr wins); sunrise null (no location) behaves as today; the year long "always in the future" sweep passes a sunrise two hours out. **Mutation proof:** drop the sunrise branch and watch the 06:00 case fail. One extra redraw per day for everyone with a widget or the badge, exact only when exact alarms are allowed, as now.

The strip needs no new alarm: it changes at the same moments as the Today widget.

## 7. Icons and their SF Symbols

| Where | Drawable or icon | SF Symbol |
|---|---|---|
| Strip columns, Next hero | `widget_glyph_fajr`, `_dhuhr`, `_asr`, `_maghrib`, `_isha` (the app's own drawings, approved 18 Oct and 24 Oct) | `sunrise.fill`, `sun.max.fill`, `sun.horizon.fill`, `sunset.fill`, `moon.stars.fill` |
| Chip | `widget_icon_pin`, `widget_icon_update` (exist) | `mappin`, `arrow.clockwise` |
| Picker preview | rendered PNGs, no icon | n/a |

No new icon. No Material Symbol is used on a widget (RemoteViews cannot reach Compose icons; the drawings are vectors), which also keeps the two widget families identical in style.

## 8. Strings (English; the main session writes ur, ar, tr, in)

```
widget_strip_label    SajdaTime: five prayers in a row
widget_strip_desc     All five prayers in one row, the next one marked.
widget_strip_suhoor   Suhoor          (only if Q3 is yes; Urdu سحری, Arabic السحور, Turkish Sahur, Indonesian Sahur, from DETAILED-PLAN 8.13)
```

Reused unchanged: `widget_iftar`, `widget_suhoor_ends` (spoken), `widget_no_location`, `widget_tomorrow`, `widget_a11y_today`, `widget_a11y_row`, `widget_a11y_next`, `widget_a11y_next_tomorrow`, `widget_update_location`, `notif_approximate`, `location_makkah`, `settings_value_pair`. Not added: `widget_strip_name` (DETAILED-PLAN 6.2). The picker label follows the two existing labels' shape ("SajdaTime: …"); "strip" is avoided in user words because it has no clean equal in Urdu or Arabic. Checked: no "Tap", no "missed", "late" or "owed", no hyphen or dash, sentence case, UK spelling. Translation note: "five prayers" is the five daily prayers, not "five times" (Urdu پانچ نمازیں, Arabic الصلوات الخمس, Turkish beş vakit namaz, Indonesian salat lima waktu).

## 9. Tests

- `WidgetManifestTest`: three providers (`TodayStripWidgetProvider` → `widget_strip_info`), label `widget_strip_label`.
- `WidgetResourcesTest`: `widget_strip.xml` and `_fixed` obey the four layout rules (no `android:text`, inflatable classes only, text views `importantForAccessibility no`, every colour a resource); the four `widget_sky_*` and their fixed copies have the same structure as `widget_background` (one gradient, one sheen, one rim); the provider file has `previewLayout` and `targetCellHeight 1`.
- `WidgetThemeTest`: `PAINTS[widget_strip]` covers every coloured view in the layout; `FIXED_COLOURS` and `FIXED_DRAWABLES` gain the sky, pill and track entries and their copies match.
- `WidgetColorsTest`: twins for the 9 sky colours per theme (18 equalities); 3 inks × 3 stops × 4 skies × 2 themes (72); 3 inks × 4 tops under the sheen × 2 themes (24); the track composited on 3 stops × 4 skies × 2 themes at 1.3 (24). **120 new pairs plus 18 equalities**; the haiku `mechanic` counts them after the build. Mutation proof: set the light night top to `#C9D6DD`, see `on_surface_variant` fail, put it back.
- `WidgetStateTest`: `period` for 02:00 (NIGHT), 06:30 (DAWN), 10:00 (DAY), 16:00 (EVENING), 21:00 (NIGHT), no location (DAY).
- `WidgetRefreshSchedulerTest`: §6.
- `WidgetDeviceTest`: binds three providers (`appwidget grantbind` first); the strip's root description starts with the town and contains five prayer names and no "Sunrise"; at font scale 1.3 the five columns are gone and `widget_strip_next` shows.
- By hand on the API 36 emulator's launcher, through the real `RemoteViews.apply` path, before any picture: the four sizes of the strip by resizing; dark and light; Urdu (`:app:installRtl`, judged for mirroring only); 130 percent; the Next widget at the four clock times from STEP-5 §12; a Theme Light widget on a dark phone and the reverse.

## 10. DESIGN_SYSTEM changes

- §1 "The four skies": the sentence "never on a widget's text" becomes "never behind a prayer time on the phone; on the Next prayer widget the sky is the body, its 120 pairs held by `WidgetColorsTest` (step 8)", **only after Ali's Q1 yes**.
- §4 Home screen widgets: a third row in the table ("Today strip | one layout, four size entries | five columns, the next in the pill; the chip from 380dp; next only at 130 percent"); a bullet "The sky on the Next prayer widget" with the rim and track rule; "The town on every widget" gains "on the strip only from 380dp wide".
- §9: a dated entry: the strip as designed, the Next widget on the sky, the rim and the track, sunrise as a refresh boundary, the 40dp minimum and why, the single 130 percent rule, and the rejections in §1 (the arc, the sky on Today and the strip, the green glyph, a period word, dropped meridiem). It records that the 18 Oct rejection "a time of day colour behind the countdown (untestable contrast)" is reversed by Ali for widgets because four fixed skies are tested, the reason the rejection gave.
- §11: nothing new OPEN after Q1 to Q4 are answered; `1.6-sky` row notes step 8 done.

## 11. iPhone effect, and the HIG check

**Android only, and it maps.** `AppWidgetProvider`, `RemoteViews` and the refresh alarm have no iPhone equal; WidgetKit does the same job with a `TimelineProvider` whose entries are exactly `nextRefreshAt`'s boundaries (prayer, sunrise, midnight), so keeping `WidgetState` pure and `nextRefreshAt` a plain function of instants is the one design choice that carries over. Sizes: the strip's five columns are WidgetKit's `systemMedium` content, the small Next is `systemSmall`, the large Today is `systemLarge`; a 4 by 1 has no WidgetKit size, so the iPhone strip would be a medium with the same five columns and the chip. The sky is a `LinearGradient` container background. HIG: one fact per small widget (the countdown), text legible on every stop, no branding spent on the strip, the whole widget one tap target (as WidgetKit requires), nothing animates, the look is the app's own paper and green with a quiet sky (not the stock prayer app sky header, the 4.3 risk). The strip's chip is 24dp tall inside a 40dp widget: below 44pt alone, but a miss lands on the root, which does the same thing; on iPhone it would be one target.

## 12. Pictures for Ali

On the emulator's launcher (Pixel Launcher, API 36), 823dp frame, English, light unless said, clock set so the pill and the sky agree.

1. Home screen with the strip at 4 by 1 and the Next prayer widget 3 by 2 beside it, 16:00 (evening sky, Maghrib in the pill), light. 2. The same, dark.
3. The Next prayer widget, small, on the four skies, light (06:30, 10:00, 16:00, 21:00). 4. The same, dark. (Q1)
5. The wide Next widget, day, light and dark.
6. The strip after Isha (no pill) and at 02:00. 7. The strip on the 320dp frame (times at 12sp). 8. The strip at five cells with the chip.
9. The strip on a Ramadan day (22 Feb 2027 clock), "Suhoor" and "Iftar", and the same with the names unchanged (Q3, a TEMPORARY switch removed before merge).
10. The strip without the glyph row at a tall placement (Q2, the thin build forced once; TEMPORARY).
11. The strip at 130 percent (next only) and at 2.0. 12. Urdu, strip and Next widget (mirroring only).
13. The widget picker with three previews. 14. No location, strip and Next.
For `plan-verifier` and `reviewer` only: Theme Light on a dark phone; the four strip size entries by resizing; a TalkBack dump of the strip; the large Today beside the sky Next for the family check.

## 13. Questions for Ali (from the pictures; my recommendation first)

1. **The Next prayer widget on the sky, from pictures 3 and 4:** "Yes, the small and wide Next widget sit on the sky (Recommended)", "Only the small one; the wide one stays paper", or "No sky on widgets". His because it reverses a §9 reasoning (18 Oct, untestable colour behind the countdown) and changes a widget he approved.
2. **The strip's glyph row, from pictures 1 and 10:** "Glyph over name over time (Recommended): the sun's position reads in every language and the Today widgets use the same drawings", or "Names and times only, a thinner strip". His because it is a new object's look.
3. **Ramadan words on the strip, from picture 9:** "'Suhoor' and 'Iftar' replace Fajr and Maghrib's names (Recommended, the timetable convention)", or "Names stay, no Ramadan word on the strip". His because it is religious wording on a home screen.
4. **The town chip on the strip, from picture 8:** "Show it when the strip is 380dp or wider, never below (Recommended, his 18 Oct rule as far as a row allows)", or "Never on the strip; five columns only". His because the 18 Oct decision said every widget and this one cannot always comply.

Not asked: the 40dp minimum, autosize at the floor, the rim and track on the sky, sunrise as a boundary, the single 130 percent rule, the spoken sentence, no sky on Today or the strip. Each is decided by a rule or prior decision named above.

## DESIGN_SYSTEM §10 answers (the two widgets touched)

Strip: easier to read than five rows squeezed into one? Yes, the columns are the Today widget's row inks at the same sizes; hierarchy: the pill and bold say next, nothing else competes; type consistent with the other widgets (12 and 13sp, `tnum`); spacing even (equal weights, 1dp margins, 3dp pill padding); understood quickly: five glyphs in day order; controls obvious: one tap, the chip; no unnecessary taps; grouped: one row; modern: the gloss paper and pill of the approved family; balanced: equal columns, the chip only when there is room; colours and components consistent: no new token; clutter removed: no sunrise, no "Next" word, no town below 380dp; large text: next only at 1.3; contrast: existing pairs; targets: the whole widget; Urdu: mirrored by `applyDirection`; no `Space` tokens apply (DESIGN_SYSTEM §2 excludes the widget layouts, stated); no icon buttons; no CTA buttons (widgets have none); no dialog. Next widget on the sky: every ink held at every stop and under the sheen; the rim keeps the chip and row visible; nothing moved; the build Ali tests is named in the install line with the commit.

## Verified in code, and assumed

**Verified:** `WidgetRenderer.kt` in full (size maps, `LARGE_TEXT 1.3`, `forText`, `responsive`, `themedViews`, `PAINTS`, `FIXED_COLOURS`, `FIXED_DRAWABLES`, `fillRows` writes the label id, row glyph tint never switched, `applyDirection`, `applyTap`, `fillChip`, `applyCountdown`); `WidgetState.kt` in full (no `period`, `isHighlighted` false for tomorrow, `PRAYERS`, `progress`); `WidgetRefreshScheduler.kt` in full (no sunrise; request code 20,000); `WidgetUpdater.kt` (two providers, badge rule); `widget_next_prayer.xml` and `widget_today.xml:1-140` (ids, sizes, 11sp label, padding); `widget_today_info.xml`; `colors.xml` and `values-night/colors.xml` widget sections; `WidgetColorsTest.kt`, `WidgetRefreshSchedulerTest.kt`, `WidgetManifestTest.kt:25-60` in full; `SkyPeriod.kt` in full; `Color.kt:159-176` the sky tokens; `PrayerEngine.currentPrayer` exists (`:360`); the eight refresh call sites; `dimens.xml` and `values-v31/dimens.xml`; the two preview PNGs (360×220, 500×360); no Glance in either Gradle file; `strings.xml:610-624` widget keys, `home_next_marker`, `notif_approximate`; `docs/plans/2026-10-widget-redesign/RESEARCH.md` and the hero countdown picture; DESIGN_SYSTEM §0, §1, §3, §4 widgets, §9 (18 Oct, 24 Oct), §10, §11; BUILD-INSTRUCTIONS in full; DETAILED-PLAN §5, 6.1 J, 6.2, 8.11, 8.13; STEP-5 and STEP-7 designs; UX.md:30; PLAN.md widget lines; GAP-REVIEW-1 §11; HANDOVER lessons 130, 131, 142; the step 5 pictures.

**Assumed:** launcher cell heights (one row is 64dp or more on Pixel and One UI; 40dp only on dense grids); character widths (about 6.5dp per character at 12sp Roboto, 52dp for "12:45 pm" at 13sp), the test and the 320dp picture pin them; a `RemoteViews(Map<SizeF, RemoteViews>)` whose entries share one layout id behaves as lesson 169 describes when every property is written both ways (the `_fixed` precedent suggests a second id is only needed when the property set differs; if the launcher misbehaves, give the thin build its own id `widget_strip_thin.xml` as a copy); `centerY` on a three colour `<gradient>` is honoured by the launcher's inflater; `setViewPadding` reaches a launcher below Android 12 (it is a `RemoteViews` method since API 16); `TextView` autosize works with `maxLines 1` and `wrap_content` height inside a `0dp` weighted column; `PrayerEngine.currentPrayer` takes the same arguments as `nextPrayer`; the emulator's launcher is the Pixel Launcher.

**Flags in the built code:** (1) DESIGN_SYSTEM §1 "never on a widget's text" contradicts Ali's 23 Oct sky widget decision (Q1 settles it); (2) DETAILED-PLAN's 60dp minimum would make the strip 4 by 2 on formula launchers; (3) 8.11's 160 percent threshold cannot be met at 1.5 on a 340dp widget; (4) `fillRows` would throw on the strip because the label ids are absent, so the strip needs its own `fillColumns`; (5) `WidgetManifestTest` pins exactly two providers (GAP-REVIEW-1 said so, confirmed).

**Risks:** the 250dp floor is ugly on Android 7 (ellipsis, no autosize), a minority accepted as the Hijri line was; a launcher that gives 1 row under 64dp drops the glyphs, which the thin picture shows Ali before he meets it; the twelve sky drawables and six pill and track copies are mechanical but easy to mis-pair, so `WidgetThemeTest` must compare each fixed copy to its phone resolved twin by colour, not by name; the store screenshot 06 and the Next preview PNG must be retaken (the preview is what the picker shows before the first update); Ali may want the sky on the large Today's header once he sees the Next widget, which is a new question for after Q1, not something to build.

## 14. plan-verifier corrections (25 Oct 2026, not yet applied)

The builder applies items 1 to 13 before building; where they conflict with §1 to §13, §14 wins. Q1 is narrowed to "both Next layouts" or "small only" (item 4). The two extra questions at the end go to Ali with Q1 to Q4.

### Verdict

Nothing that breaks a hard rule. No network, no new data (privacy page needs no change, though the plan should say so), iPhone line present, rejections recorded. The design is buildable, but a Sonnet implementer would have to guess in five places, two claims about the code are false, and one option in Q1 reopens a scope decision Ali already made. Contrast arithmetic is right where it matters (every ink clears 4.5 on every stop, I recomputed all 72 plus the sheen and track cases); two quoted figures are wrong but on the safe side.

### Corrections, most severe first

**1. Blocker: the large text rule and the chip rule contradict each other** (`STEP-8-DESIGN.md:61`, `:77`, `:30`). The plan says at 1.3 and above the strip "keeps the one fact" via `forText`, which is `sizes.take(1)` (`WidgetRenderer.kt:103-104`), so there is one `RemoteViews` and no `SizeF`. It also says the chip shows "under the same 380dp rule" at large text. With one entry the renderer cannot know the width. Fix: at 1.3+ keep all four size entries (each building the `widget_strip_next` row, chip only on the 380 entries), or state the chip is hidden at large text. Pick one and write it in §3 and the §4 table.

**2. Blocker: `responsive` cannot build four variants from one layout id** (`:38`, `:49`). `responsive(sizes, build: (Int) -> RemoteViews)` hands the builder only the layout id (`WidgetRenderer.kt:106-111`). Four `SizeF` entries sharing `R.layout.widget_strip` would produce four identical views. Fix: say the strip's builder takes the `SizeF` (or a small enum thin/glyph × chip/no chip), e.g. `responsive(sizes) { size, layout -> stripViews(context, state, now, layout, size) }`, and that `nextPrayer`/`today` keep their shape.

**3. Blocker: "Nothing else in the chain changes" is false; the provider and updater are missing from the build list** (`:105`, `:131`). `WidgetUpdater.refreshAll` queries exactly two providers (`WidgetUpdater.kt:26-28`, `:56-57`); a third needs its own `getAppWidgetIds`, inclusion in `anyWidget`, and an `updateAppWidget(stripIds, WidgetRenderer.strip(...))`. `TodayStripWidgetProvider.kt` (copy of `TodayWidgetProvider.kt`, 4 callbacks) and its manifest `<receiver>` block (pattern at `AndroidManifest.xml:155-165`) appear only inside the test bullet. Also `WidgetDeviceTest.removeAllOurWidgets` lists two providers (`WidgetDeviceTest.kt:86`); add the third or a strip instance leaks between tests. Fix: a "files touched" list in §5/§6 naming all three.

**4. Should fix: Q1's third option reopens a decided scope item** (`:167`). BUILD-INSTRUCTIONS §2 item 8 ("the four skies on the widgets") got Ali's "Yes, go" (Q27). Offering "No sky on widgets" invites undoing it. Fix: keep Q1 as "both Next layouts" vs "small only", and present the sky as confirming his 23 Oct decision from pictures.

**5. Should fix: Ali's recorded words say the *current* prayer is marked; the plan marks the *next*** (`:15`, `:53`; `PLAN.md:221` "a wide strip of the five prayers with the current one marked"). The plan dismisses a "Now" marker in half a sentence ("the Times rule, next only"). It may well be right, but it contradicts his words without saying so. Fix: one sentence in §1 naming his wording and why next wins (the Today widgets and the Times screen mark next; DETAILED-PLAN J, which he approved, already said "the next one marked"), or add it as a question (see below).

**6. Should fix: the thin variant does not fit 40dp by the plan's own arithmetic once font padding is counted** (`:49`). A 12sp `TextView` with default `includeFontPadding` is about 16dp tall, a 13sp one about 17dp: 16 + 17 + 6 (column padding) + 4 (root at 2dp) = 43dp, not 40. The glyph variant is 16 + 2 + 16 + 17 + 6 + 8 = 65 against a 64 entry. Fix: `includeFontPadding="false"` on name and time (the countdown already does this, `widget_next_prayer.xml:98`) and column vertical padding 2dp; and make picture 10 the check at exactly 40dp, not "forced once".

**7. Should fix: hard coded counts in existing tests will fail and the plan does not name them.** `WidgetThemeTest.kt:151` `assertEquals(8, themed.size)` becomes 8 + 4 skies + pill + track + divider track = 15 (see item 8); `WidgetResourcesTest.kt:188` `assertEquals(13, names.size)` becomes 15 (16 with `widget_strip_suhoor`); the lists at `WidgetResourcesTest.kt:31-34`, `:98-103`, `:131-134`, `:151`, `:172-175` and `WidgetThemeTest.kt:28-33`, `:110` (drawables "switched by the renderer after the paint": add the skies, pill, track, divider track) all need the strip and the new drawables. Fix: list them in §9 so Sonnet does not treat the failures as its own bugs.

**8. Should fix: two counts and one hedge in §5 are wrong** (`:87`, `:95`, `:97`). (a) `WidgetThemeTest.kt:141-143` requires every `widget_*` colour to have `widget_light_`/`widget_dark_` copies, so `widget_sky_track` needs copies too: 20 fixed colours, not 18. (b) The divider is an `ImageView` with `src="@drawable/widget_divider"` (`widget_next_prayer_wide.xml:231-239`), so the sky layouts need a `widget_divider_sky.xml` (+ `_light`, `_dark`): say so, drop the "no new drawable if" hedge. (c) Each fixed copy must be the source with `@color/widget_` → `@color/widget_light_` and nothing else (`WidgetThemeTest.kt:147-158`); the plan's names comply, but say the test is textual so no comment or attribute may differ.

**9. Should fix: `nextRefreshAt` signature change breaks nine callers** (`:105`). Eight tests in `WidgetRefreshSchedulerTest.kt` and `WidgetDeviceTest.kt:302` call the 3 argument form. Fix: either update them all (say so) or add `sunriseAt: Instant? = null` as a defaulted second parameter. Also name the date for the new 06:00 case (London 22 Feb 2027 sunrise is about 07:00, so it works; 24 Oct would too).

**10. Note: two contrast figures are misquoted, both on the safe side** (`:91`, `:95`). Under the dark sheen (7 percent) `on_surface_variant` on the day top is 5.05, not "about 5.4" (5.44 is the evening top). `widget_outline_variant` on the day top is 1.24 light / 1.02 dark, not 1.15. Everything else I recomputed matches: light 4.57 / 5.83 / 11.62, dark 6.22 / 6.19 / 11.15; the 30 percent track clears 1.3 everywhere (light min 1.47, dark min 1.82); `widget_highlight` on skies 1.01 to 1.32, so the rim is needed in both themes. The test is the judge, as the plan says.

**11. Note: provider file name differs from every other document** (`:47`). BUILD-INSTRUCTIONS §5 row 8, DETAILED-PLAN 8.11 and GAP-REVIEW-1 §11 all say `widget_today_strip_info.xml`; the plan says `widget_strip_info.xml`. Either is fine; say which wins in one line so the three documents are not contradicted silently.

**12. Note: the strip preview PNG is a new asset the plan never lists.** `WidgetResourcesTest.kt:130-155` requires `drawable-nodpi/widget_preview_strip.png` under 30 KB. The existing two were captured through the Robolectric harness of HANDOVER lesson 132 (`HANDOVER.md:253`). Say that, so nobody draws one by hand; and the Next preview retake is in §Risks only, move it to §9.

**13. Note: privacy page.** The plan should state in one line that `docs/privacy.html` is unchanged (the strip and the sky read the same saved settings; nothing new is stored or sent), as STEP-6 did.

**14. Note: DETAILED-PLAN §5's sun path arc is dropped without telling Ali.** Fable's reasons are sound and BUILD-INSTRUCTIONS §2 item 8 never promised an arc, but Ali's "Widgets follow Pillars' UX" names Pillars' sky widget, which has one. One sentence in the pictures message ("no sun path on the Next widget, because the five bars already show it") keeps it from arriving later as "where is the arc".

**15. Note: the "Assumed" list** (`:182`). Verified for the plan: `currentPrayer` takes the same four arguments as `nextPrayer` (`PrayerEngine.kt:313-318`, `:360-365`); the phone uses the same `Sky.periodOf(currentPrayer(...))` (`SajdaViewModel.kt:691`, `HomeScreen.kt:200`), so widget and phone always agree; `RemoteViews(Map)` has no same layout id constraint and the host recycles on equal ids, so lesson 169's "write both ways" is the right assumption; `setViewPadding` is API 16; `minSdk` is 24 so "Android 7 ignores autosize" is the right floor. Still unverified and worth one emulator check before building: autosize with `maxLines 1` and `wrap_content` height inside a weighted 0dp column (Android documents this combination as unreliable).

### Questions only Ali can answer

1. **Which prayer does the strip mark: the next one (recommended, same as the Today widgets and the Times screen, so one rule everywhere) or the current one, as you said on 23 Oct ("the current one marked")?** Pillars marks current; our other widgets mark next.
2. **Pillars' sky widget has a sun path arc and a period word; Fable leaves both off the Next widget (recommended: the five bars already show the day, and a period word would be colour baked at draw time). Fine, or do you want the arc seen in a picture first?**

The plan's own Q1 to Q4 stand, with Q1 narrowed as in item 4.

Files cited: `/Users/aliimrankhan/Developer/SajdaTime-1-6-0/docs/plans/2026-10-release-1.6.0/STEP-8-DESIGN.md`, `/Users/aliimrankhan/Developer/SajdaTime-1-6-0/app/src/main/java/com/sajdatime/app/widget/WidgetRenderer.kt`, `/Users/aliimrankhan/Developer/SajdaTime-1-6-0/app/src/main/java/com/sajdatime/app/widget/WidgetUpdater.kt`, `/Users/aliimrankhan/Developer/SajdaTime-1-6-0/app/src/main/java/com/sajdatime/app/widget/WidgetRefreshScheduler.kt`, `/Users/aliimrankhan/Developer/SajdaTime-1-6-0/app/src/test/java/com/sajdatime/app/WidgetThemeTest.kt`, `/Users/aliimrankhan/Developer/SajdaTime-1-6-0/app/src/test/java/com/sajdatime/app/WidgetResourcesTest.kt`, `/Users/aliimrankhan/Developer/SajdaTime-1-6-0/app/src/androidTest/java/com/sajdatime/app/WidgetDeviceTest.kt`, `/Users/aliimrankhan/Developer/SajdaTime-1-6-0/docs/plans/2026-10-release-1.6.0/PLAN.md`.

## 15. Ali's answers (25 Oct 2026, ~01:45)

Asked the two §14 questions, he said: "Let's go with recommendation". So:
- The strip marks the **next** prayer, as the Today widgets and the Times screen do (supersedes "the current one marked" in PLAN.md:221).
- **No sun path arc and no period word** on the Next widget.
Q1 to Q4 (§13, Q1 narrowed by §14 item 4) are still his, from the pictures.

## 16. Build notes (25 Oct 2026)

- §14 item 1 was first built as "chip on the 380 entries at large text"; Opus review showed the chip squeezes the one fact at 2.0, so the chip is hidden at 1.3 text and above, and capped at 64dp otherwise.
- The time autosize floor is 9sp (not 11sp) so "pm" is never cut at 250dp; names and times ellipsise.
- Arabic label and description use سطر (line), not صف, which reads as the row of people praying.
