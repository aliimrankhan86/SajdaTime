# Release 1.6.0 plan: the three features after 1.5.0 (draft, 23 Oct 2026)

> **Signed off 23 Oct 2026 (Fable 5.1): read `SIGNOFF.md` first. It overrides this file, including §0.** Final order: 1 prayer tracker, 2 Islamic dates sheet, 3 jama'ah times on the Times screen (cut first if short of time). Reminders in 1.6.1. Submit by 15 Dec 2026, or all of 1.6.0 goes after Eid if 1.5.0 is not live by 27 Nov.

> **Added 23 Oct 2026 (Ali): five items from the Pillars review, and a design pause.** See §0.1. Nothing in
> §0.1 is built, and no UI change is made until Ali says go.

Status: **draft for Ali's yes, nothing built.** The research behind it is
`docs/research/2026-10-next-release-features.md`. This plan builds on the tracker plan Ali already approved in
`docs/plans/2026-10-widget-ramadan-tracker/` (on the `claude/quiet-glass` branch, section 7, B0 to B8) and does not
redo it.

## 0. Agreed shape after the independent Fable 5 review (23 Oct 2026). This section overrides the sections below.

Ali, 23 Oct 2026: this is the plan for the release after 1.5.0, and 1.5.0 comes first.

**Split into two releases:**
- **1.6.0, before Ramadan.** The tracker, the Islamic dates sheet, and jama'ah times shown on screen if the
  height measurement allows it.
- **1.6.1, after Eid al-Fitr, live by early April 2027.** The jama'ah reminder and the fasting-day reminders.
- **Why split:** the reminders are the only scheduler change and the only risk rated severe. Fasting
  reminders also do almost nothing before Eid. Ramadan is fasted anyway, and Shawwal, Arafah (15 May 2027)
  and Ashura (15 Jun 2027) all come after it.

**Timing.** Ali's 16 Oct rule stands: submit 1.6.0 by about 15 Dec 2026, which needs 1.5.0 live by about
1 Dec. If that slips, all of 1.6.0 ships after Eid. Build order: the tracker first, because it is the reason
to beat Ramadan.

**Naming.** The new Settings row is called **"Jama'ah times"**, not "Mosque times". "Match your mosque"
gains the word "start" so the two are not confused.

**Scheduler changes (1.6.1):**
- **Overlap with 1.5.0's reminder.** 1.5.0's "prayer in N minutes" reminder already exists
  (`ACTION_PRAYER_REMINDER`, codes 30,010 to 33,669, channel `prayer_reminders`). Drop duplicates against it,
  the same way the Fajr and suhoor clash is handled (`PrayerAlarmScheduler.kt` lines 96 to 101). Consider
  reusing the same quiet channel.
- **Not an alarm clock.** Pass `asAlarmClock = false`. `schedule()` defaults to true, and that would show
  the reminder as the phone's next alarm and end Do Not Disturb early.
- **Where to lay them.** Before the `alertFor.isEmpty()` return, inside `runCatching`.
- **IDs and channels.**
  - Notification id bases must avoid 1000, 2000, 3000 and 4000.
  - New channels go through `ensureChannels`, under `channelLock`.
- **Request codes.**
  - `RequestCodeTest` must cover all six code families: prayer, suhoor 10,000, widget 20,000, the
    reminder 30,000, jama'ah 40,000 and fasting 50,000.
  - Key the fasting code on **the day it fires**, so `cancelAll`'s −1 to +2 loop always reaches it.
- **The receiver re-checks** the setting, the sect and the calendar when the reminder fires.

**Jama'ah guard and travel:**
- Fajr's window ends at sunrise.
- Jumu'ah must fall between Dhuhr and Asr.
- Typed times are hidden, not deleted, when the user is more than about 25 km from where they were set
  (the `PlaceLabelPolicy` pattern).

**Fasting rules (moon-sighting buffer):**
- No fast reminder on 1 to 2 Shawwal, or on 9 to 14 Dhul Hijjah except Arafah.
- The Arafah reminder says "on this calendar, follow your mosque".
- The Shawwal reminder moves to 3 Shawwal.
- No reminders on the last two days of Sha'ban.
- The Ashura reminder notes that many also fast the 9th.
- Post it at Isha, not at Maghrib, because Maghrib is the adhan and iftar.
- Checked with `HijrahChronology`: Thu 20 May 2027 is Umm al-Qura 14 Dhul Hijjah. For a community a day
  behind that calendar, it is a Tashreeq day.

**The dates list:**
- Plain names only.
- For Shia users, no "New Year" wording on 1 Muharram and nothing celebratory for Ashura.
- "Laylat al-Qadr (the last ten nights)" is Sunni framing. Shia practice stresses the nights of the 19th,
  21st and 23rd of Ramadan, so wait for the named Shia source in D5.

**Correction.** "Arabic shipped in 1.5.0" means "ships with 1.5.0". It is not live yet.

**Adhkar, duas and tasbih: none in 1.6.0** (Ali asked, 23 Oct 2026; both reviewers said no):
- **A full adhkar library.** It is hundreds of religious texts, each needing exact Arabic, a source and five
  translations. It would not rank for "dua", and it reverses the 17 Oct brief.
- **A plain counter.** Every phone already has one.
- **The after-prayer tasbih.** This is the only one worth considering later, after Ramadan. It needs a
  different sequence for each sect:
  - Sunni: 33/33/33 plus the tahlil (Muslim 597), or 33/33/34.
  - Shia (Tasbih al-Zahra): Allahu Akbar 34, then Alhamdulillah 33, then SubhanAllah 33.
- **If Ali wants it later, it must be a Settings-only screen**, off by default. It must not:
  - prompt after a tick
  - store counts, a streak or a history
  - send a notification
  - add a usage-count event
  - claim a reward or quote a hadith

**UX:** `UX.md` in this folder is the screen-by-screen guidance. It too was written before this section,
so read its "Mosque times" as "Jama'ah times", and its reminders as 1.6.1.

## 0.1 Design direction and five additions (Ali, 23 Oct 2026)

**Design direction.** Ali shared screenshots of The Pillars app (thepillarsapp.com and its Play listing,
`com.pillars.pillars`) as a reference for *thinking*, not for colours. SajdaTime keeps its green theme and
`docs/DESIGN_SYSTEM.md`. What to take from Pillars:
- Calm screens with lots of space, large type and one accent colour.
- Every part of the screen has a job.
- Icons carry functions that used to need a menu: a bell on each prayer row, and the pause on the tracker.
- Past, now and next can be read at a glance.
- Each feature has its own tab: Prayer, Qibla, Tracker, Settings.
- The tick animates as a prayer is marked.

What not to copy:
- Unlabelled icons. Every icon has a content description, and a visible word wherever its meaning is not
  obvious.
- Faded rows that fail WCAG AA.
- Dead space, such as the gap on their tracker between Isha and the week row.
- "Featured in" logos, and any gamification beyond the quiet streak.

Every animation stops when the phone's "remove animations" setting is on. Every screen is checked RTL and
at font scale 1.5.

**Not done until Ali says go:** moving the tracker to its own tab, and the home layout. Ali is still sending
references.

**The five additions, all approved by Ali for the 1.6 plan:**

1. **Week rings that fill partly.** A day with 3 of 5 prayers shows three fifths of a ring, so a half-done
   day reads differently from an empty one. The data already exists (`DayRecord` mask). Each ring also needs
   a text equivalent ("3 of 5") for screen readers, not colour and arc alone.
2. **Paused days look different from prayed days.** Their second most helpful review complains that paused
   days look the same as prayed days in the trends. Ours mark a paused day with a pause icon or pattern, not
   colour alone.
3. **A view longer than one week.** The same reviewer asks for months and the year. We keep `HISTORY_DAYS = 30`,
   so a month view is nearly free. **A year view needs longer storage. Decide that separately**, because it
   grows the export file and what the privacy page describes.
4. **A one-tap pause icon on the tracker screen, with a label,** in place of the pause living further away.
   Same `pause`/`resume` logic, no rule change.
5. **Missed-fast (qada) tracker. New scope.**
   - Record a missed fast on a day, optionally with a reason: travel, illness, period, pregnancy or other.
   - Show "N days to make up", and tick a made-up fast against the count.
   - Works for Sunni and Shia users alike, because making up missed Ramadan fasts is common to both. Unlike
     the 1.6.1 Sunnah fast reminders, it is not Sunni only.
   - **Privacy is the hard part.** Period and pregnancy are health data:
     - Kept on the phone only, the way the prayer tracker is.
     - Included in the export file, and removed by the existing delete.
     - Nothing sent to anyone, and no usage-count event.
     - The reason is optional, and the count works without one.
     - `docs/privacy.html` needs a new section saying all this, in the same commit, still undated.
   - **Not linked to the prayer-tracker pause.** Inferring a missed fast from a paused day would be the app
     guessing about a woman's body. The user records it herself.
   - **Release:** most useful from Ramadan 1448 (8 Feb 2027), when the missed fasts happen. So 1.6.0 if the
     build and review time allow, and 1.6.1 otherwise. The prayer tracker still comes first.
   - Pillars lists this as "coming soon" (Play listing, 23 Oct 2026).

**Design decisions (Ali, 23 Oct 2026, recorded in `DESIGN_SYSTEM.md` §9):**
- **Four fixed skies (dawn, day, evening, night), with an animated change.** The artwork shows the time of
  day. The prayer name and the time left keep a prominent readable place, contrast-tested on every sky.
- **One-tap bell on each prayer row** turns that prayer's alert on or off. No long press. Sounds stay in
  Settings.
- **Labelled chips with a gap** for the tracker's actions, never icon buttons side by side.
- **Qibla restyle.**
  - The direction sentence comes first, and the place name is optional and small.
  - Distance and bearing sit behind a labelled details button, in km or miles by the phone's region.
  - The key stays.
- **Timetable (PDF) placement: open.** Today it is "Save as PDF" on the Today line of Times, which opens a
  Today, 7 days or month sheet, plus the Ramadan timetable sheet. The new layout needs a place for it.
  Ali to choose from screenshots. His requirement: the button must be **easy on the eyes and easy to
  understand**.
  - Claude's recommendation: a Timetable button under the list that opens a sheet showing the month on
    screen, with Save as PDF inside. During Ramadan the same sheet opens on the Ramadan timetable.

**Ali's answers to Fable's six questions (23 Oct 2026, FABLE-REVIEW.md section E):**
1. **Dark mode skies.** Ali has no firm view. He suggests the same sky images in dark mode, since dark mode
   is for users who find it easier on the eyes. To be shown to him as screenshots before it is decided.
2. **The sky does not replace the green card.** "We have to keep it consistent": the green card (dark) and
   the light card stay as the place for the current prayer and the time left. The sky must work with the
   card, and still fit 823dp.
3. **Skies and the Qibla restyle are in 1.6, if possible.** Fable's "after Eid" split is not accepted as the
   default. Build order still puts the signed-off features first.
4. **Undo when a bell is turned off: yes.** A small message with an Undo option.
5. **The Tracker tab is always visible: yes.** When tracking is off, the tab shows the switch to turn it on.
6. **Missed fasts.** Research how Pillars' fasting tracker works before deciding between count only and
   reasons, and between before and after Ramadan.

**Widgets (Ali, 23 Oct 2026).** Similar to Pillars in UX: a wide strip of the five prayers with the current
one marked, and a sky-style current-prayer widget with the time left. Complemented with our own features
(jama'ah times, Ramadan suhoor and iftar, Hijri date) and better UX. Nothing in a widget animates
(RemoteViews), and widgets must stay readable at large text and in RTL.

**Ali, 23 Oct 2026: UI and UX decide this app's scope.** Ease of use, being easy to understand, and a modern
look and feel are extremely important. Every 1.6 choice is judged on that first.

**Ali, 23 Oct 2026: wording, journeys and languages.**
- Wording must connect with users.
- The plan needs detailed user experience, user journeys and UI.
- Five languages (Pillars has two) should make SajdaTime stand out.
- When the plan is complete, it gets a second independent review for gaps before development.

**Ali, 23 Oct 2026: plan every detail meticulously before any development starts.** Development waits for
a complete plan and his go.

**Animation (Ali, 23 Oct 2026: "some animation like Pillars, where required").**

*Tool.* Use Compose's own animation APIs: `animate*AsState`, `AnimatedContent`, `AnimatedVisibility`,
`Animatable`, and path drawing on a `Canvas`. The app already uses them (Settings folds, the Qibla dial,
the tab pill).
- **Framer is rejected.** Framer and Framer Motion are web (React) tools and cannot run in a Compose app.
- **Lottie is rejected for now.** It adds a dependency and a designer's file format for effects Compose can
  draw in a few lines.

*Where:*
1. **The tracker tick.** The ring fills and the tick draws in, about 300 ms, with a light haptic. Unticking
   reverses it.
2. **Week rings.** The arc grows to its share when the screen opens or a tick changes it.
3. **The day path on the home screen.** It draws in once when the screen opens. After that the "now" dot
   moves only when the time updates. **No endless animation**, to save battery.
4. **The current prayer.** When a prayer time arrives, the highlight moves to the next row.
5. **Changing day.** Content slides left or right with the arrow tapped, mirrored in RTL.
6. **Time of day.** The header shifts its tone between morning, day, evening and night, using
   `DESIGN_SYSTEM.md` colours.
7. **Qibla.** A gentle confirmation, with a haptic, when the phone is lined up.
8. **The bell on each row.** A short switch between on and off.

*Rules:*
- Short: 150 to 400 ms.
- Nothing animates in the widget or on the watch.
- With the phone's "remove animations" setting on, every animation jumps to its end state. Check this on
  the emulator, do not assume it.
- Nothing flashes.

**Later, not 1.6:**
- A moon-age number beside the Hijri date.
- More widget sizes (a reviewer asked for a one-row widget).
- A short explanation of each calculation method, if Settings does not already give one.

## 1. What is in 1.6.0, and what is not

**In 1.6.0, and nothing else new:**

1. **My prayers**: the private prayer tracker (Ali's Release B design of 16 Oct 2026).
2. **Mosque times**: jama'ah (iqamah) and Jumu'ah times the user types in, with an optional reminder.
3. **Islamic dates and fasting days**: a list of the year's key dates, plus optional Sunnah fast reminders.

**Not in 1.6.0** (each one is a separate later decision):

| Item | Why not now |
|---|---|
| A choice of adhan (a Fajr adhan, one per prayer, volume control) | The 1.7.0 candidate, ranked fourth |
| Mosque times on the widgets and the watch | Needs widget layout work, and a watch release with a new sync key. Phone screens first |
| A "Prayed" button on the prayer notification | Ali, 16 Oct: tracker phase B "not planned, keep it simple" (decision D1) |
| A count of missed or qada prayers, or a whole-year view with empty days | Ali, 16 Oct: no missed-prayer counter (D2) |
| Silencing the phone during jama'ah | Breaks the manifest's promise that the app "never changes the phone's Do Not Disturb state" (D3) |
| Duas, adhkar, tasbih, Quran | Outside the app's one job, and the research review advised against them |
| The Chrome and Edge extension | A separate product on its own track, not an Android release |

## 2. Timing: the one thing that decides everything

These facts come from `docs/HANDOVER.md` and the Release B decisions:

- Ramadan 1448 starts on Mon 8 Feb 2027.
- There is no release from 18 Jan to 9 Mar 2027 except a critical fix (Claude's rule, with Ali's trust).
- 1.5.0 has no date yet, and the latest is 25 Jan.

**Gates:**

- **G1:** 1.5.0 has been live for 14 days with a clean health check. A new release should never land on top of an unproven one.
- **G2:** the 1.6.0 pull request is ready by **3 Jan 2027** and Ali submits it by **6 Jan**. With Play reviews taking one to two days, it is live around 8 to 10 Jan, which leaves a week before the freeze.
- **Consequence:** 1.6.0 can only land before Ramadan if **1.5.0 is live by about 18 Dec 2026**. In practice that means Ali submits 1.5.0 in November. If 1.5.0 goes later, 1.6.0 ships after Eid al-Fitr (from about 10 Mar 2027). That is safe, but it misses the month when people resolve to pray more.
- **Building does not wait for 1.5.0.** Claude builds on branch `claude/1-6-0` from `claude/quiet-glass` now, and merges `main` in after 1.5.0 is merged. Nothing reaches `main` before 1.5.0 is live.

**Cut line.** The features are built in the order below, each as its own droppable set of commits. If G2 is at risk, the last one drops to 1.6.1 after Ramadan. The order runs from lowest risk to alarms to highest:

1. Islamic dates list (no scheduler)
2. Tracker (no scheduler)
3. Mosque times display (no scheduler)
4. Reminders for jama'ah and fasting days (touch the alarm scheduler, the only severe risk)

## 3. Feature 1: My prayers (the tracker)

**Follow `PLAN.md` section 7 (B0 to B8) on `claude/quiet-glass`, as amended by the Release B decisions in its README.** In short:

- **B1:** `TrackerLogic` in `:core` (day record, tick states, streak over pause ranges)
- **B2:** `TrackerRepository` on its own DataStore file `sajdatime_tracker`, plus a file codec
- **B3:** a Settings group with an on/off switch, "Show streak" (off by default), Pause and resume, and Delete
- **B4:** ticks on the Times screen and a history sheet
- **B5:** **save to a file and load from a file through the system file picker.** This is the answer to cloud backup being off, so a new phone does not wipe the record.
- **B6:** strings
- **B7:** the privacy policy, documents and the release gate

**Changes this plan makes to B:**

- **Five languages, not four.** Arabic shipped in 1.5.0, so B6 adds `values-ar` and the RTL check covers the tracker.
- **Wording:** "My prayers" for the group, "Prayed" for a tick, "Pause" and "Resume". Never "missed", "excused" or "qada" anywhere. `TrackerWordingTest` keeps it that way.
- **The save file carries a one-line warning** that it is a personal record of prayer and is for the user to keep private.
- **The history sheet shows the last 30 days.** No year grid, because a grid of blanks is a missed-prayer counter in another form.
- **Version:** phone versionCode 10 / 1.6.0. The watch is unchanged and its bundle is not uploaded.

## 4. Feature 2: Mosque times

**What the user sees**

- **Settings, PRAYER TIMES group:** a new row, "Mosque times", directly above "Match your mosque".
- **The Mosque times screen:**
  - For each of Fajr, Dhuhr, Asr, Maghrib and Isha, the user picks **Off**, **A fixed time**, or **Minutes after it begins** (0 to 60, in steps of 5).
  - A **Jumu'ah** row (Off or a fixed time) replaces Dhuhr's jama'ah on Fridays.
  - A note at the top explains the choice: use minutes after the start if your mosque moves its times with the seasons.
- **Times screen:** under each prayer with a jama'ah time set, a small second line reads "Jama'ah 13:30" (on Fridays, "Jumu'ah 13:15").
- **Reminder (optional, off by default):** "Remind me before jama'ah", 5 to 30 minutes. A notification only, never the alarm.

**The rule that protects users from a wrong time.** A jama'ah time is shown only when it falls **at or after the prayer begins and before the next prayer begins**. The case to watch for is a fixed time typed in summer that is now earlier than the prayer's start. That time is not shown, and the screen shows a quiet note instead: "Your Asr jama'ah time is before Asr begins. Check your mosque's times." Showing a jama'ah time earlier than the adhan would tell someone to pray before the prayer is due.

**Code**

- **`:core/JamaahTimes.kt`:**
  - `sealed interface JamaahRule { Off; Fixed(LocalTime); After(minutes) }`
  - `JamaahTimes(rules: Map<PrayerSlot, JamaahRule>, jumuah: LocalTime?)`
  - A pure function, `JamaahPlan.on(date, day: PrayerDay): Map<PrayerSlot, JamaahResult>`, where each result is `Shown(time)`, `BeforeStart` or `AfterNext`.
  - A codec, `JamaahCodec`, modelled on `AdjustmentCodec`: one string key, versioned, and anything it cannot read drops to Off.
- **`AppSettings`:**
  - `jamaah: JamaahTimes = empty`
  - `jamaahReminderMinutes: Int = 0`, where 0 means off
  - Stored in the existing `sajdatime` store.
- **Adjustments.** "Match your mosque" shifts the start times. Jama'ah "minutes after" counts from the **adjusted** start, because that is the time the user sees.
- **Scheduler:**
  - A new action, `ACTION_JAMAAH_REMINDER`.
  - A new request code band, `JAMAAH_REMINDER_REQUEST_BASE = 40_000 + prayerRequestCode`. The highest prayer code is 3665, so the band never meets the 10_000 or 30_000 bands. `RequestCodeTest` is extended to prove all four bands stay apart.
  - Its own channel, `jamaah_v1`, so the user can mute it alone.
  - A cancel loop added to `cancelAll`.
- **Not synced to the watch in 1.6.0.** `WatchSyncContract` is unchanged.

**Tests**

- `JamaahPlanTest`:
  - each rule
  - Friday
  - BeforeStart and AfterNext
  - DST days in the UK and US
  - minutes after an adjusted start
  - high latitude, where Isha is missing or late
- `JamaahCodecTest`: round trip, a corrupt string, and an unknown version
- `RequestCodeTest`: the extended bands
- A device test that the reminder appears in `dumpsys alarm` only when it is on
- Times-screen fit: everything shows with no scrolling on the 823dp S23 Ultra frame at default text with all five jama'ah lines. **This must be measured.** If it does not fit, the jama'ah sits on the same line as the start time.

## 5. Feature 3: Islamic dates and fasting days

**What the user sees**

- **The Islamic dates sheet:** tapping the Hijri date in the Home header opens a sheet listing the coming year's dates, each with its Gregorian date. The list follows the user's Hijri date shift.
  - Under the list sits one line: "Dates follow the Umm al-Qura calendar with your date shift. Your local moon sighting may differ by a day."
- **Settings, a new "Fasting days" group, off by default:**
  - a "Remind me" switch, then sub-switches for **Mondays and Thursdays**, **the White Days (13th to 15th)** and **special days** (Ashura, Arafah, and the six days of Shawwal)
  - one notification **at Maghrib the evening before**: "Tomorrow is a Sunnah fast (Monday). Suhoor ends at 05:12." The suhoor time comes from the same engine as the Ramadan card.

**Dates listed for everyone.** These are widely agreed, and the wording carries no ruling:

- Islamic New Year (1 Muharram)
- Ashura (10 Muharram)
- Start of Ramadan
- Laylat al-Qadr (the last ten nights)
- Eid al-Fitr
- The first ten days of Dhul Hijjah
- The Day of Arafah (9 Dhul Hijjah)
- Eid al-Adha

**Dates not listed by default, for Ali to decide (D4):**

- **Observances some Muslims hold and others reject**, such as Mawlid, Isra and Mi'raj, and the 15th of Sha'ban. Showing one is taking a side.
- **Shia-specific dates**, such as Ghadir and Arbaeen. Before any of these go in, Claude compiles the list from a named Shia source.

**Fasting reminders are offered to Sunni users only in 1.6.0.**

- **Why:** Monday and Thursday fasts and the White Days are Sunni Sunnah practice. For Shia users, fasting on Ashura is discouraged, so a Shia user must never get an Ashura fast reminder.
- **What Shia users get:** the dates list, without the fasting group. A Shia set of recommended fasts is D5.
- **The precedent:** the madhab step is already shown to Sunni users only.

**Days that must never be offered as a fast:**

- Eid al-Fitr (1 Shawwal) and Eid al-Adha (10 Dhul Hijjah)
- **The Tashreeq days, 11 to 13 Dhul Hijjah.** **13 Dhul Hijjah is also a White Day, so the White Days rule must skip it.**
- Every day of Ramadan, which is already fasted
- Fridays on their own: a Friday that is the 13th, 14th or 15th still gets its reminder, but the wording says "White Day", not "Friday"

**Six days of Shawwal.** These get one reminder, on 2 Shawwal ("The six days of Shawwal can begin"), not six reminders.

**Code**

- **`:core/IslamicDates.kt`:**
  - `data class IslamicDate(id, hijriMonth, hijriDay, kind = OCCASION | FAST, forSunni, forShia)`
  - `fun upcoming(from: LocalDate, offset: Int, sect: Sect, count: Int): List<Pair<LocalDate, IslamicDate>>`
  - `fun fastOn(date, offset, sect, prefs): FastReason?` contains every exclusion rule above in one place.
  - Built on `HijriCalendar.of`, with the same "out of the calendar's range gives null, not a throw" rule.
- **`AppSettings`:** `fastReminders: Set<FastKind> = empty`
- **Scheduler:**
  - `ACTION_FAST_REMINDER`
  - `FAST_REMINDER_REQUEST_BASE = 50_000 + dayOfYear`
  - channel `fasting_v1`
  - the existing 2-day horizon, which is enough for "the evening before"
  - a cancel loop
- **Strings:** names of the dates in all five languages. These are religious names, so they get the same care as prayer names. Arabic uses the standard names, and Urdu, Turkish and Indonesian use the common local forms (Muharram, Aşure, Asyura).

**Tests**

- `IslamicDatesGoldenTest`: every listed date for 1448 and 1449, checked against an independent Umm al-Qura source, the way `RamadanGoldenTest` was.
- `FastRulesTest`:
  - the Tashreeq and 13 Dhul Hijjah exclusion
  - Eid days
  - Ramadan suppression
  - the date shift from -2 to +2
  - Shia gets no fast reminder, and none at all for Ashura
  - the Friday wording
  - the out-of-range year
- `RequestCodeTest`: the fifth band
- A device test: the reminder for a Monday is scheduled for Sunday's Maghrib, and not during Ramadan

## 6. Rules every step keeps (from `CLAUDE.md`, unchanged)

- **No change to the app's footprint:**
  - no new permission
  - no new dependency
  - no network
  - **no new usage-count event or parameter.** Ticks, pauses, jama'ah times and fasting choices are religious-practice data and never leave the phone.
- **Design and checks:**
  - Colours come from Material 3 roles only, through `ColorContrastTest`, with Quiet Glass components.
  - Every new screen gets the `./gradlew installRtl` mirroring check.
  - Screens are checked at font scale 1.5.
- **Strings:**
  - every string in English, Arabic, Urdu, Turkish and Indonesian
  - nothing user-facing written in Kotlin
  - translations done and checked by Claude, as Ali decided
- **Privacy policy (`docs/privacy.html`):** add one section saying the prayer record, mosque times and fasting choices stay on the phone, are never sent anywhere, and are deleted when the app is uninstalled or deleted in Settings. **The Data safety form does not change**, because data that stays on the device is not "collected" in Play's terms. Ali reads that section before submitting.
- **Store listing:** "prayer tracker", "salah tracker", "iqamah", "Jumu'ah" and "fasting days" are added to the description only in the 1.6.0 listing, and pasted on its release day. The short description then becomes "Offline prayer times, adhan, Qibla and a private salah tracker, with Wear OS." (77 of 80 characters), with Ali's yes and the Console's paste, save and reload check. The store screenshots are retaken to show all three features.
- **Release gate:**
  - `release-1.6.0.sh`, copied from 1.5.0 with the versions and new expected test counts
  - the full device suite with the new tests
  - `dumpsys alarm` compared before and after (the R1 check)
  - bundle size within 300 KB of 1.5.0

## 7. Build order, with what Claude can do alone and what needs Ali

| Step | What | Who |
|---|---|---|
| 0 | Ali answers D1 to D5 below. A default stands if he doesn't | Ali (optional) |
| 1 | Branch `claude/1-6-0`, baselines (B0) | Claude |
| 2 | Islamic dates: core, golden test, sheet, strings | Claude |
| 3 | Tracker B1 to B6 with this plan's changes | Claude |
| 4 | Mosque times: core, codec, Settings screen, Times screen lines, fit measured | Claude |
| 5 | Both reminders: scheduler bands, channels, receivers, device tests | Claude |
| 6 | Privacy section, docs (HANDOVER, PROJECT_BRIEF, DESIGN_SYSTEM), listing text, screenshots | Claude |
| 7 | Independent review of the whole diff, then the gate, device suite and both emulators | Claude |
| 8 | Merge `main` after 1.5.0 is live, re-run the gate, open the PR (by 3 Jan) | Claude |
| 9 | Merge, then the Play Console one screen at a time, then Send for review (by 6 Jan) | Ali, with Claude walking him through it |

## 8. Decisions for Ali (each has a default, so silence is fine)

| # | Question | Default if he says nothing |
|---|---|---|
| D1 | Add a "Prayed" button on the prayer notification? Pillars users ask for it | **No**, as he decided on 16 Oct |
| D2 | Count missed or qada prayers? | **No**, as he decided on 16 Oct |
| D3 | Silence the phone during jama'ah? | **No** |
| D4 | List the contested observances (Mawlid, Isra and Mi'raj, 15 Sha'ban)? | **No** |
| D5 | Shia: add Shia dates and recommended fasts, compiled from a named source? | **Dates list only** in 1.6.0 |
| D6 | Submit 1.5.0 in November, so 1.6.0 can land before Ramadan? | **His call**. Otherwise 1.6.0 ships after Eid |

## 9. Risks

| Risk | Impact | Guard |
|---|---|---|
| A new reminder breaks a prayer alarm | Severe | Separate actions and bands, `RequestCodeTest`, the `dumpsys alarm` check, reminders built last and droppable |
| A jama'ah time shown before the prayer begins | High (religious) | The BeforeStart rule and its test |
| A fast reminder on a forbidden day, or a Sunni practice shown to a Shia user | High (religious, trust) | `FastRulesTest`, Sunni only in 1.6.0 |
| Local moon sighting differs from Umm al-Qura | Medium | The date shift already exists, plus the one-line note on the sheet |
| Tracker record lost on a new phone | High for that user | Save and load file (B5), its own DataStore file, excluded from backup, as R9 in the tracker plan |
| The Times screen no longer fits with jama'ah lines | Low | Measured on the S23 Ultra frame, with the same-line fallback |
| 1.5.0 slips, so 1.6.0 misses the pre-Ramadan window | Medium (growth) | D6, and the plan ships safely after Eid |
| Five-language wording of religious names | Medium | Common local names, corrected on any user report, as `CLAUDE.md` says |
