# 10. New feature playbook

The order of work for any new feature, screen, sheet, dialog, widget or setting. Follow it start to
finish; each step exists because skipping it once cost the owner's time or trust.

## 10.1 Before any design: should it exist?

Answer all five in the plan, in one line each:

1. **Who is it for, and what do they do today without it?** If nobody asked and nothing is hard
   without it, it is not needed.
2. **Does it fit the charity rules?** No ads, accounts, own server, network for times, revenue, or
   anything about belief sent anywhere (`CLAUDE.md` hard rules). A feature that needs any of these is
   not built.
3. **Does it add pressure?** Streaks to protect, badges, reminders to open the app, counts that shame:
   no. A prayer record is private worship. Calm, never nagging.
4. **Does it work for the hardest user?** Offline, old phone, default settings, Urdu, 2x text.
5. **iPhone effect**, one line: *none*, *carries over*, or *Android only, because* (widgets, Wear,
   exact alarms are Android only and labelled so).

Then: is any part of it an OPEN item in `docs/DESIGN_SYSTEM.md` §11? If yes, stop and ask.

## 10.2 Who does what

From `docs/HANDOVER.md` §5.28 and `.claude/agents/`:

| Step | Who |
|---|---|
| Journeys, what the screen shows and in what order, icons, wording, every state | `ux-lead` (Fable), reading this folder first |
| Checking the plan against the code before work starts | `plan-verifier` (Fable) |
| Building a step whose spec is written | `implementer` or the main session |
| Reviewing every diff before commit | `reviewer` (Opus) |
| Choosing anything visual | **The owner**, from screenshots |

## 10.3 Design: decide with existing parts first

Work down this list and stop at the first that fits. Each step down needs a stronger reason.

1. **An existing screen, with a row or card added.** Most features are a Settings row, a notice, or a
   line on a card. See [`05-screens-and-features.md`](05-screens-and-features.md) for where things
   live.
2. **An existing component** from [`03-components.md`](03-components.md): `CtaButton`, `Notice`,
   Settings group and row, `DialogTitle` dialog, bottom sheet, `Tick`, `Bell`, switch row, radio row,
   stepper.
3. **An existing pattern** from [`04-patterns.md`](04-patterns.md): chooser dialog, explainer dialog,
   sheet with a close X, undo snackbar, permission card, folded details.
4. **A new component**, built from tokens only, specified here (anatomy, metrics, states, a11y, RTL,
   SF Symbol, SwiftUI equivalent) **before** it is built. Ask the owner if it is visible.
5. **A new tab.** Only with the owner's explicit decision. There are four tabs today; Apple's guidance
   tops out at five.

Placement rules that decide most questions:

- Something the user acts on daily goes on Times or My prayers. Something set once goes in Settings.
- A permission or system problem is a `Notice`: warning tone on Times (closable) and in Settings
  (permanent until fixed).
- An explanation is folded behind "Details" or a question shaped button, never shown by default if it
  pushes the six prayer times off the first screen.
- **Times must still show the city, the next prayer card, all six times and the mosque button without
  scrolling** at default text on an 823dp tall phone. Measure it.

## 10.4 Spec: write it before code

Copy this into the plan for each screen or component:

```
Name:
Purpose (one sentence):
Who sees it, when (conditions, defaults):
iPhone effect (none / carries over / Android only because):
Content, top to bottom (component, token, string key, exact English):
Gaps between each (Space token and the relationship that picks it):
Actions (kind A to G from DESIGN_SYSTEM §4.1, height, width, order):
States: first run / loading / empty / no location / permission missing / paused / offline / error:
Motion (which Motion token, what triggers it, or "none"):
Haptics (which, on which tap, or "none"):
Accessibility (TalkBack label, role, state, heading, order, 48dp targets):
RTL (what mirrors, what must not: clock faces, the Qibla dial, numbers):
Large text (what wraps, what shrinks via WholeWords, what scrolls):
Icons (Material name + SF Symbol name):
New strings (keys, English, and all four translations: ar, in, tr, ur):
Data (stored where, format, portable to iPhone?):
Open questions for the owner (with recommendation first):
```

## 10.5 Build rules

- Colours from `MaterialTheme.colorScheme` or a function in `Theme.kt`. **No hex in a composable.**
- Gaps from `Space`; durations from `Motion`; type from `MaterialTheme.typography` (only the ten
  defined styles) or `PrayerTimeTextStyle` and `actionLabelStyle()`.
- Bounded surfaces use `Modifier.sajdaSurface(shape)`, never a hand made shadow or border.
- Read the theme from `LocalDarkTheme`, never `isSystemInDarkTheme()`.
- New logic (dates, counts, rules) goes in `:core` as plain Kotlin with no `android.*` import.
- Stored data as plain values or JSON.
- Anything that names a language goes through `AppLocale.wrap` at the moment of use.
- Every scrolling tab screen ends with `BottomBarClearance()`.
- Name the new UI file in backticks in this folder, or `DesignTokensTest` fails.
- A new button: add its row to the inventory in `docs/DESIGN_SYSTEM.md` §4.1, or `DesignRulesTest` fails.
- A new colour pair: add it to `ColorContrastTest`.
- Mark deliberate simplifications with a `ponytail:` comment.

## 10.6 Checks before you say done

Run, read every line, paste the counts in your reply:

```bash
# 1. Design, contrast and token tests
./gradlew :app:testDebugUnitTest --tests '*DesignTokensTest' --tests '*DesignRulesTest' --tests '*ColorContrastTest'

# 2. Every acting control, with file and line (DESIGN_SYSTEM §4.1)
grep -rnE "\b(CtaButton|PermissionButton|FilledIconButton|IconButton|TextButton|Button)\(" \
  app/src/main/java --include=*.kt | grep -v "fun \|import"

# 3. Text links: must print only action_close and action_got_it
grep -rn -A1 "TextButton(" app/src/main/java --include=*.kt | grep -oE "R\.string\.[a-z_]+" | sort | uniq -c

# 4. No hex outside the theme
grep -rnE "Color\(0x" app/src/main/java --include=*.kt | grep -v "/ui/theme/"

# 5. No hyphen or em dash in English app copy (check each hit is a name that needs one)
grep -nE "—|–| - |[a-z]-[a-z]" app/src/main/res/values/strings.xml

# 6. Full gate before merge (on the Mac)
./gradlew clean test lint :app:bundleRelease :wear:bundleRelease
```

Then, on the emulator, for every screen you changed: **light, dark, 2.0 text, Urdu RTL, 320dp
width**, and `./gradlew :app:installRtl` read as a layout check (ignore English prose in RTL). Show
the owner the screenshots, with the install command and the commit he should see. Answer the review
questions in `docs/DESIGN_SYSTEM.md` §10. Write down what you did not check.

## 10.7 After it ships

- Update [`05-screens-and-features.md`](05-screens-and-features.md) and any component page.
- Add a line to [`CHANGELOG.md`](CHANGELOG.md).
- Record decisions in `docs/DESIGN_SYSTEM.md` §9 and lessons in `docs/HANDOVER.md` §15.
- Retake `docs/store/screenshots/` if the feature is visible there.

## 10.8 Guidance for features likely to come

Not decided; patterns to reach for if the owner asks. Each still needs his decision.

| Possible feature | Reach for | Watch out for |
|---|---|---|
| Tasbih (dhikr counter) | Its own sheet from My prayers; one large tap area; `SegmentTick` haptic per count; count in `displayLarge` tabular | Signed off as a later candidate, not 1.6.0 or 1.6.1. No targets that shame, no streaks |
| Mosque finder or directory | **No**: needs network and a server | Breaks the charity rules |
| Duas or adhkar text | A reading screen in `bodyLarge`, Arabic in the system Naskh font, translation under it | Religious content: never machine paraphrase; source each text; disclaimer applies |
| Hijri calendar month view | Reuse the Last 30 days grid and Islamic dates sheet patterns | Hijri in `primary` only where it already is; follow the date shift setting |
| Qada prayers (missed prayers to make up) | Copy "Fasts to make up": a count with −/+ steppers in Settings and one compact button on My prayers | No shame wording; count never goes below 0 |
| More languages | `values-xx`, translated and checked by Claude; RTL if the script is | Packaged in the app (no language splits); `values-in` trap for legacy codes |
| Lock screen or iPhone Live Activity | Android: the existing notification. iPhone: Live Activity with the countdown | Android only vs iPhone only, label it |
| Tablet layout | Content capped at 600dp and centred, sky full width (already built) | Do not invent a two pane layout without the owner |
