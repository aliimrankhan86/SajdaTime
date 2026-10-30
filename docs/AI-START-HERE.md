# Start here: briefing for any AI working on SajdaTime

Self-contained. Read all of it before you change anything or ask Ali a question. It tells you what the app
is, what is live, which branch to use, how the design system works, the rules that cause the most mistakes,
and how to finish. Kept current by every session that moves the project (last updated 30 Oct 2026).

`AGENTS.md` (loaded by Codex, Cursor and similar tools) and `CLAUDE.md` (loaded by Claude) both point here.

---

## 1. What SajdaTime is

A free, ad free, privacy first Muslim prayer times and Qibla app for Android phones, home screen widgets and
Wear OS. Charity (*sadaqah jariyah*): no ads, no accounts, no own server, no revenue, ever. Prayer times are
calculated on the phone and never need the network. It must work for the masses: no signal, old phones,
people who never change a setting, people who cannot read English, right to left readers.

**Ali** is the owner. He is not technical. Write to him in plain UK English, recommendation first, short, no
hyphens or em dashes. He decides anything visual, from screenshots. When unsure, ask him (with a
recommendation) instead of guessing. Never ask him to test the watch.

## 2. What is live, and which branch to use

Check what is live yourself: the public page
`https://play.google.com/store/apps/details?id=com.sajdatime.app` shows "Updated on"; that date moves only
when a release publishes.

| Branch | What it is | What you do with it |
|---|---|---|
| `main` | Exactly what users have: **1.5.0**, live since 23 Oct 2026 | **Never push to it, never merge into it, never open a pull request into it.** It takes 1.6 only on release day, by `docs/release-tools/RELEASE-1.6.0.md` (Ali, 25 Oct 2026) |
| `claude/1-6-0` | **Release 1.6.0**, built, approved by Ali from pictures, not yet released; plus the design system | **Work from here.** Cut your own branch from it |
| your branch | `claude/<task>` or `deepseek/<task>`, cut from `claude/1-6-0` | Commit and push here. Merging into `claude/1-6-0` only when Ali says so, after review |
| `deepseek/ds-deviations-record` | DeepSeek's record of the 22 design deviations, with pictures (25 Oct 2026) | Not merged: an Opus review (30 Oct 2026) found wrong conclusions to fix first (HANDOVER §11) |

Every other branch was deleted on 30 Oct 2026 at Ali's word ("delete the old branches ... which could cause confusion"):
all of them were already wholly inside `claude/1-6-0`, except two dropped on purpose, kept as tags
`archive/analytics-default-on-1-4-0` and `archive/customer-review-pain-points-9d35kl`. Tag `v1.5.0` marks what is live.

If GitHub shows a yellow "Compare & pull request" bar for `claude/1-6-0`, do not use it: that would merge
unreleased 1.6 into `main`.

Version numbers: phone 1.6.0 (versionCode 10) on `claude/1-6-0`; watch stays 1.5.0 (1002). Release day
checkpoints: pull request into `main` by 11 Dec 2026, submit by 15 Dec 2026 (before Ramadan).

## 3. Read before you work, in this order

1. `CLAUDE.md`: the hard rules. Several are owner decisions that must never be reversed.
2. `docs/HANDOVER.md` section 11, **the first 📍 block only**: what is true now, what is left, whose job it is.
   Everything below that block is dated history: reasoning, not tasks. Do not invent work from it.
3. `docs/design-system/README.md`, then the page it points to for your job (section 5 below).
4. A helper model (DeepSeek or similar): `docs/AI-WORKER-BRIEF.md`, which sets your limits. Never
   translations, user facing words, alarms, widgets, usage counts, privacy, signing, calculations, data
   logic or design, even with a spec.
5. For a specific feature: `docs/design-system/05-screens-and-features.md`, then the code it names.

## 4. The app in one screen

Four tabs, fixed order: **Times, Qibla, My prayers, Settings**. Floating glass tab bar. Everything else opens
as a dialog or bottom sheet over the tab. Code under `app/src/main/java/com/sajdatime/app/`; rules and
calculations in `core/` (plain Kotlin, portable to iPhone); watch in `wear/`.

- **Times**: the current period's sky (dawn, day, evening, night), the city, Gregorian and Hijri date (opens
  Islamic dates), the next prayer card with a countdown, notices (Makkah fallback, approximate times, Eid,
  Ramadan, permissions), six prayer rows with Now and Next and a bell each, Timetable with Save as PDF, and
  "Different from your mosque?".
- **Qibla**: a turn sentence first, the dial (the needle is you, the Kaaba stays upright), legend, details.
- **My prayers**: a private checklist, off by default; ticks, pause, week rings, Last 30 days, fasts to make up.
  Records, never judges.
- **Settings**: prayer times (location, school, method, jama'ah times, match your mosque, Islamic dates),
  appearance (theme, language), reminders, My prayers, About (disclaimer first).
- Also: three home screen widgets, notifications, Ramadan card and timetable, the Wear OS app and tile.

Languages: English, Urdu, Turkish, Indonesian (`values-in`), Arabic, Bengali (30 Oct 2026). Urdu and Arabic are right
to left. Adding one: the checklist in `docs/design-system/06-content-and-language.md` §6.6; the tests find a new
`values-<lang>/` folder on disk and fail until it is registered. Text held to one line shrinks to fit (`shrinkToFit`).

Not in 1.6, do not build without Ali: jama'ah reminder and Sunnah fast reminders (1.6.1, logic built and
switched off), tasbih, adhan choice per prayer. Never: a missed or qada prayer counter, network for times,
anything that sells or nags.

## 5. The design system (added 25 Oct 2026)

`docs/design-system/` is the reference for anything a user sees.

| Page | Use it for |
|---|---|
| `README.md` | Reading order, what wins when sources disagree, code map of every UI file |
| `01-principles.md` | Identity (Quiet Glass), what green and amber mean, what the app never does |
| `02-foundations.md` | Colour, type, spacing, layout, shape, elevation, motion, haptics, icons |
| `03-components.md` | Every component: anatomy, sizes, states, accessibility, iPhone equivalent; known deviations in §3.6 |
| `04-patterns.md` | Navigation, messages, choices, states every screen needs, undo, permissions, religious content |
| `05-screens-and-features.md` | Every 1.6 screen top to bottom with every state; features; defaults |
| `06-content-and-language.md` | Voice, house style, banned words, numbers and dates, languages, right to left |
| `07-accessibility.md` | Contrast, targets, text size, screen readers, checklist |
| `08-surfaces.md` | Widgets, Wear OS, notifications, PDF, launcher icon, store pictures |
| `09-ios-mapping.md` | Tokens, components and icons (Material to SF Symbol) for the future iPhone app |
| `10-new-feature-playbook.md` | **Follow it start to finish for any new feature or screen** |
| `CHANGELOG.md` | Every change to the folder, dated, with the reason |
| `tokens/*.json` | Exact values: colour, typography, spacing, motion, shape |

How it stays true:

- **`DesignTokensTest`** (12 checks, app unit tests) fails the build if a colour, Material role, widget or
  watch colour, gap, duration or type style differs between the code and `tokens/`, if any hex quoted in a
  page is not a token, or if a phone UI file is not named in the pages.
- So: change a value in `app/src/main/java/com/sajdatime/app/ui/theme/` **and** the token JSON in the same
  commit, and add a line to `docs/design-system/CHANGELOG.md`. Add a UI file → describe it in the right page
  and add it to the README code map. Add a colour pair → add it to `ColorContrastTest`.
- **Never invent a value.** A new colour, size, gap or timing is a design decision for Ali.
- `docs/DESIGN_SYSTEM.md` still owns Ali's rules (§0, read before any visual change), the **button inventory**
  (§4.1, enforced by `DesignRulesTest`: a new button needs a row there), the decision log (§9) and **open
  decisions** (§11: never build an answer to an OPEN item). Its older value tables lose to `tokens/`.
- What wins: Ali's decisions and `DESIGN_SYSTEM.md` §0 and §4.1 → §11 open items → the code → `tokens/` → the pages.

**Known deviations (open item `ds-deviations`): do not copy them, do not fix them without Ali.** Listed in
`03-components.md` §3.6: `bodySmall`, `labelSmall`, `titleSmall` used 13 times though `Type.kt` does not
define them (below the 14sp floor); two cards not at 24dp; older dialog rows; the Qibla tab's 20dp gutter;
one red error line in setup; the watch tile's own greens; the old icon green on the watch and Android 7
icons. New work follows the rule column; each fix is a visible change Ali decides from pictures.

## 6. Rules that cause the most mistakes

Visual:
- Every action is a solid `CtaButton` with white bold text. The only text links are "Close" and "Got it" at
  a dialog's foot (plus the usage question's two bold links). No outlined or tonal buttons.
- Gaps from `Space` (4, 8, 12, 16, 24) by relationship, never a number. Durations from `Motion` (150, 250,
  400); no springs, loops or entrance animation. Colours from `MaterialTheme` roles; no hex in a composable;
  read the theme from `LocalDarkTheme`.
- No two icon buttons side by side. Every dialog the user can leave has a close X (a must answer question
  does not).
- Never show meaning by colour alone. Targets at least 48dp. Text in sp, nothing below 14.

Words:
- No hyphens or em dashes in app text (except names like Eid al-Fitr). Sentence case. Never uppercase in code.
- Every new string in every language in the same commit. Prayer and madhab names exactly as written.
- "SajdaTime" is never translated or written in another script, in any language: it is the Play Store name
  (Ali, 30 Oct 2026). `TranslationIntegrityTest` and `tools/check-translation.py` fail if it is.
- Never "missed", "qada", "must", "valid" for prayers; explanations end "follow your mosque".
- The religious disclaimer is never removed or softened; its dua request is the only ask in the app.
- No rating or sharing asks in our words (only Google's own card and the passive About rows).
- Every release, the store description names everything the app offers first, then What's new gives the changes
  (Ali, 30 Oct 2026, hard rule; HANDOVER §5.38).

Privacy and safety:
- No network for prayer times, ever. Location coarse, foreground, on device. No new usage count event,
  parameter or user property without Ali and a privacy policy change. Never send sect, method or settings.
- Never touch, request or view the release signing key or `keystore.properties`.

Platform:
- iPhone is planned: every plan states its iPhone effect in one line (none, carries over, or Android only
  because); new rules and calculations go in `core/` as plain Kotlin with no `android.*` import; stored data
  as plain values or JSON; name the SF Symbol beside any new Material icon. No iOS code until Ali decides.
- Install on Ali's phone only with `./gradlew installSideload`, never `installDebug`.

## 7. Before you say done

```bash
# Design, contrast and token checks
./gradlew :app:testDebugUnitTest --tests '*DesignTokensTest' --tests '*DesignRulesTest' --tests '*ColorContrastTest'
# Full gate before anything merges (Ali's Mac needs JAVA_HOME set to 21 first)
./gradlew clean test lint :app:bundleRelease :wear:bundleRelease
```

- Unit test counts on 30 Oct 2026: **app 338, core 238 (1 skipped), wear 27**. If you add tests, update
  `EXPECTED_COUNTS` in `docs/release-tools/release-1.6.0.sh` and the counts in `docs/AI-WORKER-BRIEF.md`.
- Run the checks in `docs/design-system/10-new-feature-playbook.md` §6 and paste the counts. Never write
  "done", "every" or "all" without them.
- Any visual change: screenshots in light, dark, 2x text and Urdu, shown to Ali with the install command and
  the commit he should see. He decides.
- Get an independent review of the diff before committing (the `reviewer` agent, or ask Ali).
- Commit messages explain why, what was rejected and what was not tested. Update the first block of
  `docs/HANDOVER.md` §11 and `docs/PROJECT_BRIEF.md` when the state changes. Push your branch.
- Say plainly what you did not check.

If anything here conflicts with `CLAUDE.md` or Ali's own words, stop and ask Ali.
