# Fable 5.1 independent design review of the 1.6 direction (23 Oct 2026)

Read only, written by a separate Fable 5.1 agent from PLAN.md 0.1, PILLARS-STUDY.md, DESIGN_SYSTEM.md, the code and Ali's reference screenshots. Claude Code spot-checked RamadanState.kt:32-34, MainScaffold.kt:156 and HomeScreen.kt:196-201 against it. **These are recommendations, not decisions: Ali decides from screenshots.**

---

# SajdaTime 1.6 design review (independent, read only)

## A. Overall verdict and the biggest risk

The direction is right for the list and the tracker: bells on rows, labelled actions, partial rings and a visible pause all make the screen say more with fewer trips to Settings, and all of it fits the design system as written. Two parts do not come free: the four skies and the Qibla restyle are visual overhauls, not additions.

**The single biggest risk is scope against the date.** SIGNOFF.md:32 fixes the PR for 11 Dec with three features already signed off. §0.1 then adds a sky header, a bell, a fourth tab, a month view, a pause chip, a qada tracker, a Qibla restyle, eight animations and a moved timetable. That is roughly a second release. Every one of them needs light, dark, RTL, 1.5 text, 823dp and screenshots for Ali (DESIGN_SYSTEM.md §0.3). The likely outcome is either a slipped 1.6.0 that misses Ramadan, or a visual overhaul that ships half checked in the month people judge the app. My recommendation is in D: split it.

## B. The seven decisions

**1. Four fixed skies: change, do not reject.** The fixed gradient makes it testable, which is exactly why a moving sky was removed twice (DESIGN_SYSTEM.md:461-464 and the 17 and 18 Oct entries at 927 and 933). Four skies in two themes is 24 stops against three inks, about 72 assertions: mechanical, fine. The conflicts are elsewhere:
- *Height.* Times must fit 823dp without scrolling (DESIGN_SYSTEM.md:722-725; HomeScreen.kt:173-190 records the 37dp overrun). Pillars gives the sky a quarter of the screen. Our card is already about 200dp. A sky with a sun path above it does not fit unless the sky *replaces* the card rather than sitting above it.
- *Dark theme.* A "day" sky in dark mode is a contradiction. Decide: in dark, all four skies are dark variants, or dark gets one sky. Question for Ali (E1).
- *Artwork behind words.* §9 already says the artwork never sits behind the words (DESIGN_SYSTEM.md:753). Keep the countdown and name on a fixed plate inside the sky, and let sun, moon and the day path live in the margins.
- *Boundaries.* Dawn = Fajr to sunrise, day = sunrise to Asr, evening = Asr to Isha, night = Isha to Fajr. Say it in one place and test it, or the "animated change" fires at an odd minute.
- *RTL.* A day path drawn left to right is a timeline. In Urdu and Arabic, time runs the other way. Ask (E2).

**2. One-tap bell: agree, with two changes.** The alert model has three states per prayer, off, notification or alarm (SettingsRepository.kt:59-65; AlertsDialog, SettingsScreen.kt:1191). A single tap from off needs a style: restore the prayer's last style, default notification. Shape must show the state (bell, bell with a slash, alarm clock for ALARM), which §9 already says. The accidental tap: no confirmation dialog, but a four second snackbar "Fajr alert off. Undo". Silencing the Fajr alarm by brushing the row is the one case that costs a prayer, and Undo is cheaper than a dialog on every tap. Rule conflict: §4.1 lists six kinds of control and the bell is none of them; add it to the inventory as a toggle, like Tick (DESIGN_SYSTEM.md:429), not a button. Width conflict: UX.md:44 already measures the row at 300dp against 292dp with a tick alone. A bell and a tick on the same row do not fit on a 360dp phone. See decision 5 and C.

**3. Labelled chips: agree, but call them what they are.** §4.1 bans anything that acts but is not solid (DESIGN_SYSTEM.md:379-387). A "chip" is a compact CtaButton (40dp drawn, 48dp target, wraps, 8dp gap), the same as "Save as PDF". Do not add a chip component; two of them side by side is already specified (Back and Continue).

**4. Qibla: agree.** The sentence first is right; the current screen puts "5024 km at 118°" above the dial (02-qibla.png), which is the least useful line for the most people. Keep the degree in "Turn right 58°": Pillars drops numbers, but the number is what tells you to keep turning. "Details" is a solid compact CtaButton; Notice already has that shape with a chevron. Miles: use the locale's measurement system (US, Liberia, Myanmar, and the UK reports miles for distance), not a setting. Do not grow the dial beyond what leaves the instruction under it (QiblaScreen.kt:190). No compass and no location states exist (qibla_sensor_missing, qibla_needs_location); keep them as amber Notices above the dial, unchanged.

**5. The five additions.**
- Partial rings: agree. Text equivalent "3 of 5" as planned.
- Paused days distinct: agree; the sheet already prints "Paused" (last-30-days.png). Add the pause glyph to the ring.
- Month view: agree only as the capped 30 days drawn as a grid. A year grid was rejected because a grid of blanks is a missed-prayer counter (PLAN.md:256, UX.md:104). A 30-day grid of empty rings is the same object; keep every empty ring a plain outline, no count, title stays "Last 30 days".
- One-tap pause: change slightly. Pause needs "from today or yesterday" (UX.md:62). One tap means pause from today, with "Paused from today. Undo" in a snackbar; the dialog stays in Settings.
- Qada tracker: **defer to 1.6.1.** Reasons: it is useful only after Ramadan (PLAN.md:142 says so); "period" and "pregnancy" are health data and go into the export file (PLAN.md:136); "Missed fasts" puts the banned word on the same screen where TrackerWordingTest forbids it for prayers (PLAN.md:254). If it ships at all, ship the count without reasons first. Reasons add privacy text and five translations for no change to the number.

**6. Animation: agree.** One warning, marked as a guess: Compose's own animations do not automatically follow the phone's "Remove animations" setting the way View animators do. The app has no handling today (nothing in the source reads the animator scale). PLAN.md:188 already says check it on the emulator; budget for writing it. Drop item 5 "Changing day" (PLAN.md:178): there is no day stepper, and §7 says there should not be one (DESIGN_SYSTEM.md:666).

**7. Timetable: agree with the sheet, change the button.** Keep the button on the Today line where Ali already placed and sized it (HomeScreen.kt:412 in the inventory), relabelled "Timetable" with a calendar icon. The sheet shows the month on screen with "Save as PDF" inside, and opens on Ramadan when `offersTimetable` is true (HijriCalendar.kt:62). Putting it *under* the list adds a third thing there beside the tracker line and the mosque door, costs height, and the rename alone meets "easy on the eyes and understand".

**The fourth tab.** §8 "every tab opens the same way" is met with a ScreenTitle "My prayers". The real conflict is MainScaffold.kt:156-160: one fixed usage-count word is sent per main tab. A new tab is a new value, and CLAUDE.md forbids new events without agreement and a privacy change. **Send nothing for the Tracker tab.** Second question: what the tab shows when tracking is off. Recommend it is always present and its empty state is the switch itself (one sentence, one solid "Start tracking"). That is discovery, not a nag.

## C. Screens, top to bottom

**Times.** City with caret; date line, Hijri in primary (opens dates). Sky header holding the next prayer plate (label, name, time, countdown). Rare notices. Today line with "Timetable". Six rows: glyph, name, optional jama'ah second line, Now or Next pill, time, bell. Mosque door. States: past rows stay full contrast with no treatment (Pillars fades them, which fails AA); current is `surfaceContainerHigh` and the Now pill; next is the fill, bar and Next pill (three signals, DESIGN_SYSTEM.md:469-478); sunrise dimmed as now; alert off is the slashed bell plus content description; no location is the existing banner. Ticks leave this screen.

**Tracker.** Title "My prayers". Today's five rows as a card list with the ring tick at the start, time at the end. Under it, in one row: "Pause" and "Last 30 days" as compact solid buttons, 8dp apart. Then the 7-day ring strip with "3 of 5" text. States: done is a filled ring with a tick; not yet due is a faded disabled ring with "not due yet"; paused day is the pause glyph; paused mode replaces the buttons with "Tracking is paused" and "Resume" (MyPrayers.kt:56-60 already). Empty: the switch. Streak only when on and above zero.

**Qibla.** Title. "Turn right 58°" large, "Facing 60°" under it. Dial with arc and Kaaba. Key. Place name small. "Details" button opening distance and bearing. Aligned: the existing fill, rim and wording. No compass or no location: amber Notice above the dial.

**Settings.** Loses nothing; the Prayer alerts dialog and the My prayers group stay so both places show one setting (DESIGN_SYSTEM.md:764). Settings is where sounds, pause from yesterday, save, load and delete remain.

## D. Order before Ramadan

1. Tracker (signed off) with partial rings and the pause button, on the Tracker tab, ticks off Times.
2. Islamic dates sheet.
3. Bell with Undo.
4. Timetable rename and sheet.
5. Jama'ah lines (first to cut).
After Eid, 1.6.1: skies, Qibla restyle, month grid, qada count, the reminders.

## E. Questions for Ali, with recommendations

1. In dark mode, do all four skies stay dark, or does dark get one night sky? Recommended: dark variants of all four, shown as four screenshot pairs.
2. Should the sky header replace the green card or sit above it? Recommended: replace it, the words on a plate inside the sky, so Times still fits his S23 Ultra.
3. Ship the skies and Qibla restyle after Eid, so 1.6.0 lands before Ramadan? Recommended: yes.
4. Tapping a bell off: a short "Undo" message, or nothing? Recommended: Undo.
5. Tracker tab always visible, with its switch on it when off? Recommended: yes.
6. Missed fasts: count only, no reasons, after Ramadan? Recommended: yes.

## F. Missed

- Pillars highlights Sunrise as the current period (ali-1). We deliberately show no "Now" between sunrise and Dhuhr (DESIGN_SYSTEM.md:494-497). Do not copy it.
- Pillars' "Show more" (midnight, last third) carries rulings; we already give suhoor. Leave it.
- We have what Pillars lacks and should show it: the Ramadan card in the sky slot during the month, the tappable Hijri date, five languages, widgets and the watch. None of them changes in 1.6, so the header must still leave room for the Ramadan card.
- `docs/privacy.html` has no tracker section yet (grep found none); whichever release carries the tracker needs it in the same commit.

Not checked: any device measurement; everything about height is read from the code's comments, not rendered.

---

# Follow-ups for Ali (read only)

## 1. Ramadan in the new layout

**Start from what the code does today.** Suhoor ends is Fajr and iftar is Maghrib (RamadanState.kt:32-34). For a Shia user Maghrib is already pushed past sunset by the Jafari and Tehran conventions (PrayerEngine.kt:27-35, 215, 241), so "Iftar 5:33 pm" is correct for both sects with no separate state. Suhoor ends at Fajr for both. The only sect difference in Ramadan wording is the last ten nights (PLAN.md:68-69), and SIGNOFF.md:59 already dropped that range, so nothing on Times names them for anyone. "Day 15 of 29" in the card title is neutral and stays.

**The timeline, as the code already runs it:**

| When | What appears | Where | Goes away |
|---|---|---|---|
| 30 days before day 1 | Ramadan timetable offered (`offersTimetable`, HijriCalendar.kt:62, `EXPORT_LEAD_DAYS` 30) | The Timetable sheet opens on it | Last day of Ramadan |
| 3 days before to day 2 | Quiet Ramadan notice with "Shift the date" (RamadanCard.kt:87, 364; `NOTICE_LEAD_DAYS` 3) | Under the banners, above the Today line (HomeScreen.kt:203-209) | X, or day 3; days 1 and 2 fold the date hint into the card so two Ramadan items never stack (RamadanCard.kt:94-97) |
| Day 1 to the last day | The Ramadan card: two tiles, countdown, fast length, tomorrow's times, timetable button | Same slot | Day after the last day |
| Eid day | Eid quiet notice with the "follow your mosque" footnote (EidCard.kt:74-81) | Same slot; cannot meet the card (HomeScreen.kt:199-201) | X, once per Eid |

**Rule for 1.6.0 (before Ramadan 1448): change nothing in that table.** The sky ships after Eid, so the card layout that 1.5.0 proves in the field is the one Ramadan 1448 runs on. The only 1.6.0 touches:
- The renamed "Timetable" button opens the Ramadan sheet while `offersTimetable` is true, otherwise the month. The card keeps "Get the full Ramadan timetable" (Ali approved it, DESIGN_SYSTEM.md:860-862); two doors to one sheet is a duplicate but not a confusion, and removing it is a visible change needing his screenshot.
- The Fajr bell controls the Fajr alert only. The suhoor reminder is its own setting (`suhoorReminderMinutes`, on by default 60 minutes before, PrayerAlarmScheduler.kt:37) and stays in Settings. The scheduler already drops a Fajr heads up that lands on the same minute as suhoor (lines 96-101), so a bell off plus suhoor on behaves correctly. Say it in the bell's content description for Fajr only when Ramadan is on: "Fajr alert off. Suhoor reminder stays on."
- Tracker in Ramadan: no change. Five prayers only, no fasting tick, no Tarawih, no qada (deferred). The Isha after midnight rule (UX.md:109) is what matters here, because Isha is prayed late in Ramadan.
- Height: Ramadan already scrolls at 823dp, knowingly (HomeScreen.kt:198-201). Keep that for 1.6.0. Do not spend December making the card fit.

**After Eid, when the sky replaces the card (Ramadan 1449):** the two countdowns on today's screen are the same number twice (03-ramadan-dark.png: "02:32:51" in the hero and "Iftar in 02:32:51" in the card). Merge them:
- The plate inside the sky carries the Ramadan phase (`RamadanState.Phase`, RamadanState.kt:54-60): before suhoor ends, label "Suhoor ends", Fajr and its time, the countdown; while fasting, label "Iftar", Maghrib, the countdown; after iftar, back to "Next prayer" Isha. Plate height is fixed, so nothing below it moves when the words change.
- Fajr and Maghrib rows carry "Suhoor ends" and "Iftar" second lines, which the widget already does (WidgetState.kt:35-45, 86-92) and the row code already budgets at 6dp (HomeScreen.kt:1050-1052).
- "Day 15 of 29" becomes a small chip on the Today line; fast length, tomorrow's times and the (i) move into the Timetable sheet's header.
- The card then disappears entirely and Times fits 823dp with no scroll: two second lines cost about 12dp, the chip costs none. The 3-day notice and the Eid notice keep their slot above the Today line, both closable.
- Sky in Ramadan and on Eid: the ordinary time-of-day sky, nothing special. A festive sky is decoration, and green is never decoration (DESIGN_SYSTEM.md:719-721).
- Ask Ali with two screenshots: A, plate carries the Ramadan countdown and the card goes (recommended); B, trimmed card kept and the screen scrolls.

**Nothing jumps:** cards (notice, Eid) only enter when the screen is opened or the tab returned to, never while it is on screen (guess: this needs a "state at open" snapshot in HomeScreen; cheap but new). Phase changes and the Now/Next move are crossfades at fixed height. The countdown digits change text, never position (tabular figures, Type.kt:22-38).

**Widget and watch.** The widgets need nothing: they already switch Fajr and Maghrib labels and follow the Ramadan card switch (WidgetState.kt:36-37). The watch has no Ramadan code at all (no match in `wear/` or `WatchSyncContract`); it shows Fajr and Maghrib and that is the known gap, not 1.6 work (PLAN.md:257 keeps the watch unchanged).

## 2. Animation plan

**Animator scale, what is known.** The app has no handling of the animator scale today (no `ANIMATOR_DURATION_SCALE` or `MotionDurationScale` in `app/` or `wear/`). Compose BOM 2026.06.01 (libs.versions.toml:8). From memory, marked as a belief to verify: Compose has honoured the system "Animator duration scale" since 1.7 through `MotionDurationScale`, so "Remove animations" (scale 0) makes every `animate*AsState`, `Animatable`, `AnimatedVisibility` and `AnimatedContent` jump to its end state with no code. Verify once on the emulator with Developer options, Animator duration scale, off, on the tick and the sky, as PLAN.md:188 asks. Only hand-rolled loops escape it, and none are planned.

**Rules.** 150 to 400 ms, `FastOutSlowInEasing` (Material standard) unless stated. No `rememberInfiniteTransition` anywhere (add to `DesignRulesTest`, suggestion). Nothing enters with motion while the user is looking; entrances happen on open. Never animated: prayer times, the countdown and its digits, notice text, the disclaimer, the widget (RemoteViews cannot) and the watch (PLAN.md:187).

| # | Screen | Trigger | What moves | Duration | Haptic | Remove animations | Release |
|---|---|---|---|---|---|---|---|
| 1 | Tracker | Tick tapped | Ring fills, tick draws in; reverse on untick | 300 ms | ToggleOn / ToggleOff (UX.md:81) | Jumps to end | 1.6.0 |
| 2 | Tracker | Screen opens, tick changes | Week rings grow to their share | 400 ms | None | Jump | 1.6.0 |
| 3 | Times | Bell tapped | Crossfade bell to slashed bell (shape, not only colour) | 150 ms | ToggleOn / ToggleOff | Jump | 1.6.0 |
| 4 | Times, Tracker | Undo snackbar | Material default | Platform | None | Platform | 1.6.0 |
| 5 | Tracker | Pause or Resume | Button row crossfades to "Tracking is paused" at the same height | 200 ms | None | Jump | 1.6.0 |
| 6 | All | Tab change | Existing crossfade (MainScaffold.kt:186-191) | Existing | None | Jump | Exists |
| 7 | All | Sheets open | Material bottom sheet slide | Platform | None | Platform | Exists |
| 8 | Settings, notices | Details fold | Existing chevron turn and reveal (SettingsList.kt:277, Notice.kt:117) | 200 ms | None | Jump | Exists |
| 9 | Times | Prayer time passes | Now and Next fill and bar crossfade to the next row | 300 ms | None | Jump | After Eid |
| 10 | Times | Time crosses a sky boundary, or opened in a new sky | Gradient crossfade, plate text fixed | 400 ms | None | Jump | After Eid |
| 11 | Times | Screen opens | Day path draws from Fajr to now once; then the dot moves only on the minute tick, no tween | 400 ms | None | Jump | After Eid |
| 12 | Times, Ramadan | Phase changes (suhoor end, iftar) | Plate words crossfade, fixed height | 300 ms | None | Jump | After Eid (1.6.0 keeps the card, which changes words with no motion today) |
| 13 | Qibla | Aligned becomes true | Face fill and rim colour animate; existing LongPress haptic kept (QiblaScreen.kt:148-165) | 250 ms | Existing | Jump | After Eid |
| 14 | Qibla | Heading changes | Existing 120 ms dial tween (QiblaScreen.kt:207-209); input driven, not endless | Existing | None | Jump | Exists |
| 15 | Times | Ramadan notice, Eid notice, exact alarm notice | No entrance animation; present on open | None | None | None | 1.6.0 |
| 16 | Times, Tracker | Streak number, countdown, times | Text changes only, never position | None | None | None | Always |

**Notes.** The sky change (10) should be the only animation tied to the clock, and it fires at most four times a day. The day path (11) is drawn once per open, which is where Pillars' looping weather was rejected (PILLARS-STUDY.md:68). Item 9 is cheap but is a visible change to a screen Ali has approved, so it waits for screenshots with the sky. Items 1 to 5 are the whole of 1.6.0's motion: all on the Tracker tab or the bell, all under 400 ms, all one haptic at most, none while the user is not touching the screen.

Not verified: any of the durations on a device, and the Compose animator scale behaviour, which is stated from memory.
