# Pillars app: design reference study (23 Oct 2026)

> **READING ORDER (23 Oct 2026, the same line in every 1.6 plan file). Start with `BUILD-INSTRUCTIONS.md`. Where files disagree, the earlier one in this list wins: `BUILD-INSTRUCTIONS.md`, then `DETAILED-PLAN.md` §9, §8, then §1 to §7, then `PLAN.md` §0.1, then `SIGNOFF.md`, then `PLAN.md` §0 and §1 to §9, then `UX.md`, then `FABLE-REVIEW.md`. `PILLARS-STUDY.md`, `GAP-REVIEW-1.md` and `GAP-REVIEW-2.md` are records, not instructions.**

Ali shared The Pillars app (thepillarsapp.com, Play `com.pillars.pillars`) as a reference for
**layout and interaction thinking only**. SajdaTime keeps its own green theme and `docs/DESIGN_SYSTEM.md`.
Screenshots are not kept in the repo because they are another app's material. This file is a description
in our own words, drawn from:
- a Claude in Chrome study of the website and both store listings
- Claude Code's own read of the Play listing on 23 Oct 2026: 4.4 stars, 4.38k reviews, 500k+ downloads,
  data safety says "device or other IDs may be shared"
- three screenshots Ali sent (Prayer, Tracker and Qibla tabs)

## What their screens do

**Prayer tab.**
- The top quarter is a sky scene for the time of day: weather art, sun and moon, and a sun arc with a dot
  per prayer. The part of the day that has passed is drawn solid.
- The current period name is large, with a "time left until next" pill under it.
- A "TODAY | London" chip, then the date row with chevrons and the Hijri date in the accent colour.
- The prayer list is a reading list, so it uses hairline dividers:
  - name left, time right, alert icon at the edge
  - past rows fade, and the current row is bold with an outline
  - the alert icon's shape shows the alert type (bell, alarm clock, megaphone), and tapping it opens that
    prayer's alert sheet
- A "Show more" pill reveals Duha, the middle of the night and the last third of the night.
- Four tabs: Prayer, Qibla, Tracker, Settings.

**Tracker tab.**
- One card per prayer, because it is an action list. Tapping the ring ticks the prayer with an animation.
- A 7-day row of rings that fill in fifths.
- A flame streak chip.
- Two unlabelled round icons, side by side, top right: fasting and period mode.
- Period mode is per prayer: the excused prayers dim, and the day still completes.
- A large empty gap between the cards and the week row.

**Missed-fast sheet.**
- Reason chips, each an icon plus a word: travel, health, period, pregnancy, other.
- A status sentence that states the outcome ("you have 1 day to make up").
- A made-up list, and a count badge on the tracker icon.

**Qibla tab.**
- The city is large in a pill, and an "i" button opens the details.
- A big soft dial with a thick rim, a teardrop needle and the Kaaba on the rim.
- One large status line: "Turn to your right", then "You're facing Makkah", with the dial glowing on
  alignment. No numbers on screen.

**Sheets.**
- Secondary tasks open from the bottom with an X: alerts, calculation method, smart alarm, fasting.
- The selected option is shown by an outline, not a fill.

**Animation.**
- Each animation plays once on a state change: tick and ring fill, list expansion, dial rotation, sheet
  slide.
- The weather in the sky loops.

## Their weak spots (from the study and their reviews)

- **Unlabelled icons** in the tracker header and the map toolbar.
- **Excused or paused prayers look like done or passed ones.** Their second most helpful Play review
  complains of exactly this.
- **Pale grey past rows** that are likely to fail WCAG AA.
- **Single-line rows** that break at large text.
- **Right-to-left untested:** only English and Turkish are shipped.
- **No history beyond 7 days.** The same review asks for months and the year.
- **A privacy headline stronger than their store data labels.**

## Rejected for SajdaTime (Ali agreed, 23 Oct 2026)

- **Map and walking route to the mosque, weather in the sky, and a live "people praying now" counter.**
  All need the network or a server.
- **PrayFirst app blocking.** It needs intrusive permissions.
- **"Share the Reward".** It breaks the no in-app share prompt rule.
- **Premium features, a tasbeeh counter, "featured in" logos, and gamification beyond the quiet streak.**

What was taken, and the decisions that followed, are in `PLAN.md` §0.1 and `docs/DESIGN_SYSTEM.md` §9
(23 Oct 2026).
