# SajdaTime design system

The tokens and component patterns the app is built from. `docs/HANDOVER.md` §7 is the
short version; this is the reference you check a colour against.

Every token here is a **Material 3 colour role**, set in
[`ui/theme/Color.kt`](../app/src/main/java/com/sajdatime/app/ui/theme/Color.kt) and wired
into the scheme in [`Theme.kt`](../app/src/main/java/com/sajdatime/app/ui/theme/Theme.kt).
Nothing in the UI hardcodes a hex value. If you find one, it is a bug.

---

## 0. Read this before any visual change: the owner's rules, and when to ask him

This section exists because the same mistakes were made again and again, each time after
being told it was done. It is written so that the next session cannot repeat them.

### 0.1 What went wrong, so you recognise it in yourself

| Date | What the owner asked | What was done instead | Why it happened |
|---|---|---|---|
| Up to 18 Oct 2026 | Buttons that stand out, hard to miss | Outlined buttons | Material's default "secondary" choice was taken as good enough |
| 18 Oct 2026 | Same, again | Tonal buttons (pale green fill) | Material calls tonal "medium emphasis"; in dark it was green on dark green and read as a panel |
| 18 Oct 2026 | "Make the text white" | Solid green, but about 15 actions left as green text links, and the reply said "every action button" was solid | The claim was made without listing the buttons; this document itself said text links were right for "Skip", "Details" and "Save as PDF", contradicting him |
| 18 Oct 2026 | Even gaps between bold and normal text | Said "every gap comes from `Space`" | Only `Notice.kt` was checked; 74 eyeballed gaps remained in 10 files |
| 18 Oct 2026 | Test on the emulator | He judged an old build twice | Nobody checked which build was installed |

The pattern: **a general instruction was applied to the place in front of you, then reported
as applied everywhere.** The fix is §0.3: never say "every" without the inventory.

### 0.2 When to ask the owner first (do not guess)

Ask him **before building** when any of these is true:

1. The change alters how something **looks** on a screen he has seen, and no rule below
   decides it exactly.
2. Two of his instructions, or one of his instructions and a rule here, **pull in different
   directions** (for example "every action solid" against "a clear main action").
3. You would **reverse an entry in §9** (the decision log), or in §11 the item is marked OPEN.
4. A choice changes **what the user can do or what they see first** (order of buttons, what is
   hidden behind a tap, what is removed).
5. You are about to use a word like "prominent", "clean", "premium" or "consistent" to justify
   a choice. Those are his words; ask what he means by them on this screen.
6. You cannot say in one sentence which rule in this document makes the choice for you.

How to ask: with the `AskUserQuestion` tool, at most four questions, two to four options each,
your recommendation first and labelled "(Recommended)", each option saying what he will see.
Show screenshots, light and dark, of the options when the question is visual. **Silence is never
a yes.** Record the answer in §9 the same day, and remove the item from §11.

Do **not** ask about engineering detail he cannot judge (dp values, component names). Decide
those from §1 to §4 and say what you decided.

### 0.3 The words "done", "every" and "all"

Do not tell him a rule is applied "everywhere" or that a change is "done" until all of these
are true, and say which you checked:

1. **The inventory is clean.** Run the command in §4.1 "How to check" and read every line.
   Paste the count into your reply ("19 solid buttons, 2 text links, both Close").
2. **The tests pass**: `DesignRulesTest` and `ColorContrastTest` at least; the full gate before
   a merge.
3. **Screenshots** of every screen you changed, light and dark, have been shown to him.
4. **The build he will test is the new one**: give him the install command and the commit he
   should see (`git log --oneline -1`).
5. **What you did not check is written down**, in the reply and in `docs/HANDOVER.md`.

### 0.4 When this document and the owner disagree

The owner wins, and this document is corrected **in the same commit**, with the old rule moved to
§9 as reversed. When this document and the code disagree, the code is the bug unless §9 says
otherwise; if neither says, ask him (§0.2).

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
| dialBezel | `surfaceContainerHighest` (read through `dialBezel()`, not a role of its own) | `#262C29` | The Qibla dial's bezel when not aligned; `primary` when aligned |
| primary | `primary` | `#4FC48F` | Accent bar, Qibla needle, north letter and aligned bezel, "Next" pill |
| onPrimary | `onPrimary` | `#062117` | Text on a filled primary surface |
| navPill | `secondaryContainer` | `#1E4034` | The selected tab pill |
| onSurface | `onSurface` | `#E7EAE8` | Body text, prayer times |
| onSurfaceVariant | `onSurfaceVariant` | `#A9B2AE` | Secondary text, icons |
| outline | `outline` | `#8B948F` | Chevrons, tick marks, the facing tick |
| outlineVariant | `outlineVariant` | `#2A312E` | Card borders, row dividers |
| warning | `tertiary` | `#E8B14A` | The warning icon |
| warningSurface | `tertiaryContainer` | `#2A2113` | The warning banner |
| onWarningSurface | `onTertiaryContainer` | `#F3E4C5` | Text in that banner |
| warnTop, warnBottom | `DarkWarnTop`, `DarkWarnBottom` in `Color.kt` (not a Material role) | `#352A17`, `#20190D` | The warning notice's gloss gradient, `tertiaryContainer` in the middle (1.6, §9 24 Oct) |
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
| dialBezel | `surface` (read through `dialBezel()`, not a role of its own) | `#FDFCFB` | The Qibla dial's bezel when not aligned, lifted by the card shadow; `primary` when aligned |
| primary | `primary` | `#0B5A43` | Accent bar, Qibla needle, north letter and aligned bezel, "Next" pill, every link and action (was `#0E6B4F` until 17 Oct 2026, see §9) |
| onPrimary | `onPrimary` | `#FFFFFF` | Text on a filled primary surface |
| navPill | `secondaryContainer` | `#CDE3D7` | The selected tab pill |
| onSurface | `onSurface` | `#1B211E` | Body text, prayer times |
| onSurfaceVariant | `onSurfaceVariant` | `#5A605C` | Secondary text, icons |
| outline | `outline` | `#757C77` | Chevrons, tick marks, the facing tick |
| outlineVariant | `outlineVariant` | `#C9CFC9` | Row dividers |
| warning | `tertiary` | `#9A6208` | The warning icon |
| warningSurface | `tertiaryContainer` | `#FBF1DC` | The warning banner |
| onWarningSurface | `onTertiaryContainer` | `#5A4408` | Text in that banner |
| warnTop, warnBottom | `LightWarnTop`, `LightWarnBottom` in `Color.kt` (not a Material role) | `#FBF0D8`, `#F3E2BE` | The warning notice's gloss gradient, `tertiaryContainer` in the middle (1.6, §9 24 Oct) |
| kiswah | *(none — `kiswahGold()`)* | `#E8B14A` | The Kaaba's hizam and door on the Qibla dial |

### The light hero gradient

The next-prayer card in **light only** is a fixed three-stop gradient, mint at the top to
sand at the bottom: `#D6E8DC` → `#DDE2CE` → `#E4D2B4`. On it: countdown and prayer name
`#0A2419`, the section label `#2C4A3C`, the time and "until it begins" `#3E5C4C`.

Nine contrast assertions cover it — every ink against every stop. That is the whole reason
a gradient is permitted here: it is **fixed**. The gradient this replaced changed with the
prayer slot, so the contrast under the countdown was different at Isha than at Fajr and
could not be asserted at all. Fixed gradient, testable. Moving gradient, not.

In **dark** the card is a fixed green gradient in the widgets' countdown card colours: `#18382D` →
`primaryContainer` `#143028` → `#0F241D`. It was flat until 18 Oct 2026, when the owner asked for
"the widget style glossy look". Ink: countdown `onSurface`, label `onPrimaryContainer`, time
`secondary`.

**The gloss, both themes** (owner, 18 Oct 2026): a white sheen over the top half fading out by the
middle (light `#99FFFFFF`, dark `#12FFFFFF`), a 1dp rim brightest at the top (light `#CCFFFFFF` to
`#33FFFFFF`, dark `#29FFFFFF` to `#0AFFFFFF`), and in light only a soft green shadow (`#590B5A43`,
12dp). The tokens are `LightHero*` and `DarkHero*` in `Color.kt`. `ColorContrastTest` checks every
ink against every stop and against the top stop under the sheen; the sheen lightens, so in dark it
is the worst case (5.75:1 for the time).

### The rule that has bitten this project four times

**Set every Material 3 colour role, not just the obvious ones.** Dialogs, bottom sheets
and the navigation pill all rendered in Material's default **lilac** because
`surfaceContainer*`, the inverse roles and `secondaryContainer` were left unset. It then
happened a fourth time on the watch, which was using Wear's default scheme. If anything
looks purple, an unset role is the cause.

### One colour is depictive rather than semantic, and it is the only one (until the skies of 1.6)

Every other hue here carries meaning: green is the identity, amber is a warning, and
nothing is decorative. **`kiswah` is the exception, and it is deliberate** (the only one until the
skies of 1.6, see the next heading but one). It is the gold
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

### The four skies (1.6, step 5; prototype, awaiting the owner's pictures)

The top of Times is a fixed three stop gradient per period, from under the status bar to the foot of the
next prayer card, where it has faded into the page (`background`). Four skies, never a continuous
gradient. Tokens `LightSky*` and `DarkSky*` in `Color.kt`, read through `skyStyle(period)` in `Theme.kt`.
The period is `Sky.periodOf` in `:core` (Dawn Fajr to sunrise, Day sunrise to Asr, Evening Asr to Isha,
Night Isha to Fajr; no times means Day).

| Sky | Light top, mid | Dark top, mid |
|---|---|---|
| Dawn | `#EADFE3`, `#F0E9EC` (pale rose) | `#231E2C`, `#171520` (deep violet) |
| Day | `#D6E8DC`, `#E6EFE9` (mint) | `#17323A`, `#13262B` (deep teal) |
| Evening | `#ECD8BB`, `#F1E5D3` (sand) | `#262A1E`, `#181B15` (olive dusk) |
| Night | `#D0DBE1`, `#DFE6EA` (slate) | `#0B1730`, `#0C1320` (navy) |

The foot of every sky is the page: `#F7F5F3` light, `#101312` dark. **Inks on a sky:** the city
`onSurface`, the Gregorian date, arc and dots `onSurfaceVariant`, the Hijri date, caret, glyph and passed arc
`primary`. Nothing but the header's three texts and the sky mark ever sits on a sky. The status bar icons
follow the theme and hold 3:1 on every top. **88 new assertions** in `ColorContrastTest`: 72 text pairs
(3 inks x 3 stops x 4 skies x 2 themes), 8 feet equal the page, 8 status bar pairs. The light night top
is the thin one, 4.57:1 under `onSurfaceVariant`: it cannot be darkened without a darker date ink.
These sixteen stops are the second depictive colour family after the Kaaba gold: the sky shows the time of
day the way the gold shows what the Kaaba is. No role, no state, never behind a prayer time or the
countdown, never on a widget's text. Dark night is told by hue (`#0B1730` is 1.05:1 against the page), which
Ali kept on 24 Oct 2026 ("Keep it this deep"; `1.6-dark-skies`, §9).

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

### Gaps by relationship (owner, 18 Oct 2026; enforced)

The owner reported uneven gaps between bold and normal text many times. The cause was never
the line heights; it was gaps chosen by eye, so a title, its sentence and its button were
spaced 0, 12 and 20 in one card and 12, 8 and 12 in the next. Now a gap is chosen by **how
closely the two things belong together**, from `ui/theme/Spacing.kt` (`Space`):

| Relationship | Token | dp |
|---|---|---|
| A title and the line that belongs to it | `Space.Tight` | 4 |
| A paragraph and the button that acts on it; two buttons side by side | `Space.Related` | 8 |
| One paragraph and the next in the same block | `Space.Paragraph` | 12 |
| One group (text plus its button) and the next; card padding | `Space.Group` | 16 |
| One section of a screen and the next | `Space.Section` | 24 |

Closer means related (proximity), so a reader can tell which button belongs to which paragraph
without reading. Never two different gaps for the same relationship on one screen.
`DesignRulesTest` fails the build if every `Spacer`, every column's spacing and every top or bottom padding in the phone app's screens is written as a
number instead of a token (D5, 18 Oct 2026). Not covered, so never claim them: horizontal gaps inside a
row, a component's own `padding(vertical = …)` (a row's height, a pill, a card's edge), and the watch app
(`wear/`), which has its own small screens and was not converted.

How the common cases were decided, so the next screen uses the same answers:

| Between | Token |
|---|---|
| A screen headline (headlineMedium or larger) and its paragraph | `Paragraph` 12 (bigger type needs more than `Tight`) |
| A sheet or card title (titleLarge or smaller) and its line; a label and its value | `Tight` 4 |
| A dialog's help text and the choices under it | `Group` 16 (it was 8 in some dialogs and 16 in one) |
| A section heading and the field it introduces | `Related` 8 |
| The last choice card and setup's Back and Continue | `Section` 24 (it was 8: the buttons looked stuck to the cards) |
| Stacked choice cards; summary rows | `Paragraph` 12 |
| A divider and what is either side of it | `Related` 8 each side |
| A progress line under the button that started it | `Related` 8 |

Not covered by the check, on purpose: horizontal gaps inside a row (an icon beside its label, two
tiles side by side) and a component's own padding (a pill, a card, a sheet's edge). Those are the
component's fixed metrics, not gaps between pieces of text.

> Reversed 18 Oct 2026: this section used to say "dp literals, not a `Spacing` object". The
> literals are exactly how the gaps drifted apart, card by card. Named tokens make the
> relationship part of the code, so the reviewer sees `Space.Related` where a 12 would hide.

### Motion (1.6, 23 Oct 2026)

Durations come from `ui/theme/Motion.kt`, the way gaps come from `Space`. There are three, all with
`FastOutSlowInEasing`, chosen by what moves and never by mood:

| What moves | Token | ms |
|---|---|---|
| An icon or arrow swaps or turns (the bell, the fold chevrons) | `Motion.quick()` | 150 |
| A shape, colour, position or block of text changes in place (the tick, the theme thumb, the pause row, Now and Next) | `Motion.standard()` | 250 |
| A drawing runs across the screen (rings, sky, day path) | `Motion.draw()` | 400 |

- **Exits** use the token one step shorter than the enter. A `Standard` enter leaves at `Quick`, and a
  `Quick` enter also leaves at `Quick`. Reversing the same animation (an untick) counts as an exit. A call
  site passes the shorter spec itself; there is no exit helper.
- **No entrance animation.** Nothing moves when a screen opens. Something moves only when the user touched
  it, a sensor reading changed (the Qibla dial), or a real event happened (a prayer time passed, the sky
  period changed). The one exception is the day path drawing in once per app open.
- **Never animated:** prayer times, the countdown digits, notice text, the disclaimer, the city title and
  Hijri date, the glass bar's position, any dialog or sheet text. Letters never move or morph on their own,
  because per letter effects break Arabic and Urdu joining. A whole block of text may fade.
- **Haptics** go through `LocalHapticFeedback` only, on the user's own tap that changes something stored
  (`ToggleOn` and `ToggleOff`, `SegmentTick` for pickers). Two shipped exceptions stay, named so nobody
  "fixes" them: `SegmentTick` on a tab change (`MainScaffold.kt`) and the Qibla arrival buzz
  (`QiblaScreen.kt`, a sensor event). Since 1.6 step 6 that one goes through the `Vibrator` (70 ms, media usage, the `VIBRATE`
  permission), not `LocalHapticFeedback`: touch feedback is off by default on many phones, and Ali asked that
  the buzz always happen (24 Oct 2026). Never on a prayer passing, a sky change, Undo, a
  snackbar or a ring filling. One pulse per tap.
- **Enforced** by `DesignRulesTest`: a written `tween` or `TweenSpec` anywhere except `Motion.kt` and the
  Qibla dial's one 120 ms call (input driven), a `rememberInfiniteTransition` or `infiniteRepeatable`, and a
  written `spring(`, `spring<…>(` or `SpringSpec` all fail the build. It scans the phone app only. The watch has
  no animation and is not covered. Not caught, so never claim it: a duration hidden in a constant passed to an
  API other than `tween`, and an animation loop written by hand with a `LaunchedEffect`; the reviewer checks those. Compose's own default
  specs on approved screens (tab fade, folds) are kept.

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

**A word is never split across two lines** (19 Oct 2026). Android breaks a word that is wider than
its line, and short labels in fixed width slots hit that at large text: "Settings" in the tab bar
became "Setting" over "s" on a 320dp phone at 2.0, and Turkish "Telefona uy" and Indonesian
"Pengaturan" split on an ordinary phone at 2.0. Such labels (today the tab bar and the theme
selector) use `WholeWords`, which shrinks the text only while a break falls inside a word, to no
less than 60% of its size; at normal sizes nothing changes. Wrapping between words stays. A button
label that wraps is centred (`actionLabelStyle`), never left hugging the start edge.

Section labels (`PRAYER TIMES`, `NEXT PRAYER`) carry `letterSpacing` 1.4–1.6sp.

> They are **not** uppercased in code. `String.uppercase()` on a translated string is a
> trap — Turkish dotted and dotless i, and scripts with no case at all. A language that
> wants capitals gets them from its translator, in the string.

---

## 4. Components

### 4.1 Buttons and actions: the full specification (owner, 18 Oct 2026)

**The rule in one line:** every action a person can take is a **solid button with white bold text**
in light and dark, so nobody has to guess what is tappable. The only text links in the app are
**"Close" and "Got it" at the foot of a dialog**, and **one named exception the owner chose: the
two bold links on the usage question** (kind F below). Decided by the owner on 18 Oct 2026, after
outlined and then tonal buttons were both too easy to miss (§0.1).

#### The seven kinds of control that act, and nothing else

| Kind | Component | Fill, light | Fill, dark | Label and icon | Used for |
|---|---|---|---|---|---|
| **A. Solid green** | `CtaButton` (or `ctaButtonColors()` on a Material `Button` inside `Notice` and `PermissionButton`) | `#0B5A43` (`LightCta`) | `#17805A` (`DarkCta`) | `#FFFFFF` (`OnCta`) | Every action that is not a warning |
| **B. Solid amber** | Material `Button` with `tertiary`/`onTertiary`, only inside `Notice` (warning tone) | `#9A6208` | `#E8B14A` | light `#FFFFFF`, dark `#3A2A00` | The action and "Details" in a warning notice ("Allow exact alarms") |
| **C. Solid green icon** | `FilledIconButton` with `ctaIconButtonColors()` | `#0B5A43` | `#17805A` | `#FFFFFF` icon | The − and + minute steppers in "Match your mosque" |
| **D. Close X** | `IconButton`, `Icons.Outlined.Close`, ink of the surface | none | none | `onSurface` (or the notice's ink) | Top right of a dialog (`DialogTitle`) and of a closable notice on Times. Always **alone** |
| **E. Dialog text link** | `TextButton` | none | none | `primary`, labelLarge | **Only** "Close" or "Got it" at the bottom end of a dialog |
| **F. Bold information link** | `InfoLink` (private to `UsageCountsConsent.kt`) | none | none | `primary`, `actionLabelStyle()` (16sp SemiBold) **underlined**, 48dp target, start aligned, announced as a button | **Only** "See exactly what is sent" and "Read the privacy policy" on the usage question (owner's choice, 18 Oct 2026). Nowhere else without asking him |
| **G. Snackbar action** | Material's own `SnackbarHost` action, written with `showSnackbar(actionLabel = ...)` in `MainScaffold.kt`, so no `TextButton` appears in our source | none | none | platform (`inversePrimary` on `inverseSurface`) | **Only** "Undo" in the message at the bottom ("Paused from today"; "Dhuhr alert off" and its three forms, 1.6 step 3), above the tab bar, on every tab. Added with step 1 of 1.6.0 |

Plus one icon that informs rather than acts: the **(i)** at the end of the Ramadan card's last
row (48dp target, `onSurfaceVariant`). It opens the Ramadan explanation. It is never placed next
to another icon button.

Dark amber text is `#3A2A00`, not white, because white on `#E8B14A` is about 1.9:1 and fails. This
is the only exception to white text, and the owner accepted it on 18 Oct 2026 ("keep amber for
warnings").

#### Every property of a solid button (kinds A and B)

| Property | Value | Notes |
|---|---|---|
| Shape | Full pill (Material `Button` default) | Never square corners, never a custom radius |
| Label style | `actionLabelStyle()`: labelLarge (SemiBold) at **16sp** | The same in A and B. Never 14sp, never regular weight |
| Label case | Sentence case, as written in the string | Never uppercased in code (§3) |
| Height | **56dp** the main button of a setup step or a must answer dialog; **52dp** every other standalone button; **48dp** buttons inside a notice; **40dp drawn, 48dp to touch** a button that shares a line with a heading or a sentence, or sits in a card row (`CtaButton(compact = true)`) | Minimums; large text grows them. The compact size exists because "Save as PDF" at 52dp beside "Today" looked squashed (owner, 18 Oct 2026). Its 48dp tap area was measured once, in the scratch render harness (Robolectric, not part of the repo's tests, which carry no Robolectric): taps 2 and 3.5dp outside the drawn edge pressed it, 6dp did not. Material 3 `Button` gives it that area through `minimumInteractiveComponentSize` |
| Width | **Full width** when the button stands alone on its line or in a column of buttons; **wraps its label, compact** when it shares a line with text (a heading, a question) or sits inside a card row; two buttons side by side in setup share the width equally. A compact button may stand alone, centred, when it is a disclosure under a picture (Qibla Details; the welcome globe is the earlier case): a 52dp full width button under the dial would outweigh the thing it explains | `DesignRulesTest` fails a `CtaButton` that states none of the three (`fillMaxWidth`, `weight`, `compact = true`) |
| Padding | Material default (24dp each side); compact 16dp each side | |
| Icon | Optional, **leading** (before the label), 24dp (18dp in a wrapped button), the label's colour, `Space.Related` (8dp) gap | Notice actions carry a trailing arrow; "Details" a trailing chevron that turns |
| Elevation and shadow | None | Flat on purpose: the fill does the work |
| Pressed | Material ripple | |
| Disabled | Material default (12% `onSurface` fill, 38% label). **Only while the action is running** (finding your location) | Never shown as a grey placeholder: hide the button until it can be used ("Find place" appears once something is typed) |
| Contrast | Label to fill at least 4.5:1; fill to the surface behind it at least 3:1 | `ColorContrastTest` asserts both, in both themes |

#### Order, hierarchy and wording

- **Order:** the main action comes first: top of a column, start of a row (right in Urdu and
  Arabic, which mirror automatically). The one exception is setup's Back and Continue pair,
  where Back is first because that is where every phone puts it.
- **Hierarchy comes from order, position and wording, never from fading a button.** A button that
  is easy to miss is a bug, however secondary it is.
- **Two equal answers stay equal.** The usage question's "Yes, share usage counts" and "No thanks"
  are the same size, colour and height, and neither is first by accident (yes is first because it
  is the question's own word). Nudging either way breaks the consent.
- **Wording:** a verb first ("Allow location", "Save as PDF", "Shift the date"), two to five words,
  no full stop, never "Tap here" or "Click". A question may label a button that opens an
  explanation ("Different from your mosque?").

#### Spacing around buttons (from §2, by relationship)

| Between | Gap |
|---|---|
| A single paragraph and the button that acts on it (explainer dialogs) | `Space.Related` 8dp |
| A text field and its button ("Find place") | `Space.Related` 8dp |
| Two buttons stacked or side by side | `Space.Related` 8dp |
| A block of several lines (title and sentence, or several paragraphs) and its button row | `Space.Group` 16dp |
| A group of buttons and a different group (the usage question's two links and its two answers) | `Space.Section` 24dp |
| A button and the next section of the screen | `Space.Section` 24dp |

#### Banned, and the test that fails the build

| Banned | Why | Enforced by |
|---|---|---|
| `OutlinedButton`, `FilledTonalButton` | Easy to miss (§0.1) | `DesignRulesTest` |
| `FilledTonalIconButton`, `OutlinedIconButton` | Pale, the same problem as tonal | `DesignRulesTest` |
| A bare Material `Button` outside `CtaButton.kt`, `Notice.kt`, `PermissionAction.kt` | Its dark default is pale mint with black text | `DesignRulesTest` |
| A `TextButton` saying anything but "Close" or "Got it" | Every action is solid (owner, 18 Oct 2026) | `DesignRulesTest` |
| `InfoLink` outside `UsageCountsConsent.kt` | It is his exception for one screen, not a new style | `DesignRulesTest` |
| A file with a button that is not in the inventory below | So no button is added without being specified here | `DesignRulesTest` |
| Two icon buttons next to each other | Opposite actions a finger apart (§4 Notices) | Review question in §10 |
| A disabled button shown as a placeholder | Reads as broken | Review question in §10 |
| A `CtaButton` with no stated size (no `fillMaxWidth`, `weight` or `compact = true`) | It defaults to 52dp wrapped, which looked squashed beside a heading | `DesignRulesTest` |

#### The inventory: every button in the app

Update this table in the same commit as any button you add, move or remove. `DesignRulesTest`
fails if a Kotlin file containing a button is missing from the **File** column.

| Screen | Label | Kind | Height, width | File |
|---|---|---|---|---|
| Setup, welcome | Begin | A | 56, full | `OnboardingScreen.kt` |
| Setup, welcome | Globe and the language's own name ("English", "اردو"), top end corner, 16dp in; opens the language list | A, compact | 40 drawn, 48 target, wraps | `OnboardingScreen.kt` |
| Setup, location | Allow location (pin icon) | A | 56, full | `OnboardingScreen.kt` |
| Setup, location | How your location is used (info icon) | A | 52, full | `OnboardingScreen.kt` |
| Setup, location | Find place (search icon), once something is typed | A | 52, full | `OnboardingScreen.kt` |
| Setup, location | Skip for now and use Makkah, at the **foot of the screen** under "Prayer times will be for Makkah until you set your location." (after the content at large text) | A | 52, full | `OnboardingScreen.kt` |
| Setup, location | Continue, once located | A | 56, full | `OnboardingScreen.kt` |
| Setup, location | "How your location is used" dialog: Got it | E | | `OnboardingScreen.kt` |
| Setup, sect, madhab, method | Back, Continue | A | 56, half each, 8 apart | `OnboardingScreen.kt` |
| Setup, confirm | Allow exact alarms, until allowed | A (`PermissionButton`) | 52, wraps | `OnboardingScreen.kt`, `PermissionAction.kt` |
| Setup, confirm | Finish | A | 56, full | `OnboardingScreen.kt` |
| Usage question (setup, and the Settings switch) | See exactly what is sent / Hide, 16dp under the text | F | 48 target, wraps | `UsageCountsConsent.kt` |
| Usage question | Read the privacy policy, straight under it | F | 48 target, wraps | `UsageCountsConsent.kt` |
| Usage question | Yes, share usage counts; No thanks, 24dp under the links (under "The full privacy policy is in English." when the app is not in English, since 19 Oct 2026) | A | 56, full, equal, 8 apart | `UsageCountsConsent.kt` |
| First run disclaimer | I understand | A | 56, full | `MainActivity.kt` |
| First run disclaimer | Read in full, until opened | A | 52, full | `MainActivity.kt` |
| Times | Timetable (calendar icon), end of the Today heading, under it from 1.5 text; 16dp under the card above, 8dp over the list | A, compact | 40 drawn, 48 target, wraps | `HomeScreen.kt` |
| Times | Different from your mosque? (mosque icon) | A | 52, full | `HomeScreen.kt` |
| Times, mosque dialog | Calculation method; Asr; Match your mosque | A | 52, full | `HomeScreen.kt` |
| Times, mosque dialog | Close, and the X | E, D | | `HomeScreen.kt`, `Common.kt` |
| Times, Ramadan card | Shift the date, days 1 and 2 only | A, compact | 40 drawn, 48 target, wraps | `RamadanCard.kt` |
| Times, Ramadan card | Get the full Ramadan timetable, filling its row, 24dp above and below; (i) after it, 8dp apart, centred on it | A; info icon | 52, fills the row (weight); 48 | `RamadanCard.kt` |
| Times, Ramadan info dialog | Close, and the X | E, D | | `RamadanCard.kt` |
| Timetable sheet | Save as PDF (PDF icon), end of the place line under the title | A, compact | 40 drawn, 48 target, wraps | `TimetableSheet.kt` |
| Timetable sheet | the X, alone at the top end (`DialogTitle`) | D | 48 | `TimetableSheet.kt`, `Common.kt` |
| Ramadan timetable | Save as PDF moves to the place line; the X alone at the top end (shared header) | A, compact; D | 40 drawn, 48 target, wraps | `RamadanTimetableSheet.kt` |
| Ramadan timetable | Shift the date, alone under the notes | A | 52, full | `RamadanTimetableSheet.kt` |
| Location sheet | Use my current location; Find place, once something is typed | A | 52, full | `Common.kt` |
| Every notice | The action (trailing arrow); Details or Hide details (chevron) only when the notice has details (not the two permission cards, since 1.6); the X, alone | A or B; D | 48, wraps | `Notice.kt` |
| Times, notifications notice (1.6.0, while the phone blocks notifications) | Allow (trailing arrow); the X, alone | B; D | 48, wraps | `Notice.kt`, `PermissionAction.kt` |
| Times | the bell at the end of each prayer row, not Sunrise (a 48dp blank keeps its time aligned) | Bell (`Bell.kt`), a toggle like Tick | 24 drawn, 48 target | `HomeScreen.kt`, `Bell.kt` |
| Settings, Match your mosque | Use your mosque's board (clock icon), under the help text, 16dp over the first prayer; opens "Which prayer?" (radio rows), then the clock | A | 52, full | `SettingsScreen.kt` |
| Settings, Match your mosque | Choose a calculation method, under a prayer at the 30 minute limit or whose board time was too far, 8dp under the note | A | 52, full | `SettingsScreen.kt` |
| Settings, the clock ("Isha: the time on your mosque's board") | Set time; the X | A; D | 52, full | `SettingsScreen.kt` |
| Settings, Match your mosque | − and + for six times and the date | C | 40 visual, 48 target | `SettingsScreen.kt` |
| Settings, Match your mosque | Reset all, once something is adjusted: **its own row, full width (so centred)**, 24dp under the steppers | A | 52, full | `SettingsScreen.kt` |
| Settings, every chooser and info dialog | Close, Got it, and the X | E, D | | `SettingsScreen.kt` |
| My prayers tab | the tick at the start of each prayer row | Tick (`Tick.kt`), a toggle | 24 drawn, 48 target | `MyPrayersScreen.kt`, `Tick.kt` |
| My prayers tab | Pause (pause icon) at the start, Last 30 days (calendar icon) at the end, under the today card | A, compact | 40 drawn, 48 target, wraps | `MyPrayersScreen.kt` |
| My prayers tab, while paused | Resume (play icon) at the end, beside "Tracking is paused" | A, compact | 40 drawn, 48 target, wraps | `MyPrayersScreen.kt` |
| My prayers tab, tracking off | the switch row; not a button | Switch | 56 row | `MyPrayersScreen.kt` |
| My prayers tab, no location | Set location, the notice's action | A (notice) | 48, wraps | `Notice.kt` |
| Qibla | Details (down chevron that turns; "Hide details" when open), centred under the place name, 16dp under it | A, compact | 40 drawn, 48 target, wraps | `QiblaScreen.kt` |
| Qibla, no location (or the Makkah fallback) | Set location, the notice's action | A (notice) | 48, wraps | `Notice.kt` |
| Last 30 days sheet | the X, alone at the top end | D | 48 | `MyPrayers.kt` |
| Last 30 days sheet | each day cell is `selectable`, not a button; the chosen day's five ticks under the grid | Tick | 24 drawn, 48 target | `MyPrayers.kt` |
| Every tab | Undo in the message at the bottom (Material's own snackbar action, written with `showSnackbar(actionLabel = ...)`, so no `TextButton` in our source) | G, new | platform | `MainScaffold.kt` |
| Settings, Pause from | Pause, under Today and Yesterday; the X | A; D | 52, full | `SettingsScreen.kt` |
| Times, mosque dialog (1.6.0) | Add your mosque's jama'ah times, first, under the congregation paragraph | A | 52, full | `HomeScreen.kt` |
| Settings, Jama'ah times (1.6.0) | Each prayer and Jumu'ah is a Settings-style row with a chevron, not a button; a time outside its prayer gets an amber Notice under its row | Row | 56 | `SettingsScreen.kt` |
| Settings, a prayer's jama'ah | Off, A fixed time (opens the clock, "Asr: the jama'ah time on your mosque's board"), Minutes after it begins (radio rows); − and + in fives, 0 to 60, value is the number alone | radio; C | 48; 40 visual, 48 target | `SettingsScreen.kt` |
| Times, header (1.6.0) | The date line under the city, Hijri half in primary; opens Islamic dates. A 48dp text target, not a button, and no caret | Text target | 48 target | `HomeScreen.kt` |
| Islamic dates sheet | Shift the date, alone under the footer | A | 52, full | `IslamicDatesSheet.kt` |
| Settings, Delete your prayer record? | Delete; the X. Solid green like every action: there is no error role, the words carry the warning | A; D | 52, full | `SettingsScreen.kt` |

Not buttons, and not to be turned into buttons without asking (§0.2): Settings rows (the whole row
is the target, with a chevron), switches, radio rows, the tab bar, and the city name with its caret
on Times, which opens the location sheet.

#### How to check, before you say "every"

```bash
# Every acting control in the app, with file and line. Read all of it.
grep -rnE "\b(CtaButton|PermissionButton|FilledIconButton|IconButton|TextButton|Button)\(" \
  app/src/main/java --include=*.kt | grep -v "fun \|import"
# Must print only Close and Got it (the usage question's InfoLinks are kind F, not TextButtons):
grep -rn -A1 "TextButton(" app/src/main/java --include=*.kt | grep -oE "R\.string\.[a-z_]+" | sort | uniq -c
./gradlew :app:testDebugUnitTest --tests '*DesignRulesTest' --tests '*ColorContrastTest'
```

Then screenshots of each changed screen in light and dark, and §0.3.

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

**Qibla dial** (restyled in step 6, 24 Oct 2026, the Pillars style in our theme) — a circle with the lift every
card has (shadow in light, hairline in dark, `sajdaSurface`). Its **bezel** is the lifted disc: 0.08 of the radius,
`surface` in light and `surfaceContainerHighest` in dark (`dialBezel()`, a different role per theme because the dark
hairline is invisible against the page). Inside it the face, `surfaceVariant`. Sixty ticks, one every 6°, in
`outline` (minor 0.845 to 0.87, cardinal from 0.80, round caps); the letters at 0.74, E S W `onSurfaceVariant`
Normal and N `primary` Medium. The needle is **you**: a `primary` teardrop (tip at 0.64, round end past the centre,
no hub), fixed at the top with a compass (it points at the bearing without one). There is no arc: the Kaaba's side
of the dial and the sentence ("Turn right 58°") say how far to turn. Inside ±5° the face fills with
`primaryContainer`, the bezel turns `primary` and thickens to 0.11, all over 250 ms (150 ms on leaving), the line
above the dial turns green with a tick, and the phone buzzes once: three signals that are not colour (the
sentence with its tick, the thicker bezel, the buzz).

**The Kaaba mark** sits at 0.70 of the dial radius, 0.22 across, in
`onSurface` with its band and door in `kiswahGold()`. Same
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
shapes and this one is a picture.

**Notices** (one component, `ui/components/Notice.kt`, since 17 Oct 2026). Every message that
is not a setting goes through it, so a person learns one shape and reads every notice the same
way. Two tones only: **warning** (`tertiaryContainer`, amber: something is wrong or withheld)
and **quiet** (`secondaryContainer`, soft green: information or an occasion, Ramadan and Eid).
Anatomy, since 24 Oct 2026 (1.6.0 step 3, Ali's four choices, `STEP-3-DESIGN.md` §19 and §20): a
**bare mark**, the icon itself at 24dp in the tone's colour, top-aligned, with no tile behind it;
the title, then **one brief sentence** 4dp under it; a footnote that must stay visible (Eid's
"follow your mosque"); the full explanation, folded; then, 16dp under the text, **one row of
buttons**: the action as a **solid button** in the tone's colour with an arrow ("Allow", "See
why", "Change location", "Shift the date"; never "Tap to…") and, when there is more to say, a
**"Details" button with a down chevron** ("expanded"/"collapsed" spoken). The close X sits
**alone at the top right**, on Times only. The **warning tone is glossy**: a soft top to bottom
amber gradient with a light sheen (`warningGloss()`, the `Warn` tokens in §1); the quiet tone
stays flat. The two **permission cards** (exact alarms, and notifications while the phone blocks
them) carry **one button only, "Allow"**, and no Details: the brief sentence says what goes
wrong, the button fixes it. Their marks are `WarningAmber` and the crossed bell
`NotificationsOff`. On Times a card leaves with a quick fade and shrink once the permission is
granted. Before 24 Oct the mark sat on a 14% tile and every warning was flat amber.

**Never put two icon buttons side by side** (owner, 18 Oct 2026, from the emulator: the fold
chevron sat next to the close X, two opposite actions a finger apart). A close X is the last
trailing element and stands alone; "show more" is a labelled button with a chevron; the main
action is a filled button (Android banner guidance, Devon and Primer design standards).

**Dialogs.** Every dialog a person can simply leave has a close X at the top right
(`DialogTitle`) as well as its text button at the bottom (owner's request). Two must be
answered, not closed, and have no X: the first run disclaimer and the usage counts question.
Explainer dialogs pair each paragraph with its button (`ExplainerParagraph`, `ExplainerAction`):
paragraph to its button 8dp, group to group 16dp. `DesignRulesTest` checks the X.

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
  Oct") so the chip never cuts it short. The Hijri line under it shrinks from 12 to as small as 9sp
  rather than cut the year (it read "2 Rabi al-Awwal 14…" beside "Manchester" on a 5 column home
  screen; owner's choice, 18 Oct 2026, §9). Verified on a real launcher (Pixel Launcher, Android 16),
  pressing the chip opens the app.
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
and it should stay that way. (Reversed by the owner on 23 Oct 2026 for 1.6: a one-tap bell on each row,
and a tracker. See §9.) (The Ramadan card and the widgets reuse these tokens and add none; the
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
pill, `surface` at `GlassBarAlpha` (97% since 18 Oct 2026; it was 90%, see §9), so what scrolls
underneath shows through only as a hint, never as words that run into the labels.
Prayer times, cards and every line of text stay on solid surfaces. This is Apple's own rule for
Liquid Glass, and the usability research behind it is in the HANDOVER block. The bar's labels
are tested against pure black and pure white behind it (`ColorContrastTest`).

> ponytail: translucency without blur, on every Android version. Real backdrop blur needs
> Android 12+ and the Haze library; at 97% opacity the visible difference is small. Add it only
> if the owner asks for more glass, and keep the current bar as the Android 7 to 11 fallback.

**Every tab opens the same way.** `ScreenTitle` (headlineMedium, start aligned, 20dp gutter)
on Qibla and Settings; on Times the title is the city, same size and line, with a green caret
because it opens the location sheet, and (1.6, step 5) on a sky that says the period. Every scrolling screen ends with `BottomBarClearance()`,
which is the bar's measured height, so it grows with large text.

**Every call to action is a `CtaButton`: solid green, white bold 16sp text, at least 52dp tall,
full width where it stands alone** (owner, 18 Oct 2026, many times: "make the buttons stand out,
hard to miss, look like a button with prominent text ... make the text white"). Light fill
`#0B5A43` (white 8.2:1), dark fill `#17805A` (white 4.9:1; 3.2:1 or more against every dark
surface, so the shape stands out too). `ColorContrastTest` asserts both.

**The full specification, with every property, the inventory and the checks, is §4.1.** It
replaces what this paragraph used to say: until 18 Oct 2026 it allowed text links for "Skip for
now and use Makkah", "Details" and "Save as PDF". That contradicted the owner, and it is how
about 15 actions stayed as easy-to-miss green text after he had asked for solid buttons. The
owner then chose (18 Oct 2026): every action solid, only "Close" and "Got it" stay text.

> History, so nobody softens it again: outlined until 18 Oct 2026 ("easy to miss"), then tonal
> for one day ("do you think this button stands out?"), then this. Hierarchy between two buttons
> comes from position and wording, never from making one of them faint.

**Actions are solid buttons; the only links are "Close" and "Got it".** Both are labelLarge
(SemiBold), so they read as tappable without a guess. Rows that open something carry a chevron.

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

- **24 Oct 2026, the Qibla restyle (step 6, Fable's design, prototype built; the owner has not yet seen the pictures).**
  Detail: `docs/plans/2026-10-release-1.6.0/STEP-6-DESIGN.md` (section 14 wins where it differs).
  * **The direction first.** Under the title, "Turn right 58°" in `headlineMedium` with tabular figures, start
    aligned like the city on Times; on arrival it turns green with a tick, the same size so nothing jumps. **"Facing
    60°" stays on arrival** (with a compass): it now sits above the dial, and a line vanishing there would move the
    dial at the very moment it animates. Q1 (start aligned or centred over the dial) is Ali's from the pictures.
  * **The dial.** The card lift, a rim band that thickens and turns green on arrival, a rounder needle (filled and
    stroked with round joins), and new shares: ticks 0.84 to 0.92, letters 0.77, Kaaba 0.60, arc 0.11 thick inside
    the band. Face and rim animate at `Motion.standard()` in and `Motion.quick()` out. The one buzz and its guard are
    unchanged. Q2 (lifted or flat) is Ali's.
  * **Details.** Distance and bearing sit behind a compact solid "Details" button with the Notice chevron, "Hide
    details" when open, closed on each fresh open. **Miles or km by the phone's region** from a plain list (US, GB,
    LR, MM, the CLDR data), not ICU: one path that unit tests and an iPhone can share, and `android.icu` is banned
    in `:core` anyway.
  * **No location, and the Makkah fallback, show the amber "Set location" notice** (same door as My prayers; Q3 is
    Ali's). The fallback counts as no location because the bearing from Makkah to Makkah is 0 degrees: the screen
    would say "You are now facing the Kaaba" whenever the phone pointed north. A defect fix, not taste.
  * **Two notice slots.** "No compass" is permanent, so it sits above the dial beside the number it explains.
    Calibration and "may wander" come and go in the hand, so they sit below Details and never move the dial.
  * **No sky on Qibla.** The sky answers which prayer period it is, a Times question; the dial's `surfaceVariant`
    face would lose its edge on a tinted page, and the green arrival fill would sit on a green day sky.
  * **Rejected:** numbers on the dial face (sat on the needle, removed once already); a bare "i" for details; the
    place name as a second door to the location sheet; hiding the degree number as Pillars does; a teardrop needle;
    artwork. **Comparison switches** `qiblaCentreInstruction` and `qiblaFlatDial` in `QiblaScreen.kt` are marked
    TEMPORARY and go when Ali has chosen.

- **24 Oct 2026, the skies (step 5, Fable's design, prototype built; the owner has not yet seen the pictures).**
  Detail: `docs/plans/2026-10-release-1.6.0/STEP-5-DESIGN.md` (section 14 wins where it differs).
  * **Built as recommended, until Ali answers the four questions.** Option D: the sky is a backdrop behind the
    header and the unchanged card. One addition to the header: the sky mark at the end of the city row, a sibling
    of the clickable city (never inside it), 104 by 36dp, the row's own height, so the header adds no height. The
    mark is a glyph riding a shallow arc of five dots, the five prayers, spaced evenly by prayer and not by clock
    (Fajr to Dhuhr is six hours, Maghrib to Isha ninety minutes). The passed part is green and stops 11dp short of
    the glyph. It hides when the city name needs the room (`Sky.markFits`) and always at 2.0 text. No period word
    on screen; TalkBack hears "Dawn, until 7:40 am" (`sky_now_a11y`, which replaces 9.6's wording).
  * **Glyphs.** The app's own widget drawings (`widget_glyph_fajr`, new `sky_glyph_day`, `widget_glyph_maghrib`,
    `widget_glyph_isha`), so step 8 puts the same four on the widgets. Ali chose them over the row icons (WbTwilight,
    WbSunny, WbTwilight, NightsStay) from picture 8 on 24 Oct 2026 ("Widget drawings"); the comparison switch is gone. The 23 Oct entry's "sun, moon, stars, the day path with the now dot" is built as: sun or moon
    as the glyph, the one star inside the Isha glyph as the stars, the arc as the day path and the glyph itself as
    the now dot; a separate star field was considered and dropped as clutter. The sun is `primary`: no third
    depictive colour.
  * **Motion.** The crossfade is one `drawBehind` lerp at `Motion.draw()` (no animation on open, a change mid
    fade starts from what is on screen); the arc draws in once per process (`Motion.draw()`). Row 8 is built here:
    a prayer row's fill and bar fade to the next row at `Motion.standard()`, read inside `drawBehind`.
  * **Layout.** The Scaffold no longer takes the top inset; Times draws its sky under the status bar and Qibla, My
    prayers and Settings take the same inset one level down. Everything is capped at 600dp and centred
    (`Modifier.readingWidth()`, Q18 and Q20), the sky full width; a phone in landscape gets it too.
  * **Rejected, with reasons.** A visible period word (a fifth text on a calm header); a star field, skyline or
    mosque silhouette (clutter, and a skyline is the stock prayer app look); a yellow sun (second depictive colour);
    dots by clock time (they bunch); the arc on its own band (costs the no scroll rule at 823dp); the mark beside
    the date line (too narrow in most languages); Material Symbols as the default glyphs (offered as Q2).

- **23 Oct 2026 (night), the My prayers tab (step 1, Fable's design, Ali approved from pictures).** Ali chose
  "B: calendar grid" and then "Yes, build it". Detail: `docs/plans/2026-10-release-1.6.0/STEP-1-DESIGN.md`
  (section 9 wins where it differs).
  * **Chosen.** A fourth tab, with the tick at the start of each prayer row, a week card of seven rings, and
    Last 30 days as a calendar grid in weeks of seven, replacing the old list of day cards (Ali chose B).
    Weeks wholly before tracking began are dropped, so there are no rows of empty numbers to read as misses; the
    week holding the start day stays. Pausing keeps the ticks already given: it means "do not count what is
    still to come", never "forget what I prayed". The tab icon is `CheckCircle`. Day names are always `SHORT`, on one
    line, shrinking to 70 percent and then ending in an ellipsis where a cell is too narrow (Urdu at 320dp and 2.0
    text); never `NARROW` (Fable, after the review showed NARROW gives Latin letters on an Urdu screen). Switching
    tracking on again starts the rings afresh unless something was ticked in the last 30 days. A paused day's ring
    shows only the pause mark; the sheet's row for that day shows its earlier ticks, disabled, under "Paused".
    Known and accepted: at 320dp and 2.0 text the Turkish tab label "Namazlarım" wraps inside the word (rare; the
    icon carries it; revisit only if seen on a real phone). Grid cells are
    about 45.7dp wide at 320dp because the sheet's side padding is dropped for the grid, which meets Apple's 44pt.
  * **Rejected.** A list of 30 day cards: long to scroll, and it reads newest first, against every calendar.
    `TaskAlt` for the tab icon: it has no filled twin for the selected state. `NARROW` day names, anywhere:
    "T T" and "S S" are ambiguous, and Urdu's are Latin letters. Per language two letter abbreviations of our own:
    invented words in Urdu and Arabic are a religious wording risk for no gain.
  * **Exempt from contrast, on purpose.** The 38 percent ring for days to come and prayers not due yet is a
    disabled control (WCAG 1.4.11), so `ColorContrastTest` has no pair for it.

- **23 Oct 2026 (night), motion rules for 1.6 (Fable; Ali approved).** Written down before any animation
  is built, so the eight new animations of 1.6 (BUILD-INSTRUCTIONS §6 rows 1 to 4 and 6 to 9) feel like one app. Detail: §2 Motion and
  `docs/plans/2026-10-release-1.6.0/BUILD-INSTRUCTIONS.md` §6.
  * **Three durations, not four.** `Quick` 150, `Standard` 250, `Draw` 400. A fourth, `Reveal` 300, was
    dropped: 50 ms from `Standard` is below what anyone notices, so the choice would drift by mood, the way
    gaps did before `Space`. Rejected: leaving durations as numbers at each call site.
  * **No entrance animation.** Nothing moves when a screen opens; only a touch, a sensor or a real event
    moves something. Rejected: a fade or slide on arrival, because it makes every screen slower to read.
  * **Haptics only on the user's own tap that changes something stored.** Never on events the app causes.
    Two shipped exceptions are kept: the tab bar's tick on a tab change and the Qibla's buzz on alignment.
  * **Material 3 `MotionScheme` and `MaterialExpressiveTheme` considered and not used.** Switching either on
    changes the motion of every component at once, and Ali has not seen that. Revisit only with screenshots
    or video in front of him.
  * **Two shipped values moved to tokens:** the fold chevrons from 200 to 150 ms (`Quick`) and the theme thumb
    from 240 to 250 ms (`Standard`). Nobody will see the thumb change; the arrows turn a little faster.

- **23 Oct 2026, the 1.6 design direction (Pillars as a reference for thinking, our green kept).** The
  owner answered three questions in chat. **Decided, not built.** Each choice is shown to him on screenshots
  before it ships (§10). The reference study is in `docs/plans/2026-10-release-1.6.0/PLAN.md` §0.1.
  1. **Four fixed skies, with an animated change between them.** Dawn, day, evening and night. Each one is
     a fixed gradient, so `ColorContrastTest` checks every ink against every stop of all four, the way it
     checks the hero gradient today (§1). The change between skies is animated. The sky has drawn
     artwork (sun, moon, stars, the day path with the "now" dot) that shows which prayer time it is, as well
     as the words.
     - **The prayer name and the time left keep a prominent, readable place on the sky.** The owner made
       this point himself: "I used the gradient to have a prominent space for the reader to read and
       understand the time of which prayer is current." The artwork never sits behind those words.
     - **Rejected:** a sky that changes colour continuously through the day, because contrast cannot be
       asserted on a moving background; keeping the single gradient with only the path moving, because the
       owner wants the time of day shown in pictures; weather in the sky, because it needs the network.
  2. **A bell on each prayer row: one tap turns that prayer's alert on or off.** This reverses §7's
     "no per-row mute", which was a choice made when the app first took its design from Claude Design. It
     was not a rule. The owner's reason: the user gets the feature with nothing added, and the screen
     stays uncluttered.
     - **One tap and nothing else.** No long press and no per-row sound sheet. Sounds stay in Settings.
     - The bell shows its state by shape (filled or crossed out) as well as colour.
     - It has a 48dp target and a content description saying the prayer and the state.
     - Settings keeps the same choice too, so both places show the same setting.
  3. **No icon buttons side by side.** Where the tracker needs more than one action at the top (pause,
     Last 30 days), each is a **labelled compact solid button with its icon** (§4.1 kind; not a new chip component), with a clear gap between them and a 48dp target.
     - **Rejected:** Pillars' two unlabelled round icons, because they are easy to mis-tap and unclear (the
       §10 review question "Is any icon button sitting next to another?").
  4. **Qibla: modern look in our theme** (Pillars' Qibla screen as the reference).
     - **The direction is the message.** One large line: "Turn right 58°", then "You are now facing the Kaaba"
       on alignment (the shipped `qibla_facing`).
     - **The place name is optional,** and small when shown.
     - **Distance and bearing move behind a labelled details button,** never a bare "i".
     - **Distance is in km or miles, by the phone's region.** Today it is km only (`qibla_subtitle`).
     - **The key (Kaaba and needle) stays on screen,** because it is what helps a first-time user.
     - **These stay:** the green turn arc, the alignment vibration and the calibration warnings.
     - The dial stays its current size (room for the instruction under it), with a thicker soft rim and a rounder needle.

- **19 Oct 2026, "Match your mosque" can be set from the mosque's board: one button at the top.**
  From the 8 Oct 2026 branch, whose version made the minutes number an underlined, tappable link (now
  banned, §4.1). Three solid button versions were drawn on the emulator in light and dark, and the
  owner chose with AskUserQuestion: **one full width "Use your mosque's board" above the list**, which
  opens "Which prayer?" (each prayer with the time it shows now) and then the clock. The minutes are
  worked out (`AdjustmentFit`); a board time more than 30 minutes away is refused and explained under
  that prayer with "Choose a calculation method" (§5.17 of HANDOVER: a gap that size is a method or a
  congregation board). Rejected: a time button under each prayer ("5:53 am") and "Shows 5:53 am" with
  "Set time" under each prayer: both added six green buttons to a calm dialog. The clock's chosen a.m.
  or p.m. is drawn in `primaryContainer`, not the stock amber, because amber means a warning here.
- **19 Oct 2026, the welcome screen offers the language list.** From the 8 Oct branch (a bare globe
  icon). The owner chose from screenshots: **a compact solid button in the top end corner with a globe
  and the current language's own name**, so someone who cannot read English sees a way out before the
  first question. Rejected: the bare globe icon (easy to miss, and a seventh kind of control) and a
  "Language" button under Begin (the word itself is English). The Settings Language row stays under
  Theme, as before: only its subtitle changed ("Phone language · English" until a language is chosen).

- **18 Oct 2026 (Mac release check), the tab bar made 97% solid.** Found on the emulator while
  retaking the store screenshots: at 90% the text and buttons scrolling under the floating bar were
  readable through it and ran into its labels, clearest in dark (a notice's amber "See why" behind
  "Times" and "Qibla"; Isha's time behind "Settings"). Measured over the bar: +19 shades at 90%, +4 at
  97%. The owner chose 97% from before and after screenshots, asked with AskUserQuestion. Rejected:
  keeping 90% (the collision is a legibility fault, not a look); fully solid (loses the glass he chose
  on 17 Oct). Still true and not changed: content is fully visible in the strip between the floating
  bar and the bottom edge, which is how a floating bar works.
- **18 Oct 2026 (Mac release check), the large Today widget's Hijri line shrinks to fit.** Found
  while taking the store widget screenshot: on a 5 column home screen (the Pixel 2 profile) the town
  chip left the Hijri line too little room and it read "2 Rabi al-Awwal 14…". First offered and
  approved: wrap onto a second line; **then withdrawn the same hour**, because measured on the launcher
  at 4 by 3 the extra line pushed Isha off the bottom. Chosen by the owner instead: one line that
  autosizes from 12 down to 9sp (`widget_today_large.xml`). Rejected: leaving the year cut; dropping
  the year (a longer month such as "Jumada al-Akhirah" would still be cut); raising the large layout's
  height threshold (4 by 3 users would lose the countdown card). Android 7 ignores autosize and keeps
  the ellipsis. **9sp is deliberately below the floor** (14sp in §3, 12sp in the widget research): it
  applies to this one line only, only when it would otherwise be cut, and the owner chose it from
  screenshots, so do not raise the minimum without asking him. Known and cosmetic: with a wrap_content
  height the shrink can stay until the launcher redraws the widget from scratch (a resize, a restart),
  so a shorter month can show a little small; it is never cut.

- **18 Oct 2026 (night), D5 built: the gaps between text and buttons on the scale, on every phone screen.** The owner, asked whether to do
  it before 1.5.0: "I told you about the design system ... you should know the clean, spacing, and
  premium look of the app." So it was done in one pass across every screen (setup, Times, Ramadan
  card and timetable, export sheet, location sheet, Qibla, Settings and every dialog): 88 hand picked
  gaps replaced by tokens by relationship (the table in §2), and `DesignRulesTest` fails on a new one
  (mutation tested). An independent review caught the first write-up claiming "every vertical gap"
  while four paddings and the watch were left; the four were converted, the check widened, the claim
  narrowed (§0.3 again). Visible changes, so nobody is surprised: setup's Back and Continue 24dp under
  the cards (was 8); every Settings dialog's help text 16dp above its choices (was 8, one was 16); the
  Hijri help 16dp above its stepper (was 4); the welcome button 24dp under the tagline (was 48) and the
  Bismillah 24dp above the name (was 32); the confirm step's gaps 24, 4 and 8 (were 32, 8 and 12); the
  permission step's progress line 8 under its button (was 20) and the located row 16 (was 20); the
  location sheet's text 16dp above its field (was 12) and the progress line 8 (was 16); the Ramadan
  card's header 12 above the times (was 10), the countdown to the fast line 4 (was 6), small lines 4
  (were 2); the export sheet title to its text 4 (was 6); Settings rows 4 between title and subtitle
  (was 2), the folded row's text 16 above the next row (was 14); the tab bar icon to label 4 (was 2);
  Qibla's "set your location" 24 from the top (was 48). **Shown to the owner as screenshots before
  merging** (`docs/plans/2026-10-times-gloss/d5-*.png`), as D5 was agreed. **Approved:** "the
  screenshots look correct to me and I am happy to proceed further" (owner, 18 Oct 2026). Rendered in light, dark, Urdu and 2x text (`docs/plans/2026-10-times-gloss/d5-*.png`).

- **18 Oct 2026 (late night), from his emulator screenshots: Save as PDF, place wording, sounds.**
  - *Save as PDF* "too large and squashing": a compact size, 40dp drawn and 48dp to touch, for every
    button that shares a line with text (Save as PDF on Times and in the Ramadan timetable, Shift the
    date on the Ramadan card). Times gaps moved to the scale at the same time:
    `Space.Group` between blocks, `Space.Related` from the Today heading to its list.
  - *Place wording*: he asked for "your nearest region" and then for help ("city, town, village ...
    something suitable for the masses"). Chosen, with his request for help as the brief: heading
    **"Or type where you are"**, box **"Town, city or village"**, hint **"For example: Karachi,
    London, Makkah, New York"** (his examples, in every language), not found **"... or type the
    nearest town"**, button **"Find place"**. Rejected: "region" (vague, translates as province, and a
    province name gives times for its middle, minutes out); "city" (excludes villages, his objection).
  - *Sounds*: "From your phone" already existed but sat below a fold at the foot of Prayer alerts, so
    he never saw it. He chose (AskUserQuestion) **each sound its own Settings row** under Prayer
    alerts, showing the current choice, over moving it to the top of the dialog or adding a scroll
    hint. He then asked for a sound choice for notifications too ("adhan or another sound for an
    alarm or notification"): a **Notification sound** row beside **Alarm sound**, each offering Adhan
    or From your phone. Nobody's sound changes until they pick one. Found by the independent review
    and fixed before he saw it: the phone picker's "Default" row now means the default for that sound
    and is saved as blank, and only one "Prayer times" channel exists at a time.
  - *Vibration*: every prayer notification, alarm and reminder already vibrates. He chose **"follow
    the phone"** (Ring: sound and buzz; Vibrate: buzz; Silent: nothing; Do Not Disturb: nothing unless
    the alarm's Play on silent is on). Rejected: forcing a buzz on Silent (overrides the person's mute).
  - *Ramadan card*, after the screenshots ("all good but"): the timetable button says **"Get the full
    Ramadan timetable"** (was "Full timetable") and **fills its row** beside the (i), at 52dp. Then,
    "spacing looks tight": 24dp above and below it, the (i) centred beside it.
  - *Panel stops the adhan* (Android's rule, found on his emulator): he chose to **tell users briefly
    and change nothing else for now**. The Notification sound pop up's help ends "Pulling down your
    notifications or swiping the notification away stops the sound". Rejected for now: the app playing
    the adhan itself so the panel cannot stop it (a different design, his decision if ever).

- **18 Oct 2026 (night), D2 decided, and D1 and D3 approved from screenshots.** Usage question: the
  owner chose **B, bold links**, over A (solid buttons under "Before you decide" and "Your answer"
  headings), from the two drawn side by side in light and dark. "See exactly what is sent" and
  "Read the privacy policy" are bold, underlined green links (kind F, `InfoLink`), 16dp under the
  text; Yes and No stay equal solid buttons 24dp below. So finding out more never looks like an
  answer. This is the **only** exception to "every action is a solid button", allowed on this
  screen alone; `DesignRulesTest` fails if `InfoLink` appears anywhere else. Rejected: A (four
  solid buttons read as four answers even with headings). He also approved the setup screen (Makkah
  skip at the foot) and Match your mosque (Reset all full width, the steppers as they are): "happy
  with it".
- **18 Oct 2026 (late evening), the owner's answers to D1, D3, D4 and D5.** D1: "Skip for now and
  use Makkah" stays solid but moves to the foot of the setup location screen, under "Prayer times
  will be for Makkah until you set your location." (his choice of the recommended option). Pinning it
  outside the scroll was built first and took a quarter of the screen at 2x text, so it sits at the
  foot when there is room and after the content when not. D3: he kept the fourteen steppers and asked
  for "Reset all" centred, in its own row and wider: it is now full width, 24dp under the steppers. D4:
  "happy with" notices as they are (Allow and Details equal in weight; order and the arrow lead). D5:
  spacing to be converted screen by screen, each with light and dark screenshots, after this merge.
  D2 followed (next entry up).
- **18 Oct 2026 (evening), every action a solid button, and this document made the place that
  stops repeat mistakes.** The owner, after the third round on buttons: "You always say it's done
  but nothing is done." Asked, not guessed (§0.2), he chose: every text link becomes a solid
  button except "Close" and "Got it" at the foot of a dialog; warning buttons stay amber; the
  minute steppers go solid green; screenshots before merging. At the same time every solid
  button was given one label style (16sp SemiBold; notice and permission buttons had been 14sp)
  and heights by placement (56, 52, 48), and the location sheet's "Find city" stopped being a
  grey placeholder. Reversed: the 18 Oct entry below that rejected "the full green fill for
  secondary actions", and the §8 list of allowed text links. Added: §0 (when to ask, and the
  words "done" and "every"), §4.1 (the full button spec and inventory), §11 (open decisions),
  and a `DesignRulesTest` check that fails when a file with a button is missing from the
  inventory.
- **18 Oct 2026, notices restructured, dialogs given a close X, spacing made a scale.** From the
  owner's emulator screenshots. Rejected: keeping the chevron in the header and moving the X
  away from it (still two icon buttons in one corner); making the whole card the fold target
  (it was, and nobody knew; a labelled "Details" button says so); the notice action as a bold
  link (he could not see it as a button); an X instead of the bottom "Close" (he wants both).
- **18 Oct 2026, the Times card made glossy, calls to action made solid, setup location screen
  shortened**, each approved by the owner from screenshots. This extends the widgets' gloss override
  to the next prayer card (fixed gradient, opaque, tested; still no colour that moves with the time).
  Rejected: a graphite glass card in dark (the widget body colour; the green card is what he pointed
  at and keeps "green means the next prayer"); the full green fill for secondary actions (two filled
  buttons on one screen lose the main one; **reversed the same evening by the owner, see above**); keeping the 66 word city lookup paragraph above the empty
  field (it hid the choice); putting it only behind "How your location is used" (missed at the moment
  of typing, the independent review); showing it only once typing starts (text jumping in under the
  cursor; the owner found it confusing). Chosen: the standard short notice plus detail, one line in the
  city box's hint area, always visible, with the full detail behind the link.
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

- **19 Oct 2026, the emulator checks left from the 8 Oct merge** (owner: test whatever is left, do it
  autonomously). Found and fixed, each seen before and after on the emulator: words split at large
  text (`WholeWords`, §3); the theme selector's ends cut the first letters once a label wrapped (fixed
  24dp and 28dp radii, the same pill at normal size) and its height was measured before the shrink
  (`matchParentSize`); wrapped button labels sat at the start edge (centred); the board time clock in
  landscape covered its own minutes (always the upright layout, the dialog scrolls); Match your mosque's
  plus and minus said nothing to TalkBack (the value is a polite live region, "Dhuhr, +2 min").
  Rejected: shorter Turkish and Indonesian words (English splits too on a 320dp phone, so the cause is
  the layout, not the language); capping the font size (the clock's 1.3 cap is for fixed size boxes;
  these labels can shrink a little instead); one line labels ("Follow phone" would need well under
  half size on a 320dp phone at 2.0; reasoned, not run).
- **17 Oct 2026, research pass (FINDINGS.md):** granted permissions show nothing on Settings;
  every card is 24dp, notices included; dialog section labels speak in the Settings section
  voice. Kept against the research: the iOS style chevron (the owner asked for an iPhone feel).
  Rating is Google's card only, never a component of ours.

- **24 Oct 2026, 1.6 step 3, the bell and the permission card (Ali, from pictures; `STEP-3-DESIGN.md` §15 to §21):**
  the bell is a plain filled bell (on), an alarm clock (alarm) and a crossed bell (off), not `NotificationsActive`
  (its sound waves say "ringing now" and break the crossfade); grey `outline` while the phone blocks notifications,
  with an amber notice to explain. Settings' alerts dialog Off also remembers the style but never touches the
  reminder: only the bell carries Q6. A fourth message key covers Fajr in Ramadan with a reminder removed, because
  joining two sentences in code breaks Urdu and Arabic. **The permission cards:** no switch (only the phone can
  allow notifications, so an in-app switch springs back; Apple's own pattern is a message with a button to
  Settings). Ali then asked for a nicer look: warning notices get the hero card's gloss (sheen, rim, the new Warn
  tokens in §1, contrast pairs in `ColorContrastTest`) minus its glow; quiet notices stay flat because the dark CTA
  on `secondaryContainer` is only 2.32:1 and a sheen would drop it to 1.64:1 (that 2.32:1 is itself an open gap,
  HANDOVER §11); every notice's icon is bare, 24dp, no tinted circle; the two permission cards show one button,
  Allow, because their details only repeated the sentence (the Fajr fact moved into the exact alarms sentence);
  the notifications card shows the crossed bell, exact alarms keeps the triangle. Rejected: a switch; a text
  Details link; green CTA on amber (3.2:1 dark); a full width amber button (17 Oct); a glow under the card;
  glossing the quiet tone; a white card with an amber bar (loses the amber card Ali kept).
- **24 Oct 2026, 1.6 step 4, the Timetable (`STEP-4-DESIGN.md`; Ali's answers Q1, Q2, Q4):** "Timetable" with the
  calendar glyph (`CalendarMonth`, the one that already means "the month" on Last 30 days) replaces "Save as PDF" on the
  Today line and opens a full height sheet: this month and the next as day cards (a six column table breaks at 320dp
  and 2.0, which is why cards), opened on today's card, today in `primaryContainer` with a "Today" pill. Month
  headings are pinned on the sheet's own colour. Past days are not dimmed (Pillars fades them and fails AA; a faded
  day reads as a day to feel bad about). No Hijri date on cards (Q4). Inside Ramadan with the card on, the same door
  opens the approved Ramadan table from day 1 to the last day (Q1); someone who hid the card gets the month view and
  still has "Ramadan 1448" in the PDF chooser. Save as PDF moved into the sheet's header, which the Ramadan table
  shares (so it gained the close X and a `headlineSmall` title); the existing chooser opens over the sheet and the
  sheet stays after a save. Times across a card follow one rule, `floor(width / (84 x fontScale))` clamped to 1..3
  (`TimetableGrid`), the Dynamic Type equivalent. Rejected: a rolling 30 days (no month heading, and "This whole
  month" in the PDF would not match the screen); a segmented "Month | Ramadan" control (a component the system does
  not have); a dialog chooser; a FAB; `timetable_month_heading` (the heading is the month name from the date
  formatter). Q8 (Ramadan as cards or the table): **Ali, 24 Oct 2026, "Keep the table"** from side by side pictures
  (`plans/2026-10-release-1.6.0/screens/step4/9a` to `10b`): the table fits about seventeen days on a screen against
  about four cards, and suhoor, iftar and the fast compare day to day. The card prototype was deleted.
- **24 Oct 2026, 1.6 step 6, the Qibla dial in the Pillars style, in our theme (Ali; `STEP-6-DESIGN.md` §15 and §16):**
  Ali: "I like pillar app qibla locator style ... I want that style of compass and needle but in my own theme."
  The dial is now a lifted bezel with the face inside it, sixty fine ticks, quiet letters and a teardrop needle, in
  our palette (the green N, the Quiet Glass lift). **The teardrop reverses the old §1 rejection of one**: Ali asked
  for that style. **The turn arc is removed**: "Turn right 58°" already says it in words and a number, and the
  Kaaba's side of the dial says it without; arrival keeps three signals that are not colour (sentence with tick,
  thicker bezel, buzz). The bezel takes a different role per theme (`dialBezel()`: `surface` in light,
  `surfaceContainerHighest` in dark) because the card hairline is invisible against the dark page. The pin and key
  marks are sized from the text line so they grow with it. Pairs added to `ColorContrastTest` (`outline` on both
  faces, `onSurfaceVariant` on the aligned face). Open for Ali's pictures, behind temporary switches: Q1 instruction
  placement, Q2 sand or white light bezel, Q4 keep the arc as the bezel band, and whether the dark bezel needs
  `surfaceBright`. Rejected: a hairline at the bezel's inner edge (1.37 and 1.32, neither clears 3:1 nor stays
  invisible); a hub.

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
in Urdu right to left? **Added 18 Oct 2026, because the owner had to repeat them:** Is every gap
a `Space` token chosen by relationship? Is any icon button sitting next to another? Is every call
to action a solid button? Does every dismissable dialog have the X? **Is the build he will test the
one these screenshots came from** (an emulator still running yesterday's install showed none of
the day's changes, and that cost a round of his trust)?

**Added 18 Oct 2026 (evening):** Did any choice here depend on a guess about what he wants
(§0.2)? If so, did you ask? Is every button in the §4.1 inventory, with the right kind, height and
width? Did you run the §4.1 checks and paste the counts before writing "every" or "done"? Is any
§11 item still OPEN on the screen you changed?

---

## 11. Open decisions: waiting for the owner

Anything here is **not decided**. Do not build an answer to it, do not describe it as done, and
raise it with him (§0.2) the next time the screen is touched. When he answers, move it to §9 with
the date and his words, and delete it here.

| Id | Raised | Question | Recommendation given to him | Status |
|---|---|---|---|---|
| 1.6-timetable | 24 Oct 2026 | Timetable on Ramadan from day 1 or 30 days before; this month or this and next; Hijri on cards? | Day 1; two months; no Hijri | ANSWERED 24 Oct 2026: Ramadan view from day 1, this and next month, no Hijri on cards (built, step 4; remove this row when merged) |
| 1.6-dark-skies | 23 Oct 2026 | Dark versions of the four skies, or the same as light? | Dark versions; decide from four pairs | DECIDED 24 Oct 2026 from the eight pictures: dark versions, night kept this deep ("Keep it this deep"); also four light hues kept, light skies kept pale, widget drawings as glyphs |
| 1.6-ramadan-cards | 23 Oct 2026 | Ramadan timetable as day cards or the approved table? | Cards; decide side by side | ANSWERED 24 Oct 2026: keep the table (Ali, from step 4 pictures) |
| 1.6-mosque-door | 23 Oct 2026 | Keep "Different from your mosque?" under the times? | Keep | OPEN: screenshots at step 7 |
| 1.6-language-row | 23 Oct 2026 | Language row subtitle lists five scripts? | Yes | OPEN: screenshot at step 10 |

**Decided, work not yet done** (do not report these as done):

| Id | Decision | Next step |
|---|---|---|
| 1.6-sky | (Dark variants are OPEN until the owner sees four pairs.) Four fixed skies with animated change, the prayer name and time left on a prominent readable area (§9, 23 Oct 2026) | Prototype built on `claude/1-6-0-step5-proto` with the 88 contrast assertions; pictures next. Ali answered the four STEP-5-DESIGN §13 questions on 24 Oct 2026, all as recommended |
| 1.6-bell | One-tap bell on each prayer row (§9, 23 Oct 2026) | **Decided and built (24 Oct 2026)** on `claude/1-6-0-step3-proto`; Ali approved the prototype and the card; merge pending (HANDOVER §11) |
| 1.6-chips | Labelled compact solid buttons with a gap, never icon buttons side by side (§9, 23 Oct 2026) | Same |
| 1.6-tablet | Content capped at 600dp and centred on tablets, the sky full width (owner, 23 Oct 2026) | Built in the step 5 prototype (`claude/1-6-0-step5-proto`); becomes built when he says yes to the pictures |
| 1.6-qibla | Qibla restyle: direction line first, labelled details button, km or miles, key kept (§9, 23 Oct 2026) | Same |
