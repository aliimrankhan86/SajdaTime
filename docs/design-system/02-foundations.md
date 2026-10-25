# 2. Foundations

Colour, type, spacing, layout, shape, elevation, motion, haptics and icons. The exact values live in
`tokens/*.json` and are tested against the code; this page says **when and why** to use each. Every
hex written on this page must exist in `tokens/color.json` (`DesignTokensTest` checks), so a value
quoted here cannot go stale silently.

---

## 2.1 Colour

### How colour is organised

| Layer | What | Where |
|---|---|---|
| Roles | 32 Material 3 roles, each set in both themes | `color.json` → `roles`; `Color.kt`; wired in `Theme.kt` |
| Brand | The call to action fill, the Kaaba gold | `color.json` → `brand` |
| Gradients | The hero card, the warning gloss, the four skies | `color.json` → `gradients` |
| Surfaces outside the app | Widgets, the watch, the watch tile | `color.json` → `widget`, `wear`, `wearTile` |

**Rule 1: set every Material role.** Material falls back to lilac for any role left unset. It has
happened four times (dialogs, sheets, the tab pill, the watch). If anything looks purple, an unset role
is the cause. `DesignTokensTest` now fails if the doc and `Theme.kt` disagree on any role.

**Rule 2: read roles, never hex.** Composables use `MaterialTheme.colorScheme.X` or a function in
`Theme.kt` (`heroStyle()`, `warningGloss()`, `skyStyle()`, `kiswahGold()`, `dialBezel()`,
`ctaButtonColors()`). A hex in a composable is a bug.

**Rule 3: read the theme from `LocalDarkTheme`**, never `isSystemInDarkTheme()`. The user can override
the phone.

### What each colour means

| Meaning | Role | Light | Dark | Never |
|---|---|---|---|---|
| Identity: the next prayer, the Qibla, tappable | `primary` | `#0B5A43` | `#4FC48F` | Decoration, washes, large fills behind white text in dark |
| Solid button fill | `brand.cta` | `#0B5A43` | `#17805A` | Anything that is not an action |
| Button label | `brand.onCta` | `#FFFFFF` | `#FFFFFF` | Regular weight |
| Highlight (next row, aligned dial) | `primaryContainer` | `#D7EAE0` | `#143028` | A whole screen background |
| Selected and quiet (tab pill, Now pill, quiet notice, Settings icon tile) | `secondaryContainer` | `#CDE3D7` | `#1E4034` | Warnings |
| Withheld, warning | `tertiary` (`accent`) | `#9A6208` | `#E8B14A` | Errors, the Kaaba, decoration |
| Warning panel | `tertiaryContainer` | `#FBF1DC` | `#2A2113` | Information (use quiet) |
| Page | `background` | `#F7F5F3` | `#101312` | |
| Card, list, tab bar | `surface` | `#FDFCFB` | `#171B1A` | |
| Dialog, sheet | `surfaceContainerHigh` | `#EDEFE8` | `#1E2321` | |
| Main text | `onSurface` | `#1B211E` | `#E7EAE8` | |
| Secondary text, icons | `onSurfaceVariant` | `#5A605C` | `#A9B2AE` | |
| Chevrons, ticks, meaningful edges (3:1) | `outline` | `#757C77` | `#8B948F` | |
| Dividers, dark card edge (decorative, below 3:1) | `outlineVariant` | `#C9CFC9` | `#2A312E` | Icons, anything that must be seen |
| Snackbar | `inverseSurface`, `inverseOnSurface`, `inversePrimary` | | | |

Amber, not red, for warnings: alerts still arrive, and red would say the app is broken. Dark amber
button text is `#3A2A00`, not white (white on `#E8B14A` fails at about 1.9:1), the one exception to
white button text.

### Light and dark are designed separately, not inverted

| | Light | Dark |
|---|---|---|
| Surfaces | Warm paper neutrals | Near neutral, not green tinted (a tint reads as a colour cast on OLED) |
| Card separation | 2dp shadow, no border | 1dp `outlineVariant` hairline, no shadow |
| Primary | Deep emerald, 7.5:1 on the page | Brighter green, cannot carry white text, so buttons use `#17805A` |
| Hero card | Mint to sand gradient, dark inks, green glow | Green gradient, light inks, no glow |
| Qibla bezel | `surface`, lifted by the shadow | `surfaceContainerHighest`, carries its own edge |
| Kaaba gold | Bright `#E8B14A` on the black cube | Deep `#8A5807` on the white cube |

All of this lives in `Modifier.sajdaSurface()` and the functions in `Theme.kt`, so no call site
branches on theme. Default is **System**, which resolves to light when the phone has no preference.

### Gradients: fixed only

A gradient is allowed only when its stops are **fixed**, because then every ink on every stop can be
asserted in `ColorContrastTest`. The old time of day card was removed because its contrast could not
be tested.

| Gradient | Where | Stops (top to bottom) | Finish |
|---|---|---|---|
| Hero, light | Next prayer card | `#D6E8DC` → `#DDE2CE` → `#E4D2B4` | Sheen `#FFFFFF99` over the top half, rim `#FFFFFFCC` to `#FFFFFF33`, glow `#0B5A4359` 12dp |
| Hero, dark | Next prayer card | `#18382D` → `primaryContainer` → `#0F241D` | Sheen `#FFFFFF12`, rim `#FFFFFF29` to `#FFFFFF0A`, no glow |
| Warning gloss | Warning notices | light `#FBF0D8` → `tertiaryContainer` → `#F3E2BE`; dark `#352A17` → `tertiaryContainer` → `#20190D` | The hero's sheen and rim. Quiet notices stay flat |
| Sky | Top of Times, Next prayer widget | Four periods × two themes, see below | Fades into the page |

### Depictive colour: two families, nowhere else

Depictive colours show **what something is**, not a state. They have no role and never carry meaning.

**The Kaaba gold** (`brand.kiswah`): the hizam and door on the Qibla dial's Kaaba, drawn only on the
`onSurface` silhouette. Never aliased to the warning amber, though the hue family is shared: restyling
warnings must never redress the Kaaba.

**The four skies** (`gradients.sky`): the top of Times shows the time of day. Period from
`Sky.periodOf` in `:core`: dawn (Fajr to sunrise), day (sunrise to Asr), evening (Asr to Isha), night
(Isha to Fajr); no times means day. Four fixed skies, never a continuous blend.

| Sky | Light top, mid | Dark top, mid |
|---|---|---|
| Dawn | `#EADFE3`, `#F0E9EC` | `#231E2C`, `#171520` |
| Day | `#D6E8DC`, `#E6EFE9` | `#17323A`, `#13262B` |
| Evening | `#ECD8BB`, `#F1E5D3` | `#262A1E`, `#181B15` |
| Night | `#D0DBE1`, `#DFE6EA` | `#0B1730`, `#0C1320` |

Foot of every sky = the page background. Only the city (`onSurface`), the Gregorian date, arc and dots
(`onSurfaceVariant`), and the Hijri date, caret, glyph and passed arc (`primary`) may sit on a sky.
Never a prayer time. The light night top is the thin one (4.57:1 under `onSurfaceVariant`): do not
darken it. Dark night is told by hue (1.05:1 against the page), kept by the owner on 24 Oct 2026.

### Colours that deliberately differ

| Where | Value | Why |
|---|---|---|
| Launcher icon, PDF logo, widget mark, store icon | `#0E6B4F` green with `#F0D69A` sand | The icon is the Play listing's face; it moves with an icon refresh, not as a side effect of `primary` moving to `#0B5A43` |
| Watch background | `#000000` | OLED pixels switch off |
| Watch tile | See `wearTile` | **Known gap**: three colours that do not match the palette. Held by test; aligning them is the owner's call |
| Android 7 launcher icon and the watch launcher icon | `#14624B` | **Known gap**: the older green. Not in the token file because it is an icon asset, not a theme colour |

### Contrast is enforced

`ColorContrastTest` holds every pair to WCAG 2.1 AA in both themes: 4.5:1 for text, 3:1 for icons and
meaningful boundaries (1.4.11). Light button: white on `#0B5A43` is 8.2:1; dark: white on `#17805A` is
4.9:1. A new colour pair is not finished until it is in that test. Check the arithmetic, never a design
tool's label: four values in the original design overstated their ratio, every one in the direction
that ships unreadable text.

---

## 2.2 Typography

System font only (Roboto on Android, SF Pro on iPhone, the system Naskh for Urdu and Arabic). Sizes in
sp, so the user's text size always applies. **Nothing below 14 in the phone app.** Values in
`typography.json`.

| Style | Size / line | Weight | Use for |
|---|---|---|---|
| `displayLarge` | 52 / 60, tabular, −0.5 tracking | 600 | The countdown. Nothing else |
| `displayMedium` | 40 / 48, tabular | 600 | The app name on the welcome screen |
| `headlineMedium` | 26 / 32 | 600 | Screen titles, the city, the next prayer name and time, the Qibla sentence |
| `headlineSmall` | 22 / 28 | 600 | Sheet titles, dialog titles (inherited), the Bismillah |
| `titleLarge` | 20 / 26 | 600 | Location and export sheet titles, setup choice cards |
| `titleMedium` | 17 / 23 | 500 | Row titles, notice titles, section headings like "Today" |
| `bodyLarge` | 17 / 24 | 400 | Body text, list rows, radio labels |
| `bodyMedium` | 15 / 21 | 400 | Secondary text: descriptions, notice briefs, help, footnotes |
| `labelLarge` | 15 / 20 | 600 | Close and Got it links. At weight 500: section labels |
| `labelMedium` | 14 / 18, +0.3 tracking | 500 | Pills, tab labels, day names, captions. The floor |
| `PrayerTimeTextStyle` | 18, tabular | 500 | Every time of day in a list. 15 for Sunrise and table cells, 17 on My prayers |
| `actionLabelStyle()` | 16 / 20, centred | 600 | Every button label |

**Line height is one rule** (17 Oct 2026): about 1.4× for body, about 1.3× for titles. They were
1.5× and more, which read as double spaced.

**Hierarchy recipe.** A screen has one `headlineMedium` title. A card has at most one prominent line.
Secondary information drops to `bodyMedium` in `onSurfaceVariant`, not to a smaller size. Emphasis is
weight (500 or 600), never colour alone, never italics, never capitals.

**Rules that bite:**

- Never uppercase in code. Capitals, if a language wants them, are in its string.
- A word is never split across lines. Fixed width labels (tabs, theme selector) use `WholeWords`
  (shrinks to no less than 60% only while a break falls inside a word).
- Numbers that change in place use tabular figures.
- Numbers are formatted in the language the words are in. Arabic uses `ar-u-nu-latn` so digits stay
  Western on every Android version.
- Urdu and Arabic single lines stand about 4dp taller than Latin. Never fix a height that assumes
  Latin (OPEN item `1.6-script-line-box`).
- **Known gap:** `bodySmall` (12), `labelSmall` (11) and `titleSmall` (14) are not defined in
  `Type.kt` but are used 13 times (list in `typography.json`). Do not add new uses.

---

## 2.3 Spacing

One scale, chosen by **how closely two things belong together** (owner, 18 Oct 2026). Closer means
related, so a reader sees which button belongs to which paragraph without reading.

| Token | dp | Between |
|---|---|---|
| `Space.Tight` | 4 | A title and its own line; a label and its value |
| `Space.Related` | 8 | A paragraph and its button; two buttons; a field and its button; a heading and its field; either side of a divider |
| `Space.Paragraph` | 12 | Paragraphs in one block; stacked choice cards; a screen headline and its paragraph |
| `Space.Group` | 16 | One group and the next; card padding; help text and the choices under it; a block of lines and its button row |
| `Space.Section` | 24 | One section and the next; a button and the next section; two groups of buttons |

- Never two different gaps for the same relationship on one screen.
- `DesignRulesTest` fails a `Spacer`, column spacing, or top or bottom padding written as a number in
  the phone app.
- **Not covered by the test**, so review: horizontal gaps inside a row (icon to label is 8; tile to
  text 14), a component's own padding (pill 9 × 3, card 20 × 16, row 16 × 12), and the watch.

## 2.4 Layout

| Rule | Value |
|---|---|
| Screen gutter | 16 (Qibla uses 20: a known deviation) |
| Screen title start | 20 (`ScreenTitle`); the city on Times lines up with it (16 + 4) |
| Sheet content gutter | 24 for headers and text; 16 for card lists |
| Card padding | 16 (`Space.Group`), the hero and Ramadan cards 20 × 16 |
| Content width cap | 600, centred (`Modifier.readingWidth()`); the sky stays full width |
| Bottom of every scrolling tab | `BottomBarClearance()`, the bar's measured height (grows with large text) |
| Minimum touch target | 48 (Apple's is 44pt, so this carries over) |
| Minimum row | 56; Settings rows 64; fold rows 60; prayer rows 50 |
| Times without scrolling | City, next prayer card, six times and the mosque button fit at default text on an 823dp tall phone |
| Large text | From 1.5× rows stack (time under name, compact buttons under their heading). Content may scroll; it never clips |
| Narrow phones | Everything works at 320dp wide and 2.0× text |

Screens are one column. There is no two pane layout, no navigation drawer, no floating action button.

## 2.5 Shape

From `shape.json` (a census, not tested):

| Shape | Radius | Use |
|---|---|---|
| Card | 24 | Every card and bounded surface. Default for anything new |
| Inner tile | 16 | A tile inside a card |
| Icon tile | 10 | The Settings row's 36dp icon tile |
| Tab bar | 32 | The floating bar |
| Pill | 50% | Buttons, pills, chips, the selected tab |
| Circle | | The Qibla dial, toggles' press areas, badges |
| Dialog, sheet top | 28 | Material default |

Never square corners on a button, never a custom button radius.

## 2.6 Elevation and surfaces

There are four levels, and depth comes from colour as much as shadow:

| Level | Light | Dark |
|---|---|---|
| Page | `background` | `background` |
| Card | `surface` + 2dp shadow | `surface` + 1dp hairline |
| Floating (tab bar) | `surface` at 97% + 10dp shadow + 70% hairline | `surface` at 97% + 70% hairline |
| Dialog, sheet | `surfaceContainerHigh`, Material tonal elevation | same |

Glass is only for what floats (the tab bar). Content never sits on glass. Real backdrop blur is not
used (it needs Android 12 and a library); 97% opacity is the fallback for every version. On iPhone the
tab bar is the system one, which brings its own material.

## 2.7 Motion

From `motion.json`. Pick by **what moves**, never by mood.

| Token | ms | What moves |
|---|---|---|
| `Motion.quick()` | 150 | An icon or arrow swaps or turns |
| `Motion.standard()` | 250 | A shape, colour, position or block of text changes in place |
| `Motion.draw()` | 400 | A drawing runs across the screen (rings, sky, day path). Nothing is longer |

Easing is always `FastOutSlowInEasing`. Exits use the next shorter token. Nothing moves on screen open
(except the day path, once per app open). Never animated: prayer times, countdown digits, notice text,
the disclaimer, the city and Hijri date, the tab bar position, dialog and sheet text. Letters never
move on their own (it breaks Arabic and Urdu joining). Nothing loops forever, no springs.
`DesignRulesTest` enforces the durations, loops and springs.

**Known gap:** the tab cross fade, setup's slide between steps and the tab pill's colour use library
default timings, not `Motion`. Do not copy them.

## 2.8 Haptics

Through `LocalHapticFeedback` only, on the user's own tap that changes something stored: `ToggleOn`
and `ToggleOff` (Tick, Bell, switches), `SegmentTick` (pickers, the theme selector, a tab change).
One pulse per tap. Never on a prayer passing, a sky change, Undo, a snackbar or a ring filling.

One exception: the Qibla arrival buzz goes through `Vibrator` (70ms) because touch feedback is off by
default on many phones and the owner wants it always (24 Oct 2026).

## 2.9 Icons

- **Set:** Material Icons from `material-icons-extended`. **Rounded** (filled) for the selected tab and
  for marks that carry meaning (prayer slots, notice marks, the bell on); **Outlined** for everything
  else. Moving to Material Symbols Rounded drawables is planned, not done.
- **Sizes:** 24 default; 18 inside a compact button; 20 in a Settings tile; 14 for row chevrons; 21
  for prayer row icons (18 Sunrise).
- **Colour:** the ink of the text beside it, or `primary` when it is the Qibla, the next prayer or
  tappable. Never a colour of its own.
- **Direction:** arrows and chevrons that mean "forward" use `AutoMirrored` so they flip in RTL. Sun
  glyphs, the Qibla dial, the week rings and clocks never mirror.
- **Pictures over words:** the Kaaba, the six prayer glyphs (where the sun is) and the sky mark are
  drawings, so they read without language.
- **Every icon has an SF Symbol named** beside it (see [`09-ios-mapping.md`](09-ios-mapping.md) §9.4).
  A new icon without one is not finished.
- **Licences:** only assets free for both stores (Apache, MIT, OFL, CC0), bundled not fetched, no
  generative AI images, no other app's artwork. Record source and licence in `docs/ASSETS.md`.
