# Step 6: the Qibla restyle, design (Fable `ux-lead`, 24 Oct 2026)

> **READING ORDER:** as `STEP-1-DESIGN.md`: `BUILD-INSTRUCTIONS.md` first, then this file for step 6 only.

Written before any code, as Ali's process asks: journey and design first, then `plan-verifier`, then pictures for
Ali's yes, then the build. Skills: none invoked; DESIGN_SYSTEM §2, §4.1 and §9 (23 Oct, item 4), BUILD-INSTRUCTIONS
§3 Q12 and §6 rows 9 and 10, DETAILED-PLAN 8.10 and 9.5, PLAN §0.1 item 7 and the shipped `Notice` pattern decide
nearly everything. Apple HIG checks applied by hand (§11).
**Status: a recommendation. Nothing is decided until Ali says yes from the pictures (§12).**

Decided before this file and not reopened: the direction sentence first; the place name optional and small; distance
and bearing behind a labelled compact solid "Details" with a chevron, in km or miles by the phone's region (US, GB,
LR, MM), no setting (Q12, 9.5); the key stays; the dial stays its size with a thicker soft rim and a rounder needle
(§9); alignment keeps `qibla_facing`, the fill, the rim and the existing `LongPress` buzz, with the fill and rim
colour animated at `Motion.standard()` (row 9); the dial's 120 ms `tween` stays, exempt by name (row 10); the
four strings and the removal of `qibla_subtitle` (BUILD-INSTRUCTIONS §5 row 6); the amber notices stay; nothing
on the watch changes.

## 1. Recommendation

The Qibla screen answers one question, in this order: **which way**, then the dial that shows it, then what the
marks mean, then where you are, then the numbers for anyone who wants them. Under the title "Qibla" the first
line is the instruction, "Turn right 58°", with "Facing 60°" under it; on arrival the first line becomes the green
"You are now facing the Kaaba" with a tick, the dial fills green over 250 ms and the phone buzzes once, as today.
The dial keeps its size and its marks, gains the soft lift every card already has (shadow in light, hairline in
dark) and a real rim band instead of a hairline, and the needle's corners are rounded. Under the dial: the key
(unchanged), then the place name small with a pin, then one compact solid button, "Details", which opens two
quiet lines, "About 2979 miles from here" and "118° from north". The accuracy notices stay amber and unchanged
in words. **No sky on Qibla**: the sky says which prayer period it is, which is the Times question; here the only
question is which way, the dial's `surfaceVariant` face would lose its edge against a tinted page (about 1.1:1),
and the green arrival fill would sit on a green day sky. One hero screen, the rest plain, is also what keeps the
app its own (App Store 4.3).

Three changes beyond the plan's words, each small, each with its reason in §3 and §7: "Facing 60°" stays on screen
when aligned (it is now above the dial, and a line vanishing there would make the dial jump at the very moment
it animates); the no location state is the same amber "Set location" notice My prayers already has, not a bare
sentence; and the Makkah fallback counts as no location on this screen, because the bearing from Makkah to Makkah
is 0° and the distance 0 km (verified: `recalculate()` uses `settings.coordinates`, which the fallback fills).

Rejected: numbers on the dial face (the needle and the Kaaba are the answer; a heading readout in the middle sat
on the needle and was removed once already, `QiblaScreen.kt:305`); a bare "i" for the details (§9: never a bare
"i"); the place name as a second door to the location sheet (the notice is the door when it matters; a tappable
name beside a button invites mis-taps); hiding the degree number as Pillars does (FABLE-REVIEW: the number is what
tells you to keep turning); a teardrop needle (Pillars' shape; ours stays a tapered arrow with rounded corners);
any artwork or illustration (nothing to download; `docs/ASSETS.md` is not created by this step).

## 2. The journey, numbered

Words in quotes are on screen.

1. **Opening, located, with a compass.** "Qibla". "Turn right 58°" in the title's size, "Facing 60°" under it in
   grey. The dial under them, the green arc from the needle round to the Kaaba. Under the dial the key, "The Kaaba"
   and "Where you are facing"; then a small pin and "London"; then the green "Details" button with a down chevron.
2. **Turning.** The dial turns with the phone (the existing 120 ms follow), the arc shrinks, the two numbers change
   in place with tabular figures, so nothing wobbles sideways.
3. **Arriving.** The first line becomes "You are now facing the Kaaba" in green with a tick before it; the dial
   face fades to the pale green, the rim fades to green and thickens, over 250 ms; one buzz. "Facing 118°" stays
   under it. Nothing else moves.
4. **Details.** Tap "Details": the chevron turns up, the label becomes "Hide details", and two lines unfold under
   the button: "About 2979 miles from here" and "118° from north" (km in most of the world). Tap again to fold.
5. **The compass needs calibrating.** Under "Details", the amber notice "The compass needs calibrating. Hold your
   phone and move it in a figure of eight a few times." The dial stays where it is while the user waves the phone;
   the notice goes when the reading settles.
6. **No compass.** "Face 118° from true north" as the first line, no "Facing" line; the amber notice "This phone has
   no compass, so we can give you the angle but cannot point the way." sits above the dial, beside the number it
   explains; the dial holds north up with the needle at 118°. No key (there is no "you" to explain). "Details"
   shows the distance only; the bearing is already the headline.
7. **No location, or the Makkah fallback.** "Qibla", then the amber notice "Set your location to find the Qibla."
   with "Set location", which opens the location sheet. No dial, no key, no Details.
8. **Urdu and Arabic.** The title and the direction block at the right edge; "Turn right" is mirrored in meaning
   (right is right), the dial is not mirrored (north is north), the chevron points down in both directions, the
   pin sits before the name at the right.
9. **TalkBack.** "Qibla, heading", then the direction line as a polite live region ("Turn right 58 degrees" as
   the string reads), "Facing 60°", "London", "Details, button, collapsed"; after a tap "Hide details, button,
   expanded", "About 2979 miles from here", "118° from north". The dial and the key say nothing (decorative; the
   live region is the spoken dial).

## 3. The screen, top to bottom, with tokens

Portrait, located, with a compass. The column is `ReadingFrame`'s 600dp reading width, centred (verified
`MainScaffold.kt:440-446`); the status bar inset is already taken there (step 5; `screens/step5/checks/qibla-top.png`).

| # | Element | Style and ink | Alignment, target | Gap above |
|---|---|---|---|---|
| 1 | "Qibla" | `ScreenTitle` (unchanged) | start, 20dp gutter | |
| 2 | Direction line: `qibla_turn_right` / `qibla_turn_left` / `qibla_facing` / `qibla_no_compass` | `headlineMedium` with `fontFeatureSettings = "tnum"`; `onSurface`; aligned: `primary` with `CheckCircle` 24dp `primary` before it, `Space.Related` apart, top aligned, text `weight(1f, fill = false)` | start, 20dp gutter; polite live region, `contentDescription = message` (exists) | `Space.Paragraph` (a headline and its paragraph, §2 table) |
| 3 | "Facing 60°" `qibla_heading_now` | `bodyLarge`, `tnum`, `onSurfaceVariant`; **shown whenever there is a compass, aligned or not** | start | `Space.Tight` (a title and the line that belongs to it) |
| 3a | No compass notice, only when `compassAccuracy == UNAVAILABLE` | `Notice(brief = qibla_sensor_missing)` (exists) | full width, 20dp gutter | `Space.Group` |
| 4 | The dial (§4) | | centred; `dialMaxWidth()` unchanged | `Space.Section` |
| 5 | The key `DialLegend` (exists), hidden with no compass | unchanged, but a `FlowRow` (§7, 2.0 text) | centred | `Space.Section` |
| 6 | Place name, only when `placeName(context)` is not blank and `!usingDefaultLocation` | `Icons.Outlined.LocationOn` 16dp `onSurfaceVariant` (SF `mappin`), `Space.Tight` gap, then the name `bodyMedium` `onSurfaceVariant`, one line, ellipsis, `bidiIsolated()` | centred; not clickable; spoken as the name | `Space.Paragraph` |
| 7 | "Details" | `CtaButton(compact = true)`: label `qibla_details` closed, `notice_hide_details` open; trailing `Icons.Rounded.ExpandMore` 20dp (SF `chevron.down`) rotated 0 to 180 at `Motion.quick()`, `Space.Tight` after the label (as `Notice.kt:212-229`); `stateDescription` `state_expanded` / `state_collapsed` | centred; 40 drawn, 48 target, wraps | `Space.Group` (a block and its button row) |
| 8 | The two lines, folded: `qibla_distance_miles` or `qibla_distance_km`, then `qibla_bearing` (bearing omitted with no compass) | `bodyMedium` `onSurfaceVariant`, `tnum`, centred; `AnimatedVisibility(open)` with Compose's default fold (row 11, an approved pattern), `rememberSaveable` as `Notice` | centred | `Space.Related` (a button and what it reveals) |
| 9 | Accuracy notice, `LOW` or `MEDIUM` only (`HIGH` nothing, `UNAVAILABLE` is 3a) | `AccuracyNotice` (exists) | full width | `Space.Group` |
| 10 | `Space.Section` then `BottomBarClearance()` (exists) | | | |

**Why the two notice slots differ.** The no compass notice is permanent, so it can sit above the dial where it
explains "Face 118° from true north" without anything ever jumping. Calibration and "may wander" come and go
while the phone is in the hand; above the dial they would move the dial by 100dp at the moment the user is
watching it. Below Details they inform without disturbing.

**Height at 823dp, English, 1.0:** status bar ~30 + title 48 + 12 + 32 + 4 + 24 + 24 + dial 296 (`dialMaxWidth` =
0.36 × 823) + 24 + key 20 + 12 + place 20 + 16 + button 40 + 24 + clearance 88 ≈ 714. With a calibration notice
(about 90) ≈ 804. Fits; no scroll in the common states. Details open adds about 48 and scrolls a little, allowed
(assumed from the line heights in `Type.kt`; measure on the frame before the pictures).

**At 320dp:** the column is 280dp wide; "Turn right 118°" at 26sp fits one line (about 200dp); the dial is
`min(320, 0.36 × 569)` = 205dp; the key fits one row at 1.0.

**1.5 and 2.0 text:** the direction line wraps between words (`qibla_facing` is two lines from 1.0 on 360dp
already); the dial does not grow (`dialMaxWidth`, verified `:195-196`); the key wraps to two rows (§7); the
place name ellipsises; the button label wraps centred (`actionLabelStyle`); the screen scrolls.

**Landscape:** the dial is about a third of the height (`:195`, verified); the direction block above it is start
aligned in the centred 600dp column; everything scrolls. **Tablet:** the 600dp column, the dial 320dp centred.

## 4. The dial

Only the look changes; the geometry rule stays "one budget, four numbers, move together" (`:439-459`). All in
shares of the radius, as today.

| Part | Today (verified) | New |
|---|---|---|
| Surface | Canvas draws the face circle; rim `Stroke(2f)` **pixels** (0.33dp at 600dpi), 6f aligned | the dial `Box` takes `sajdaSurface(CircleShape, color = face)` (`Theme.kt:163-177`: shadow in light, hairline in dark, the same lift every card has); `face` is `animateColorAsState(if (aligned) primaryContainer else surfaceVariant, Motion.standard(), label = "dialFace")`; the Canvas no longer draws the face |
| Rim | hairline | a band inside the edge, outer edge at 1.0; thickness `0.04r` not aligned, `0.07r` aligned (`animateFloatAsState(Motion.standard(), label = "dialRimWidth")`); colour `outlineVariant` to `primary` (`animateColorAsState(Motion.standard(), label = "dialRim")`). Three values read inside the Canvas draw lambda, never in composition, so a frame recomposes nothing. Thickness as well as colour, so a user who cannot tell green from grey still sees the ring get heavier (the existing reason, `:259-263`) |
| Ticks | 0.91 to 0.99 | 0.84 to 0.92 (major 0.08, minor 0.045), widths as today |
| Letters | 0.83 | 0.77, size `0.115r`, N bold `primary`, as today |
| Kaaba | `KAABA_DISTANCE` 0.66, `KAABA_SIZE` 0.30 | 0.60, 0.30 (worst corner 0.765, inside the letters' band as today; drawn last) |
| Needle | sharp triangle, tip 0.74, base ±0.075, hub 0.055 | the same path filled **and** stroked in `primary`, `Stroke(width = 0.03r, join = StrokeJoin.Round, cap = StrokeCap.Round)`, so the three corners round off; tip 0.68 (+0.015 from the stroke), base ±0.075, hub 0.055. The tip stays inside the cube when the two coincide (cube 0.45 to 0.75) |
| Arc | `0.13r` thick, inset `thickness/2 + 3f` px | `0.11r` thick, inset `0.07r + thickness/2` so it clears the rim band at its widest; drawn only while not aligned, as today |
| Rotation | `tween(120)` | unchanged, exempt by name in `DesignRulesTest` (verified `:179`) |
| Semantics | `clearAndSetSemantics {}` | unchanged: the direction line is the spoken dial |

The watch keeps its own dial (`WearApp.kt`); the shared Kaaba drawables are untouched.

**Haptic and animation rule for this step.** The one buzz stays `HapticFeedbackType.LongPress`, fired where
`aligned` is decided with the 5 s quiet gap and the "not on open" guard (`:147-170`, verified; DESIGN_SYSTEM §2
names it as a kept exception). PLAN §0.1 item 7's "gentle confirmation" is the 250 ms fade plus that single buzz;
nothing new vibrates, and `Confirm` is not used (it would change a named exception for a feel difference Ali has
not asked for). Nothing animates on open; the chevron turns at `Motion.quick()`; the fold is Compose's default
(row 11). Under animator scale 0 every value lands in the next frame.

## 5. Details, and what it shows

| State | Lines |
|---|---|
| Compass, miles region | "About 2979 miles from here", "118° from north" |
| Compass, elsewhere | "About 4794 km from here", "118° from north" |
| No compass | the distance line only |
| Open or closed on open | closed on each fresh open; `rememberSaveable` keeps it across rotation |

Numbers: `distanceKm.roundToInt()` or `MeasurementSystem.milesFromKm(distanceKm).roundToInt()`, and
`bearing.roundToInt()`, passed to `stringResource`, which formats `%1$d` through the app's pinned configuration
(HANDOVER §5.11: Arabic stays Western by `ar-u-nu-latn`, verified in CLAUDE.md). No grouping separator: "4794",
as today's `qibla_subtitle` prints it.

Why a button and not always on: FABLE-REVIEW §4, "the least useful line for the most people" was the top line;
behind a labelled button the numbers cost nothing and are one tap away for the person who wants a sanity check.

## 6. The km or miles rule, where it lives, and the iPhone line

**`core/src/main/java/com/sajdatime/core/MeasurementSystem.kt`** (plain Kotlin, no `android.*`):

```kotlin
/**
 * Distance in miles or km, by the phone's region (Ali, 23 Oct 2026, Q12). The four regions are CLDR's own
 * measurement data (US, LR and MM use the US system; GB uses the UK system, which is miles for distance), written
 * as a list so every Android version and a future iPhone give the same answer. No setting.
 */
object MeasurementSystem {
    const val KM_PER_MILE = 1.609344
    private val milesRegions = setOf("US", "GB", "LR", "MM")
    fun usesMiles(country: String): Boolean = country.uppercase() in milesRegions
    fun milesFromKm(km: Double): Double = km / KM_PER_MILE
}
```

The reader of the phone's region is two lines in `QiblaScreen.kt` (no second file):
`Resources.getSystem().configuration.locales[0].country`, as 9.5 says (the phone's own region, not the app's
language; `LocaleDisciplineTest` bans only `Locale.getDefault()`, verified `LocaleDisciplineTest.kt:171`).

**Change from 9.5, for `plan-verifier`:** 9.5 uses ICU `LocaleData.getMeasurementSystem` on API 28+ with the list
as the fallback below. The ICU answer *is* that list (CLDR `measurementSystem`: US for US, LR, MM; UK for GB;
metric elsewhere), so two code paths give one answer and only one of them is unit testable. Ponytail: the list
alone, same on every API level and on iPhone. Rejected: a unit setting (Q12: no setting); the app's language as
the region (an Urdu speaker in Bradford wants miles).

**iPhone effect:** carries over. The rule is plain Kotlin; `Locale.current.region` gives the country; the compass is
`CLLocationManager.heading`; the dial is a `Canvas`; the buzz is `.sensoryFeedback(.success)`;
`Motion.standard()` is `.timingCurve(0.4, 0, 0.2, 1, duration: 0.25)`. Nothing Android only in this step.

## 7. States, accessibility, right to left, sizes

| State | What shows |
|---|---|
| Located, compass, turning | §3 rows 1 to 10; the arc and numbers change in place |
| Aligned | the green line with the tick; face, rim colour and rim width fade over 250 ms; one buzz (not on open, not twice in 5 s); "Facing N°" stays; the arc is not drawn |
| Calibrating (`LOW`) or wandering (`MEDIUM`) | the amber notice under Details; the dial still turns; nothing above the dial moves |
| No compass (`UNAVAILABLE`) | "Face 118° from true north"; the notice above the dial; needle held at the bearing; no "Facing" line; no key; Details shows the distance only |
| No location (`qiblaBearing == null`) or Makkah fallback (`usingDefaultLocation`) | title, then `Notice(brief = qibla_needs_location, actionLabel = action_set_location)` opening the location sheet exactly as `MyPrayersScreen.kt:194-201` wires it; no dial, key, place or Details |
| Details open or closed | §5 |
| Light and dark | roles only; `sajdaSurface` gives the shadow or hairline; the gold Kaaba detail unchanged |
| Urdu and Arabic | §2 item 8; cardinal letters stay N E S W (the existing ponytail note, `:379-381`, unchanged in this step); digits Western in both (`nu-latn` in Arabic; Urdu's default) |
| 1.5 text | the direction line may wrap; the key stays one row on 360dp; everything scrolls |
| 2.0 text at 320dp | "Turn right 118°" two lines; the dial 205dp; **the key becomes a `FlowRow`** (`Arrangement.spacedBy(18.dp)` across, `Space.Related` down), so "Where you are facing" drops under "The Kaaba" instead of clipping (today's `Row` at `:627` would clip, assumed from widths: "Where you are facing" at 30sp is about 290dp); Details wraps centred; the two lines wrap between words |
| 823dp | fits without scroll in the common states (§3) |
| Landscape, tablet | §3 |
| TalkBack | §2 item 9; the dial and the key stay cleared; the place name is a plain text node; the notice's "Set location" is spoken as a button |
| Remove animations | every value lands next frame; the dial follow still works (input driven) |

## 8. Strings

English; the main session translates ur, tr, in, ar; `qibla_subtitle` is deleted from all five.

```
qibla_details           Details
qibla_distance_km       About %1$d km from here
qibla_distance_miles    About %1$d miles from here
qibla_bearing           %1$d° from north
```

Reused: `qibla_title`, `qibla_needs_location`, `qibla_facing`, `qibla_heading_now`, `qibla_turn_left`,
`qibla_turn_right`, `qibla_no_compass`, `qibla_calibrate`, `qibla_sensor_missing`, `qibla_accuracy_medium`,
`qibla_legend_kaaba`, `qibla_legend_facing`, `notice_hide_details`, `state_expanded`, `state_collapsed`,
`action_set_location`, `location_makkah`. Not added: a "hide" twin of `qibla_details` (`notice_hide_details`
exists in five languages and means exactly this) and a spoken form for the place name (the name alone is enough;
the Details lines say "from here").

Translation notes: "miles" is a unit, not a prayer word; Arabic takes the singular after numbers from 11 ("نحو
%1$d ميلاً من هنا"), which is every distance to the Kaaba outside Makkah itself, so one `<string>` is right, as the
old "كم" line was; Urdu "یہاں سے تقریباً %1$d میل" and "شمال سے %1$d°"; Turkish "Buradan yaklaşık %1$d mil" and
"Kuzeyden %1$d°"; Indonesian "Sekitar %1$d mil dari sini" and "%1$d° dari utara". Checked: no "Tap", no "missed",
"late" or "owed", no hyphen or dash, sentence case, UK spelling.

## 9. Tests, with mutation proofs

- **`MeasurementSystemTest`** (core, new): `usesMiles` true for US, GB, LR, MM and lowercase "gb"; false for PK,
  TR, ID, SA, "" ; `milesFromKm(4794.0)` rounds to 2979; `milesFromKm(0.0)` is 0. **Mutation proof:** take GB out of
  the set, see the GB case fail, put it back.
- **`DesignRulesTest`** (exists): `QiblaScreen.kt` gains an inventory row (§10) or the inventory test fails (verified
  `:72-87`); the three new `animate*AsState` use `Motion.standard()` and `Motion.quick()`, the dial's one `tween`
  stays and a second would fail (`:179`); every new `Spacer` and top or bottom padding is a `Space` token.
  **Mutation proof:** write `Spacer(Modifier.height(12.dp))` once, see it fail, change it to `Space.Paragraph`.
- **`ColorContrastTest`** (exists): add four non text pairs at 3.0, `primary` on `surfaceVariant` and on
  `primaryContainer` in light and dark (the arc, needle and aligned rim on the two faces; today only `onSurface`
  and the gold are asserted, verified `:162-180`). **Mutation proof:** set the threshold to 20.0, watch all four
  fail, put 3.0 back. If the dark `primary` on `primaryContainer` pair fails at 3.0, that is a real finding for
  Ali, not a test to soften.
- **`TranslationIntegrityTest`** (exists): the four keys in five languages; `qibla_subtitle` absent from all five
  (grep `qibla_subtitle` in `app/src/main` returns nothing).
- **`LargeTextAndTalkBackDeviceTest`** (exists; `QiblaBody` becomes `internal` and takes a seeded `UiState`:
  `qiblaBearing = 118.0`, `qiblaDistanceKm = 4794.0`, `compassHeading = 60.0`, `compassAccuracy = HIGH`,
  `settings = AppSettings(cityName = "London")`): at 1.0 on 360dp the Details button's `touchBoundsInRoot` is at
  least 47.5dp both ways (the step 5 pattern, `:180-191`), its state description is "Collapsed", a click makes it
  "Expanded" and shows a node whose text ends "from north"; the direction node has a live region; the dial has no
  content description. At 2.0 on 320dp both key labels are displayed, whole, inside the root bounds.
- **By hand on the emulator** (main session): turning via Extended controls, Virtual sensors (yaw), including the
  arrival at 118°; `animator_duration_scale 0`; `:app:installRtl` judged for mirroring only; 1.5 and 2.0 at 823dp
  and 320dp; a seeded no compass state and the Makkah fallback (`useDefaultLocation`); the phone region set to
  en-US (miles) and en-GB (miles) and ur-PK (km) through the emulator's language and region.

## 10. DESIGN_SYSTEM changes

- §4 "Qibla dial" paragraph: rewrite (it still says "a fixed grey tick at the top" and 0.72; the needle is "you",
  the Kaaba sits at 0.60 of the radius, the rim is a band that thickens and turns green on arrival).
- §4.1 inventory: `Qibla | Details (down chevron, turns; "Hide details" open), centred under the place name | A,
  compact | 40 drawn, 48 target, wraps | QiblaScreen.kt` and `Qibla, no location | Set location, the notice's action
  | A (notice) | 48, wraps | Notice.kt`.
- §9: a dated entry: direction first; "Facing" kept on arrival and why; the dial's lift, rim band and rounded
  needle with the new budget; Details as a compact solid button with the Notice chevron; the list rule for miles
  and why not ICU; the Makkah fallback treated as no location; no sky on Qibla and why; the two notice slots and
  why. Rejected, as §1.
- §11: `1.6-qibla` in "Decided, work not yet done" becomes built when Ali says yes to the pictures.

## 11. iPhone effect, and the HIG check

**Carries over** (§6). Icons and their SF Symbols: `ExpandMore` → `chevron.down`; `LocationOn` → `mappin`;
`CheckCircle` → `checkmark.circle`; `WarningAmber` → `exclamationmark.triangle`; the tab's `Explore` is not this
step's. HIG: one instruction in the title's size, then the picture, then the detail (progressive disclosure, 4.1);
every target 48dp; text scales and the dial does not (Dynamic Type); the fold is a disclosure button, not a hidden
gesture; no FAB, no drawer; the look is the app's own dial and green, not a stock compass template (4.3).

## 12. Pictures for Ali

English, 823dp unless said: (1) light, turning ("Turn right 58°", the arc); (2) dark, turning; (3) light, aligned
(green line, tick, filled dial); (4) light, Details open; (5) dark, Details open; (6) Urdu, turning; (7) 2.0 text
at 320dp, light (the key in two rows); (8) no location, light (the notice). For `plan-verifier` and `reviewer`:
no compass, calibration notice, Makkah fallback, 1.5, landscape, tablet, TalkBack on Details, animator scale 0,
the phone region at en-US for miles.

## 13. Questions for Ali (from the pictures; my recommendation first)

1. Where the instruction sits, from pictures 1 and 3: **"At the start, under the title, like the city and date on
   Times (Recommended)"**, or "Centred over the dial". Why it is his: it changes how an approved screen reads at
   first glance (§0.2 item 1); both pass every rule.
2. The dial's finish, from pictures 1 and 2: **"Lifted like the cards, with the rim band (Recommended)"**, or "Flat
   face with the rim band only". Why it is his: §9 decided "thicker soft rim" in words; what soft looks like is a
   judgement from the picture.
3. No location, from picture 8: **"The amber notice with Set location, as on My prayers (Recommended)"**, or "The
   plain sentence, as today". Why it is his: it adds a door on this screen (§0.2 item 4), though it is the same
   door he approved on My prayers.

Not asked: dp values, the miles list, the notice slots and keeping "Facing" on arrival are decided from the rules
in §2 to §4 and said in this file.

## Verified in code, and assumed

Verified: `QiblaScreen.kt` in full (title and body `:85-183`; the buzz guard `:147-170`; `dialMaxWidth` `:195-196`;
the dial, rim in pixels, arc, ticks, letters, needle, Kaaba shares `:199-515`; `GuidanceText` and the hidden heading
`:537-599`; `DialLegend` as a `Row` `:609-657`; `AccuracyNotice` `:660-672`); `QiblaEngine.kt` (bearing, distance,
hysteresis, `relativeTurn`); `SajdaViewModel.kt:186-190` the four state fields, `:678-714` `recalculate()` computing
the Qibla from `settings.coordinates` with no fallback branch; `SettingsRepository.kt:152, 743-744` `usingDefaultLocation`
and `placeName`; `MainScaffold.kt:297, 440-446` `ReadingFrame`; `CtaButton.kt` (compact 40dp, 16dp padding);
`Notice.kt:184-229` the Details chevron pattern and strings; `MyPrayersScreen.kt:194-201` the no location notice;
`Motion.kt`, `Spacing.kt`, `ScreenFrame.kt`; `DesignRulesTest.kt:72-87, 170-183`; `ColorContrastTest.kt:150-180`;
`LocaleDisciplineTest.kt:171`; `LargeTextAndTalkBackDeviceTest.kt:180-191`; `strings.xml` Qibla keys `:282-297`,
`notice_hide_details` `:153`, `state_*` `:389-390`, `action_set_location` `:627`, `location_makkah` `:639`, and the
Urdu, Arabic, Turkish and Indonesian `qibla_subtitle` lines; `core/build.gradle.kts` keeps `:core` free of
`android.*` apart from the two named files; `screens/step5/checks/qibla-top.png` and `RESULTS.md:15`; `docs/ASSETS.md`
does not exist.

Assumed: `sajdaSurface` accepts a `CircleShape` and a colour as `Theme.kt:163-177`'s signature suggests (not read in
full); `useDefaultLocation()` in `SettingsRepository` stores Makkah's coordinates (inferred from `recalculate()`
producing times under the fallback); `HapticFeedbackType.Confirm`'s fallback below API 30 (not relied on: LongPress
stays); the line heights used in the 823dp sum (`Type.kt` not reread for this step); "Where you are facing" at 2.0
on 320dp overflows today's `Row` (from widths, not measured); the emulator's virtual yaw drives
`CompassRepository` as a real phone does (the S23 measurements in the code suggest the emulator never wobbles).

**Risks (Fable):** the dark `primary` on `primaryContainer` pair may fall under 3:1 and need a colour decision from
Ali; the new budget numbers are arithmetic until the first picture (if the Kaaba clips a letter or the needle tip
sits outside the cube on alignment, the shares are wrong, not the rule); a start aligned instruction above a centred
dial is the one thing in this file that could read as unbalanced, and Q1 exists for that.

## 14. Corrections from `plan-verifier` (24 Oct 2026). Where the body disagrees, this section wins.

Verifier answers: the ICU drop is safe; the Makkah fallback defect is real (`useDefaultLocation()` writes
`21.4224779, 39.8251832`, the same point as `QiblaEngine.KAABA`, so the screen says 0 km and "You are now facing the
Kaaba" whenever the phone points north); `Resources.getSystem().configuration.locales[0].country` is allowed and
API 24 safe (empty country gives km); `sajdaSurface(shape, color)` exists (`Theme.kt:163-177`, now verified).

1. **Arc (§4).** `0.11r` thick, inset `0.04r + thickness/2` (the rim is `0.04r` whenever the arc is drawn; the arc
   is never drawn aligned, `:273`), so the band runs 0.845 to 0.955, over the ticks as today, ticks drawn on top,
   clear of the letters at 0.77.
2. **Exit speed (§4, §7).** Face, rim colour and rim width use `Motion.standard()` when `aligned` becomes true and
   `Motion.quick()` when it becomes false (DESIGN_SYSTEM §2: a reversal is an exit):
   `if (aligned) Motion.standard() else Motion.quick()`.
3. **The no location door (§7, Q3).** `QiblaScreen` gains `onUseGps: () -> Unit, onSearchCity: (String) -> Unit`,
   passed at `MainScaffold.kt:297` as My prayers gets them at `:300`; it hosts `LocationSheet` with the same
   `rememberSaveable` flag and the `LaunchedEffect(current)` close as `MyPrayersScreen.kt:223-231`. This reopens
   DETAILED-PLAN 8.10 ("No location keeps today's single sentence, with no dial"), which is why it is Q3 for Ali.
   The Makkah fallback counts as no location either way: that is a defect fix, not taste.
4. **Strings (§8).** `qibla_details` is **not added**: `notice_show_details` already says "Details" in all five
   languages (main session, 24 Oct), as `notice_hide_details` is reused for the open label. The three other keys and
   the deletion of `qibla_subtitle` are in all five `strings.xml`; `QiblaScreen.kt:123` still references
   `qibla_subtitle`, so the strings are committed with the screen, not before it.
5. **Recomposition (§4 Rim row).** Rim width and rim colour are read inside the Canvas draw lambda; the face colour
   is a composition read through `sajdaSurface`, so the dial `Box` recomposes for the 250 ms of an arrival and at no
   other time. Accepted: `drawBehind` would lose the card lift.
6. **Kaaba corner (§4).** The corner reaches 0.765 at a cardinal bearing and 0.812 at a diagonal one, both under the
   ticks at 0.84; it overlaps the letter band and is drawn last, as today. Fix the comment at `:446-447` (its 0.822
   is the cardinal case; today's diagonal is 0.872). The watch keeps 0.66 (`WearApp.kt:897`), so the two dials now
   differ in Kaaba distance; rewrite the `WearApp.kt:887-892` comment that says the mark must not drift between them
   (comment only; no watch behaviour change).
7. **Contrast (§1, §9, Risks).** Computed from `Color.kt`: light `primary` on `surfaceVariant` 7.08, on
   `primaryContainer` 6.54; dark 8.05 and 6.49. All pass 3.0; the risk is withdrawn. The not aligned rim
   (`outlineVariant` on `surfaceVariant`) is 1.37 and 1.32, the face against the page 1.07 and 1.06, which is why
   the dial takes the `sajdaSurface` lift; neither pair is asserted.
8. **§4.1 Width row (§10)** gains: "A compact button may stand alone, centred, when it is a disclosure under a
   picture (Qibla Details; the welcome globe is the earlier case): a 52dp full width button under the dial would
   outweigh the thing it explains."
9. **Docs (§10)** also: HANDOVER §5 new rule (miles list and why not ICU; Makkah fallback is no location on Qibla;
   "Facing" kept on arrival), §10 evidence, §11 STATE OF PLAY; PROJECT_BRIEF; `docs/privacy.html` unchanged and said
   so (the region is read on device for a unit choice and goes nowhere); DESIGN_SYSTEM §4 legend sentence about the
   subtitle goes with it; the `QiblaScreen.kt:588-589` comment (why the heading was hidden on arrival) is rewritten.
10. **Test for the fixed defect (§9).** `LargeTextAndTalkBackDeviceTest` adds a seeded
    `AppSettings(usingDefaultLocation = true, cityName = "Makkah")` with `qiblaBearing = 0.0`,
    `qiblaDistanceKm = 0.0`: the `qibla_needs_location` node exists, the only click action is the notice's Set
    location, and no text ends "from north". **Mutation proof:** drop `|| usingDefaultLocation` from the guard, see
    it fail. `QiblaBody` reads `LocalContext.current` for `placeName(context)`.
11. **Arabic miles (§8).** "نحو %1$d ميل من هنا", the bare singular, right for every number ("ميلاً" is wrong after
    100, 300, 1000). Applied.
12. **§6 reason.** Also: `android.icu.util.LocaleData` is an `android.*` import, which `:core` forbids
    (`core/build.gradle.kts:9-14`), so 9.5's ICU branch could not have lived in `MeasurementSystem.kt` at all.
13. **§3 sum.** `bodyMedium` line height is 21, not 20 (`Type.kt:70-74`): about 716dp. Still fits.

## 15. The dial in the Pillars style, in our theme (Ali, 24 Oct 2026). Where this disagrees with §4 and §14 items 1 and 6, this section wins.

Ali: "I like pillar app qibla locator style ... I want that style of compass and needle but in my own theme." This
replaces §4's dial and the §1 rejection of a teardrop needle. Layout and style from `screens/step6/ref-pillars-site.jpg`
and `ref-pillars-dial.png`; nothing of theirs is copied (no artwork, no colour, no type). Everything else in §2, §3,
§5 to §14 stands; the one knock-on is the key's needle mark (15.6). Ali also asked (24 Oct): "Make sure when you point
correctly then message also appears and vibration happens": done in `f43d50a` (the buzz through the `Vibrator`).

**15.1 Geometry, as shares of the radius r, outer edge of the bezel at 1.0.**

| Part | Share | Notes |
|---|---|---|
| Bezel | 0.92 to 1.00 (0.08r) not aligned; 0.89 to 1.00 (0.11r) aligned, `animateFloatAsState` | The dial `Box` keeps `sajdaSurface(CircleShape, bezel)` (shadow in light, hairline in dark), so the bezel *is* the lifted disc. The Canvas draws the face as a filled circle of radius `r × (1 − bezelWidth)` in `face` over it. Nothing else is drawn in the bezel band |
| Ticks | 60, every 6°. Minor 0.845 to 0.88, width 0.007r. Cardinal (0, 90, 180, 270) 0.80 to 0.88, width 0.014r, `StrokeCap.Round` on both | Colour `outline` for all 60 (light `outline` on the face is 3.69:1). Widths as shares, not raw pixels |
| Letters | centre at 0.74, size 0.09r | E S W `onSurfaceVariant`, `FontWeight.Normal`; N `primary`, `FontWeight.Medium`. Upright, positions rotate, as today. Band 0.695 to 0.785 |
| Kaaba | `KAABA_DISTANCE` 0.70, `KAABA_SIZE` 0.22 | Box 0.59 to 0.81 at a cardinal bearing; worst corner 0.856 at a diagonal, inside the bezel at 0.92. Drawn last |
| Needle | teardrop, path in 15.2; tip at 0.64, round end centred at (0, +0.10r) radius 0.12r, bottom at +0.22r; filled and stroked `primary`, stroke 0.025r `StrokeJoin.Round` `StrokeCap.Round` | With the stroke the tip reaches 0.6525, inside the cube when the two coincide. No hub |
| Arc | none | 15.4 |
| Rotation | `tween(120)` | unchanged, exempt by name |
| Semantics | `clearAndSetSemantics {}` | unchanged |

One budget, five numbers: tip 0.6525, Kaaba 0.59 to 0.81 (0.856 diagonal), letters 0.695 to 0.785, ticks 0.80 to
0.88, bezel inner edge 0.92 (0.89 aligned). Move one and check the others.

**15.2 The needle path** (dial centre at the origin, y down the screen, all in r):

```
moveTo(0, -0.64)                                  // tip
cubicTo(-0.05, -0.38,  -0.12, -0.12,  -0.12, 0.10) // left flank, lands tangent to the round end
arcTo(Rect(-0.12, -0.02, 0.12, 0.22), startAngle 180, sweep -180)  // round end, through the bottom
cubicTo( 0.12, -0.12,   0.05, -0.38,   0, -0.64)   // right flank
close()
```

Draw filled, then stroked, both `primary`, as `drawNeedle` does today. The pivot stays the dial centre.

**15.3 Colour roles and the non text pairs** (computed from `Color.kt`).

| Element | Light | Dark | Pair asserted |
|---|---|---|---|
| Bezel, not aligned | `surface` | `surfaceContainerHighest` | not a boundary (the lift carries the edge); against face 1.13 / 1.23 |
| Bezel, aligned | `primary` | `primary` | `primary` on page 7.55 / 8.56, add at 3.0 |
| Face | `surfaceVariant` | `surfaceVariant` | unchanged |
| Face, aligned | `primaryContainer` | `primaryContainer` | unchanged |
| Ticks | `outline` | `outline` | on face 3.69 / 5.63; on aligned face 3.41 / 4.54; add four at 3.0 |
| Letters E S W | `onSurfaceVariant` | `onSurfaceVariant` | on aligned face 5.13 / 6.52, add two at 4.5 |
| N, needle | `primary` | `primary` | exists |
| Kaaba | `onSurface` + `kiswahGold()` | same | exists |

The bezel takes a different role per theme: read it through `dialBezel()` in `Theme.kt` beside `kiswahGold()`, same
`LocalDarkTheme` pattern, with a `dialBezel` row in both DESIGN_SYSTEM §1 tables. Dark fallback if too faint:
`surfaceBright`. Rejected: a hairline at the bezel's inner edge (1.37 / 1.32, neither clears 3:1 nor stays invisible).

**15.4 How far to turn: nothing on the dial.** The arc goes. "Turn right 58°" says it in words and a number; the
Kaaba's side of the dial says it without words. Arrival keeps four signals, two not colour: the sentence changes and
gains the tick, the face fills, the bezel turns green **and thickens**, one buzz. Q4 lets Ali keep the arc.

**15.5 Arrival and motion.** `face`, `bezel` (`dialBezel()` → `primary`) and `bezelWidth` (0.08 → 0.11) each
`animate*AsState` with `if (aligned) Motion.standard() else Motion.quick()`, labels `dialFace`, `dialBezel`,
`dialBezelWidth`. Needle, ticks, letters and Kaaba do not change on arrival. Buzz guard unchanged. Nothing animates on open.

**15.6 The key.** "Where you are facing" shows the same teardrop in miniature from the 15.2 path, `primary`.

**15.7 Picture fixes folded in.** Degree sign isolate: committed (`98564e7`). Pin and key marks sized from the text:
`bodyMedium.lineHeight` in dp (21dp at 1.0, about 34 at 2.0, since Compose scales large text non-linearly; measured 34.6dp) instead of `16.dp` / `18.dp`; the Kaaba and teardrop fill 0.75
of the box, the pin 0.8.

**15.8 Ours, not theirs (App Store 4.3).** (1) Our needle is *you*, fixed at the top with the dial turning under it,
and its tip ends at the Kaaba on arrival; (2) the Kaaba is our own flat elevation with the gold hizam (`ic_kaaba*.xml`,
`kiswahGold()`), not a tilted cube; (3) the arrival is ours: the sentence with the tick above the dial, the green face
and thickened green bezel, and a Details disclosure below. Plus our palette, the green N and the Quiet Glass lift.

**15.9 Dark, sizes, states.** All §7 states unchanged; no compass holds north up with the teardrop at the bearing.
320dp: dial 205dp, Kaaba 22dp. 2.0 text: the dial does not grow; the pin and key marks do.

**15.10 Code, tests, docs.** `QiblaScreen.kt`: delete `qiblaFlatDial` (Q2 superseded), keep `qiblaCentreInstruction`
until Q1; delete `drawTurnArc`, `rimWidth`, the hub; replace ticks, letters, needle, the two constants and the budget
comment. TEMPORARY `qiblaShowArc` (default false) for the Q4 picture only. `Theme.kt`: `dialBezel()`.
`ColorContrastTest`: the pairs in 15.3, mutation proof at 20. `DesignRulesTest`: no change. Device test: at 2.0 on
320dp the pin's bounds are at least 40dp tall. `WearApp.kt`: comment only. DESIGN_SYSTEM §1 `dialBezel`, §4 dial
paragraph, §9 dated entry (Ali's words, arc removed and why, teardrop reverses §1, per theme bezel).

**15.11 iPhone effect.** Carries over: `Path` `cubicTo`/`arcTo` maps one to one to SwiftUI `Path`; the bezel is a
`Color` asset with light and dark variants.

### 13 (rewritten after 15). Questions for Ali, from the new pictures, recommendation first

1. **Where the instruction sits:** "Under the title (Recommended)" or "Centred over the dial".
2. **Light bezel colour:** "White bezel, lifted (Recommended)" or "Sand bezel". (Replaces lifted or flat.)
3. **No location:** "Amber notice, Set location (Recommended)" or "Plain sentence, as today".
4. **The turn arc:** "No arc, Pillars style (Recommended)" or "Keep the green arc".

Answered 25 Oct 2026, see §17.

## 16. Corrections to §15 from `plan-verifier` (24 Oct 2026). This section wins over §15.

No blockers. Verified: 15.3 contrast numbers, the 15.2 path (Compose angles clockwise from 3 o'clock, y down:
`startAngle 180`, `sweep -180` runs through the bottom and ends at (0.12, 0.10), tangent joins), the tip inside the
cube, `dialBezel()` fits the `kiswahGold()` pattern, Motion and the exempt `tween(durationMillis = 120)`.

1. **Kaaba corner.** The box corner reaches 0.856 at a diagonal and crosses the minor ticks, but the drawn artwork
   (`ic_kaaba.xml` plinth) reaches 0.816 at worst, under the ticks and the aligned bezel at 0.89. Budget line: tip
   0.6525; Kaaba ink 0.59 to 0.816; letters 0.695 to 0.785; ticks 0.845 to 0.87 (cardinal from 0.80); bezel inner
   edge 0.89 aligned, 0.92 not.
2. **Ticks end at 0.87** (minor 0.845 to 0.87, cardinal 0.80 to 0.87), so the round cap ends 0.877, clear of the
   aligned bezel.
3. **The Kaaba covers a letter** when the Qibla is within about 15° of a cardinal (W from Pakistan and India, E from
   Morocco), as in the Pillars dial. Accepted: the Kaaba is the more useful mark there.
4. **`arcTo(Rect(-0.12f, -0.02f, 0.12f, 0.22f), 180f, -180f, forceMoveTo = false)`**; every coordinate is multiplied
   by r and offset by the dial centre before rotation about the centre.
5. `primary` on the page already exists in `ColorContrastTest` (`:111`, `:137` at 4.5); new pairs are the four
   `outline` and two `onSurfaceVariant` on `primaryContainer`.
6. §15 also overrides §14 item 5 (now the bezel colour is the composition read through `sajdaSurface`, the face and
   width are read in the draw lambda) and every mention of the arc or rim in §2, §7, §9, §12. DESIGN_SYSTEM §1
   `primary` row drops "and arc" in both tables.
7. **Q4 picture arc:** the bezel band itself, `primary`, from the needle round to the Kaaba, at the bezel's width;
   aligned, the whole bezel is green (15.5 unchanged). Behind TEMPORARY `qiblaShowArc` (default false).
8. **Q2 sand:** `surfaceContainerHighest` in light, behind TEMPORARY `qiblaSandBezel` (default false). Both new
   switches are deleted with `qiblaCentreInstruction` once Ali answers.
9. **Dark bezel:** the `sajdaSurface` hairline is invisible on either dark candidate, so the bezel carries the edge.
   The dark picture decides between `surfaceContainerHighest` and `surfaceBright` before the code is final (main
   session and Fable judge; take `surfaceBright` if the disc does not read against the page).
10. **Pin test:** `PlaceRow` icon gets `Modifier.testTag("qibla_place_pin")`; at 2.0 on 320dp, with §9's London seed,
    its bounds are at least 40dp tall. Mutation proof: put `16.dp` back, see it fail.
11. **`WearApp.kt:883-884` comment:** phone 0.70 and 0.22, watch keeps 0.66 and 0.30.
12. Arrival has three signals that are not colour (sentence with tick, bezel thickening, buzz), not two.
13. Keep the region read through `LocaleManagerCompat.getSystemLocales` (a7a39d4); §14 item 3's `Resources.getSystem()`
    is superseded. The buzz stays `arrivalBuzz` (f43d50a).

## 17. Ali's answers, 25 Oct 2026. This section wins over §13 to §16.

From the pictures in `screens/step6/pillars/`, Ali chose the recommendation each time: "Under the title
(Recommended)", "White ring, lifted (Recommended)", "Amber notice + Set location (Recommended)", "No arc, Pillars
style (Recommended)". The dark bezel is `surfaceContainerHighest` (Fable, from 02/04 against x-dark-bright-*: the disc
already reads against the page, so §16.9's condition for `surfaceBright` is not met). All four TEMPORARY switches
(`qiblaCentreInstruction`, `qiblaSandBezel`, `qiblaShowArc`, `qiblaDarkBezelBright`) are removed with the paths they
guarded.
