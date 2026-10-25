# Step 10 design: Languages made visible (Fable, 25 Oct 2026)

**Ali's rule (25 Oct 2026, binding):** "write their name also urdu in their own text then write urdu in English
also". Every language is shown in its own script and named in English, e.g. اردو with "Urdu". The English name is
English in every app language (his words). Not recommended: the English name following the app language (an Urdu
reader would see "ترکی"); it helps only readers of the current language and adds four translations that can drift.

## 1. Language chooser rows (`LanguageChooser.kt`)
- **Two lines.** Line 1: own script name, `bodyLarge`, `onSurface` (unchanged). Line 2: English name, `bodyMedium`,
  `onSurfaceVariant`, `Space.Tight` under it (the Settings subtitle pattern). Each line is one script, so nothing
  for bidi to reorder; wraps cleanly at 320dp and 2x. A one line "اردو · Urdu" needs isolation and wraps mid pair.
- **English row:** one line, "English" (never "English (English)"): same names show once.
- **Phone language row:** title `language_phone` as today; second line says what following the phone gives, in the
  pair form "اردو (Urdu)", or "English" when the phone's language has no words. Source: the phone's own first locale
  (`Resources.getSystem().configuration.locales[0]` via `AppLanguage.fromLocale`, which falls back to ENGLISH).
  **Assumed, check on the emulator on Android 12 and 14:** the system configuration ignores the per app override.
- **Unavailable:** line 3, `language_unavailable` as today (a safety net: every language is packaged).
- **RTL:** names align to the start edge; no `TextDirection` override, no isolation (no Text mixes scripts). Radio at
  the start edge. TalkBack: "اردو, Urdu, radio button, selected".
- **Order** unchanged (Phone language, then enum order). **No new icons**, no flags (a flag is a country).
- **Welcome button:** unchanged, own name only (Ali chose it from screenshots, 19 Oct 2026, DESIGN_SYSTEM §9); the
  list it opens now shows both names.

## 2. Language row subtitle in Settings (`SettingsScreen.kt` ~334), Q17
- **(A, recommended)** name only the language showing, pair form: following the phone "Phone language · اردو (Urdu)"
  (`settings_value_pair`); chosen "اردو (Urdu)"; English stays "Phone language · English" / "English". The pair is
  `bidiIsolated()` (`core/Bidi.kt`) before it enters `settings_value_pair`.
- **(B)** all five: "English · العربية (Arabic) · اردو (Urdu) · Türkçe (Turkish) · Bahasa Indonesia (Indonesian)",
  each isolated; 3 lines at 320dp, 5 or 6 at 2x.
- Cost of A: on an English phone the row looks as today; discovery stays the globe icon, the welcome button and the
  store listing in their language. Hence Ali decides from screenshots.

## 3. Strings
- New, `translatable="false"`: `language_name_pair` = `%1$s (%2$s)`.
- `englishName` on the `AppLanguage` enum beside `nativeName` (constants, not resources: they must not translate).
- Nothing new to translate. `settings_language_desc` looks unused: implementer confirms by grep before deleting.

## 4. Store screenshots per language: step 11, not here
The store set must show the finished app. Step 11's release script takes them (loop already in `release-1.5.0.sh`
6c and 6f): English 01, 02, 03, 05, 06 (widgets with the Today strip), My prayers; ar, ur, tr, in: 01 Times, 02
Qibla, My prayers; `raw/phone/<locale>/` and `upload/phone/<locale>/` with Play's codes (`ar`, `ur`, `tr-TR`,
`id-ID`); `tools/build-store-assets.sh` glob (line 150) needs recursion. London for all.

## 5. Question for Ali: Q17 (A recommended, B), from screenshots.

## 6. Pictures
1 chooser, English, light and dark. 2 chooser, Urdu, light. 3 chooser at 2x on 320dp. 4 Settings with Urdu chosen,
light and dark, A and B. 5 Settings at 2x, A and B. 6 chooser with Arabic chosen on Android 12 (setOverride route).

## 7. iPhone effect
Carries over: a list with a check per row and a value row in Settings exist on both; names come from the same enum.

## 8. Plan verifier corrections (25 Oct 2026), all accepted
1. **Phone language source:** `Resources.getSystem()` is wrong on Android 13+ (the app's own language reaches it,
   HANDOVER §15 lesson 181). Use `AppLanguage.fromLocale(LocaleManagerCompat.getSystemLocales(context)[0])`, as
   `QiblaScreen.kt:276` does. Emulator check on Android 12 and 14 stays.
2. `settings_language_desc` is unused: delete it from all five `values*/strings.xml` in one commit
   (`TranslationIntegrityTest` fails on keys missing from the source).
3. Tests in `LanguagePickerTest.kt`: `englishName` pinned as the written out list English, Urdu, Turkish,
   Indonesian, Arabic (lesson 180); `language_name_pair` is `translatable="false"` and equals `%1$s (%2$s)`.
   Mutation proof.
4. The Phone language row's second line is one Text in two scripts ("اردو (Urdu)"): render it `bidiIsolated()`.
5. Docs: DESIGN_SYSTEM §9 (~986) and §11 `1.6-language-row` (~1287, closed after Ali's answer); HANDOVER §5.16;
   `BUILD-INSTRUCTIONS.md:260` and `DETAILED-PLAN.md:447` get a line: `settings_language_all` superseded by this file.
6. Q17: **B is the planned item 18** (all five names, now with English names); **A is Fable's new recommendation.**
7. TEMPORARY switch `languageSubtitleAll` in `SettingsScreen.kt` near the Language row renders B for the pictures;
   removed with the losing option after Ali answers.
8. Privacy: no data change, `docs/privacy.html` untouched. DESIGN_SYSTEM §4.1: no new button.
9. Update the `AppLanguage.kt:44` comment and `LanguageChooser.kt:27-28` doc to the two name rule, Ali's date.

## 9. After the build (25 Oct 2026)
- **Opus review, two fixes applied.** (a) A pair on its own Text is not isolated: an FSI isolate takes Arabic
  script's direction and set "اردو (Urdu)" as "(Urdu) اردو" inside an English line, so §8 item 4 was wrong. Option B
  isolates each pair in the paragraph's own direction (LRI or RLI). (b) The Phone language row uses
  `AppLanguage.phone()`: the phone's whole language list resolved against our resources (a phone set to French then
  Urdu gets Urdu), not `locales[0]`.
- **Fable, from the pictures:** chooser rows padded `Space.Related` top and bottom (at 2x "Urdu" touched "Türkçe").
  Her `LineHeightStyle` fix for the taller Urdu and Arabic line box was tried and measured: no change (75px vs 63px
  line boxes before and after), so it was removed; recorded as DESIGN_SYSTEM §11 `1.6-script-line-box`.
- Q17 for Ali: A (recommended) vs B (the planned item 18). B in Urdu splits "العربية (Arabic)" across lines and
  strands a dot at line starts, worse at 2x.
