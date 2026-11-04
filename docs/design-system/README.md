# SajdaTime design system

The single reference for how SajdaTime looks, reads and behaves, written so that any person or AI can
extend the app, add a feature, or build the iPhone version without drifting from what ships.

Taken from the **1.7.0 build** (branch `claude/1-7-0`, 4 Nov 2026). Earlier descriptions of the 1.6
look are history: the Next row on the Times screen and the three-way theme bar are gone.

**Where this lives.** The owner asked (4 Nov 2026) for these pages to live on his Google Drive, not in the
repo. The move is not finished: until it is, the copies here are the ones in force. The repo keeps, and
will keep, what the build reads: the token files in `tokens/`, `tokens/ui-files.json` (the
list of every phone UI file, checked by `DesignTokensTest`), and `docs/DESIGN_SYSTEM.md` (owner rules,
the enforced button inventory in §4.1, the decision log in §9). If a page on Drive and a repo file
disagree on a value, the repo wins; tell the owner so the page is fixed.

---

## Philosophy: why it looks like this

- **Quiet Glass.** Calm paper-coloured surfaces, one green identity, the period's sky behind the time.
  The time is the hero. Nothing competes with the next prayer or the current one.
- **One highlight at a time.** The Times screen and every widget mark the prayer that is current now,
  with the same row, the same fill and the same pill. Only one row is ever marked. Nothing is marked
  when no prayer is current: between sunrise and Dhuhr, and from midnight until today's Fajr.
- **Green means something.** Green marks what can be tapped, the current prayer, and the Qibla. It is
  never decoration.
- **Dependable beats elegant.** It has to work with no signal, on an old phone, at default settings,
  for someone who cannot read English, and right to left.
- **The owner decides what he can see.** Visual and wording choices are his, from screenshots. A rule
  this page does not decide is asked, not guessed. Silence is never a yes.
- **Keep the iPhone road smooth.** Use only patterns both platforms share. Every icon names its SF
  Symbol. Rules and calculations stay plain code that could move to iPhone.

## How to add a feature on top of this

Nothing here is rebuilt for each feature. A new feature is added on top of what exists, in this order:

1. **Start from the pages.** Find the screen or component it resembles (the code map in
   `tokens/ui-files.json` names every file). Reuse its components, tokens and states before inventing any.
2. **Ask the owner for any visual or wording choice this page does not decide**, with screenshots in
   light, dark, 2x text and Urdu. Record his answer in `docs/DESIGN_SYSTEM.md` §9 with the date and his words.
3. **A new value is a new token.** Put it in `tokens/*.json` and in the code in the same commit.
   `DesignTokensTest` must pass. Never write a colour, gap, size or duration in a composable.
4. **A new phone UI file gets a row** in `tokens/ui-files.json`, and a new button gets a row in
   `docs/DESIGN_SYSTEM.md` §4.1. The build fails otherwise.
5. **Write the page.** Add the feature to the Drive page for the screen it lives on (the screen,
   its states, its copy rules, its accessibility, its iPhone line). Add a dated line to the changelog.
6. **Check the states and both directions.** Light, dark, 2x text, Urdu (right to left), and the
   empty and error states. Say in the commit what was not seen.

A change to something already shipped follows the same path, and says in the changelog what it
replaces and why.

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

## Where things live

| For Google Drive (the pages, target) | Kept in the repo (what the build reads) |
|---|---|
| `README.md` (this page) | `tokens/color.json`, `typography.json`, `spacing.json`, `motion.json`, `shape.json` |
| `01-principles.md` to `10-new-feature-playbook.md` | `tokens/ui-files.json` (the list of phone UI files) |
| `CHANGELOG.md` | `tokens/README.md` is on Drive; the token files are the same as in the repo |
| | `docs/DESIGN_SYSTEM.md` (owner rules, §4.1 inventory, §9 decisions) |

## Code map: every phone UI file

The full list (folder, file, what it holds, which page describes it) is in
[`tokens/ui-files.json`](tokens/ui-files.json). It is checked by `DesignTokensTest`: a phone UI file
that is not in that list fails the build.

## What wins when two sources disagree

| Order | Source | Holds |
|---|---|---|
| 1 | **The owner** | Any decision he has made, recorded in `docs/DESIGN_SYSTEM.md` §9 with his words; and his rules in `docs/DESIGN_SYSTEM.md` §0 and the enforced button inventory in §4.1 |
| 2 | **Open questions** | `docs/DESIGN_SYSTEM.md` §11. An OPEN item is not decided: never build an answer to it |
| 3 | **The code** for values | `app/src/main/java/com/sajdatime/app/ui/theme/` (`Color.kt`, `Type.kt`, `Spacing.kt`, `Motion.kt`, `Theme.kt`) |
| 4 | **`tokens/*.json`** | The same values, platform neutral. `DesignTokensTest` fails the build if 3 and 4 differ |
| 5 | **The pages on Google Drive** | How and when to use the values; specs for components, patterns and screens |
| 6 | **`docs/DESIGN_SYSTEM.md`** (§1 to §8) | Older copies of what is in `tokens/`; the learning loop (§10) still applies |
| 7 | Plans in `docs/plans/` | The reasoning behind each past step. History, not instruction |

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
3. Update the Drive page that describes the thing, and the matching row in
   [`09-ios-mapping.md`](09-ios-mapping.md) if it has one.
4. Add a dated line to the changelog on Drive: what changed, why, what was rejected.
5. Retake `docs/store/screenshots/` if the change is visible there.
6. Commit message explains the reasoning (project rule in `CLAUDE.md`).

## For the iPhone app

`tokens/*.json` is deliberately platform neutral: hex colours with light and dark values (one Asset
Catalog colour set each), sizes in points, durations in milliseconds, and an `iosRelativeTo` text
style for every type style so Dynamic Type works. No iOS code exists yet and none is written until
Ali decides (`CLAUDE.md`). When it starts, generate the Swift constants from these files rather than
retyping them, and add an iOS twin of `DesignTokensTest`.

*Made with love, free for the Ummah.*
