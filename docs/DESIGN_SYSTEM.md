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

### Light

Unchanged from 1.0. Deep green primary `#14624B`, warm gold accent `#8A5200`, paper
background `#FBFAF7`, white surfaces. The one addition is the warning banner:
`tertiaryContainer` `#FBEEDA` with `onTertiaryContainer` `#3A2A00`.

### The rule that has bitten this project four times

**Set every Material 3 colour role, not just the obvious ones.** Dialogs, bottom sheets
and the navigation pill all rendered in Material's default **lilac** because
`surfaceContainer*`, the inverse roles and `secondaryContainer` were left unset. It then
happened a fourth time on the watch, which was using Wear's default scheme. If anything
looks purple, an unset role is the cause.

### Contrast is enforced, not aspired to

`ColorContrastTest` asserts **WCAG 2.1 AA** for every foreground/background pair in both
themes — 4.5:1 for text, 3:1 for icons and boundaries under 1.4.11. **Edit a colour below
threshold and the build fails.** Add the pair to the test when you add a role.

> The design document this palette came from listed its `outline` (`#3A4441`) at 3.1:1.
> It is **1.85:1** against its own background. A colour that is only ever a hairline card
> border can get away with that; Material spends `outline` on chevrons and tick marks,
> which cannot. So `#3A4441` became `outlineVariant` (decorative) and `outline` took the
> grey the design uses for its facing tick. Check the arithmetic, not the label.

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
`outlineVariant` hairlines. Three row states:

| State | Treatment |
|---|---|
| next | `primaryContainer` fill · 3dp `primary` bar down the leading edge · "Next" pill in `primary`/`onPrimary` |
| sunrise | `surfaceContainerLow` fill · `onSurfaceVariant` text · one type step smaller |
| plain | transparent |

> Three signals for "next", not one. Colour alone answers "which prayer is next" for
> nobody on a greyscale display or a colour-blind palette, and that question is the entire
> reason the screen exists.
>
> Sunrise is dimmed because **it is not a prayer**. It is on the list because people need
> it; it should not compete with the five that are.

**Qibla dial** — face in `surfaceVariant`, rim in `outlineVariant`, ticks every 15°, a
`primary` arrow at the true bearing, and a fixed grey tick at the top meaning *you are
pointing here*. Between them, a `primary` arc: **the turn you still owe**. Signed, so a
left turn sweeps anticlockwise and nobody is sent the long way round. Inside ±5° the arc
clears, the face fills with `primaryContainer`, the rim switches to `primary`, and the
wording changes to confirmation. Three independent signals again, only one of them colour.

A legend names the two marks, because otherwise they are just shapes.

**Warning banner** — `tertiaryContainer` fill, `tertiary` icon and border,
`onTertiaryContainer` text, 16dp radius. Used for "the system is withholding something you
asked for": exact alarms, Do Not Disturb access, compass calibration.

> Amber, not red. Prayer alerts still arrive; they are just at the mercy of the scheduler.
> An error colour would say the app is broken when it is not. And it is not grey any more,
> because as one more grey card in a stack of grey cards the exact-alarm notice went
> unread.

**Settings groups** — a letterspaced `primary` label, then the rows inside one card with a
1dp `outlineVariant` border, 16dp gutter, 20dp radius.

---

## 5. Wear OS

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

## 6. Where this came from

The dark palette and the component treatments are from a design produced in Claude Design
and applied in the "dark theme and design system" commit. What was taken: the tokens, the
row states, the dial arc, the banner treatment, the group cards. What was **not** taken:
anything that would have added a feature the app does not have. The design was drawn
against the shipping feature set on purpose — no day stepper, no per-row mute, no tracker —
and it should stay that way.
