# Morning review for Ali (written overnight, 3 Nov 2026)

Plain words. "Verified" means I saw it on the emulator or in a test. "Not verified" means I did not.

## State
- 1.6.0 is still IN REVIEW, not live (last check: Play page still said "Updated on 23 Oct 2026").
- The next release is being built on the branch `claude/1-7-0`. Nothing is merged. versionCode is still 10 on it.
  You press every merge, submit and publish button.

## What is built (all on `claude/1-7-0`, pushed)
1. **Time left in the current prayer.** The Now row says "Until Fajr · 2h 37m".
   It counts only to the next time already on the list. It never says when a prayer "ends".
   Verified: emulator at 02:34 showed Isha "Until Fajr · 2h 37m" (Fajr is 05:11, correct), at 1.0 and 2.0 text, light and dark. Unit tests green (hand-worked times).
2. **Optional Duha row** (Settings, Prayer times, switch "Duha", OFF by default, Sunni only).
   Shows "Duha 07:27, Until 11:37" (sunrise 07:07 + 20, Dhuhr 11:52 - 15). Verified on emulator, light and dark.
3. **Midnight row at 2x text** now reads name, time, "Last third from..." (it used to put the time under the caption). Verified at 2.0 text.
4. **"Hear it" button** in the Notification sound and Alarm sound dialogs, with a line saying what the phone will do.
   Verified: button shows, pressing it flips to "Stop" and sound starts (phone tone). Not verified: the notification dialog's audio stream, the "volume turned down" and "phone on silent" lines, a real phone.
5. Translations for all new words in Arabic, Bengali, Indonesian, Turkish, Urdu (by me, ledger stamped).

## Decisions that are YOURS (I built the recommended option so you can see it; say if you want another)
- **A: after Isha**, count to Islamic midnight while the night row shows, then to Fajr (built). Alternative: always Fajr. Religious content: your call.
- **B: Duha minutes** 20 after sunrise, 15 before Dhuhr (built). Alternative 15 and 10. Also: the label "Duha" (Chasht named in the Settings line), and **Sunni only** (Shia users never see it, because classical Shia scholars say it is not legislated; change if you disagree). These numbers came from web pages I read, not a fiqh book. Please do not ship them without your own check.
- **C:** just look at the picture. No volume control in this release.
- **F:** fixed for Sunrise, midnight and Duha rows only (recommended). Alternative: every row.

## Designed by Fable but NOT built (needs your pick from the pictures)
Files are in `private/plans/2026-11-next-release/` (also copy on Drive when synced):
- `DESIGN-D-E.md` + `mockups/D`, `mockups/E`: banner-free Set location and Allow exact alarms rows (option A quiet rows recommended; B; C), and the one-row strip widget (E2 recommended: redesign the existing strip widget, works at one cell tall).
- `DESIGN-G-header-graphic.md` + `mockups/G`: bigger coloured sky arc. Recommended: a full width 52dp band under the date (costs 52dp of height); alternative: a 1.4x bigger corner mark (+8dp).
  Open `mockups/G/contact-sheet.png` first.
- I did not build these because they are visual choices that are yours, and widgets are Android-alarm-adjacent code I was told to leave for you.

## Not done / not verified
- No real phone, no TalkBack, Urdu RTL, 320dp width.
- Full release gate (`clean test lint bundleRelease`) result is recorded in PROGRESS.md.
- Adhan voices: still only weak candidates (research doc). Nothing added.
- DeepSeek was not used for any real job (connection tested only). The work was mostly hard work or translations, which are not for it.
- Drive: updated copies of these planning files may lag. Screenshots (in `private/screenshots/1-7-0/`) are NOT on Drive.

## What I need from you
1. Pick A or A2, Duha minutes (and Sunni only), F or F2.
2. Look at the D, E and G pictures and pick.
3. Tell me if the branch name `claude/1-7-0` and "next is versionCode 11" are right.
