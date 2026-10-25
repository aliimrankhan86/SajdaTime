# Step 7: jama'ah times on the Times screen, design (Fable `ux-lead`, 25 Oct 2026)

> **READING ORDER:** as `STEP-1-DESIGN.md`: `BUILD-INSTRUCTIONS.md` first, then this file for step 7 only.

Written before any step 7 code. Skills: none invoked (DESIGN_SYSTEM §2, §3, §4.1, the `Notice` anatomy, BUILD-INSTRUCTIONS §3 Q13 to Q15, DETAILED-PLAN 8.7 and 9.12, SIGNOFF "Jama'ah times" decide nearly everything); Apple HIG applied by hand (§11).
**Status: a recommendation. The mosque door (Q15) is decided only from Ali's pictures (§13).** Ali asked on 25 Oct 2026 for a full UX review of the door ("Check it with fable and user experience, User interface, user journeys also the placements etc"): §5.

Decided before this file and not reopened: typed jama'ah times belong to the home mosque and are **hidden** while the phone's time zone differs from the one they were typed in, no "check" line, no stored coordinates, zone id stored with the rules as codec v2 with v1 still readable (Q13, 9.12 amended); `jamaah_out_of_window` becomes "Your Asr jama'ah time is before Asr begins. Check your mosque's times." plus an after form, five languages (8.7); no jama'ah on widgets or watch (Q14); no 25 km hide; reminders stay inert in 1.6.1 (`InertReminderSettingsTest`); the Times line is a second line under the prayer name, one line in Ramadan ("Suhoor ends · Jama'ah 5:45 am"), never two (UX.md, built); Friday's Dhuhr line is Jumu'ah or nothing (built, tested); no paragraph at the top of the Jama'ah times dialog, the seasons help under "Minutes after it begins" (built, verified `jamaah_rule_after_help`).

## 1. Recommendation

Keep what `9e9102e` built and change five things, all small.

1. **The Times line stays the quiet second line it is** (`labelMedium`, grey, under the name, "Jama'ah 1:30 pm" or "Jumu'ah 1:15 pm"), with one fix: the time can no longer split so that "pm" lands alone on a third line (seen in `screens/times-tracker-jamaah.png`, Dhuhr row). The space inside the time becomes a non breaking space in the note only.
2. **Out of window on Times keeps "Check your jama'ah time"** in the same grey line (SIGNOFF, not reopened by Q13: Q13 removed the "check" line for the *zone* case only). A line that silently vanishes reads as the app losing what was typed; six words say why and where to look.
3. **Zone differs: the lines go, and the Jama'ah times dialog says why** with one quiet (green) notice at its top. The amber notice under a row still makes sense, but only for out of window and only at home; while away the dialog shows the rules as typed, no "Today" previews and no amber (a London fixed time checked against Dubai's prayer times would false flag).
4. **The out of window sentence splits into before and after**, which needs `JamaahResult.OutOfWindow` to become two results (`Before`, `After`), five strings, five languages (§8).
5. **The three SIGNOFF wording changes are not done; do them**: the clock title, the Match your mosque help line and the row name (§8; verified against `strings.xml:468-474`).

Plus the mosque door review Ali asked for (§5): **keep the door, under the rows, as it is**, with pictures with and without so Ali decides (Q15).

Rejected: an icon before the jama'ah line (the row already has a glyph, a pill, a time and a bell; a fifth mark is clutter and the word "Jama'ah" says it); the jama'ah time in `primary` or bold (it would compete with the prayer's own time, which is the answer the screen exists for); the jama'ah time on the same line as the start time ("12:45 pm · 1:15 pm" is two numbers with no label, and UX.md's fallback was only for an overflow that §3 shows does not happen); making the prayer row tappable to open Jama'ah times (the bell is the row's control; a second tap target on a row that has a toggle makes TalkBack and thumbs guess); a travel notice on Times itself (nothing is wrong; Times must stay calm); showing the zone name in the notice ("Europe/London" is a machine name and `getDisplayName` gives "Greenwich Mean Time", wrong half the year); any new artwork (`docs/ASSETS.md` untouched).

## 2. The journeys, numbered

Words in quotes are on screen.

1. **Nothing set (everyone's start).** Times is exactly as today (`screens/step5/2-day-light.png`): no second lines. Settings, "Jama'ah times" row, subtitle "Off". Nothing invites anyone.
2. **The board says 1:30 and the app says 12:45.** Tap "Different from your mosque?" under the rows. The dialog's first paragraph says the board shows the congregation; its first button "Add your mosque's jama'ah times" opens the Jama'ah times dialog, six rows, all "Off". Tap "Dhuhr": "Off", "A fixed time", "Minutes after it begins" (with "Best if your mosque moves its times with the seasons."). Pick "A fixed time": the clock, "Dhuhr: the jama'ah time on your mosque's board", opens on today's Dhuhr start. Set 1:30 pm, "Set time". Back in the list: "Dhuhr  1:30 pm". Close. Times: under Dhuhr, "Jama'ah 1:30 pm". Settings row subtitle "1 prayer set".
3. **Minutes after.** Pick "Minutes after it begins": 15 appears with − and +. The row reads "15 minutes after it begins" and, on its own line, "Today 1:00 pm". Times: "Jama'ah 1:00 pm", moving with the season.
4. **Friday.** With "Jumu'ah  1:15 pm" set, the Dhuhr row's line on Friday says "Jumu'ah 1:15 pm". With Jumu'ah off, Friday's Dhuhr row shows nothing, even if Dhuhr has its own rule (verified `JamaahPlan.on`).
5. **Summer catches a winter time.** Asr was typed as a fixed 3:30 pm in January; in June Asr begins at 5:30 pm. Times: under Asr, "Check your jama'ah time". Jama'ah times dialog: under the Asr row, the amber notice "Your Asr jama'ah time is before Asr begins. Check your mosque's times." Tap the row, set a new time or switch to minutes after, and both go.
6. **Travelling.** The phone is in Dubai; prayer times are Dubai's (as always). Every jama'ah line is gone; the Settings row still says "3 prayers set". Open it: a quiet notice at the top, "You are in a different time zone, so your jama'ah times are not shown.", "Details" unfolds "They come back when you are home. If you have moved, set them again here and they will follow this time zone." The six rows show the rules as typed with no "Today" line and no amber. Home again: the lines are back, nothing to do.
7. **Moved house to another zone.** Same as 6 until any rule is saved again, which stamps the new zone; the notice says so.
8. **Ramadan.** Fajr: "Suhoor ends · Jama'ah 5:45 am"; Maghrib: "Iftar · Jama'ah 6:10 pm". One line; at 320dp it may wrap to two, which only Ramadan's already scrolling screen pays for.
9. **Urdu and Arabic.** The line sits under the name at the right edge, "جماعت 1:30 pm" with Western digits, the time isolated as one run by the non breaking space; the Settings rows mirror with the chevron at the left; the quiet notice's mark at the right.
10. **TalkBack.** The row is one stop (`mergeDescendants`, verified `:1226`): "Dhuhr, Jama'ah 1:30 pm, Next, 12:45 pm"; the bell is its own switch node after it, as today. Out of window: "Asr, Check your jama'ah time, 3:17 pm". Nothing new to announce; nothing is live.

## 3. The line on Times, with tokens

Nothing in `PrayerRow`'s layout changes; this pins what is there and what `rowNote` does.

| Row state | Name | Line | Line ink | Verified |
|---|---|---|---|---|
| Plain | `bodyLarge` `onSurface` | `labelMedium` (14sp, 18 line height) | `onSurfaceVariant` | `:1268-1276` |
| Next (`primaryContainer`, bar, "Next" pill) | `onSurface` | same | `onPrimaryContainer` | `:1274`; pair asserted in `ColorContrastTest` |
| Now (`surfaceContainerHigh`, "Now" pill) | `onSurface` | same | `onSurfaceVariant` | same |
| Sunrise | never has a line | | | `JamaahPlan` iterates `TRACKED_SLOTS` |
| Dark | roles only; no change | | | `3-times-dark.png` |
| Sky above | the line is on the card, never on the sky | | | `HomeScreen.kt:237-252` |

**One code change in the row:** `rowNote` formats the time with the inner space replaced by ` ` (`TimeFormat.clock(context, at).replace(' ', ' ')`), so the line wraps as "Jumu'ah" / "12:46 pm" and never "12:46" / "pm". 24 hour users are unaffected. Not changed in `TimeFormat` itself: it feeds the PDF, the widgets and the card.

**Why the Next row is the tight one.** On a 360dp phone the name column on the Next row is about 86dp. "Jama'ah 12:45 pm" at 14sp is about 110dp, so it wraps; with the non breaking space it wraps once, cleanly, and the row grows by one 18dp line **only on the Next row, only when that prayer has a line**. Plain rows have about 156dp and fit one line.

**Height at 1.0 (computed from `Type.kt`, measure on the frame):** the row is `heightIn(min = 50)` outside `padding(vertical = 1)` (`:1239-1246`), so a row's content is 24 (name) + 18 (line) + 2 = 44dp, inside the 50dp floor. **A jama'ah line costs 0dp a row at 1.0 on a plain row**, not the "about 2dp" in DETAILED-PLAN line 77 (that figure predates the bell's 48dp target). The Next row with a wrapped line costs 18dp once. So the 823dp fit is unchanged: the mosque door is the one thing that scrolls, about 60dp (78dp in the single wrapped case). **Fit risk: none new.** At 1.5 and 2.0 the row is stacked (`:1181`) and the screen already scrolls.

**320dp at 1.0:** plain rows about 116dp for the name column; "Jama'ah 12:45 pm" fits; "Suhoor ends · Jama'ah 5:45 am" wraps to two lines in Ramadan only.

**320dp at 2.0:** stacked; "Jama'ah 12:45 pm" wraps once between the word and the time. Prayer names are never truncated; nothing has `maxLines`.

## 4. The Jama'ah times dialog, states

`JamaahDialog` is a `ChooserDialog` (an `AlertDialog` with the X and "Close"). Six rows: Fajr, Dhuhr, Asr, Maghrib, Isha, Jumu'ah, each a `SettingRow` with a chevron (inventory row 511).

| State | Row subtitle | Under the row | Top of the dialog |
|---|---|---|---|
| Off | "Off" | | |
| Fixed, at home, in window | "1:30 pm" | | |
| After, at home, in window | "15 minutes after it begins" then on its own line "Today 1:00 pm" (`settings_value_lines`, replacing the "·" pair that wrapped "Today" away from its time in `screens/jamaah-times.png`) | | |
| Jumu'ah set, at home | "1:15 pm" | | |
| Out of window, before | the rule as above | amber `Notice(brief = jamaah_before_window)` | |
| Out of window, after | same | amber `Notice(brief = jamaah_after_window)` (Fajr: `jamaah_after_sunrise`; Jumu'ah: its own two) | |
| Zone differs | the rule as typed; **no "Today" line**; **no amber** | | quiet `Notice(tone = QUIET, icon = Public, brief = jamaah_away_brief, details = jamaah_away_details)`, `Space.Group` above the first row |
| v1 data (no zone stored) | as at home; the next save stamps the zone | | |
| `today == null` (no location) | the rule as typed; no previews | | |
| 2.0 text, 320dp | subtitles wrap; the dialog scrolls (`:1546`) | | |
| Urdu, Arabic | chevron at the left, mark at the right, digits Western | | |
| TalkBack | "Asr, 1:30 pm, button"; amber notice read after its row; quiet notice's "Details" says collapsed or expanded | | |

**Where the zone is stamped:** on every `setJamaahRule` and `setJumuah`, `JamaahTimes.zoneId = ZoneId.systemDefault().id`; the codec writes it as `ZONE=Europe/London` (v2); a string with no `ZONE=` is v1 and reads as `zoneId = null`. Compared by **id equality**, never by offset, so summer and winter at home never hide anything. **Where it is hidden:** `SajdaViewModel.recalculate()` passes `JamaahTimes()` (empty) to `JamaahPlan.on` when the ids differ, so `state.jamaah` is empty and Times, the mosque dialog and anything later (1.6.1 reminders) all see nothing (one gate, not three). The Settings count still counts what is typed. `JamaahTimes.kt` stays plain Kotlin.

**Why a quiet notice and not amber:** DESIGN_SYSTEM §4 Notices: amber is "something is wrong or withheld"; quiet is "information". Here nothing is wrong and the withholding is by Ali's rule.

## 5. The mosque door, "Different from your mosque?", full review (Ali, 25 Oct 2026)

**Its job.** The app's one honest answer to the first thing most users notice: the board at the mosque and the app disagree. Not a setting; a door to an explanation whose four paragraphs each end in the setting that fixes that cause (`MosqueDifferenceDialog`, `:752-784`): the board shows the congregation (→ Jama'ah times), twilight (→ Calculation method), Asr (→ School of thought), a few minutes (→ Match your mosque), then "Where a difference remains, follow your mosque." Who taps it: a new user in their first week, mostly once; a user whose mosque changes its board. Above 45° of latitude with the method still automatic it becomes the amber notice with "See why" (`:713-725`).

**The journeys.**
- *First time, times differ.* Door → dialog → first paragraph → "Add your mosque's jama'ah times" → sets Dhuhr 1:30 pm → Times shows both numbers. If instead Isha is 20 minutes off, paragraph two → "Calculation method". Good.
- *Already matched.* The door is still there. It never nags (no motion, badge or colour change, `:688`), and it is the way back to Match your mosque from Times when the board changes. Costs 60dp under the rows.
- *Setting jama'ah times.* Two routes, the door's first button and Settings > Jama'ah times; same dialog. Good.
- *Traveller.* A traveller who adds a time via the door stamps the foreign zone, and their home lines then hide at home. Small risk; the quiet notice (§4) appears whenever the stored zone differs, so the home user sees why.

**What it opens: the real finding.** "Match your mosque" (nudge the *Begins* times) and "Jama'ah times" (the *congregation* times) are two numbers off the same board, and today's words do not say which is which. A user with a board that prints "Begins 12:45 / Jama'ah 1:30" can set 1:30 as the *start* and shift Dhuhr 45 minutes, which `adjustment_limit_note` guards at 30 minutes and lets through under it. SIGNOFF decided three wording changes; they are not applied (§8). With them, the two rows read "Jama'ah times" and "Match your mosque start times", one above the other (`:279-301`), and the two clocks say "the jama'ah time on your mosque's board" and "the Begins time on your mosque's board". The Match your mosque help gains "Jama'ah times go in Jama'ah times." No new screen.

**The label.** "Different from your mosque?" stays: the user's own question, allowed for a door to an explanation (§4.1), five words, translated. Rejected: "Match your mosque" (now a setting's name); "Mosque times" (sounds like a feed we do not have); "Why do the times differ?" (longer, the app's word).

**Look.** Kind A, 52dp, full width, mosque icon 20dp leading (`:727-736`; inventory row 479). Within the rules. Weight is high for a door most use once, but every lighter version failed Ali's own test (outlined, then tonal, "easy to miss", §0.1), and §4.1 says hierarchy comes from position, never from fading. It is last on the screen and on the 823dp frame sits under the glass bar until scrolled. Icon `Icons.Rounded.Mosque` (no SF Symbol; iOS step chooses `building.columns` or text only).

**Placement.**

| Option | 823dp at 1.0 | 2.0 text | 320dp | RTL | Verdict |
|---|---|---|---|---|---|
| **A. Under the rows, as now (Recommended)** | scrolls about 60dp (unchanged) | scrolls, label wraps centred | fine | mirrors | Keep. Approved 15 Aug and 18 Oct; the explanation sits where the surprise is |
| B. Inside the rows card, as a last row | saves the 16dp gap only | same | fine | fine | No: reads as a seventh time; a solid green row in a white card breaks §4.1 |
| C. On the Today line, beside Timetable | saves about 68dp, Times fits again | two compact buttons stacked | two compact labels do not fit one line | fine | No: two solid buttons side by side on a heading line (Fable, 24 Oct, `:230-232`), label too long for a compact button on 360dp |
| D. Settings only | fits | fits | fits | fits | No for a new user: the door exists because it stands where the difference is seen |
| E. Hidden once set up | fits for those users | | | | Not recommended: lines can vanish (out of window, travel) exactly when the explanation is wanted, and "set up" has no clean test |

**When it shows.** Always, plain; loud above 45° until the method is chosen or the notice closed. No change by step 7.

**Problems found behind the door.** (1) The three SIGNOFF wording changes, not applied (§8). (2) `adjustment_limit_note` could point to Jama'ah times; the help line covers it, so one change, not two. (3) The dialog's four buttons are all 52dp full width solid green (`ExplainerAction`), the pattern §9 rejected for Match your mosque. Approved by Ali; recorded as a later candidate, not a change.

## 6. Icons and their SF Symbols

| Where | Material | SF Symbol |
|---|---|---|
| Settings, Jama'ah times row | `Icons.Outlined.Groups` (exists, `:283`) | `person.3` |
| Jama'ah dialog, quiet travel notice | `Icons.Rounded.Public` | `globe` |
| Amber notices | `WarningAmber` (Notice default) | `exclamationmark.triangle` |
| Rows | chevron from `SettingRow` | `chevron.right` |
| Mosque door | `Icons.Rounded.Mosque` | none; iOS step chooses |

No icon on the Times line itself.

## 7. States summary

| State | Times | Jama'ah times dialog |
|---|---|---|
| None set | no lines | six "Off" rows |
| Fixed, in window | "Jama'ah 1:30 pm" | "1:30 pm" |
| After, in window | "Jama'ah 1:00 pm" | "15 minutes after it begins" / "Today 1:00 pm" |
| Friday with Jumu'ah | Dhuhr: "Jumu'ah 1:15 pm" | Jumu'ah row "1:15 pm" |
| Friday without Jumu'ah | Dhuhr: nothing | |
| Out of window, before | "Check your jama'ah time" | amber "Your Asr jama'ah time is before Asr begins. Check your mosque's times." |
| Out of window, after | same | amber "Your Asr jama'ah time is after Maghrib begins. Check your mosque's times." (Fajr: "...is after sunrise."; Jumu'ah: before Dhuhr / after Asr) |
| Zone differs | no lines | quiet notice at the top; rules as typed; no Today; no amber |
| Ramadan | one line, "Suhoor ends · Jama'ah 5:45 am" | unchanged |
| No location | no lines (`:679`) | rules as typed, no Today |
| Urdu, Arabic | line under the name at the right; time one run | mirrored |
| 1.5, 2.0 | stacked row; scrolls | scrolls |
| 320dp | one wrap on the Next row at most | subtitles wrap |
| 823dp | 0dp new at 1.0; door scroll unchanged | n/a |
| Remove animations | nothing animates in this step | |

## 8. Strings (English; the main session translates ur, ar, tr, in)

```
jamaah_before_window      Your %1$s jama'ah time is before %1$s begins. Check your mosque's times.
jamaah_after_window       Your %1$s jama'ah time is after %2$s begins. Check your mosque's times.
jamaah_after_sunrise      Your Fajr jama'ah time is after sunrise. Check your mosque's times.
jumuah_before_window      Your Jumu'ah time is before Dhuhr begins. Check your mosque's times.
jumuah_after_window       Your Jumu'ah time is after Asr begins. Check your mosque's times.
jamaah_away_brief         You are in a different time zone, so your jama'ah times are not shown.
jamaah_away_details       They come back when you are home. If you have moved, set them again here and they will follow this time zone.
settings_value_lines      %1$s\n%2$s          (untranslatable; the two line subtitle)
adjustment_pick_time_title   %1$s: the Begins time on your mosque's board        (SIGNOFF; was "the start time")
settings_adjustments         Match your mosque start times                       (SIGNOFF; was "Match your mosque")
settings_adjustments_help    ... and this is for the few minutes left over. Jama'ah times go in Jama'ah times.   (SIGNOFF; sentence appended)
```

Deleted: `jamaah_out_of_window` (five languages). Kept: `jamaah_check`, `jamaah_line`, `jumuah_line`, `jamaah_today`, `jamaah_rule_after_help`, `jamaah_pick_time_title`, `mosque_diff_add_jamaah`, `mosque_diff_link`.

`%2$s` in `jamaah_after_window` is the next prayer's `labelRes` (Dhuhr → Asr, Asr → Maghrib, Maghrib → Isha, Isha → Fajr); Fajr uses its own string. Translation notes: Urdu "جماعت", Arabic "الجماعة"; "Begins" is the board's column word (Urdu "شروع" or "اول وقت"; Turkish "Vakit"; Indonesian "Masuk"). No "Tap", no "missed", "late" or "owed", no hyphen or dash, sentence case, UK spelling.

**Why "after Maghrib begins" and not "after Asr ends":** "ends" states a deadline, which the app never does for a prayer; the next prayer beginning is a fact every madhab shares.

## 9. Tests

- `JamaahPlanTest`: the five `OutOfWindow` assertions become `Before` or `After` (`:49, 54, 59, 70, 89`); add Isha fixed 05:30 → `After`; codec v2 round trip with `ZONE=Europe/London`, v1 without `ZONE=` decodes with `zoneId = null`, a damaged `ZONE=` value is dropped and the rules survive. **Mutation proof:** swap `Before` for `After` in `check`, see three fail.
- New zone guard test: pure `JamaahTimes.visibleIn(zoneId: String): Boolean` (true when stored id is null or equal); Europe/London vs Asia/Dubai false, London vs London true, null true. **Mutation proof:** compare offsets instead of ids, see "Europe/London vs Europe/Lisbon in winter" wrongly pass.
- `TranslationIntegrityTest`: the new keys in five languages, `jamaah_out_of_window` absent from all five.
- `DesignRulesTest`: no new button; the quiet notice's gap is a `Space` token.
- `InertReminderSettingsTest`: unchanged and green.
- `LargeTextAndTalkBackDeviceTest`: seed Dhuhr `Fixed(13:30)` and Asr `Fixed(15:30)` on a day where Asr begins later; assert "Jama'ah 1:30 pm" under Dhuhr and "Check your jama'ah time" under Asr, inside the root at 2.0 on 320dp; the Dhuhr row's merged text starts with "Dhuhr".
- By hand: emulator zone to Asia/Dubai after typing in Europe/London (lines gone, quiet notice), back (lines back); a Friday; `:app:installRtl` judged for mirroring only; 1.5 and 2.0 at 823dp and 320dp.

## 10. DESIGN_SYSTEM changes

- §4.1 inventory row 511: the quiet notice while in another time zone; row 495 label "the Begins time on your mosque's board"; rows 480, 493 to 497 and 510 say "Match your mosque start times".
- §4 Notices: the quiet tone is also used for a withholding by the user's own rule (the travel notice), dated.
- §9: dated entry with the decisions and rejections in §1 and §5.
- §11: `1.6-mosque-door` moves to §9 when Ali answers.

## 11. iPhone effect, and the HIG check

**Carries over.** The rules, the window check, the codec and the zone id are plain Kotlin in `:core` (`ZoneId` becomes `TimeZone.current.identifier`; `visibleIn` is a string compare); stored data stays one plain string. HIG: progressive disclosure; rows 56dp (over 44pt); text scales; no FAB, no drawer, X and Close on every dialog. The mosque door's icon has no SF Symbol (§6). Nothing Android only in this step.

## 12. Pictures for Ali

English, 823dp frame, light unless said. Seed: Dhuhr fixed 1:30 pm, Maghrib 15 minutes after, Isha 20 minutes after, Jumu'ah 1:15 pm, Asr fixed 3:30 pm on a June day (so Asr is out of window).

1. Times, light, door present (Q15 "with"): three lines, "Check your jama'ah time" under Asr, Dhuhr the Next row.
2. Times, dark, door present.
3. Times, light, door removed (Q15 "without"; TEMPORARY switch, deleted after).
4. Times, light, option C once (door compact on the Today line), for comparison only.
5. Times on a Friday: "Jumu'ah 1:15 pm" under Dhuhr.
6. Jama'ah times dialog, light: rows, the two line "Today" subtitle, the amber before notice under Asr.
7. Jama'ah times dialog, dark.
8. Jama'ah times dialog with the phone in Asia/Dubai: the quiet notice, no Today lines, no amber.
9. Times with the phone in Asia/Dubai: no lines.
10. Urdu, Times (mirroring only).
11. 2.0 text at 320dp, Times.

For `plan-verifier` and `reviewer` only: Ramadan Fajr row at 320dp, 1.5 text, TalkBack dump of a row with a line, animator scale 0, v1 string decoding.

## 13. Questions for Ali (from the pictures; recommendation first)

1. **The mosque door, from pictures 1 to 4 (Q15):** "Keep it under the rows, as now (Recommended)", "Remove it from Times (Settings keeps the route)", or "Compact on the Today line beside Timetable". His because it is §11 OPEN `1.6-mosque-door` and changes an approved screen.

Not asked: the grey unmarked line, "Check your jama'ah time", the quiet travel notice, the two line subtitle, the before and after wording, the SIGNOFF renames. Each is decided by a rule or prior decision named above.

## Verified in code, and assumed

Verified: `JamaahTimes.kt` in full (no zone stored; `OutOfWindow` one object; windows; Friday rule; codec v1); `JamaahPlanTest`; `HomeScreen.kt:197-306`, `:680-784`, `:1047-1125`, `:1146-1284`; `SettingsScreen.kt:279-301`, `:790-937`, `:1535-1548`, `:1605-1660`; `SajdaViewModel.kt:676-715`; `SettingsRepository.kt:515-527, 627, 705`; `Notice.kt:94-110`; `Bell.kt:79-84`; `Type.kt`; `strings.xml` `:732-756`, `:468-477`, `:190-196`, `:329`; the four translations of `jamaah_out_of_window`; `ExtraReminderPlans.kt` reads `JamaahPlan`; pictures `times-tracker-jamaah.png`, `jamaah-times.png`, `step5/2-day-light.png`, `step3/final/3-times-dark.png`, `5-320dp-text-2.0.png`; DESIGN_SYSTEM §0, §2, §3, §4.1, Notices, §10, §11.

Assumed: Compose lays a `Text` at its `lineHeight` (the 44dp sum); the widths in §3 (character counts, not measured); a merged row reads children in layout order; `ZoneId.systemDefault().id` returns an Olson id on every phone (a phone reporting "GMT" would hide the lines at home until a save stamps "GMT"; self heals; a HANDOVER §15 line, not code); the emulator can change zone via `adb shell setprop persist.sys.timezone` or Extended controls.

**Flags in the built code:** (1) no zone guard (Q13 not built; this step); (2) `jamaah_out_of_window` is the old sentence; (3) the three SIGNOFF renames not applied (`strings.xml:468, 470, 474`); (4) DETAILED-PLAN "about 2dp per row" is stale, true cost 0dp at 1.0; (5) the Next row's note can orphan "pm"; (6) the After rule's subtitle wraps "Today" from its time. Nothing in the built code breaks a §4.1 rule.

**Risks:** the non breaking space must not reach `TimeFormat.clock` itself without a separate decision; if a `Notice` with `tone = QUIET` and `details` inside an `AlertDialog` makes its "Details" chevron the dialog's first button and fights the rows, fold the details into the brief as two sentences and record the exception; a user who types a jama'ah time while abroad stamps that zone, which the notice explains but does not prevent.

## 14. Plan-verifier corrections (25 Oct 2026). These win over §1 to §13 where they differ

1. **The gate lives in `:core`, not the ViewModel.** `JamaahPlan.on(day, zone, jamaah)` returns `emptyMap()` when `jamaah.zoneId != null && jamaah.zoneId != zone.id`. This covers the ViewModel and the 1.6.1 planner (`ExtraReminderPlans.kt:39-49` calls `JamaahPlan.on` itself). `JamaahDialog` (`SettingsScreen.kt:683, 796-823`) gets the raw settings, so it needs its own `visibleIn` check: show the quiet notice and skip `JamaahPlan.result` previews and amber. `JamaahPlanTest` gains "London rules in Dubai are hidden" (mutation: remove the check, see it fail). A device test seeds a stored string with `ZONE=Asia/Dubai`, opens Jama'ah times, and asserts the notice text is present and "Today" is absent.
2. **Picture seed.** Asr is fixed at **1:00 pm**, before Asr begins on any day, so "Check your jama'ah time" shows on the October emulator. The June seed is dropped.
3. **`docs/privacy.html:181-183`** becomes "…any jama'ah times you type in, with the time zone they were set in, are kept on this phone only…" in the same commit. `TrackerPrivacyTest` checks the phrase "kept on this phone only" and stays green.
4. **`settings_value_lines`** is `translatable="false"` in `values/strings.xml` only. `TranslationIntegrityTest` excuses that attribute.
5. **Picture 4 and option C are dropped.** Two solid buttons side by side breaks §4.1, and Fable ruled it out on 24 Oct (`HomeScreen.kt:230-232`). Q15 is with or without, pictures 1 to 3. §5's placement table stays as the record of why.
6. **The hide takes effect at the next recalculation** (location refresh on resume, a settings change, a prayer passing, the date). It is not instant on `TIMEZONE_CHANGED`. The by-hand check relaunches the app after `setprop persist.sys.timezone`. HANDOVER says so.
7. **The result carries its slot.** `JamaahResult` becomes `InWindow(at)`, `Before`, `After(next: PrayerSlot)`; `next` is `SUNRISE` for Fajr, from `windowEnd`. The UI reads `next`'s label; Fajr uses `jamaah_after_sunrise`. Jumu'ah is told apart by the caller (the dialog's Jumu'ah row calls `JamaahPlan.jumuah`), so it uses `jumuah_before_window` / `jumuah_after_window`. `JamaahPlanTest` pins Isha → Fajr and Fajr → SUNRISE.
8. **More pictures for Ali:** 1.5 text on Times; the dialog at 2.0 and 320dp; the dialog in Urdu. Plus Arabic Times.
9. **`jamaah_away_details`** becomes "They come back when you are home. If you have moved, change any one of them here and they will all follow this time zone."
10. **The stale "about 2dp" comment at `HomeScreen.kt:1240-1241`** is corrected to 0dp at 1.0.
11. **Minor.**
    - A `ZONE=` value that `ZoneId.of` rejects is dropped and read as v1, so a corrupt id never hides the lines (tested).
    - Android has renamed zone ids before (`Asia/Calcutta` → `Asia/Kolkata`). A renamed id would show the travel notice at home until the next save. It heals itself; this goes in HANDOVER §15 beside the "GMT" note.
    - The TEMPORARY switch for picture 3 follows step 6's pattern and is removed before the merge.
    - Each touched screen answers the DESIGN_SYSTEM §10 questions.

## 15. Strings, five languages (main session, 25 Oct 2026). These win over §8

Placeholders: `%1$s` is the prayer's own label (`labelRes`, so a Shia user's names follow), and `%2$s` is the label of the slot it passed (`After.next`). Fajr's after-form, and both Jumu'ah forms, take one label each: Fajr; Dhuhr, then Asr. No prayer name is ever written as a literal. Every string keeps an XML comment saying what each placeholder is.

| Key | en | ur | ar | tr | in |
|---|---|---|---|---|---|
| `jamaah_before_window` | Your %1$s jama\'ah time is before %1$s begins. Check your mosque\'s times. | آپ کا %1$s کی جماعت کا وقت %1$s شروع ہونے سے پہلے ہے۔ اپنی مسجد کے اوقات دیکھیں۔ | وقت جماعة %1$s الذي أدخلته قبل دخول وقت %1$s. تحقق من أوقات مسجدك. | %1$s cemaat vaktiniz, %1$s vakti girmeden önce. Caminizin vakitlerini kontrol edin. | Waktu berjamaah %1$s Anda sebelum waktu %1$s masuk. Periksa jadwal masjid Anda. |
| `jamaah_after_window` | Your %1$s jama\'ah time is after %2$s begins. Check your mosque\'s times. | آپ کا %1$s کی جماعت کا وقت %2$s شروع ہونے کے بعد ہے۔ اپنی مسجد کے اوقات دیکھیں۔ | وقت جماعة %1$s الذي أدخلته بعد دخول وقت %2$s. تحقق من أوقات مسجدك. | %1$s cemaat vaktiniz, %2$s vakti girdikten sonra. Caminizin vakitlerini kontrol edin. | Waktu berjamaah %1$s Anda setelah waktu %2$s masuk. Periksa jadwal masjid Anda. |
| `jamaah_after_sunrise` | Your %1$s jama\'ah time is after sunrise. Check your mosque\'s times. | آپ کا %1$s کی جماعت کا وقت طلوعِ آفتاب کے بعد ہے۔ اپنی مسجد کے اوقات دیکھیں۔ | وقت جماعة %1$s الذي أدخلته بعد شروق الشمس. تحقق من أوقات مسجدك. | %1$s cemaat vaktiniz, güneş doğduktan sonra. Caminizin vakitlerini kontrol edin. | Waktu berjamaah %1$s Anda setelah matahari terbit. Periksa jadwal masjid Anda. |
| `jumuah_before_window` | Your Jumu\'ah time is before %1$s begins. Check your mosque\'s times. | آپ کا جمعہ کا وقت %1$s شروع ہونے سے پہلے ہے۔ اپنی مسجد کے اوقات دیکھیں۔ | وقت الجمعة الذي أدخلته قبل دخول وقت %1$s. تحقق من أوقات مسجدك. | Cuma vaktiniz, %1$s vakti girmeden önce. Caminizin vakitlerini kontrol edin. | Waktu salat Jumat Anda sebelum waktu %1$s masuk. Periksa jadwal masjid Anda. |
| `jumuah_after_window` | Your Jumu\'ah time is after %1$s begins. Check your mosque\'s times. | آپ کا جمعہ کا وقت %1$s شروع ہونے کے بعد ہے۔ اپنی مسجد کے اوقات دیکھیں۔ | وقت الجمعة الذي أدخلته بعد دخول وقت %1$s. تحقق من أوقات مسجدك. | Cuma vaktiniz, %1$s vakti girdikten sonra. Caminizin vakitlerini kontrol edin. | Waktu salat Jumat Anda setelah waktu %1$s masuk. Periksa jadwal masjid Anda. |
| `jamaah_away_brief` | You are in a different time zone, so your jama\'ah times are not shown. | آپ کسی دوسرے ٹائم زون میں ہیں، اس لیے آپ کے جماعت کے اوقات نہیں دکھائے جا رہے۔ | أنت في منطقة زمنية مختلفة، لذلك لا تظهر أوقات الجماعة. | Farklı bir saat dilimindesiniz, bu yüzden cemaat vakitleriniz gösterilmiyor. | Anda berada di zona waktu lain, jadi waktu salat berjamaah Anda tidak ditampilkan. |
| `jamaah_away_details` | They come back when you are home. If you have moved, change any one of them here and they will all follow this time zone. | گھر واپس آنے پر یہ دوبارہ نظر آئیں گے۔ اگر آپ کہیں اور منتقل ہو گئے ہیں تو یہاں ان میں سے کوئی ایک بدل دیں، پھر سب اسی ٹائم زون کے مطابق ہو جائیں گے۔ | ستعود عندما تكون في بلدك. إذا انتقلت، فغيّر أيًّا منها هنا وستتبع كلها هذه المنطقة الزمنية. | Evinize döndüğünüzde geri gelirler. Taşındıysanız buradan herhangi birini değiştirin, hepsi bu saat dilimine uyar. | Waktu ini akan muncul lagi saat Anda kembali ke rumah. Jika Anda pindah, ubah salah satunya di sini dan semuanya akan mengikuti zona waktu ini. |
| `settings_adjustments` | Match your mosque start times | شروع کے اوقات اپنی مسجد سے ملائیں | طابِق أوقات الدخول مع مسجدك | Giriş vakitlerini camiye göre ayarla | Sesuaikan waktu masuk dengan masjid |
| `settings_adjustments_help` (append one sentence after the existing text) | Jama\'ah times go in Jama\'ah times. | جماعت کے اوقات، جماعت کے اوقات میں درج کریں۔ | أما أوقات الجماعة فتُدخل في أوقات الجماعة. | Cemaat vakitleri, Cemaat vakitleri bölümüne girilir. | Waktu berjamaah dimasukkan di Waktu salat berjamaah. |
| `adjustment_pick_time_title` | %1$s: the Begins time on your mosque\'s board | unchanged (already "شروع ہونے کا وقت") | unchanged (already "وقت الدخول") | unchanged ("giriş vakti") | unchanged ("waktu masuk") |
| `settings_value_lines` | `%1$s\n%2$s`, `translatable="false"`, values only | | | | |

Deleted in all five: `jamaah_out_of_window`.

## 16. Ali's answer to Q15, and Fable's verdict on the feature (25 Oct 2026)

Ali, shown pictures 1b, 1c, 3, 6 and 8: "I'm beginning to think, adding jamaat could cause more confusion for thet
timing will cause more confusion for the user check with fable and it agrees with me then maybe we remove it else go
with recommendations but before development run compact". Then: "Jamaat time I meant".

**Fable's independent verdict: keep jama'ah times as built.**
- Nobody sees a jama'ah time without typing it, so the default screen is unchanged.
- For the mosque goer it answers "when do I need to be there?".
- The one real gap is a fixed time that drifts but stays inside the window. It has no warning. It is the same risk
  as Match your mosque. A 1.6.1 candidate: the row subtitle names the month it was set ("3 prayers set in March").
- Removing it would cost both 9e9102e and step 7: 29 strings in five languages, and the 1.6.1 jama'ah reminder.
- No middle option is better.

**So, by Ali's rule, the recommendations stand:**
- Jama'ah times stay in 1.6.0.
- The mosque door stays under the rows (Q15).
- Done 25 Oct 2026 after the compact: `mosqueDoorShown` removed, `1.6-mosque-door` closed in DESIGN_SYSTEM §11,
  gate green, merged into `claude/1-6-0`.
