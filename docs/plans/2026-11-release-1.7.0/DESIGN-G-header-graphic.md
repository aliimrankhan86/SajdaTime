# Design G: the sky graphic on Times, bigger and in colour (Fable `ux-lead`, 3 Nov 2026)

Ali, 3 Nov 2026: "the graphic to show the namaaz visual on top of the card is too small and easier to miss. So we
may need to improve its size or add colours to make it better looking visually."

Status: a recommendation with rendered mockups. Nothing is decided until Ali picks from the pictures (§8).
Read with `docs/plans/2026-10-release-1.6.0/STEP-5-DESIGN.md` (the mark as built) and `docs/DESIGN_SYSTEM.md` §0,
§9 (24 Oct 2026, step 5 entry) and §11. Skills used: `apple-design` (HIG legibility, 4.3 lookalike check),
`make-interfaces-feel-better` (optical centring, glow and motion notes). DESIGN_SYSTEM and Ali's decisions win.

## 1. What the graphic shows today (verified in code)

`app/src/main/java/com/sajdatime/app/ui/home/Sky.kt` (`SkyMark`) and `core/.../SkyPeriod.kt` (`Sky`):

| Part | Today |
|---|---|
| Where | the end of the city row, a sibling of the tappable city, 104 x 36dp, inside the row's own height |
| Arc | chord 96dp, apex 16dp above the ends; track 1.5dp `onSurfaceVariant`; the passed part 2.5dp `primary`, stopped 11dp short of the glyph |
| Dots | five, 4dp, `onSurfaceVariant`: Fajr, Dhuhr, Asr, Maghrib, Isha at 0, 1/4, 1/2, 3/4, 1 of the arc. Even by prayer, not by clock (`Sky.progress`) |
| Glyph | 18dp `primary`, the period's own vector: dawn `widget_glyph_fajr` (rays over a horizon), day `sky_glyph_day` (sun), evening `widget_glyph_maghrib` (sun going down), night `widget_glyph_isha` (crescent and star). It rides the arc at `progress` and covers the dot it sits on |
| Progress | 0 before Fajr, 1 after Isha, else (segment + fraction) / 4; recomputed once a minute |
| Period | `Sky.periodOf(current)`: Fajr window = dawn; sunrise to Asr (incl. no times) = day; Asr and Maghrib = evening; Isha to Fajr = night. The same period tints the backdrop (four fixed three stop skies) |
| Night | after Isha: arc all green, crescent at the Isha end. Midnight to Fajr: arc grey, crescent on the Fajr dot |
| Hidden | when the city name needs the row (`Sky.markFits`: name wider than row less 150dp) and always at 2.0 text |
| Motion | the green part draws in once per process over `Motion.draw()` (400 ms); period change cross fades the sky |
| TalkBack | one node, "Dawn, until 7:40 am" (`sky_now_a11y`); nothing inside speaks |

Why it is easy to miss (from `private/screenshots/review-1-6-0/times-light.png`): 96dp of 1.5dp line beside a 26sp
title, an 18dp glyph with no fill, 4dp dots, and the only colour is the green that the caret and the Hijri date
already use on the same two lines. It reads as a flourish on the title, not as a picture of the day.

## 2. Recommendation (A): the sky band

Move the graphic out of the city row and give it its own full width band between the date line and the next
prayer card, still inside the sky backdrop. Three times wider, a marker twice the size on a paper disc with a soft
green glow, thicker lines, filled ticks for prayers already reached and hollow ticks for the ones to come. No
words in it. The city row gets its whole width back, so the graphic now shows for every city name and at every
text size (it was hidden for "Kuala Lumpur" and at 2.0 text).

Mockups (411dp wide, real tokens, headless Chrome): `mockups/G/A-<state>-<theme>.png` for dawn, midday, afternoon,
sunset, night, in light and dark; plus `A-predawn-dark`, `A-midday-light-2x`, `A-night-dark-2x`,
`A-midday-light-urdu`, `A-night-dark-urdu`; `before-midday-light` and `before-midday-dark` for what ships today;
`contact-sheet.png` has all of them. Source: `mockups/G/header-graphic.html?state=&theme=&x2=1&rtl=1&opt=B|before`.

### 2.1 Geometry (dp; the arc is computed from the band's width at draw time)

| Part | Spec |
|---|---|
| Band | `fillMaxWidth()`, height **52**. Sits directly under the date line (whose 48dp target already leaves about 13dp of its own under the text); the existing `Space.Tight` between header and card stays below the band. No new `Space` value |
| Arc | ends at x = 20 and width − 20, y = 34; apex y = 14 (rise 20). On a 360dp phone the chord is 288dp; on 411dp, 339dp; capped with the 600dp reading column. Circular, as today (radius = chord² / 160 + 10) |
| Track | 1.5dp `onSurfaceVariant`, round caps (already tested on every sky stop, min 4.57:1) |
| Passed arc | 3dp `primary`, from the Fajr end to 18dp of arc length short of the marker's centre (16dp radius plus 2dp), round caps |
| Ticks | five, 8dp diameter, at 0, 1/4, 1/2, 3/4, 1. Reached (u <= progress): filled `primary`. To come: filled `surface`, ring 1.5dp `onSurfaceVariant`. The difference is fill and ring, not colour alone |
| Marker | a 32dp disc filled `surface` with a 1.5dp `primary` ring, the period glyph 20dp in `primary` centred on it; drawn last. Behind it a glow: radial `primary` 32% at the centre, 10% at 60%, 0% at the edge, radius 30dp (decorative; carries nothing) |
| Horizon | none. A hairline in `outlineVariant` was tried in the mockups and is not visible on any sky; dropped |
| Fit | the disc at either end spans y 18 to 50 inside the 52; the glow may paint into the 16dp gutters and 4dp above the band (drawn, never laid out) |

Contrast computed with the `ColorContrastTest` formula (the test is the judge): `primary` on `surface` is already
tested; `primary` ring and glyph on the sky tops min 5.83 light, 6.19 dark; `onSurfaceVariant` ring on `surface`
6.28 light, 8.00 dark; `primary` tick on every sky stop at least 5.83. The disc fill is only 1.0 to 1.3 against
the skies on purpose: the ring and the glyph define it, the fill is paper. No new colour token. No hex in code.

### 2.2 Colour, and why it is still green

Ali asked for colour. Quiet Glass gives the band three sources of it and all three are already on this screen:
the period's sky behind it (pale rose, mint, sand, slate; deep violet, teal, olive, navy), the identity green for
everything reached and for the sun or moon, and paper for everything to come. The green now has weight (3dp arc,
8dp ticks, a ringed 32dp disc, a glow) rather than being a hairline. A warm gold sun was considered and is not
recommended: the Kaaba gold (`kiswah`, #E8B14A) is 1.52:1 on the light day sky, so it would need a new darker
gold for light plus contrast work, and DESIGN_SYSTEM §1 names gold as depictive of the Kaaba only, with amber
reserved for warnings. It is offered to Ali as a question (§8 Q2) because it is a taste call, not a rule.

### 2.3 States

| State | Band |
|---|---|
| Dawn (Fajr window) | marker near the Fajr end, rays glyph; the short green stub behind it |
| Day | sun glyph; the arc green up to the marker; ticks reached are green, ticks to come are paper rings |
| Evening (Asr, Maghrib) | setting sun glyph |
| Night after Isha | crescent at the Isha end; all five ticks and the whole arc green |
| Night before Fajr | crescent on the Fajr end; arc all track, ticks all paper |
| No times (no coordinates) | the band is not composed; the card sits where it does today. Makkah fallback: Makkah's band, as the card follows Makkah |
| Polar, approximated | the approximated times, as today |
| 1.5 and 2.0 text | the band is text free and stays 52dp; the header and card grow around it (`A-midday-light-2x`, `A-night-dark-2x`). Never hidden: `Sky.markFits` and its test are deleted |
| 320dp | chord 248dp, 62dp between ticks; still clear |
| Tablet, landscape | inside the 600dp reading column; chord up to 528dp |
| First open of the process | the green part and the marker draw in from the Fajr end over `Motion.draw()`, as today (Q11, once per process). Later returns snap |
| Period change on screen | the sky cross fades (today), the glyph swaps, the arc extends; the band never moves. No haptic |
| Reduce motion (animator scale 0) | both end in the next frame; the glow never pulses, nothing loops |
| Light, dark | `A-*-light`, `A-*-dark`; the dark glow reads as a halo, the light one as a soft shadow |

### 2.4 Right to left (Urdu, Arabic)

Mirror once, as the mark does today: Fajr at the start edge (the right in Urdu), the day running right to left,
glyphs unmirrored (`A-midday-light-urdu`, `A-night-dark-urdu`). Why: it is a timeline, and Android mirrors every
progress indicator for RTL readers; the prayer list below reads the same way; Ali saw and accepted this mirroring
on 24 Oct 2026. The alternative, the sun moving left to right as in the sky when facing south, was rejected: it is
only true north of the equator and it would put Fajr on the far side from where an Urdu reader starts.

### 2.5 TalkBack

Keep the one node and the existing sentence, "Day, until 3:12 pm" (`sky_now_a11y`), no new string. Reasons: the
24 Oct decision that the end time is the one fact a listener wants stands; the ticks add nothing the list below
does not already read out; no translation work. If the main session prefers the brief's wording, the sentence
"Day, between Dhuhr and Asr, until 3:12 pm" needs three new strings (between, before Fajr, after Isha) in five
languages; not recommended.

### 2.6 Height cost (the thing to weigh)

| | Header block | Band | Gap | Card foot from screen top (status bar 42dp) |
|---|---|---|---|---|
| Today | `Tight` 4 + city row 36 + date 48 = 88 | 0 | `Tight` 4 | about 270dp (measured on `times-light.png`, card 149dp) |
| A | 88 (unchanged) | **52** | `Tight` 4 | about 322dp |

**+52dp.** The hero card is whole without scrolling on every phone (foot at 322dp of 823). Below it, estimated
from the 1.6.0 picture with no banners showing: on the S23 Ultra (823dp) the Isha row moves under the glass bar
(today Isha shows and Midnight is under it); on a 914dp phone every row still shows, the caption and the mosque
door scroll as they do today. One short scroll, nothing lost, the rule since 1.6. Measure on the 823dp frame
before pictures go to Ali. Deliberately not clawed back by trimming the date line's target or the card's padding.

## 3. Alternative (B): the same mark, bigger, still in the city row

`mockups/G/B-midday-light.png`, `B-sunset-dark.png`. The mark grows to 150 x 44dp: arc chord 122dp, rise 16;
track 1.5, passed 3; ticks 6dp (filled or paper ring as in A); marker a 28dp paper disc with a 20dp glyph, ring and
glow. The city row grows from 36 to 44dp: **+8dp**. It keeps the hide rule, which gets stricter (the name must
leave 196dp, not 150, so "Birmingham" at 1.0 text and most names at 1.5 lose it) and it still hides at 2.0 text.
Cheaper, and clearly bigger than today, but it stays a corner ornament beside the title and competes with the
caret and the Hijri green. Pick B if the 52dp of A is too much.

## 4. Rejected, with reasons (so they are not re-proposed)

Prayer names under the ticks: at 2.0 text "Maghrib" is about 110dp wide and five labels cannot share a 288dp arc;
at 1.0 they would be the sixth and seventh texts on a header Ali asked to keep calm; the rows below are the labels.
A filled dome of colour under the arc: turns the scene into a progress pie and sits a fill on the sky where the
rule is that only the header's texts and the mark sit on it. A sun that changes colour by period (gold at day,
orange at sunset): a third depictive family, and orange reads as warning beside the amber notices. A star field,
horizon skyline or mosque silhouette: clutter, and the stock prayer app look (App Store 4.3). A pulsing glow: nothing
loops. The band inside the hero card: the card is unchanged by decision (DESIGN_SYSTEM §9, 23 Oct 2026).
Ticks for sunrise or midnight: the arc is the five prayers, as `Sky.progress` defines it.

## 5. Compose approach (no new library)

- `Sky.kt`: replace `SkyMark` with `internal fun SkyBand(period, progress, spoken, modifier)`: a
  `Box(modifier.fillMaxWidth().height(BandHeight).semantics { contentDescription = spoken })` holding one
  `Canvas(Modifier.fillMaxSize().clearAndSetSemantics {})` and the glyph `Icon` placed by `absoluteOffset {}`
  from the marker point, exactly as now. `BandHeight = 52.dp`, a component size, not a gap.
- In the draw: `chord = size.width - 40.dp.toPx()`, `rise = 20.dp.toPx()`, `R = chord² / (8 rise) + rise / 2`,
  centre `(size.width / 2, 14.dp.toPx() + R)`, `half = asin(chord / 2 / R)`; `arcAngle(u)` as today. Order: track
  `drawArc`, passed `drawArc` to `progress * drawIn - 18.dp / arcLength`, five ticks (`drawCircle` primary, or
  surface plus `Stroke(1.5.dp)` ring), glow `drawCircle(Brush.radialGradient(0f to primary.copy(alpha = .32f),
  .6f to primary.copy(alpha = .10f), 1f to Color.Transparent), radius = 30.dp)`, disc `drawCircle(surface)` plus
  `Stroke(1.5.dp)` in primary. Mirror every x once with `if (rtl) size.width - x else x`; no `scale(-1f, 1f)`.
- Alpha on `primary` for the glow is a colour decision: add `SkyGlowAlpha` (or two stops) to `Theme.kt` and
  `tokens/color.json` in the same commit so `DesignTokensTest` sees it; it is decorative, so no contrast pair.
- `HomeScreen.LocationHeader`: remove the `BoxWithConstraints`, measurer and `showMark`; the city row is as before
  step 5 with the mark gone; `DateLine` unchanged; after `DateLine`, `if (today != null) SkyBand(...)`.
- `:core`: delete `Sky.markFits` and its `SkyPeriodTest` cases; `periodOf`, `endOf`, `progress` unchanged.
- Tests: `DesignRulesTest` (no written durations: keep `Motion.draw()`), `DesignTokensTest` (new alpha token),
  `LargeTextAndTalkBackDeviceTest` (the band is present at 2.0 on 320dp now, one node, no click action, spoken
  text starts with the period name), `ColorContrastTest` unchanged (no new ink pair).
- Docs: `03-components.md` "Sky mark and backdrop" becomes "Sky band"; `05-screens-and-features.md` header order;
  `DESIGN_SYSTEM.md` §9 dated entry with Ali's words; `tokens/color.json`; store screenshots retaken.
- Check by hand: 823dp frame at 06:30, 10:00, 16:00, 18:05, 21:00 and 03:00, light and dark; `:app:installRtl`
  judged for mirroring only; 1.5 and 2.0 at 320dp; animator scale 0; no location; Makkah fallback; tablet frame.

## 6. iPhone effect

Carries over: `Sky` stays plain Kotlin; the band is a SwiftUI `Canvas` with the same arithmetic, the glyphs map to
`sunrise.fill`, `sun.max.fill`, `sunset.fill`, `moon.stars.fill`; `layoutDirection` mirrors; Reduce Motion drops
the draw in. Nothing Android only. HIG: AA text and 3:1 non text on every stop, no words in the picture, the app's
own scene rather than a stock sky header (4.3).

## 7. DESIGN_SYSTEM §10 review answers (Times header)

What the user needs first: the city, then the date, then where the day stands, then the next prayer; the band
sits between the date and the card in that order. Removed: the hide rule and the 2.0 text exception. Colour
roles only, each with a tested pair; nothing by colour alone (reached ticks are filled, to come are rings; the
marker's place and glyph carry the period). Targets: none new, the band is not tappable. Text scales: no text in
it. RTL mirrored once. Motion: the two existing tokens. Copy: none new.

## 8. Decisions for Ali (from the pictures; recommendation first)

1. **Which one:** "The full width band under the date, +52dp (Recommended)", or "The bigger corner mark, +8dp,
   hidden for long city names" (`A-*` against `B-*`). His because it changes a screen he approved and spends
   height he has argued over (HomeScreen height budget).
2. **The sun's colour:** "Green, like everything reached (Recommended)", or "A warm gold sun by day (needs a new
   gold for light mode and contrast work; the moon stays green)". His because it would be a third depictive colour.
3. **The glow:** "Keep the soft glow (Recommended)", or "No glow, ring and disc only". A taste call on OLED.

## 9. Verified and assumed

Verified in code: `Sky.kt` (the geometry, inks, draw in, mirroring, semantics), `SkyPeriod.kt` (periods, progress,
`markFits`), `HomeScreen.kt:246-257` (sky box, header, `Space.Tight`, card) and `:408-557` (`LocationHeader`,
`DateLine`), `Color.kt` sky tokens, `tokens/color.json` roles and sky stops, `Spacing.kt`, `motion.json`, the four
glyph vectors, `strings.xml` sky keys, `03-components.md` sky entry, `DESIGN_SYSTEM.md` §0 and §11.

Assumed, not measured: the card height (149dp) and the 823dp row positions are read off the 1.6.0 emulator
picture at 2.625 px per dp and rounded; the glass bar height (about 84dp); that drawing outside the Canvas is not
clipped by the sky box (the scroll column clips on its axis only, per STEP-5); the Urdu strings in the mockup are
illustrative, not the app's. Not verified: how the glow looks on Ali's S23 Ultra or the Redmi (emulator and
mockup only); the exact pixel of the Isha row under the bar at 823dp.
