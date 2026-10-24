# Step 5: the skies, design (Fable `ux-lead`, 24 Oct 2026)

> **READING ORDER:** as `STEP-1-DESIGN.md`: `BUILD-INSTRUCTIONS.md` first, then this file for step 5 only.

Written before any code, as Ali's process asks: journey and design first, then `plan-verifier`, then pictures for
Ali's yes, then the build. Skills: none invoked; DESIGN_SYSTEM §1, §2 Motion, §8 and §9 (23 Oct, item 1),
BUILD-INSTRUCTIONS §3 Q10, Q18, Q20 and §6 row 6 and 7, and DETAILED-PLAN §2.1 to §2.3, 8.9 and 9.6 decide nearly
everything. Apple HIG checks applied by hand (§11). Contrast figures below were computed with the WCAG formula
`ColorContrastTest` uses, on the proposed hex values; the test is the judge, not this file.
**Status: a recommendation. Nothing is decided until Ali says yes from the eight pairs (§12).**

Decided before this file and not reopened: four fixed skies, never a continuous gradient (DESIGN_SYSTEM §9);
option D, the sky as a backdrop behind the header and the unchanged card (DETAILED-PLAN 2.2); boundaries by the
day's own prayer times (2.3); the sky never replaces the green or light card (Q3 table); dark skies decided from
four light and four dark pairs (Q10, DESIGN_SYSTEM §11 `1.6-dark-skies`); tablets capped at 600dp and centred
with the sky full width (Q18, Q20); the crossfade as one `drawBehind` lerp at `Motion.draw()`, no animation on
open, the card never moves (BUILD-INSTRUCTIONS §6 row 6); the day path drawn once per process (row 7, Q11);
`SkyPeriod` a pure function in `:core` (8.9); one spoken node (9.6); the five strings `sky_dawn`, `sky_day`,
`sky_evening`, `sky_night`, `sky_now_a11y` in five languages, the main session translating.

## 1. Recommendation

The top of Times becomes a soft sky: one fixed three stop gradient per period, from under the status bar down to
the foot of the next prayer card, where it has already faded into the page. The card does not change. The header
keeps exactly what it shows today, in the same order: the city with its caret, then the date line with the Hijri
half in green. One thing is added, at the end of the city row, inside the height the header already has: a small
**sky mark**, a glyph for the period (first light, sun, setting sun, crescent) riding a shallow arc of five dots
that are the five prayers, the part of the day already prayed drawn in green. No skyline, no star field, no
illustration, no words for the period on screen: the period is read from the colour and the mark, and spoken to
TalkBack as "Dawn, until 7:40 am". Light skies are pale (rose, mint, sand, slate); dark skies are deep (violet,
teal, olive, navy), so a dark mode user at noon sees a dark day, not a light one. The mark hides when the city
name needs the room, so nothing is ever truncated for a decoration. No third party artwork is needed: the four
glyphs are the app's own widget drawings, which step 8 will put on the widgets beside the same four skies.

Rejected: a visible period word ("Day") in the header (a fifth text on a header Ali asked to keep calm; the card
and the Now pill already say which prayer it is); a star field, horizon line or mosque skyline (clutter, and a
skyline is what every stock prayer app draws, App Store 4.3); a sun that changes colour (green is the only ink
that means "now" here; a yellow sun would be the second depictive colour after the Kaaba gold, DESIGN_SYSTEM §1
says there is only one); the five dots spaced by clock time (Fajr to Dhuhr is six hours and Maghrib to Isha
ninety minutes, so the dots bunch at the end and the arc reads as broken); the arc on its own 56dp band under the
date line (DETAILED-PLAN 2.2 "D plus full-width path": costs the no scroll rule at 823dp); the mark beside the
date line (the date line is about 200dp wide in English on a 360dp phone and longer in Urdu and Indonesian, so the
mark would hide in most languages); Material Symbols as the glyphs (fine, offered as the alternative in §12).

## 2. The journey, numbered

Words in quotes are on screen.

1. **Opening in the morning.** Times opens on the day sky, drawn at once with no animation. The status bar, "London"
   with its caret and "Sat 24 Oct · 13 Jumada al-Ula 1448" sit on pale mint; at the end of the city row a small
   green sun rides an arc a third of the way along, the arc behind it green, the arc ahead a thin grey line with
   grey dots. Once, on this first open of the process, the green part draws from the Fajr dot to the sun over 400
   ms. The card below is the same card as yesterday.
2. **Glancing later.** The sun has moved a little along the arc (it moves on the minute). Nothing else changed.
3. **Asr arrives while the screen is open.** The sky crossfades from mint to sand over 400 ms (the four hundred
   milliseconds of `Motion.draw()`); the mark swaps to the setting sun; the Now pill moves to Asr's row as step 3
   already built. The card does not move. No buzz.
4. **Night.** After Isha the sky is slate (deep navy in dark), the mark is the crescent at the end of the arc, the
   whole arc green. Between midnight and Fajr it is still night: the arc is grey again with the crescent on the
   Fajr dot, the day about to begin.
5. **Dark mode at noon.** The same mark on a deep teal sky; the green card as Ali knows it; the status bar icons
   white.
6. **A long city name.** "Dar es Salaam" at 1.5 text, or "Saint Petersburg" at 1.0, needs the whole row: the mark
   is not drawn and the name is not shortened. The sky colour still says the period.
7. **No location.** The card says "Set your location to see prayer times." on the day sky, with no mark (there are
   no times to draw). With the Makkah fallback the sky follows Makkah's times, as the card does.
8. **Far north in June.** The times are approximated and the polar notice shows under the card as today; the sky
   follows the approximated times, so dawn and night still come round. Nothing special is drawn.
9. **Ramadan and Eid.** The ordinary sky. The Ramadan card and the Eid notice sit under the next prayer card on the
   plain page, as now.
10. **Urdu.** The mark sits at the left end of the city row; the arc runs Fajr at the right to Isha at the left,
    the way the day is read. The glyphs are not mirrored.
11. **TalkBack.** "London, Change location, button", then "Dawn, until 7:40 am", then "Islamic dates. Today is 13
    Jumada al-Ula 1448, button". Nothing inside the mark speaks on its own.

## 3. The header on the sky: what shows, in what order, and the height budget

Nothing is removed and nothing is reordered. The sky is a backdrop; the mark is the one addition.

| # | Element | Style and ink on the sky | Target | Change from today |
|---|---|---|---|---|
| 1 | Status bar | the phone's icons: dark on the light skies, light on the dark skies (`Theme.kt:141`, unchanged) | | the sky now runs under it |
| 2 | City with caret | `headlineMedium`, `onSurface`; caret `primary` 28dp | 48 wide row | row unchanged; `weight(1f, fill = false)` and `maxLines = 1` as `HomeScreen.kt:386-399` |
| 3 | **Sky mark** (new) | glyph `primary` 20dp; arc `onSurfaceVariant`, passed part `primary`; dots `onSurfaceVariant` | none (not actionable) | at the end of the city row, 104dp wide, 52dp tall, top aligned, overlapping the top 12dp of the date line's own 48dp target, where there is no text |
| 4 | Date line | `bodyMedium`; Gregorian `onSurfaceVariant`, Hijri `primary` | 48dp | unchanged (`HomeScreen.kt:428-470`) |
| 5 | The card | unchanged (`NextPrayerCard`) | | unchanged; the sky has faded to `background` by its foot |

**Height at 823dp (S23 Ultra frame):** zero. The header is `Space.Tight` + city row (32sp line plus 2dp top and
bottom, about 40dp) + the date line's 48dp target, about 92dp, as DETAILED-PLAN 2.1 measured it. The mark's 52dp
fits inside those 92dp: 40dp beside the city plus 12dp into the date line's top padding (its text is centred in
48dp and about 20dp tall at 1.0, so about 14dp of padding sits above it). The sky adds no height; the fade is
inside the card's own height. The 19dp of slack DETAILED-PLAN 2.1 reports stays untouched (assumed; measure on
the 823dp frame before the pictures, §9).

**At 320dp:** the column is 288dp wide. The city row holds "London" (about 90dp), the caret (30dp), the 8dp gap
and the mark (104dp) with room to spare; "Birmingham" (about 150dp) still fits; "Kuala Lumpur" does not and the
mark hides (§4, rule). The date line is one line (about 200dp) as today.

**At 1.5 and 2.0 text:** the city row grows to about 56dp and 72dp, the mark stays 52dp and is top aligned, so it
sits beside the first line of the name; the width rule hides it when the name needs the room (at 2.0 on 320dp,
nearly always). The date line wraps between words as it does today (verified in step 4's pictures `5b`).

**Tablet (Q18, Q20):** the sky is full width; the header, card and everything below are capped at 600dp and
centred, by one modifier (§6). On a 600dp or wider screen the mark always fits.

## 4. The sky mark

New file `ui/home/Sky.kt`: `internal fun SkyMark(period: SkyPeriod, progress: Float, drawIn: Float, modifier)`,
internal so `LargeTextAndTalkBackDeviceTest` can compose it. The whole mark is one `Box(Modifier.size(104.dp,
52.dp))` with one `Canvas` for the arc and the dots and one `Icon` for the glyph, placed with `Modifier.offset {}`
from the arc's point, so the frame that moves the glyph recomposes nothing.

| Part | Spec |
|---|---|
| Arc | a circular arc, chord 96dp (4dp in from each end), ends at y = 40dp, apex at y = 22dp; stroke 1.5dp `onSurfaceVariant`, round caps. The passed part, from the Fajr end to the now point, stroke 2.5dp `primary`, drawn over it |
| Dots | five, 4dp diameter, `onSurfaceVariant`, at 0, 0.25, 0.5, 0.75 and 1.0 of the arc's length: Fajr, Dhuhr, Asr, Maghrib, Isha. Not labelled (the rows below are the labels) |
| Glyph | 20dp, tint `primary`, centred on the arc at `progress`, drawn last so it covers the dot it sits on. `contentDescription = null` |
| Progress | `SkyPath.progress(now, today.ordered)` in `:core` (§7): 0 before Fajr, 1 after Isha, else `(segment + fraction) / 4`, where `fraction` is how far `now` is through the current segment by time. Read per minute (`derivedStateOf { now.epochSecond / 60 }`), never per second |
| Draw in | once per process (Q11): a top level `private var skyDrawn = false` in `Sky.kt`; `val drawIn = remember { Animatable(if (skyDrawn) 1f else 0f) }`; `LaunchedEffect(Unit) { if (!skyDrawn) { drawIn.animateTo(1f, Motion.draw()); skyDrawn = true } }`. The passed arc and the glyph are drawn at `progress * drawIn`. Coming back to the tab later: `snapTo` through the initial value, no animation |
| Right to left | `LocalLayoutDirection == Rtl`: the arc's x axis is mirrored (`scale(-1f, 1f)` inside the Canvas, and `x = width - x` for the glyph's offset), so Fajr is at the start edge in both directions. The glyph itself is not mirrored |
| Semantics | the Box: `semantics { contentDescription = stringResource(R.string.sky_now_a11y, periodName, endTime) }`; the Canvas and the Icon inside: `clearAndSetSemantics {}` (DETAILED-PLAN 9.6) |
| Shown when | `state.today != null` and the city fits (below). Otherwise nothing is composed in its place and the city row is as today |
| Motion, haptics | the draw in above; the glyph swap at a period change is a plain swap (an icon swap would be `Motion.quick()`, but it happens under the sky crossfade and a second animation on the same event is noise); no haptics (DESIGN_SYSTEM §2: never on a sky change) |

**The width rule** (replaces DETAILED-PLAN 2.2's "if the city name is long the artwork hides", made exact): in
`LocationHeader`, `BoxWithConstraints` gives `maxWidth`; `rememberTextMeasurer().measure(city,
MaterialTheme.typography.headlineMedium).size.width` gives the name's natural width in px. The mark is shown when
`nameWidth <= maxWidth - 8dp (row padding) - 30dp (caret and its 2dp gap) - 8dp (gap) - 104dp (mark)` in px,
keyed on `city`, the style and the density. Deterministic, measured before the first frame, no flicker, no
`onTextLayout` loop. At `fontScale >= 2.0f` skip the measure and hide the mark: the name wins on every width.

**The glyph per period** (the app's own widget vectors, `res/drawable/widget_glyph_*.xml`, 24dp, 1.8dp strokes,
white and tinted at draw; no licence, nothing to download, nothing for `docs/ASSETS.md` in this step):

| Period | Drawable | What it shows | SF Symbol (iPhone) | Material alternative (§12 Q2) |
|---|---|---|---|---|
| Dawn | `widget_glyph_fajr` | rays above the horizon, the sun still below it | `sunrise.fill` | `Icons.Rounded.WbTwilight` |
| Day | **new** `sky_glyph_day` | a plain sun: circle radius 3 with eight short rays, the Dhuhr glyph without its post, same stroke | `sun.max.fill` | `Icons.Rounded.WbSunny` |
| Evening | `widget_glyph_maghrib` | the sun going down below the horizon | `sunset.fill` | `Icons.Rounded.WbTwilight` (the same as dawn; the arc position tells them apart) |
| Night | `widget_glyph_isha` | a crescent and one star | `moon.stars.fill` | `Icons.Rounded.NightsStay` |

Why the widget drawings: they already mean "where the sun is" in this app (DESIGN_SYSTEM §9, widgets), Ali has
seen them on the widgets he approved, they are strokes so they read at 20dp in `primary` on every sky, and step 8
puts the same four skies on the widgets, where `RemoteViews` can only use drawables; one set of glyphs on phone and
widget. The row glyphs on Times are Material filled icons; the mark is a different kind of object (a scene, not a
list glyph), so a different drawing style does not clash, and Ali judges that from the pictures. The one new
vector, `sky_glyph_day.xml`, is drawn in house in the same style as the other three (a circle and eight lines);
none of the four is AI made, so the Play AI declaration stays "Don't label assets".

Rejected ready made assets, with reasons so they are not re-proposed: Material Symbols (Apache 2.0, already in the
app as `material-icons-extended`): offered as Q2's alternative; Lucide `sunrise`, `sunset`, `moon-star` (ISC): the
same shapes as ours, and a third icon style in the app for nothing; any skyline, Kaaba or mosque silhouette
(licensing aside, a skyline is the stock prayer app template, and the Kaaba is not decoration); any star field or
cloud illustration (clutter, and clouds say weather, which was rejected).

## 5. The four skies: periods, colours and the status bar

**Periods** (DETAILED-PLAN 2.3, 8.9), a pure function of `state.current`, which the ViewModel already recalculates
at every prayer time, at sunrise and at midnight (`SajdaViewModel.kt:293-311`), so the sky changes at the right
minute with nothing new scheduled:

| Period | From | To | `state.current` |
|---|---|---|---|
| Dawn | Fajr | sunrise | `FAJR` |
| Day | sunrise | Asr | `null` (sunrise to Dhuhr) or `DHUHR` |
| Evening | Asr | Isha | `ASR` or `MAGHRIB` |
| Night | Isha | tomorrow's Fajr | `ISHA` (after Isha, and between midnight and Fajr, when the engine looks back a day: `PrayerEngine.kt:360-372`) |

`SUNRISE` never comes out of `currentPrayer` (`:371`), but the function maps it to Day so it can never crash a
caller. **No times at all** (`state.today == null`, no coordinates): `current` is null, so the sky is Day, the
neutral one, with no mark. Never the phone's clock: the sky names the prayer period or it names nothing.
**Approximated days** (polar): the engine still gives all six times, so the same rule holds; the polar notice
under the card carries the warning as today.

**Colour stops**, three per sky, the third always the page `background` so the fade is testable (DETAILED-PLAN
2.3). Vertical gradient: top stop at the top of the sky box (under the status bar), middle stop at 55 percent
(about the top of the card), foot stop at the box's bottom, which is the card's bottom edge. Light skies stay in
the pale, warm paper family the app already has (the hero card's mint and sand are two of the four tops); dark
skies are deep, with hue doing the work the eye needs at night. Tokens in `Color.kt`, named `LightSkyDawnTop`,
`LightSkyDawnMid` and so on; read through `skyStyle(period)` in `Theme.kt` from `LocalDarkTheme` (as `heroStyle`).

| Sky | Light top, mid, foot | Dark top, mid, foot |
|---|---|---|
| Dawn | `#EADFE3`, `#F0E9EC`, `#F7F5F3` (pale rose) | `#231E2C`, `#171520`, `#101312` (deep violet) |
| Day | `#D6E8DC` (`LightHeroStart`), `#E6EFE9`, `#F7F5F3` (mint) | `#17323A`, `#13262B`, `#101312` (deep teal) |
| Evening | `#ECD8BB`, `#F1E5D3`, `#F7F5F3` (sand, beside `LightHeroEnd`) | `#262A1E`, `#181B15`, `#101312` (olive dusk; not `tertiaryContainer` `#2A2113`, because amber means warning) |
| Night | `#D0DBE1`, `#DFE6EA`, `#F7F5F3` (slate) | `#0B1730`, `#0C1320`, `#101312` (navy) |

**Inks on the sky** and their computed minimum over all three stops (the test asserts every stop):

| Ink (role) | Used for | Light min | Dark min |
|---|---|---|---|
| `onSurface` | the city | 11.0 (night top) | 11.2 (day top) |
| `onSurfaceVariant` | the Gregorian date, the arc, the dots | 4.57 (night top) | 6.2 (day top) |
| `primary` | the Hijri date, the caret, the glyph, the passed arc | 5.5 (night top) | 6.2 (day top) |

Every pair clears 4.5:1, so the 3:1 bar for the arc, dots and glyph is met by the same pairs. Two values were
tuned to get there: the light night top was `#C9D6DD` (4.3 under `onSurfaceVariant`) and is `#D0DBE1`; the light
evening top was `#EBD5B6` (4.50, no margin) and is `#ECD8BB`.

**Record for DESIGN_SYSTEM §1:** these sixteen stops are the second depictive colour family after the Kaaba gold.
§1 says the gold "is the only one" and that sentence must change to "the only one until the skies of 1.6": the
sky shows the time of day, the way the gold shows what the Kaaba is. No role, no state, never drawn behind a
prayer time or the countdown, never used on a widget's text.

**Dark night is told by hue, not by brightness.** `#0B1730` against `#101312` is 1.05:1: on an OLED the night
sky is nearly as dark as the page, which is right for a screen read at 4 am; the navy cast and the crescent say
night. Ali sees this in pair 4 dark and may want it a step brighter (§12 Q1).

**The status bar.** `SajdaTimeTheme` already sets dark icons on the light scheme and light icons on the dark one
(`Theme.kt:139-142`). Every light top stop is `#D0DBE1` or lighter (black on it 14.1:1 or more); every dark top
stop is `#262A1E` or darker (white on it 13.5:1 or more). `ColorContrastTest` adds eight explicit pairs so the
guarantee is written down: `Color.Black` on each light top and `Color.White` on each dark top at 3:1. The known
one frame of light `windowBackground` at a dark cold start (DESIGN_SYSTEM §5) now shows before a dark sky as well;
accepted, as 8.9 recorded.

**The contrast count, corrected.** BUILD-INSTRUCTIONS §5 row 5 says "72 text plus 24 non-text". The 72 holds: 3
inks × 3 stops × 4 skies × 2 themes. The 24 assumed a fourth colour (the path in `outlineVariant`, which is 1.1:1
to 1.4:1 on every sky and could never pass); here the arc, dots and glyph use only `onSurfaceVariant` and
`primary`, already asserted at 4.5:1 in the 72, so a second list at 3:1 would repeat them. What is added instead:
8 equalities (each foot stop `== background`, both themes, in `SkyPeriodTest`'s app side twin or inside
`ColorContrastTest` as a plain `assertEquals`) and the 8 status bar pairs. **Total new assertions: 72 + 8 + 8 =
88.** The haiku `mechanic` counts them after the build.

## 6. The backdrop: how it is drawn, the crossfade, the status bar, the tablet cap

**Where it lives.** In `HomeScreen`'s scroll column the header, its `Space.Tight` and the card move inside one
`Box(Modifier.fillMaxWidth().skyBackdrop(period).statusBarsPadding())` whose inner column has the 16dp side
padding and the 600dp cap. The column's own `padding(horizontal = 16.dp)` (`HomeScreen.kt:190`) moves off the
column and onto the pieces below the box (the banners, the Today line, the rows, the door), so the sky box is edge
to edge with nothing drawn out of bounds. Everything after the card is untouched.

**`Modifier.skyBackdrop(period)`** (in `Sky.kt`): holds `shown` and `previous` periods and `val t = remember {
Animatable(1f) }`. `LaunchedEffect(period)`: on the first run `shown = period` and nothing animates (no entrance,
§2 Motion); on a later change `previous = shown; shown = period; t.snapTo(0f); t.animateTo(1f, Motion.draw())`.
`drawBehind` builds one `Brush.verticalGradient(0f to lerp(prev.top, cur.top, t), 0.55f to lerp(mid), 1f to
lerp(foot))` and draws one rect. One layer, one lerp per stop, exactly row 6 of the motion table. The card never
moves: it is laid out inside the box and only the box's paint changes. `label = "sky"`. Under animator scale 0 the
`Animatable` ends in the next frame (DETAILED-PLAN verified facts), so the sky jumps.

**Under the status bar.** Today the Scaffold's padding carries the status bar inset, so a backdrop drawn by
HomeScreen would stop under the bar. Change `MainScaffold.kt:252` to `Scaffold(containerColor = ...,
contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Horizontal +
WindowInsetsSides.Bottom))` so only the top inset is no longer taken, and give each of the other three
destinations (`QIBLA`, `PRAYERS`, `SETTINGS`) a `Box(Modifier.statusBarsPadding())` around their content in the
`when` at `MainScaffold.kt:268`. HomeScreen's sky box applies `statusBarsPadding()` *inside* the backdrop, so the
colour fills the bar and the city sits below it. Visible change on the other tabs: none (the same inset, applied one
level down). `PreviewBuildFrame` (`MainActivity.kt:468-490`) already consumes the status bar inset when its notice
shows, so the preview build still lays out as before. Rejected: drawing above the column's top from inside the
scroll (`verticalScroll` clips on its axis, so the paint would be cut at the column's top); drawing the sky in
`MainScaffold` behind `AnimatedContent` (it would not scroll with the card at large text, and it would vanish in a
jump under the tab crossfade).

**The tablet cap** (Q18, Q20, `1.6-tablet`): `Modifier.readingWidth()` in `ui/components/ScreenFrame.kt` =
`widthIn(max = 600.dp).fillMaxWidth()` inside a centred Box. Applied to the inner column of the sky box and to the
rest of HomeScreen's column, and in `MainScaffold`'s `when` to the three other destinations in the same wrapper as
the status bar padding (`Box(Modifier.fillMaxSize().statusBarsPadding(), contentAlignment = TopCenter) {
Box(Modifier.readingWidth()) { ... } }`). The glass bar is not capped (it is a bar). On a phone nothing changes
(every phone is under 600dp wide). One tablet picture for Ali (§12).

## 7. Rules in `:core` (plain Kotlin, no `android.*`), and tests

`core/.../SkyPeriod.kt`:

```kotlin
enum class SkyPeriod { DAWN, DAY, EVENING, NIGHT }

object Sky {
    /** The sky for the prayer whose window is open, or Day when none is (sunrise to Dhuhr, or no times at all). */
    fun periodOf(current: PrayerSlot?): SkyPeriod = when (current) {
        PrayerSlot.FAJR -> SkyPeriod.DAWN
        null, PrayerSlot.SUNRISE, PrayerSlot.DHUHR -> SkyPeriod.DAY
        PrayerSlot.ASR, PrayerSlot.MAGHRIB -> SkyPeriod.EVENING
        PrayerSlot.ISHA -> SkyPeriod.NIGHT
    }

    /**
     * When this period ends, for the spoken "Dawn, until 7:40 am". Night before Fajr ends at today's Fajr; night
     * after Isha ends at [nextFajr], tomorrow's, which the caller has from NextPrayer; null if it has not.
     */
    fun endOf(period: SkyPeriod, today: DayPrayerTimes, now: Instant, nextFajr: Instant?): Instant?

    /** 0 before Fajr, 1 after Isha, else (segment + fraction) / 4 over the five prayers in [ordered]. */
    fun progress(now: Instant, ordered: List<Pair<PrayerSlot, Instant>>): Float
}
```

- **`SkyPeriodTest`** (core): the seven `periodOf` cases above, including `SUNRISE` and null; `endOf` for dawn
  (sunrise), day (Asr), evening (Isha), night after Isha (`nextFajr`), night at 02:00 (today's Fajr), and night
  after Isha with `nextFajr == null` (null, the string then falls back to the period name alone); `progress` at
  one minute before Fajr (0), exactly Fajr (0), halfway between Fajr and Dhuhr (0.125), exactly Isha (1), 23:59
  (1), and that it is monotonic across the whole day sampled every minute; a day from `PrayerEngine.compute` for
  London on 24 Oct 2026 and one approximated day (Tromsø in June) both give a progress in 0..1 at every minute.
- **`ColorContrastTest`**: `sky stops are readable under every ink` (72), `sky feet equal the page` (8),
  `status bar icons hold on every sky top` (8). Mutation proof: set the light night top back to `#C9D6DD` and watch
  the `onSurfaceVariant` pair fail; put it back.
- **`DesignRulesTest`**: `Sky.kt` has no button, so no inventory row; its one `Animatable` uses `Motion.draw()`
  and no written `tween` (the ban already catches a literal); every `Spacer` and top or bottom padding in the moved
  header pieces stays a token.
- **`TranslationIntegrityTest`**: the five keys in five languages.
- **`LargeTextAndTalkBackDeviceTest`**: at 2.0 on a 320dp frame the mark is absent and the city is whole; at 1.0 on
  360dp the mark's spoken text starts with the period name and contains a clock time; the city and date targets are
  48dp; the mark has no click action.
- **By hand on the emulator** (the main session, since the sky is its row in §0.1): the 823dp frame with the clock
  at 09:00, 13:30, 16:00 and 21:00 (the four pairs) in light and dark; the clock stepped across Asr while Times is
  on screen to see the crossfade; `adb shell settings put global animator_duration_scale 0` then the same step (a
  jump); Urdu with `:app:installRtl` judged for mirroring only; 1.5 and 2.0 at 823dp and 320dp; a tablet frame
  (`wm size 1600x2560`, `wm density 320`) for Q20; no location (clear the coordinates) and the Makkah fallback;
  the Ramadan clock date from step 4 (22 Feb 2027) to see the card under the sky.

## 8. States, accessibility, right to left, sizes

| State | What shows |
|---|---|
| First open of the process | the sky for the current period, no animation; the arc's green part draws in once over 400 ms (Q11); later tab returns snap |
| Period change on screen | the gradient crossfades, the glyph swaps, the arc's green part extends; the card and every word stay still; no haptic |
| Opening at night | night sky; after Isha the arc is all green with the crescent at the end; before Fajr the arc is grey with the crescent at the start |
| No times (no coordinates) | day sky, no mark; the card's "Set your location" as today |
| Makkah fallback | Makkah's period and mark; the banner under the card as today |
| Polar, approximated | the sky and mark from the approximated times; the polar notice under the card |
| Ramadan, Eid | the ordinary sky; the card and notice under the sky box on the plain page (FABLE-REVIEW: no festive sky) |
| Notices (no location, polar, exact alarms, step 3's Allow notifications card) | under the card, on `background`, unchanged; never on the sky, so the amber gloss never sits on a tinted field |
| Timetable (step 4) | the Today line and the sheet unchanged; the sheet opens over the sky as any sheet does |
| Light and dark | four skies each (§5); the card's own gloss and glow unchanged; dark hairline, light shadow as today |
| Long city name | the mark hides, the name is whole and ellipsised only where it is today |
| 1.5 text | the mark stays 52dp beside a taller name; hides by the width rule when the name needs the room |
| 2.0 text, 320dp | the mark is never drawn; the header and card as step 4's picture `5b`; everything scrolls as allowed |
| 823dp | no height change (§3); measure before the pictures |
| Tablet | sky full width; content centred in 600dp (§6) |
| Urdu and Arabic | the mark at the left end of the city row; Fajr at the right end of the arc; glyphs unmirrored; digits in the spoken time from `TimeFormat.clock` |
| TalkBack | §2 item 11; the mark is one node, nothing inside it speaks; with the mark hidden nothing is spoken for the sky (the Now pill and the card already say which prayer it is) |
| Remove animations (scale 0) | the crossfade and the draw in end in the next frame; verified once by `adb shell settings get global animator_duration_scale` |

## 9. Strings

English; the main session translates ur, tr, in, ar.

```
sky_dawn        Dawn
sky_day         Day
sky_evening     Evening
sky_night       Night
sky_now_a11y    %1$s, until %2$s
```

`sky_now_a11y` supersedes DETAILED-PLAN 9.6's "Now %1$s, between %2$s and %3$s": the end is the one fact a
listener wants (when does this change), the start is where the day already is, and "Now dawn" is not English.
When `endOf` returns null the node speaks the period name alone (`sky_dawn` and so on), never a string with a
blank in it. Translation notes: the four names are times of day, not prayer names and not fasting words, so Urdu
must not use "سحری" (that is suhoor) nor "فجر" (the prayer); plain "صبح سویرے / دن / شام / رات" or the
translator's equivalent; Arabic likewise "الفجر" is the prayer, so "الفجر الباكر / النهار / المساء / الليل" or
better; Turkish "Şafak / Gündüz / Akşam / Gece"; Indonesian "Fajar / Siang / Sore / Malam" ("Fajar" is the
everyday word for dawn in Indonesian and is acceptable there). The time comes from `TimeFormat.clock`, so digits
follow the app's language. Checked: no "Tap", no "missed", "late" or "owed", no hyphen or dash.

## 10. DESIGN_SYSTEM changes

- §1: a "The four skies" block after the hero gradient: the sixteen stops, the three inks, the rule that nothing
  but the header's three texts and the mark ever sits on a sky, the status bar guarantee, and the sentence change
  under "One colour is depictive" (the gold "and, since 1.6, the skies").
- §2 Motion: no new token; row 6 and 7 of BUILD-INSTRUCTIONS §6 are the sky's two animations, both `draw()`.
- §4.1: no new button; no inventory change. §8: "Times opens the same way" gains "on a sky that says the period".
- §9: a dated entry: option D; the mark at the end of the city row inside the existing height; widget glyphs as the
  sky's glyphs and why; the width rule; dots evenly spaced and why; no period word; no skyline or star field; no
  third depictive colour (the sun is `primary`); the status bar inset moved one level down; `sky_now_a11y` reworded.
  Rejected, with reasons, as listed in §1 and §4.
- §11: `1.6-dark-skies` stays OPEN until Ali's answer from the eight pairs, then moves to §9 with his words;
  `1.6-sky` and `1.6-tablet` in "Decided, work not yet done" become built when he says yes.

## 11. iPhone effect, and the HIG check

**Carries over.** `Sky` is plain Kotlin (the `java.time` gap is already recorded); a vertical `LinearGradient`
under the safe area with `.ignoresSafeArea(.top)` is SwiftUI's own pattern, and `preferredColorScheme` already
swaps the status bar style; the mark is a `Path` and an `Image(systemName:)` from the SF Symbols named in §4;
`Motion.draw()` is `.timingCurve(0.4, 0, 0.2, 1, duration: 0.4)`; Remove animations is `accessibilityReduceMotion`.
Nothing Android only in this step. HIG: text on the sky holds AA at every stop (4.1 legibility); no artwork behind
words (the owner's own rule and Apple's); the sky is a background, not glass, so it does not fight the floating
bar; the look is the app's own paper and green with a quiet scene, not a stock sky header, which is the 4.3
lookalike risk a prayer app runs; the header's two targets stay 48dp; nothing new to tap.

## 12. Pictures for Ali

Eight pairs for Q10, 823dp, English, the clock set so the Now pill and the mark agree: (1) dawn light, dawn dark
(06:30 on 24 Oct 2026, London); (2) day light, day dark (10:00); (3) evening light, evening dark (16:00); (4)
night light, night dark (21:00). Extra: (5) 2.0 text at 320dp, light (the mark hidden, nothing cut); (6) Urdu
at 823dp, day (the mark at the left, Fajr at the right end of the arc); (7) the tablet frame, day, light (Q20);
(8) day light with the Material glyph set, for Q2 only. For `plan-verifier` and `reviewer`: the Makkah fallback,
no location, TalkBack on the mark, a frame mid crossfade is not needed (it is a lerp of tested stops).

## 13. Questions for Ali (from the pictures; my recommendation first)

1. The dark night sky, from pair 4: **"Keep it this deep, navy told by hue and the crescent (Recommended)"**, or
   "One step brighter, so the sky shows against the page". Why it is his: `1.6-dark-skies` is OPEN in §11, and the
   difference is a matter of taste on an OLED, not of contrast (both pass).
2. The glyphs on the arc: **"The widget drawings: first light, sun, setting sun, crescent (Recommended)"**, or
   "The same icons as the rows: WbTwilight, WbSunny, WbTwilight, NightsStay" (picture 8). Why it is his: the mark
   is new on a screen he has approved and the two sets look different; §4 gives the reasons for the drawings.
3. The light skies as four hues (rose, mint, sand, slate) from pairs 1 to 4: **"Keep the four hues
   (Recommended)"**, or "Keep all four in the mint and sand family of the card, telling them apart by the mark
   alone" (DETAILED-PLAN 2.3's original candidates). Why it is his: it is the first colour in the app that is
   neither green nor amber, and whether it still feels like one calm app is a judgement from the pictures.

No fourth question. Q18 and Q20 (the tablet cap) are decided and only confirmed by picture 7.

## Verified in code, and assumed

Verified: `HomeScreen.kt:186-252` the column, its 16dp padding and the order header, Tight, card, banners, Group,
Today line; `:354-425` `LocationHeader` (city `weight(1f, fill = false)`, `maxLines = 1`, caret 28dp, row padding
4 by 2, `Space.Tight` above); `:428-470` `DateLine` (48dp target, text centred, `bodyMedium`, Hijri in `primary`
by span); `:471-500` the card's shape, glow, gloss and padding; `:1206-1213` the row glyphs; `Type.kt:40-44`
headlineMedium 26sp on 32sp, `:70-74` bodyMedium 15sp on 21sp; `Theme.kt:131-150` the status bar appearance
follows the scheme, `:163-177` `sajdaSurface`, `heroStyle` reads `LocalDarkTheme`; `Color.kt` the token families
and the three hero tokens quoted; `Motion.kt` the three specs; `Spacing.kt` the five tokens; `MainScaffold.kt:
248-275` the Scaffold with default insets and the `AnimatedContent` crossfade; `MainActivity.kt:231`
`enableEdgeToEdge()`, `:468-490` `PreviewBuildFrame` consumes the status bar inset only when its notice shows;
`SajdaViewModel.kt:171-180` `UiState.current` and its meaning, `:293-311` the three recalculate triggers (prayer
passed, day changed, sunrise passed); `PrayerEngine.kt:360-372` `currentPrayer` looks back one day and never
returns `SUNRISE`; `PrayerModels.kt:64, 111-129` `isPrayer`, `approximatedFrom`, `ordered`; `ColorContrastTest.kt`
the `Pair` shape, `assertAll`, the hero and gloss tests to copy; the four widget glyph vectors and their stroke
style; `strings.xml` has no `sky_` key and `home_date_line` is "%1$s · %2$s"; `docs/ASSETS.md` does not exist yet
(HANDOVER:1951 says it is created with the first asset; this step adds none); `ScreenFrame.kt` has `ScreenTitle`
and `BottomBarClearance`.

Assumed: the 823dp slack of 19dp (DETAILED-PLAN 2.1, a pixel reading) still holds after steps 3 and 4 (the step 4
pictures at 914dp show about 150dp spare, which says nothing about 823); the date line's text is about 20dp tall
and centred with about 14dp above it at 1.0 (from the 48dp target and the 21sp line height, not measured); the
English date line is about 200dp wide on a 360dp frame (read off picture `step4/12`); `TextMeasurer` gives the same
width the `Text` will lay out at (same style, same density; true in Compose 1.11); the `only(...)` form of
`contentWindowInsets` leaves the bottom inset for the glass bar untouched (check the bar's position in the first
picture); Compose draws the `Animatable` to its end under animator scale 0 (DETAILED-PLAN verified facts, still to
be seen once on this emulator).

**Risks (Fable):** the width rule is arithmetic until the first picture (if the mark hides for "London" the
constants are wrong, not the rule); moving the status bar inset one level down touches every tab for a change only
Times needs, so the reviewer must see Qibla, My prayers and Settings in the same pictures; the dark night sky may
look like no sky at all on Ali's S23 Ultra (Q1 exists for that); four light hues may read as a different app to
him (Q3 exists for that).

**Note from Fable to the main session:** the skies are a visual overhaul of the one screen every user sees, so the
eight pairs go to Ali before anything else in step 5 is polished, and the step 6 Qibla work does not start until he
has answered Q1 to Q3.
