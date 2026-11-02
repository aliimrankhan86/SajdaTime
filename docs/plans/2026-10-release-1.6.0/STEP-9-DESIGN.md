# STEP-9-DESIGN: Disclaimer moves to Settings, About, first row

Fable (`ux-lead`), 25 Oct 2026. Spec from DETAILED-PLAN §6.0, §8.3, §9.3 and BUILD-INSTRUCTIONS row 9 (Ali,
23 Oct 2026: "My hard rules have evolved, based on user behaviour and more data").

**Recommendation: build exactly what is decided. One row moved, one dialog retitled, the first run dialog deleted.
Nothing new on Times or in setup. No question for Ali; one screenshot set to approve.**

## 1. The row (verified in code unless marked)

| Part | Spec | Where |
|---|---|---|
| Position | First item of the About group, above Version. Today it is second (`SettingsScreen.kt:538-548`): swap the two `SettingsItem.Link` entries, nothing else | verified |
| Icon | Material Symbol `info` (`Icons.Outlined.Info`), SF Symbol `info.circle`. Already assigned by key in `SettingsList.kt:163`, so no icon code changes. 20dp glyph in the standard 36dp `secondaryContainer` tile. Non directional: no RTL mirroring. Rejected: `gavel` (says "ruling", the opposite of the text), `description`/`doc.text` (reads as licence boilerplate), `warning_amber` (amber means warning, DESIGN_SYSTEM §1; a tinted "important" row is the §0.5 "prominent" trap) | verified |
| Title | `about_disclaimer` = "Disclaimer" (unchanged) | verified |
| Subtitle | `about_disclaimer_short` = "A helper, not a religious authority. Worth reading." (replaces "Tap to read", banned wording) | done (main session, five languages) |
| Translations | Retitled `disclaimer_title` equals `about_disclaimer` word for word in each language so row and dialog match: ur "ڈس کلیمر", ar "إخلاء المسؤولية", tr "Sorumluluk reddi", in "Penafian". Subtitle second sentence (main session settles): ur "پڑھنے کے لائق ہے۔", ar "يستحق القراءة.", tr "Okumaya değer.", in "Layak dibaca." | assumed (wording) |
| Dialog | Unchanged apart from the title: `AlertDialog`, `DialogTitle` with close X (`Common.kt:60-72`), scrollable body (`SettingsScreen.kt:781`), "Got it" text button at the foot. `disclaimer_body` untouched, dua last | verified |

States: the row is a `PlainRow`; at 2x text the subtitle wraps to three lines at 320dp and the row grows from its
56dp minimum with the tile centred. Turkish and Indonesian wrap to two lines at default size, as Privacy already
does. Dark: tile `secondaryContainer`/`onSecondaryContainer`, already contrast tested. RTL: tile on the right, text
right aligned. TalkBack reads "Disclaimer, A helper, not a religious authority. Worth reading., button". To check
once built: the `PlainRow` chevron is the AutoMirrored one.

Line references the plan got wrong (the file moved): DESIGN_SYSTEM inventory rows to delete are `:477-478` (not
410-411); the sentence at `:636-638` (not 546) "Two must be answered ... the first run disclaimer and the usage
counts question" becomes "One must be answered ... the usage counts question". Also the `DesignRulesTest.kt:118-119`
comment. `action_understood` has no other use (grep of app, wear, core): delete it in all five languages.

## 2. What a first time user loses, and what already covers it

They lose the one forced reading. The four points still reach them where they matter, with no new copy: the Times
"Different from your mosque?" button and dialog ends "Where a difference remains, follow your mosque" for exactly
the user who notices a gap; the polar notice sends northern users to their mosque; the store description carries
all four points (`DisclaimerContentTest` holds LISTING.md, privacy.html, README); the watch keeps its two point
line. **Add nothing.** Rejected on purpose: a one time Notice on Times (a nag by another name), a sentence on the
setup Finish step (a softened extra copy), a badge or dot on the Settings tab (reads as an error), colouring the
row. The test messages at `DisclaimerContentTest.kt:150,173` ("belongs on the first-run screen") become "belongs
in the in-app full text".

## 3. Question for Ali

None. The screenshot set below is the approval; if he wants a different icon he can say so from it.

## 4. Pictures to take (Pixel 823dp unless stated)

1. Settings scrolled to About, light, default text: Disclaimer first, Version second.
2. Same, dark.
3. The Disclaimer dialog open, light, top (title "Disclaimer" with X) and scrolled to the foot (dua line, "Got it").
4. Settings About in Urdu, light (tile right, text right aligned, chevron mirrored).
5. Settings About at 2x text, 320dp (subtitle wrapped, tile centred).
6. First launch after setup: the Finish step, then landing straight on Times with no dialog.

## 5. iPhone effect

Carries over: a list row opening a titled sheet with a close control is native on both platforms (`info` =
`info.circle`), and removing the launch gate matches HIG, which discourages modal text before the first screen.

## 6. Plan verifier corrections (25 Oct 2026), all accepted

1. The strings are done in the working tree (five languages: `disclaimer_brief`, `disclaimer_read_full`,
   `action_understood` deleted; `disclaimer_title` retitled to match `about_disclaimer`; `about_disclaimer_short`
   reworded; stale comments fixed). They land in the same commit as the code (DETAILED-PLAN 8.3).
2. Also remove: `SajdaViewModel.kt:508` `markDisclaimerSeen()`; `androidTest/ScreenSeedDeviceTest.kt:31`;
   `SettingsRepository.kt` field (143), write (539), read (686). **Keep** `Keys.DISCLAIMER` (611) with a one line
   note that the key is retired and must never be reused: on upgraded installs it is already `true`.
3. `DisclaimerContentTest.kt:72-85` (the short form test) is deleted; a new assertion holds `about_disclaimer_short`
   to "not a religious authority" (mutation proof: shorten it, see red).
4. MainActivity: delete lines ~339-350 and the `FirstRunDisclaimer` composable (~497-547) and the imports left
   unused. MainActivity then has no `AlertDialog`, so the `DesignRulesTest.kt:123` exemption goes.
5. `docs/privacy.html:259` comment only ("shows on first run"); no data change, "Last updated" untouched.
6. Stale comments: `OnboardingScreen.kt:102-103`, `UsageCountsConsent.kt:41-42`.
7. Docs: `README.md:162`; HANDOVER `:79-81`, `:456`, `:1010`, `:1032-1042`, `:1046`; `PROJECT_BRIEF.md:38`;
   `CLAUDE.md` rule sentence; DESIGN_SYSTEM `:477-478`, `:636-638`, §9 entry.
8. `Common.kt` is `ui/components/Common.kt:60-73`.
10. A source reading test holds the row order: `key = "disclaimer"` before `key = "version"` in `SettingsScreen.kt`.
11. HANDOVER §11 non-goal: an install that never pressed "I understand" is never shown the dialog after 1.6;
    intended.
