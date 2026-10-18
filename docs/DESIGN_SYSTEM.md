# SajdaTime design system

The tokens and component patterns the app is built from. `docs/HANDOVER.md` §7 is the
short version; this is the reference you check a colour against.

Every token here is a **Material 3 colour role**, set in
[`ui/theme/Color.kt`](../app/src/main/java/com/sajdatime/app/ui/theme/Color.kt) and wired
into the scheme in [`Theme.kt`](../app/src/main/java/com/sajdatime/app/ui/theme/Theme.kt).
Nothing in the UI hardcodes a hex value. If you find one, it is a bug.

---

## 1. Colour

### Dark

| Token | Role | Hex | Where it lands |
|---|---|---|---|
| background | `background`, `surfaceDim` | `#101312` | The page behind everything |
| surface | `surface`, `surfaceContainer` | `#171B1A` | Cards, list containers, nav bar |
| surfaceLow | `surfaceContainerLow` | `#141817` | The dimmed sunrise row |
| surfaceHigh | `surfaceContainerHigh` | `#1E2321` | Dialogs, bottom sheets, menus |
| surfaceHighest | `surfaceContainerHighest` | `#262C29` | The highest tonal step |
| surfaceHighlight | `primaryContainer` | `#143028` | Next-prayer card, next row, aligned dial |
| onSurfaceHighlight | `onPrimaryContainer` | `#8FD9B8` | Labels on that highlight |
| dialFace | `surfaceVariant` | `#161A19` | The Qibla dial face |
| primary | `primary` | `#4FC48F` | Accent bar, Qibla arrow and arc, "Next" pill |
| onPrimary | `onPrimary` | `#062117` | Text on a filled primary surface |
| navPill | `secondaryContainer` | `#1E4034` | The selected tab pill |
| onSurface | `onSurface` | `#E7EAE8` | Body text, prayer times |
| onSurfaceVariant | `onSurfaceVariant` | `#A9B2AE` | Secondary text, icons |
| outline | `outline` | `#8B948F` | Chevrons, tick marks, the facing tick |
| outlineVariant | `outlineVariant` | `#2A312E` | Card borders, row dividers |
| warning | `tertiary` | `#E8B14A` | The warning icon |
| warningSurface | `tertiaryContainer` | `#2A2113` | The warning banner |
| onWarningSurface | `onTertiaryContainer` | `#F3E4C5` | Text in that banner |
| kiswah | *(none — `kiswahGold()`)* | `#8A5807` | The Kaaba's hizam and door on the Qibla dial |

### Light

| Token | Role | Hex | Where it lands |
|---|---|---|---|
| background | `background` | `#F7F5F3` | The page behind everything |
| surface | `surface` | `#FDFCFB` | Cards, list containers |
| surfaceLow | `surfaceContainerLow` | `#FAF8F5` | The dimmed sunrise row |
| surfaceHigh | `surfaceContainerHigh` | `#EDEFE8` | Nav bar, dialogs, sheets |
| surfaceHighlight | `primaryContainer` | `#D7EAE0` | Next-prayer row, aligned dial |
| onSurfaceHighlight | `onPrimaryContainer` | `#0F2A1F` | Labels on that highlight |
| dialFace | `surfaceVariant` | `#EDEFE8` | The Qibla dial face |
| primary | `primary` | `#0B5A43` | Accent bar, Qibla arrow and arc, "Next" pill, every link and action (was `#0E6B4F` until 17 Oct 2026, see §9) |
| onPrimary | `onPrimary` | `#FFFFFF` | Text on a filled primary surface |
| navPill | `secondaryContainer` | `#CDE3D7` | The selected tab pill |
| onSurface | `onSurface` | `#1B211E` | Body text, prayer times |
| onSurfaceVariant | `onSurfaceVariant` | `#5A605C` | Secondary text, icons |
| outline | `outline` | `#757C77` | Chevrons, tick marks, the facing tick |
| outlineVariant | `outlineVariant` | `#C9CFC9` | Row dividers |
| warning | `tertiary` | `#9A6208` | The warning icon |
| warningSurface | `tertiaryContainer` | `#FBF1DC` | The warning banner |
| onWarningSurface | `onTertiaryContainer` | `#5A4408` | Text in that banner |
| kiswah | *(none — `kiswahGold()`)* | `#E8B14A` | The Kaaba's hizam and door on the Qibla dial |

### The light hero gradient

The next-prayer card in **light only** is a fixed three-stop gradient, mint at the top to
sand at the bottom: `#D6E8DC` → `#DDE2CE` → `#E4D2B4`. On it: countdown and prayer name
`#0A2419`, the section label `#2C4A3C`, the time and "until it begins" `#3E5C4C`.

Nine contrast assertions cover it — every ink against every stop. That is the whole reason
a gradient is permitted here: it is **fixed**. The gradient this replaced changed with the
prayer slot, so the contrast under the countdown was different at Isha than at Fajr and
could not be asserted at all. Fixed gradient, testable. Moving gradient, not.

In **dark** the same card is a flat `primaryContainer` with a hairline border — never a
gradient. That asymmetry is the design's and is deliberate.

### The rule that has bitten this project four times

**Set every Material 3 colour role, not just the obvious ones.** Dialogs, bottom sheets
and the navigation pill all rendered in Material's default **lilac** because
`surfaceContainer*`, the inverse roles and `secondaryContainer` were left unset. It then
happened a fourth time on the watch, which was using Wear's default scheme. If anything
looks purple, an unset role is the cause.

### One colour is depictive rather than semantic, and it is the only one

Every other hue here carries meaning: green is the identity, amber is a warning, and
nothing is decorative. **`kiswah` is the exception, and it is deliberate.** It is the gold
of the Kaaba's hizam and its door on the Qibla dial — not a role, not a state, just what
the object is, in the same way the silhouette is a cube and not a circle. It has no
Material slot; it is read through `kiswahGold()` in `Theme.kt`, which reads `LocalDarkTheme`
for the same reason `sajdaSurface` does.

**It is not the warning amber, and must not be aliased to it.** The two sit on the same hue
family, which is exactly why they are written out as separate values — restyling the
warning colour must never silently redress the Kaaba, and a mark on the Qibla dial must not
borrow the colour that means "the system is withholding something".

**The two themes need opposite golds.** The mark's silhouette is `onSurface`, which is
near-black in light and near-white in dark, so the gold drawn *on* it has to flip too:
`#E8B14A` on the light theme's black cube (8.4:1) and the much deeper `#8A5807` on the dark
theme's white cube (5.0:1). The bright gold on a white cube is 1.7:1 and effectively
invisible, which is the trap to avoid if these are ever retuned.

**Only one pair per theme has to hold, and getting that wrong is what delayed this.** The
band and door sit wholly inside the wall, so they are never drawn against the dial face. An
earlier version rejected gold on the grounds that it would need to clear AA against
`surfaceVariant` *and* `primaryContainer` in *both* themes — four pairs — and painted the
detail in the dial's face colour instead. That reasoning measured the wrong background, and
what it shipped was not a band but a slot cut through the cube, which made the mark read as
a shopfront. Contrast was never the problem: both themes were already about 14:1. See
`ic_kaaba_detail.xml`.

### Light and dark separate surfaces differently

**Light: a level-1 shadow, no border. Dark: a hairline `outlineVariant` border, no
shadow.** A shadow on a dark surface is invisible; a border in light adds a hard line the
design does not want. Both live in one place, `Modifier.sajdaSurface(shape, colour)` in
`Theme.kt`, rather than branching at every call site — a card that is invisible in one
theme is a silent failure, and silent failures should have exactly one place to go wrong.

That helper reads `LocalDarkTheme`, **not** `isSystemInDarkTheme()`. Once the user can
override the system setting, the system setting is no longer the answer.

### Contrast is enforced, not aspired to

`ColorContrastTest` asserts **WCAG 2.1 AA** for every foreground/background pair in both
themes — 4.5:1 for text, 3:1 for icons and boundaries under 1.4.11. **Edit a colour below
threshold and the build fails.** Add the pair to the test when you add a role.

> **Both halves of the source design overstated a contrast ratio, in the same place.**
>
> | Design token | Claimed | Actual | What was done |
> |---|---|---|---|
> | dark `outline` `#3A4441` | 3.1:1 | **1.85:1** | Became `outlineVariant`; `outline` took the design's facing-tick grey |
> | light `outline` `#C9CFC9` | 3.0:1 | **1.55:1** | Same treatment |
> | light `warning` `#B4740B` | 4.6:1 | **3.76:1** | Darkened on the same hue to `#9A6208`, the nearest value clearing 4.5:1 both ways |
> | light `primaryContainer` `#DDEDE4` | — | 1.18:1 vs surface | `#D7EAE0`, the nearest step clearing the highlight floor |
>
> Check the arithmetic, not the label. A design tool's ratio column is a claim, and every
> one of these was wrong in the direction that ships an unreadable app.

---

## 2. Spacing — 4dp base

`space1` 4 · `space2` 8 · `space3` 12 · `space4` 16 · `space5` 20 · `space6` 24 ·
`space8` 32 · `space10` 40.

Applied as: screen gutter **16dp** · card padding **20dp** · list row minimum **56dp** ·
tap target **48dp** · gap between sections **24dp**.

> ponytail: these are dp literals in the screens, not a `Spacing` object. An indirection
> whose every call site reads `space4` instead of `16.dp` buys nothing a grep cannot do,
> and Compose already has the unit. The scale is the contract; the constant is not.

---

## 3. Type

System font family — Roboto on Android, or whatever the user has chosen. No bundled
webfont: ~400KB on a charity app for no legibility gain, and it would override a font the
user may have picked for accessibility reasons.

`displayLarge` 52sp countdown · `headlineMedium` 26sp prayer name · `bodyLarge` 17sp rows ·
`bodyMedium` 15sp secondary · `labelMedium` 14sp section labels. **Nothing below 14sp.**

`PrayerTimeTextStyle` and the countdown use **tabular figures** (`tnum`) so digits do not
jitter as they change width.

> Tabular figures make the *Latin* digits an even width. They do not make every numbering
> system the same width as Latin. On a phone set to Arabic or Bengali the 52sp countdown
> was formatted with that language's digits, which are wider, and wrapped onto a second
> line — inside a card sized for the narrower ones. The app now formats numbers in the
> language its words are in (`docs/HANDOVER.md` §5.11), so this cannot happen today. It
> becomes live again the moment a translation into a language with its own digits ships:
> `displayLarge` on the next-prayer card is the first thing to re-measure.

Section labels (`PRAYER TIMES`, `NEXT PRAYER`) carry `letterSpacing` 1.4–1.6sp.

> They are **not** uppercased in code. `String.uppercase()` on a translated string is a
> trap — Turkish dotted and dotless i, and scripts with no case at all. A language that
> wants capitals gets them from its translator, in the string.

---

## 4. Components

**Next-prayer card** — `primaryContainer`, 24dp radius, 1dp `outlineVariant` border, flat.
Label, then name and time on one optical line, then the countdown, then "until it begins".

> It used to carry a time-of-day gradient — cool before dawn, warm at Maghrib. It was
> removed. The gradient shifted the background under the countdown as the day moved, so
> the one number the screen exists to show had a different contrast ratio at Isha than at
> Fajr. A single verified surface is testable; a moving one is not.

**Today list** — one card, 24dp radius, 1dp `outlineVariant` border, rows divided by
`outlineVariant` hairlines. Four row states:

| State | Treatment |
|---|---|
| next | `primaryContainer` fill · 3dp `primary` bar down the leading edge · "Next" pill in `primary`/`onPrimary` |
| current | `surfaceContainerHigh` fill · "Now" pill in `secondaryContainer`/`onSecondaryContainer` · **no** leading bar |
| sunrise | `surfaceContainerLow` fill · `onSurfaceVariant` text · one type step smaller |
| plain | transparent |

> Three signals for "next", not one. Colour alone answers "which prayer is next" for
> nobody on a greyscale display or a colour-blind palette, and that question is the entire
> reason the screen exists.
>
> **"Now" is deliberately the quieter of the two pills.** The screen answers two questions
> — what is coming, and what can I still pray — and they are not equally urgent to a person
> glancing at it. Filled versus tonal, bar versus no bar: the eye lands on "Next" first and
> finds "Now" a beat later, which is the order people actually want them in. Two filled
> pills a row apart were tried on paper and neither reads as the answer; the row stops
> having a focal point at all.
>
> The two can never appear on the same row — one time is in the future and the other has
> passed — so the treatments cannot stack and no tie-break is needed.
>
> `onSecondaryContainer`/`secondaryContainer` was chosen partly because `ColorContrastTest`
> already asserts that pair in both themes. A new colour would have meant a new assertion;
> an existing role meant the guarantee came for free.
>
> Sunrise is dimmed because **it is not a prayer**. It is on the list because people need
> it; it should not compete with the five that are. It also does something invisible: it is
> the moment Fajr's window shuts, and it is why there is no "Now" pill anywhere on the list
> between sunrise and Dhuhr. See `PrayerEngine.currentPrayer`.

**Qibla dial** — face in `surfaceVariant`, rim in `outlineVariant`, ticks every 15°, a
`primary` arrow at the true bearing, and a fixed grey tick at the top meaning *you are
pointing here*. Between them, a `primary` arc: **the turn you still owe**. Signed, so a
left turn sweeps anticlockwise and nobody is sent the long way round. Inside ±5° the arc
clears, the face fills with `primaryContainer`, the rim switches to `primary`, and the
wording changes to confirmation. Three independent signals again, only one of them colour.

**The Kaaba mark** sits at the far end of the arrow, at 0.72 of the dial radius, in
`onSurface` with its band and door painted in whatever the dial face currently is. Same
artwork on the phone and the watch — `core/res/drawable/ic_kaaba*.xml`, shared so one dial
drawn twice cannot end up as two different buildings.

> A bearing in degrees answers *which way* only for someone who thinks in bearings, and
> the turn instruction under the dial answers it only for someone who reads the language
> the app is in. Neither describes most of the people this is for. A picture of the
> building needs neither.
>
> It stays upright at every bearing rather than rotating with the dial. Rotating it is the
> obvious build — one more `rotate` block, no trigonometry — and it turns the Kaaba upside
> down whenever the Qibla is behind you, at which point it stops being recognised and goes
> back to being a shape.
>
> The band and door are painted over an opaque silhouette, not cut out of it. Cut-outs
> were built first, reviewed clean, and were wrong: a hole shows what is *behind* the
> mark, and what is behind the mark is the needle, so the doorway filled with green.

A legend names the two abstract marks. The Kaaba is not in it, because a legend explains
shapes and this one is a picture; the subtitle above the dial already says the word.

**Notices** (one component, `ui/components/Notice.kt`, since 17 Oct 2026). Every message that
is not a setting goes through it, so a person learns one shape and reads every notice the same
way. Two tones only: **warning** (`tertiaryContainer`, amber: something is wrong or withheld)
and **quiet** (`secondaryContainer`, soft green: information or an occasion, Ramadan and Eid).
Anatomy, in order: a round mark (the icon on a 14% tile of the tone's accent); the title; **one
brief sentence**; the full explanation folded behind a chevron when there is more to say
("expanded"/"collapsed" spoken); a footnote that must stay visible (Eid's "follow your mosque");
the action as a **bold link with an arrow** ("Allow", "See why", "Change location", "Shift the
date"), never "Tap to…"; a close button on Times only.

> Why: the owner found the old notices bland and the full width amber button unclean, and asked
> for a brief accordion "applied systemically", not to one card. The old exact alarm card was
> four lines of explanation and a button that repeated its title; closed, it is now two lines
> and a link, and Times fits all six prayers with it showing. The card never does the action
> itself: opening and closing must never be the same gesture.

On **home** a notice may carry a close button and stays closed once tapped. On **Settings** the
same notice has no close button and never hides while the permission is missing.

> A warning that cannot be put away is not a warning, it is furniture. Closable on home,
> permanent in Settings: it is said once, and the way to fix it is still exactly where someone
> would go looking for it. Amber, not red: prayer alerts still arrive, just at the mercy of the
> scheduler, and an error colour would say the app is broken when it is not.

**Home screen widgets** (hero countdown, 18 Oct 2026; research in
`docs/plans/2026-10-widget-redesign/RESEARCH.md`). Two widgets, each with two layouts the launcher
chooses between by size on Android 12 and later (`WidgetRenderer.NEXT_SIZES`, `todaySizes`):

| Widget | Layout | Shows |
|---|---|---|
| Next prayer | small (default, and the only one below Android 12) | glyph, name, time; the countdown at the foot; the day's five bars |
| Next prayer | wide (from 270 by 150dp) | the small widget's countdown beside the six rows |
| Today | compact (from 250 by 180dp) | the six rows with glyphs, weighted to fill |
| Today | large (from 250 by 280dp, 330dp in Urdu and Arabic; placed at 4 by 4) | mark, full date and Hijri date; the countdown card; the six rows |

- **The countdown is the hero**: system sans in the light weight, tabular figures, 34 to 38dp, sized
  in dp because it is already three times the body text and a large font setting pushed the prayer
  name off a 110dp widget. Names and times stay in sp.
- **Prayer glyphs show where the sun is**, so they read without the language: Fajr rays above an empty
  horizon, Sunrise a rising half sun, Dhuhr the sun over a post with no shadow, Asr a post whose shadow
  equals its height (the Asr rule), Maghrib a setting half sun, Isha a crescent and star. One Asr glyph
  for every madhab. Drawn white, tinted in the layout from a widget colour (never from code).
- **The day's five bars** (Sunrise is not a prayer): done in `primary`, next outlined in `primary`, to
  come in `outlineVariant`. Shape as well as fill separates next from done.
- **The mark** (launcher mihrab and minarets, gold on green, written as the icon's own hex) on the
  large Today widget and its empty state only. Never on a small widget.
- **Glossy finish** (owner, 18 Oct 2026, "Apple premium style"): the surface is a FIXED three stop
  gradient (lighter at the top), a white sheen over the top half that fades out (7 percent in dark,
  none in light, where the top stop is already white), and a rim (16 percent white in dark, the
  hairline in light). The countdown card has the same treatment in the highlight colour. Opaque on
  purpose. `WidgetColorsTest` holds every ink to AA on every stop and on the top stop under the
  sheen; it caught the first dark card top (#1C4034, 4.30:1) and set it to #18382D.
- **The location chip**: a highlight pill with a pin, the town and an update arrow, on every widget
  (under the countdown on Next, at the end of the large Today header, at the foot of the compact
  Today). It is its own TalkBack button ("Update location, London"). Pressing it opens the app, and
  opening the app is what updates the location (onResume); it cannot update without opening the app,
  because location is read only in the foreground. The large header uses the short date ("Sun, 18
  Oct") so the chip never cuts it short.
- **The town the times are for** on every widget (the owner's request, 18 Oct 2026): under the countdown on
  the small Next widget (above it on the wide one), at the end of the large Today header, in the compact Today footer. Only the
  town ("London", not "London, United Kingdom"); Makkah in the app's language when it is the fallback.
  1.4.0 had none because a home screen is seen by others; the owner decided "times for where?" matters
  more.

**Settings groups** (Quiet Glass, 17 Oct 2026): a sentence case `onSurfaceVariant` title
(labelLarge, medium), then the rows inside one `sajdaSurface` card, 24dp radius, 16dp gutter.
Each row: a 36dp `secondaryContainer` tile with a 20dp `onSecondaryContainer` icon, a
titleMedium title over a bodyMedium `onSurfaceVariant` description, hairline dividers inset to
the text (66dp). Information-only rows (charity, credits, data) fold behind their title.
Code: `ui/settings/SettingsList.kt`.

## 5. Choosing a theme

`ThemeChoice` is `SYSTEM` (default), `LIGHT` or `DARK`, persisted in DataStore under
`theme_choice`, and shown as a sliding three way selector under **Settings → Appearance**
(three chips until 17 Oct 2026; they read as filters rather than one choice).

`SYSTEM` reads `isSystemInDarkTheme()`, which reports **false** when the OS expresses no
preference — on older devices, and on any device where the user has never chosen. So
following the phone resolves to light, and light is the effective default.

A selector rather than a chooser dialog, because unlike every other setting on that screen the
result is visible the instant it is tapped, and a dialog would sit on top of the change it
just made. They are `selectable` with `Role.RadioButton`, not `clickable`, so a screen
reader announces the active one as selected and treats the three as one choice.

> Known gap: `android:windowBackground` is a resource, resolved by the system from the
> **system's** night setting before any of the app's code runs. A user who has chosen Dark
> on a light phone therefore sees one light frame at cold start. Fixing it means writing
> the window background from the activity once settings load, which trades a one-frame
> flash for a one-frame flash in the other direction. Left alone deliberately.

---

## 6. Wear OS

**The watch does not follow the phone's theme choice.** It has one scheme and always will:
a wrist display is glanced at, and the correct thing to draw on an OLED panel at a glance
is white on black regardless of what the phone is set to. The `ThemeChoice` setting is a
phone setting.

Same tokens, one deliberate difference: the background is **pure black** `#000000`, not the
phone's `#101312`. Every Wear device ships an OLED panel, where black pixels are switched
off — the deepest contrast available and the cheapest thing to draw on a battery that size.

The palette is duplicated by hand in
[`WearTheme.kt`](../wear/src/main/java/com/sajdatime/wear/WearTheme.kt). The two modules
**cannot** share a Compose theme: Wear Compose has its own `ColorScheme` with a different
set of roles. A colour changed on the phone has to be changed there too, and a watch that
looks like a different app is the symptom of forgetting.

Layout rules: one job per screen · 12sp floor · everything inside a 10% circular inset so
nothing clips on a round display.

---

## 7. Where this came from

Both palettes and all the component treatments are from designs produced in Claude Design,
applied in the "dark design system" and "light design system" commits. What was taken: the tokens, the
row states, the dial arc, the banner treatment, the group cards. What was **not** taken:
anything that would have added a feature the app does not have. The design was drawn
against the shipping feature set on purpose — no day stepper, no per-row mute, no tracker —
and it should stay that way. (The Ramadan card and the widgets reuse these tokens and add none; the
widget glyphs and the mark are drawings, not colours. A prayer tracker, if it is ever built, is its own design pass.)

One thing in the light design was **not** adopted: it sets the countdown at 46sp in light
and 52sp in dark, on the argument that dark text on a pale background carries more optical
weight. That is a fair argument, and acting on it means a whole second `Typography` that
exists to change one number by 6sp. Not worth the machinery. Noted here so the next
session knows it was considered rather than missed.

---

## 8. Quiet Glass (17 Oct 2026)

The owner chose "Quiet Glass" from three researched directions (research, mockups and the
rejected options: `docs/HANDOVER.md` §11, the Quiet Glass block). The rules it adds:

**Glass is for what floats, never for content.** The tab bar floats over the screens as a
pill, `surface` at `GlassBarAlpha` (90%), so what scrolls underneath shows through faintly.
Prayer times, cards and every line of text stay on solid surfaces. This is Apple's own rule for
Liquid Glass, and the usability research behind it is in the HANDOVER block. The bar's labels
are tested against pure black and pure white behind it (`ColorContrastTest`).

> ponytail: translucency without blur, on every Android version. Real backdrop blur needs
> Android 12+ and the Haze library; at 90% opacity the visible difference is small. Add it only
> if the owner asks for more glass, and keep the current bar as the Android 7 to 11 fallback.

**Every tab opens the same way.** `ScreenTitle` (headlineMedium, start aligned, 20dp gutter)
on Qibla and Settings; on Times the title is the city, same size and line, with a green caret
because it opens the location sheet. Every scrolling screen ends with `BottomBarClearance()`,
which is the bar's measured height, so it grows with large text.

**Links and actions are bold and green.** `labelLarge` is SemiBold, and Material draws every
button and text button label in it, so "Save as PDF" and "Got it" read as tappable without a
guess. Rows that open something carry a chevron.

**Green means something.** It is the colour of Paradise in the Qur'an, and here it marks the
next prayer, the Qibla and anything you can tap. It is never decoration.

**Times fits without scrolling** on a phone at the default text size: city, next prayer, all
six times and the mosque button. Measured 17 Oct 2026 on 914dp (fits, including the far north
notice) and on 823dp, the owner's S23 Ultra (all six times fit; the far north notice needs a
short scroll). Large text and the rare banners are allowed to scroll; they never clip.

**Words.** Plain, short, human and accurate; no hyphens or em dashes in app text, except
where a name requires one (ir-Rahman, Eid al-Fitr, Rabi al-Awwal, Open-Meteo). Religious names
are never "simplified". The disclaimer is never softened.

**Icons.** The selected tab uses the Rounded (filled) set, the rest the Outlined set, from the
already installed `material-icons-extended`. Google has stopped maintaining that library;
moving to Material Symbols Rounded vector drawables is the planned next step (it removes a
dependency), not done yet because it means downloading the icon files.

---

## 9. Decision log

Dated, newest first. Each entry says what was chosen, what was rejected and why, so nobody
"improves" it back.

- **18 Oct 2026, widgets made glossy, at the owner's request** ("a bit glossy background like apple
  premium style"). This overrides, for the widgets only, the 17 Oct rejection of a shiny finish below.
  Built within the rules that made the rejection sensible: a fixed gradient (testable), no colour
  that moves with the time of day, opaque. Rejected: real translucency (text over an unknown photo
  wallpaper cannot be contrast tested, and a RemoteViews cannot blur the backdrop); a "Tap to update"
  sentence (DESIGN_SYSTEM 4 bans "Tap to" wording, and a pill with a pin and an arrow says it in every
  language); a background location update without opening the app (needs background location,
  against the foreground only rule and Play policy).
- **18 Oct 2026, widgets: hero countdown chosen** by the owner from three drawn directions (A hero
  countdown, B ring and sun path, C timeline strip). Rejected: B's sun arc (a bitmap drawn per refresh
  bakes in colours that go stale when the phone changes theme, lesson 130); C as the main design (names
  too small at 4 by 2); the mark on every widget (costs a 2 by 2 its prayer name, against Apple's
  widget guidance); a time of day colour behind the countdown (§4, untestable contrast); a bundled
  display font (§3); "4h 59m" wording (needs a redraw every minute; the Chronometer ticks for free).
- **17 Oct 2026, Quiet Glass chosen.** Rejected: *Refined Calm* (no glass; the owner wanted
  the premium feel), *Night & Emerald* (glass and five changing colours behind the countdown,
  against Apple's guidance and the readability research; a second brand colour). Also
  rejected: a shiny "glossy" finish (dates fast), Material 3 Expressive components (need
  material3 1.5.0, still beta), a time of day gradient (removed once already because contrast
  under the countdown could not be tested).
- **17 Oct 2026, primary deepened to `#0B5A43`.** 7.5:1 on the page, up from 6.0:1. Green kept
  as the identity on purpose. The widgets moved with it (`WidgetColorsTest` holds them equal).
  Not moved: launcher icon and PDF logo (`#0E6B4F`), to be moved together in an icon refresh.
- **17 Oct 2026, one Notice component for every notice** (exact alarms, Do Not Disturb, no
  location, far north, mosque difference, compass accuracy, Ramadan eve, Eid). Rejected: fixing
  only the exact alarm card (the owner: "applied in a systemic design system"); a whole-card tap
  for the action (opening and closing would share a gesture); folding Eid's fine print (it says
  follow your mosque, so it stays visible as a footnote).
- **17 Oct 2026, Settings "option B" chosen** over a no-card list and a green summary card.
- **17 Oct 2026, section titles and the next prayer label in sentence case** in English,
  Turkish and Indonesian (Urdu has no case). Written that way in the strings, never cased in
  code.

- **17 Oct 2026, research pass (FINDINGS.md):** granted permissions show nothing on Settings;
  every card is 24dp, notices included; dialog section labels speak in the Settings section
  voice. Kept against the research: the iOS style chevron (the owner asked for an iPhone feel).
  Rating is Google's card only, never a component of ours.

---

## 10. The learning loop

A design system that only lists colours decays. Every visual change goes round this loop, and
each lesson ends as a rule here or a test that fails the build.

1. **Observe.** Play reviews (the daily health check watches them), the owner's two phones,
   opt in screen counts, user emails.
2. **Propose.** A preview, never straight into the shipping screen: a branch, emulator
   screenshots, or a debug only switch as Settings was done.
3. **Verify, automatically where possible.** `ColorContrastTest` for every pair, including the
   glass bar over its worst backdrop. Screenshots in light, dark, 2.0 text, Urdu right to left
   and on a short (823dp) screen. No hex outside `ui/theme`.
4. **Decide.** The owner chooses from screenshots. Silence is never a yes.
5. **Record.** An entry in §9, with what was rejected and why.
6. **Enforce.** Turn the lesson into a test or into one of the review questions below.

**Review questions** (the owner's, 17 Oct 2026), answered with a screenshot for every change:
Is the text easier to read? Is the hierarchy clearer? Is the type consistent? Is the spacing
comfortable? Can someone understand the screen quickly? Are the controls obvious? Are
unnecessary taps avoided? Is information grouped logically? Does it look modern rather than
dated? Is the layout balanced? Are colours and components consistent? Has clutter been removed?
Does it work at large text? Is contrast sufficient? Are touch targets comfortable? Does it read
in Urdu right to left?
