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
| primary | `primary` | `#0E6B4F` | Accent bar, Qibla arrow and arc, "Next" pill |
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

**Warning banner** — `tertiaryContainer` fill, `tertiary` icon and border,
`onTertiaryContainer` text, 16dp radius. Used for "the system is withholding something you
asked for": exact alarms, Do Not Disturb access, compass calibration.

On **home** the exact-alarm banner carries a close button (`Outlined.Close`,
`onTertiaryContainer`, 24dp glyph in a 44dp target) and stays closed once tapped. On
**Settings** the same banner has no close button and never hides while the permission is
missing.

> A warning that cannot be put away is not a warning, it is furniture. Shown on every
> launch it stops being read — and worse, people learn to skip that whole band of the
> screen, which is where the Makkah notice lives too, and *that* one they need to act on.
> Closable on home, permanent in Settings: it is said once, and the way to fix it is still
> exactly where someone would go looking for it.
>
> The close button has its own `clickable` inside the card's `clickable`. The card opens
> the system permission screen; the button closes the card. Two different outcomes must
> never share one gesture. 44dp because a bare 24dp glyph is a target only someone with a
> steady hand and good eyes can hit.

> Amber, not red. Prayer alerts still arrive; they are just at the mercy of the scheduler.
> An error colour would say the app is broken when it is not. And it is not grey any more,
> because as one more grey card in a stack of grey cards the exact-alarm notice went
> unread.

**Settings groups** — a letterspaced `primary` label, then the rows inside one card with a
1dp `outlineVariant` border, 16dp gutter, 20dp radius.

---

## 5. Choosing a theme

`ThemeChoice` is `SYSTEM` (default), `LIGHT` or `DARK`, persisted in DataStore under
`theme_choice`, and shown as three chips under **Settings → Appearance**.

`SYSTEM` reads `isSystemInDarkTheme()`, which reports **false** when the OS expresses no
preference — on older devices, and on any device where the user has never chosen. So
following the phone resolves to light, and light is the effective default.

Chips rather than a chooser dialog, because unlike every other setting on that screen the
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
and it should stay that way.

One thing in the light design was **not** adopted: it sets the countdown at 46sp in light
and 52sp in dark, on the argument that dark text on a pale background carries more optical
weight. That is a fair argument, and acting on it means a whole second `Typography` that
exists to change one number by 6sp. Not worth the machinery. Noted here so the next
session knows it was considered rather than missed.
