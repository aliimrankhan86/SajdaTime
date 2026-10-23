# Next release after 1.5.0: what to build to grow — research, 23 Oct 2026

> **Superseded order, 23 Oct 2026:** the signed-off order is in `docs/plans/2026-10-release-1.6.0/SIGNOFF.md` (tracker, then Islamic dates, then jama'ah times). The "Prayed" button on the notification named below is **not** agreed (Ali, 16 Oct 2026).

Research only. Nothing here is planned or approved. For the release **after** 1.5.0.

## Answer: the top 3 (final, after the independent Fable 5 review)

| # | Feature | Why (strongest evidence) | Fits our rules? |
|---|---|---|---|
| 1 | **Private prayer tracker** ("My prayers", a "Prayed" button on the prayer notification), with a local export and import file | The most-praised extra in the ad-free apps people switch to: Pillars' reviews mention it 11 times out of 40, and its listing leads with it. Athan's listing leads with "Daily Salah Streaks". Reddit users leaving Muslim Pro miss their "Prayer log" by name. Play search for "salah" and "prayer tracker" is mostly tracker apps. Every new 2026 indie prayer app ships a log. It brings people back every day, which also brings them to the calm moments where 1.5.0 shows Google's review card. | Yes. **This is Ali's own plan** (Release B, 16 Oct): the streak stays hidden until switched on, Pause is neutral, there is no missed-prayer counter and there are no new usage counts. The evidence now backs it. |
| 2 | **My mosque's jama'ah (iqamah) and Jumu'ah times**, as a fixed time or "N minutes after the start time", shown on Home, the widget and the watch, with a reminder before | MAWAQIT has 5m+ installs and 23 of its 60 reviews are about the mosque. Pillars' listing says "Coming soon: local mosque prayer times". People keep asking for iqamah reminders and fixed times (Salaat First Aug 2026, zeedev, My Prayer Jul 2026, Al-Azan GitHub). Reddit's standard advice is to follow your mosque's times. | Yes. The user types the times in, with no feed and no server, extending "Match your mosque". It is weaker than MAWAQIT's live times kept up by the mosques themselves, and the gap after the start time copes with seasonal changes better than a fixed time does. |
| 3 | **Islamic dates and Sunnah fast reminders**: Mondays, Thursdays, the 13th to 15th, Ashura, Arafah, plus the main Islamic dates | People ask for it (Al-Azan #60 has 24 reactions; an Al-Moazin review from Aug 2026). It is cheap to build on the existing `HijriCalendar`, keeps people using the app between Ramadans, stays inside the app's "times" job, and earns the search phrase "hijri calendar". | Yes. It works offline. The Sunni and Shia date lists differ, so they need the same care as prayer names. |

**A close fourth: adhan choice.** That means a separate Fajr adhan, a different adhan for each prayer, and volume control while the adhan plays. The demand is real: Sadiq, Al-Azan #13 and #44, iPray twice in 2026, and MAWAQIT "extremely loud". Most of it is about reliability rather than growth.

**Moved out of the top 3 by the review: adhkar and duas with a tasbih.**
- **Correlation.** Every mass-install app bundles them, but that is not why those apps are big.
- **Ranking.** An app with 3 ratings will not rank for "dua" against Athan or Muslim Pro.
- **Our own records.** The brief (17 Oct) says "Quran reader, duas, dhikr: no", and `VISIBILITY-KEYWORDS.md` says we cannot honestly match those words.
- **Accuracy.** Each dua's Arabic, source and translation in five languages is religious content that has to be exactly right.
- **The Quran reader** has the same problem at a larger scale.

Both are Ali's call: keep the app to one job, or widen it.

**Do not build:**
- an app blocker or "lock your phone for salah" (it needs Accessibility, a Play policy risk);
- AI chat;
- a halal or mosque finder;
- community features;
- accounts.

**What to scope out of the first version:**
- **Ticking from the watch.** It needs a new data path between phone and watch, so phone first.
- **A whole-year view with empty days.** It works as a missed-prayer counter, which Ali ruled out.
- **Silencing the phone during jama'ah.** It is a separate decision for Ali. The app's manifest promises it "never changes the phone's Do Not Disturb state". On Android 15 and later it would run through the automatic DND rules. And if it failed to turn sound back on, people would miss calls.

**Timing.** Ramadan 1448 starts on 8 Feb 2027. If 1.5.0 goes out in November or early December, a tracker live by mid-January lands before Ramadan, which is the month people resolve to pray more. Aim for that window. Ali's plan already said "live by mid December".

## Words (what makes it read as mainstream)
- **Title.** The live "SajdaTime: Prayer Times, Qibla" already uses all 30 characters, so leave it. Get the gains from translated titles instead: "SajdaTime: Namaz Vakitleri" (Turkish) and "SajdaTime: Jadwal Sholat" (Indonesian). Never write "Sajda Time" with a space, because it clashes with the Sajda app.
- **Short description, only once the tracker ships** (`VISIBILITY-KEYWORDS.md` forbids "prayer tracker" before then): "Offline prayer times, adhan, Qibla and a private salah tracker, with Wear OS." (77 of 80 characters). Run the Console's paste, save and reload check on it.
- **Never in the title or short description:** "free", "no ads", "best", "#1" or "athan". Those facts stay in the body.
- **Names inside the app:** "My prayers", "Prayed" and "Pause" (never "missed" or "excused"); "Mosque times", with "Jama'ah (iqamah)" and "Jumu'ah (Jummah)"; "Fasting days" and "Islamic dates".
- **New body words, once they ship:** prayer tracker, salah tracker, namaz tracker, iqamah, jamaat times, Jummah, hijri calendar, Sunnah fasts.

## The bigger lever than any feature (the review's main challenge)
SajdaTime does not appear in the top Play results for "prayer times", "namaz", "azan", "qibla", "salah" or "prayer tracker" (checked 23 Oct 2026). With 3 ratings, the bottleneck is being found, not features. Two things could beat any feature:
1. **Ali's Console numbers** (store visitors, the share who install, uninstalls) in the monthly numbers doc. They tell us whether people find the page and leave, or never find it.
2. **More app languages.** The only 1-star review asked for Arabic, and 1.5.0 lists the store page in Bengali, Malay, French and Hindi while the app itself speaks five languages.

Sharing the link through mosques and community groups is what grew MAWAQIT and Dawat-e-Islami's app. It costs nothing and asks nothing of users.

## What users are telling us (the evidence)

### Play Store, read in the browser on 23 Oct 2026

These are the "most relevant" reviews, keyword-tallied: about 420 reviews across 8 apps, plus the listings of 11 apps.

| App | Rating | Installs | Ads | What its reviews talk about most |
|---|---|---|---|---|
| Muslim Pro | 3.8 (1.92m reviews) | 100m+ | ads + subscriptions | ads 28 of 60 (the top one has 2,358 helpful votes: "fake close buttons"), paywall 23, Quran 13 |
| Salaat First | 4.8 (799k) | 10m+ | none | simple and no ads 13, **widgets 12**, watch 4; offline, multiple adhans, Wear OS tile |
| Sajda (GOOD/APP) | 4.9 (466k) | 10m+ | none (paid extras) | Quran 11, simple 9; the next-prayer widget is "one of the main ways I use this app" |
| Prayer Now | 4.8 (1.49m) | 10m+ | ads + IAP | Quran, duas; praised for overriding silent mode |
| Athan (IslamicFinder) | 4.8 (333k) | 10m+ | ads + IAP | ads 12, Quran 12; its listing leads with Daily Goals, **Salah streaks**, widget and menstrual mode |
| MAWAQIT | 4.8 (174k) | 5m+ | none | **mosque 23 of 60**; asks: an iqamah timer, auto-silent after the adhan, Android Auto |
| Pillars | 4.4 (4.38k) | 500k+ | none | simple 13, **tracker 11 of 40**, widget 7; asks: a whole-year view, a "done" button on the notification |
| Sadiq | 4.8 (8.45k) | 500k+ | none | Quran 13, duas 6; asks: choose the adhan, pause during menstruation |

Pattern: the ad-free apps hold 4.4 to 4.9. The ad-funded giants win on installs and lose on stars. **Salaat First and Sajda prove a simple, ad-free app can reach 10m+.**

### Reddit (DuckDuckGo snippets; reddit.com cannot be opened from here)
- "I miss how it had everything. Prayer log, Quran, qibla and prayer reminders" (r/MuslimLounge).
- Pillars is recommended for "prayer habit tracking" and for entering a period so the tracker stays fair.
- "you check off a prayer once you've done it… it has helped me a lot".
- "I just want a simple app just alarmed prayer times" (r/islam): the opposite pull, toward simplicity.
- "Follow the prayer times of the mosque you the most often go to".
- Alternatives threads ask for times, Qibla, Quran and duas together, with no ads.

### X (DuckDuckGo snippets; x.com needs a login)
- The indie wave in 2026 (Athanify, Siraj, Noor) all pitch "private, no ads", and almost all add a prayer log or streak. Siraj copied "Match My Masjid".
- Pillars: "Ads don't belong in a prayer app!"
- WSJ, Mar 2026: an Iranian prayer app with 5m users was hacked to push messages. An app with no server cannot be used that way, which is a trust point.

### Forums, GitHub, Hacker News, Trustpilot (agent, 27 sources)
- Ranked by how often each is mentioned:
  - times matching my mosque or method (about 10)
  - widgets with a countdown (about 12)
  - alert control: Fajr adhan, volume, silent (about 9)
  - **iqamah, fixed or mosque times (about 8)**
  - watch asks (about 8)
  - **desktop or browser reminders (about 8, including a full spec for a browser extension)**
  - calendar events and Sunnah fasts (about 7)
  - **auto-silence during prayer (about 6)**
  - Ramadan alarms (about 5)
  - **private tracker and qada (about 5, plus a flood of new single-purpose apps)**
- Frustrations: ads (Muslim Pro Trustpilot 1.7), paywalled widgets, data-sale scandals (Muslim Pro 2020, Salaat First 2021), alerts that do not fire, no adhan volume control, bloat.

## What SajdaTime already wins on (keep saying it)
- No ads, no account, offline times.
- Free widgets.
- A Wear OS app and tile.
- "Match your mosque".
- Alarms tested on real phones.
- No server.

These match the top frustrations. Most of them are already the listing's message.

## Decisions only Ali can make
1. **Go ahead with the tracker (Release B) for a mid-January release?** It is his own design. The added parts are the "Prayed" button, the export file and phone-only for the first version.
2. **Qada.** Users ask for a count of missed prayers. His 16 Oct "no missed prayer counter" stands unless he changes it.
3. **Widen the app with adhkar and duas, or the Quran?** The review advises no for now.
4. **Silencing the phone during jama'ah.** It needs a new use of the Do Not Disturb permission and breaks a promise in the code.
5. **The translated store titles** for Turkish and Indonesian.

## Gaps in this research
- **Reddit and X.** We only saw search snippets. Reddit and x.com cannot be opened from here; Google showed a CAPTCHA and it was not solved.
- **Search volumes.** There are none, because no paid tool was available, so we do not know which words are thinly contested.
- **Review counts.** They come from Play's "most relevant" sample and from keyword matching, so read them as direction, not statistics.
- **Muslim Pro's Ramadan figure** ("up to 100k installs a day") is from a press release of 16 Apr 2014, so it is old.
- **Spot checks.** The independent review re-checked the ratings, install bands and listing claims for all 8 apps against Play on 23 Oct 2026, and all were correct. It did not check the Reddit and X quotes or the keyword counts.
