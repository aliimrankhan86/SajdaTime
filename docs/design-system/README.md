# SajdaTime design system

The single reference for how SajdaTime looks, reads and behaves, written so that any person or AI can
extend the app, add a feature, or build the iPhone version without drifting from what ships.

Taken from **release 1.6.0** (branch `claude/1-6-0`, commit `1cdae4a`, 25 Oct 2026). The values in
`tokens/` are checked against the Android code on every build by `DesignTokensTest`, so they cannot
quietly go stale.

---

## Read this first (every session, every AI)

1. **The owner decides anything visual.** Ali chooses from screenshots. If no rule here decides a
   choice exactly, ask him before building (`docs/DESIGN_SYSTEM.md` §0.2 says exactly when). Silence is
   never a yes.
2. **Never invent a value.** Every colour, gap, size, duration and type style comes from `tokens/`. If
   you need one that is not there, that is a design decision: ask, then add it to the code and the
   token file in the same commit.
3. **Never write "done", "every" or "all"** until the checks in [`10-new-feature-playbook.md`](10-new-feature-playbook.md)
   §6 have run and you have pasted the counts.
4. **Build for the masses.** No signal, old phone, default settings, cannot read English, reads right
   to left. Dependable beats elegant.
5. **Keep the iPhone road smooth.** Use only patterns both platforms share. Every new icon has its SF
   Symbol named. See [`09-ios-mapping.md`](09-ios-mapping.md).

## Which file to read for which job

| You are about to | Read |
|---|---|
| Do anything visual at all | This page, then [`01-principles.md`](01-principles.md) |
| Pick a colour, size, gap, font or timing | [`02-foundations.md`](02-foundations.md) and the matching `tokens/*.json` |
| Use or build a button, card, row, notice, dialog, sheet | [`03-components.md`](03-components.md), then `docs/DESIGN_SYSTEM.md` §4.1 (the button inventory, enforced) |
| Lay out a screen, handle an empty, error or permission state | [`04-patterns.md`](04-patterns.md) |
| Change or extend an existing screen or feature | [`05-screens-and-features.md`](05-screens-and-features.md) |
| Write any words the user sees, or touch RTL and languages | [`06-content-and-language.md`](06-content-and-language.md) |
| Check accessibility | [`07-accessibility.md`](07-accessibility.md) |
| Touch widgets, the watch, notifications, the PDF, store pictures | [`08-surfaces.md`](08-surfaces.md) |
| Plan or build for iPhone, or check a design against Apple's rules | [`09-ios-mapping.md`](09-ios-mapping.md) |
| Add a new feature or screen | [`10-new-feature-playbook.md`](10-new-feature-playbook.md), start to finish |
| Change the design system itself | §"Changing the design system" below, and [`CHANGELOG.md`](CHANGELOG.md) |

## The files

```
docs/design-system/
  README.md                     this page: purpose, precedence, how to change it
  01-principles.md              identity, Quiet Glass, the owner's fixed rules
  02-foundations.md             colour, type, spacing, layout, shape, elevation, motion, haptics, icons
  03-components.md              every component: anatomy, metrics, states, a11y, iPhone equivalent
  04-patterns.md                screen anatomy, notices, dialogs, sheets, states, forms, undo, destructive
  05-screens-and-features.md    every screen and feature in 1.6, top to bottom, every state
  06-content-and-language.md    voice, wording rules, numbers and dates, languages, RTL
  07-accessibility.md           contrast, targets, text size, TalkBack and VoiceOver, colour blindness
  08-surfaces.md                widgets, Wear OS, notifications, PDF, launcher icon, store pictures
  09-ios-mapping.md             token, component and pattern mapping to SwiftUI and Apple's rules
  10-new-feature-playbook.md    the step by step for any new feature, with the checks
  CHANGELOG.md                  every change to this folder, dated, with the reason
  tokens/
    README.md                   format of the token files and how to consume them
    color.json                  every colour, both themes, Material role, usage (tested)
    typography.json             type scale with sizes, line heights, weights (tested)
    spacing.json                spacing scale and layout metrics (scale tested)
    motion.json                 durations, easing, motion rules (tested)
    shape.json                  corner radii, borders, elevation, component sizes (census)
```

## Code map: every phone UI file

Under `app/src/main/java/com/sajdatime/app/ui/`. `DesignTokensTest` fails if a UI file is not named
in these pages, so a new file means a new row here and a spec in the right page.

| Folder | File | What it holds | Spec |
|---|---|---|---|
| `theme/` | `Color.kt` | Every colour | `tokens/color.json`, 02 §2.1 |
| | `Theme.kt` | Material schemes, `sajdaSurface`, hero, warning, sky, Kaaba gold, dial bezel | 02 §2.1, 03 §3.0 |
| | `Type.kt` | Type scale, `PrayerTimeTextStyle` | `tokens/typography.json`, 02 §2.2 |
| | `Spacing.kt` | `Space` | `tokens/spacing.json`, 02 §2.3 |
| | `Motion.kt` | `Motion` | `tokens/motion.json`, 02 §2.7 |
| `components/` | `CtaButton.kt` | The solid button, `actionLabelStyle()` | 03 §3.1 |
| | `Notice.kt` | Warning and quiet notices | 03 §3.3 |
| | `PermissionAction.kt` | `PermissionButton`, `PermissionCard` | 03 §3.1, §3.3 |
| | `Common.kt` | `DialogTitle`, `SectionHeading`, `ProgressRow`, `LocationSheet` | 03 §3.1, §3.5 |
| | `ScreenFrame.kt` | `ScreenTitle`, `BottomBarClearance`, `readingWidth` | 03 §3.5 |
| | `WholeWords.kt` | Shrink rather than split a word | 03 §3.5 |
| | `Tick.kt` | The prayed checkbox | 03 §3.2 |
| | `Bell.kt` | The alert toggle | 03 §3.2 |
| | `DayRing.kt` | Week and history rings | 03 §3.3 |
| | `MethodChoices.kt` | `RadioRow`, `MethodChoiceList` | 03 §3.2 |
| | `UsageCountsConsent.kt` | The usage question, `InfoLink` | 03 §3.1, 05 §5.6 |
| (root) | `MainScaffold.kt` | Tabs, glass bar, snackbar | 03 §3.4, 05 §5.1 |
| | `SajdaViewModel.kt` | Screen state | 05 §5.1 |
| | `RamadanState.kt` | Ramadan card and label state | 05 §5.2 |
| | `RamadanTimetable.kt` | Ramadan table rows | 05 §5.2 |
| `home/` | `HomeScreen.kt` | Times, next prayer card, list, mosque dialog, export sheet | 05 §5.2 |
| | `Sky.kt` | Sky backdrop and mark | 03 §3.3 |
| | `RamadanCard.kt` | Ramadan card, notice, info dialog | 05 §5.2 |
| | `EidCard.kt` | Eid greeting | 05 §5.2 |
| | `TimetableSheet.kt` | Timetable sheet, shared header | 05 §5.2 |
| | `RamadanTimetableSheet.kt` | Ramadan table sheet | 05 §5.2 |
| | `IslamicDatesSheet.kt` | Islamic dates sheet | 05 §5.2 |
| | `MyPrayers.kt` | Last 30 days sheet | 05 §5.4 |
| `prayers/` | `MyPrayersScreen.kt` | My prayers tab | 05 §5.4 |
| `qibla/` | `QiblaScreen.kt` | Qibla tab and dial | 05 §5.3, 03 §3.3 |
| `settings/` | `SettingsScreen.kt` | Settings content, every chooser | 05 §5.5 |
| | `SettingsList.kt` | Settings look: groups, rows, theme selector | 03 §3.3 |
| | `LanguageChooser.kt` | Language list | 05 §5.5 |
| | `AppLanguage.kt` | Which language the app is in | 06 §6.6 |
| `onboarding/` | `OnboardingScreen.kt` | Setup | 05 §5.6 |

## What wins when two sources disagree

| Order | Source | Holds |
|---|---|---|
| 1 | **The owner** | Any decision he has made. Recorded in `docs/DESIGN_SYSTEM.md` §9 (decision log, dated, with his words) |
| 2 | **Open questions** | `docs/DESIGN_SYSTEM.md` §11. An OPEN item is not decided: never build an answer to it |
| 3 | **The code** for values | `app/src/main/java/com/sajdatime/app/ui/theme/` (`Color.kt`, `Type.kt`, `Spacing.kt`, `Motion.kt`, `Theme.kt`) |
| 4 | **`tokens/*.json`** | The same values, platform neutral. `DesignTokensTest` fails the build if 3 and 4 differ |
| 5 | **This folder's pages** | How and when to use the values; specs for components, patterns and screens |
| 6 | **`docs/DESIGN_SYSTEM.md`** prose | The owner's rules (§0), the enforced button inventory (§4.1), the learning loop (§10). Its value tables are older copies of what is in `tokens/` |
| 7 | Plans in `docs/plans/` | The reasoning behind each 1.6 step. History, not instruction |

If a page here disagrees with the code, the code is right for the value; fix the page in the same
commit. If the code disagrees with an owner decision in §9, the code is the bug.

## How drift is stopped

| Check | Fails the build when |
|---|---|
| `DesignTokensTest` | A colour, Material role, gap, duration or type style differs between code and `tokens/`, or one exists on only one side; a phone UI file is not named in these pages |
| `DesignRulesTest` | An outlined, tonal or bare button; a text link other than Close or Got it; a button file missing from the §4.1 inventory; a gap written as a number; a written animation duration, spring or infinite loop; a dismissable dialog without its X |
| `ColorContrastTest` | Any text or icon pair falls below WCAG 2.1 AA in either theme, including every gradient stop, the skies and the glass bar over black and white |
| `WidgetColorsTest`, `WidgetThemeTest` | A widget ink drops below AA on any stop, or widget colours stop matching the app |
| `WholeWordsTest`, `LocaleDisciplineTest`, `TranslationIntegrityTest` | A word splits, RTL is applied to English, or a translation drifts from English |

Run them with:

```bash
./gradlew :app:testDebugUnitTest --tests '*DesignTokensTest' --tests '*DesignRulesTest' --tests '*ColorContrastTest' --tests '*Widget*Test'
```

What the tests cannot see, and review must: whether a gap was chosen for the right relationship,
whether a screen reads well, whether copy is kind and accurate, shapes and component sizes
(`shape.json` is a census, not a test), and anything on the watch.

## Changing the design system

1. Is it an owner decision? If it changes what he has seen, ask him first with screenshots (light,
   dark, 2x text, Urdu). Record his answer in `docs/DESIGN_SYSTEM.md` §9 with the date and his words.
2. Change the code and the token file **in the same commit**. Run `DesignTokensTest` and
   `ColorContrastTest`.
3. Update the page in this folder that describes the thing, and the matching row in
   [`09-ios-mapping.md`](09-ios-mapping.md) if it has one.
4. Add a dated line to [`CHANGELOG.md`](CHANGELOG.md): what changed, why, what was rejected.
5. Retake `docs/store/screenshots/` if the change is visible there.
6. Commit message explains the reasoning (project rule in `CLAUDE.md`).

## For the iPhone app

`tokens/*.json` is deliberately platform neutral: hex colours with light and dark values (one Asset
Catalog colour set each), sizes in points, durations in milliseconds, and an `iosRelativeTo` text
style for every type style so Dynamic Type works. No iOS code exists yet and none is written until
Ali decides (`CLAUDE.md`). When it starts, generate the Swift constants from these files rather than
retyping them, and add an iOS twin of `DesignTokensTest`.

*Made with love, free for the Ummah.*
