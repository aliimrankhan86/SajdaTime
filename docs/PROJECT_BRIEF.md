# SajdaTime: project brief (snapshot 18 Oct 2026, Play "Updated on" 17 Oct 2026)

The source of truth is `docs/HANDOVER.md` §11. This file is the short version Ali's claude.ai project reads live from GitHub (raw.githubusercontent.com/aliimrankhan86/SajdaTime/main/docs/PROJECT_BRIEF.md), so **every commit that moves the project updates it too** (CLAUDE.md commit rule). If they disagree, HANDOVER wins.

## What is live on Google Play
- **Phone 1.4.0** (versionCode 8), published 17 Oct 2026, available in 178 countries.
  - Two home-screen widgets: the next prayer with a countdown, and all of today's times.
  - The Ramadan pack. It stays out of sight until the eve of Ramadan. It includes:
    - the Ramadan card
    - suhoor and iftar labels
    - the Ramadan timetable, which can be saved as a PDF
    - an optional suhoor reminder
    - the Eid greeting
  - Corrected privacy wording: the town name label may be looked up by Google's geocoder.
- **Watch 1.2.0** (versionCode 1001), live since 5 Sept 2026. Its testing is finished, so never raise it.
- **Languages:** English, Urdu, Turkish and Indonesian. Urdu reads right to left.
- **Ratings:** 2.33 from 3 ratings and 2 reviews (17 Oct 2026). The only complaint was a 1-star review asking for Arabic.
- **Privacy policy:** https://aliimrankhan86.github.io/SajdaTime/privacy.html, dated 17 Oct 2026.

## Next release: 1.5.0
- **Branch:** `claude/quiet-glass`. It passes all checks and is not merged. The 18 Oct widget redesign and short disclaimer were merged into it on 18 Oct 2026 at Ali's word.
- **Contents:**
  - The Quiet Glass design: a floating tab bar, one header style, a new Settings screen, one Notice style for every notice, a slightly deeper green, and widgets restyled to match.
  - Arabic as the fifth language. It reads right to left and uses Western digits.
  - The watch translated into Urdu, Turkish, Indonesian and Arabic, plus a fix so the watch disclaimer no longer runs under the clock at large text.
  - Google's own in-app review card at calm moments (at most 3 times, 120 days apart).
  - Passive Rate and Share rows in Settings.
  - The usage counts question moved later in setup.
  - Clearer setup and notice wording.
  - An offline message when the city search fails.
  - A live system countdown in the next prayer notification.
  - **Reminder before prayer** (added 17 Oct 2026 from the competitor review): off by default, 5 to 30 minutes before, chosen per prayer, always a quiet notification.
  - **Built-in adhan for both schools** (17 Oct 2026): Sunni, a CC0 recording Ali listened to and approved; Shia, a studio recording by Saeed Hatamzadeh-Varmazyar (CC BY-SA 4.0), checked by transcription to be a complete Shia adhan. Both from Wikimedia Commons, opt in, never the default, credited in About (+3.5 MB). Any phone sound still works.
  - **Clearer alerts:** each prayer is Off, Notification or Alarm (the adhan or a chosen sound). "Play on silent" (off by default) decides whether an alarm plays when the phone is on silent or Do Not Disturb.
  - Screen reader fixes.
  - **Shorter disclaimer after setup** (Ali's request, 18 Oct 2026): about 70 words, still all four required points, with "Read in full"; the complete disclaimer stays in Settings, About. It was already never shown after updates.
  - **Widget redesign, "hero countdown"** (18 Oct 2026, Ali chose it from three drawn designs; Ali called 1.4.0's widgets bland): the countdown is now the biggest thing on the widget, each prayer has a small picture of where the sun is, five bars show how the day is going, and the large Today widget has the date, the Hijri date and the app's mark. Every widget names the town the times are for in a small location chip (pin, town, update arrow) that opens the app to refresh the location (Ali's request), and the privacy page now says so. Glossy "Apple premium" finish (Ali's request): a soft gradient with a sheen, still opaque so the text stays readable on any wallpaper. Both widgets switch layout by size and fill the space they are given. Checked by drawing them with Android's own code in light, dark, five languages and large text; not yet seen on a real launcher.
  - **Premium look on the Times screen** (Ali's requests, 18 Oct 2026, approved from screenshots): the next prayer card is glossy in light and dark (dark now uses the widgets' green); every button that was a thin outline is now a solid button so it is hard to miss, the "Different from your mosque?" button with a mosque icon; the setup location screen is short (one line, a big Allow button, one line under the city box saying a typed name is looked up over the internet, and the full detail behind "How your location is used"). Checked by drawing the screens in light, dark, Urdu and large text; not yet seen on a phone.
  - **Cleaner notices and pop‑ups** (Ali's emulator feedback, 18 Oct 2026): the close X sits alone in the corner, "Allow" is a proper button with "Details" beside it, every pop‑up has an X at the top right. The notices follow one spacing scale (since extended to every phone screen, below). These rules are checked automatically on every build.
  - **Every action a solid button** (Ali's choice when asked, 18 Oct 2026 evening): the remaining text links ("Full timetable", "Save as PDF", "Details", "See exactly what is sent" and others) became solid buttons with white bold text; only "Close" and "Got it" stay as links; warnings stay amber; the minute steppers are solid green. The design system now says exactly when to ask Ali instead of guessing, specifies every button, and keeps a list of decisions waiting for Ali. Ali's answers (18 Oct): "Skip and use Makkah" moved to the foot of the setup screen under a line saying times will be for Makkah; "Reset all" full width on its own row; notices kept; spacing to be fixed screen by screen next. On the usage question Ali chose bold underlined links for "See exactly what is sent" and "Read the privacy policy" (the one exception to solid buttons), and approved the setup and Match your mosque screens. All merged into `claude/quiet-glass` on 18 Oct 2026; Ali continues on his own computer and tests it on his emulator next.
  - **From Ali's emulator test, 18 Oct 2026 late night:** "Save as PDF" is a smaller button with even gaps around it (every button that sits beside text now uses the same smaller size, still easy to tap); the type-a-place box now reads "Or type where you are", "Town, city or village", "For example: Karachi, London, Makkah, New York", so village and town users are included; the Ramadan card's button now says "Get the full Ramadan timetable" and fills its row; and Settings has two new rows under Prayer alerts, **Notification sound** and **Alarm sound**, each letting the user pick the adhan or any sound on their phone (the phone sound option already existed but was hidden at the bottom of a pop‑up). Notifications follow the phone: ring, vibrate, silent and Do Not Disturb behave as the phone is set. Approved from screenshots and merged into `claude/quiet-glass` on 18 Oct 2026. Checked by drawing the screens in light, dark, Urdu and large text. The adhan as a notification sound was then tested on Ali's emulator: it plays the whole adhan, and stops when the notification is swiped away or the notification panel is opened (Android does the second). Ali chose to tell users this in one line in the Notification sound pop-up and change nothing else for now. Not yet tried on a real phone or on Android 7. **This round is approved and parked, merged into `claude/quiet-glass`, ready for the 1.5.0 release.**
  - **Turkey times match Diyanet** (decided 18 Oct 2026; Ali delegated it, with an independent review): the Turkey method now adds Diyanet's precaution minutes, so iftar is no longer 7 to 8 minutes before the ezan. Sunrise 7 minutes earlier; Dhuhr 5, Asr 4 and Maghrib 7 minutes later; Fajr and Isha unchanged. Every change is in the safe direction. The watch gets the same change, so ship both.
  - **Even spacing on every phone screen** (18 Oct 2026, waiting for Ali to see the screenshots before it is merged): the gaps between text and buttons follow the design system's spacing scale, and the build fails if a hand-picked gap is added. Not covered: the watch app.
  - **Security hardening** (17 Oct 2026): every library checksum-verified at build time, the two known library vulnerabilities fixed (0 known in the 204 shipped libraries), task hijacking blocked, network trust locked to the system's certificates. Firebase key restricted to the app and GitHub protections on (18 Oct). Passkeys on GitHub and Google. Signing values moved to ~/.gradle and proven (18 Oct); a spare copy is kept in ~/.gradle until 1.5.0 is live, then Ali deletes it (he gets a reminder).
  - The habit tracker, only if it is built by then (see below).
- **Store screenshots** are retaken once, when 1.5.0 ships.
- **Timeline:**
  - Sun 20 Dec 2026, 18:00 UK time: a phone reminder to leave the Mac on.
  - Mon 21 Dec 2026, 10:00: the preparation job runs on the Mac. It stops before any merge or submit.
  - Around 4 Jan 2027: Ali merges and submits.
  - Mid January: live.
  - Latest acceptable date: 25 Jan 2027, two weeks before Ramadan.
- **Ramadan 1448** begins on Monday 8 Feb 2027.

## Decisions still open (Ali's)
- **January store listing.** Add the Ramadan lines (already written in `docs/store/LISTING.md`) in January.

## Ideas from the competitor review (17 Oct 2026), not planned
- **Saved places for travel** is a real gap. It is medium effort and lower priority, because the city search already covers travel.
- **Bundled adhan voices:** one per school in 1.5.0 (Ali's decision, 17 Oct 2026). No better free Sunni recording exists (checked Commons, Freesound, Internet Archive). Later: a Fajr adhan, ideally recorded by a mosque with written permission.
- **A record of missed fasts: Claude advised no.** It is too close to the "no missed prayer counter" decision. Ali has not ruled on it.
- **Quran reader, duas, dhikr: no.** They are outside the app's one job.
- **Ads, paywalls and a double adhan after the clock change** are where competitors fail. SajdaTime has no ads and nothing to buy, and its alarms replace rather than duplicate.

## Habit tracker (not started)
Ali decided on 16 Oct 2026:
- The streak is hidden until the user switches it on.
- A neutral Pause freezes the streak instead of counting excused days.
- There is no missed-prayer counter.
- It adds no new usage counts.

## Automatic jobs on Ali's Mac (they need the Mac on)
- **Daily, around 12:50:** drafts replies to new Play reviews into `build/review-replies/DRAFTS.md` and notifies Ali, who posts them himself. It also refreshes this brief when a new version goes live.
- **Monthly, days 2 to 9:** Play numbers go into the "SajdaTime monthly numbers" Claude Doc.
- **Daily cloud health check at 12:00 UTC:** a silent alarm that speaks only if something changes on the store page.
- **One reply waiting:** to the Arabic reviewer who asked for Arabic. Post it once 1.5.0 is live. It must not ask for stars.

## Known gaps (documented, accepted)
- On a small round watch (192dp) at large text, the disclaimer's top line touches the clock at the very end of the scroll.
- The Google review card can only be seen on a Play-installed copy, so it has not been seen yet.
- Not tested: Android 7 to 11 and a real phone. Ali's choice: emulator testing is enough.

## Contact
The public email for the app is aikstudies@gmail.com.
