# Widget redesign: research and visual language (18 Oct 2026)

Status: **proposal, waiting for the owner's choice.** Nothing in the app has changed. Mockups:
the private design canvas "SajdaTime widget redesign" (https://claude.ai/artifact/HM8N9XtmfHoSxZym8YxDRf),
boards A, B, C, "Visual language" and "A+ refined" in dark and light. Target release: 1.5.0, on top of
`claude/quiet-glass`.

## Why

The owner, from his S23 Ultra on 18 Oct 2026, with a screenshot of both 1.4.0 widgets: "extremely
bland", wants an Apple style widget "with decent size fonts and some fancy information".

What the screenshot shows, and why:

1. **Content stranded in a corner.** The Next widget is a fixed 20/16/14sp stack centred vertically;
   the Today widget's rows are fixed height and stop a third of the way down. One UI gave both more
   space than the 3x2 and 4x3 layouts assume. Nothing fills or scales. Fix: responsive layouts
   (`RemoteViews(Map<SizeF, RemoteViews>)`, API 31+) with rows weighted to fill, plus the single layout
   as the API 24 to 30 fallback.
2. **The countdown, the thing people look at most, is the smallest thing on the widget** (14sp).
3. **No sense of the day.** Prayed and still to come look identical. No date, no Hijri date, even
   though the Times screen has both.

## Method, and its honest limits

This is **desk research, not research with users.** No interviews or usability tests were run: the
app has no user panel and opt in usage counts do not include widgets (adding a widget event needs the
owner's agreement and a privacy policy change first, CLAUDE.md). What was used:

| Source | What it gave | Strength |
|---|---|---|
| SajdaTime's own Play reviews (2 reviews, 3 ratings, 17 Oct) | Only complaint: no Arabic. Nothing about widgets either way | Real users, tiny sample |
| Competitor store listings and help pages (Muslim Pro, Athan Pro, Pray Watch, Minaret, Ezan Vakti apps) | What the category offers and where it fails | Vendor text, public reviews |
| Apple HIG widgets, Android widget layout docs | Glanceability, size roles, responsive sizing | Primary platform guidance |
| The fiqh definitions behind each time (already in `core`) | What each prayer time physically *is* | Exact |

## Findings

1. **Reliability beats decoration.** The recurring widget complaints in the category are stale or
   frozen times, a countdown that disappeared, and times hidden behind a premium lock (Athan Pro's
   help page on widgets not updating; Muslim Pro reviews about locked widget times). SajdaTime already
   avoids all three (§5.20: a redraw alarm at each boundary, free forever). Any redesign must keep that
   machinery untouched. *Evidence: medium.*
2. **The countdown is the job.** Every listing that advertises widgets leads with "next prayer and
   countdown"; Pray Watch's distinctive feature is a ring showing time remaining. Apple's guidance: a
   small widget shows the single most important fact. *Evidence: medium to strong.*
3. **Hijri date is expected.** Several listings call it out on the widget. *Evidence: medium.*
4. **Words alone fail part of the audience.** The app ships in five languages, two right to left, and
   is meant for people who may not read the app's language well (CLAUDE.md, "for the masses"). A glyph
   that shows *where the sun is* reads the same in every language. *Evidence: reasoning from the brief,
   not tested.*
5. **The crescent is a generic "Islam" sticker in icon marketplaces.** Using it for one prayer risks
   reading as decoration rather than meaning. *Evidence: weak (marketplace copy), so the design uses
   the moon only where it literally means night.*

## The visual language that follows

**Prayer glyphs, drawn from what each time is**, not from generic religious imagery:

| Prayer | Glyph | Meaning |
|---|---|---|
| Fajr | rays above an empty horizon | first light, sun still below |
| Sunrise | half sun on the horizon, arrow up | the sun crosses; Fajr's window shuts |
| Dhuhr | sun overhead, an upright post with no shadow | the sun at its height |
| Asr | low sun, a post and a shadow **the same length as the post** | the Asr rule itself |
| Maghrib | half sun on the horizon, arrow down | sunset |
| Isha | crescent and one star | full night |

The Asr glyph shows the shadow equal to the post (the majority rule). The Hanafi rule is twice the
length. *Open question for the owner:* draw the shadow by the user's madhab, or keep one glyph for all.
The madhab is never sent anywhere (UsageCounts rule) and a widget is drawn locally, so either is
allowed. One glyph is simpler and avoids a widget that looks different between two family members.

**The mark.** The launcher icon's mihrab and minarets (gold `#F0D69A` on green), in a small tile in the
large widget's header and on the "open SajdaTime" empty state only. *Rejected:* the mark on every
widget. Apple's guidance is not to spend a widget's space on branding, and on a 2x2 it would cost the
prayer name its room.

**Type.** System sans only (DESIGN_SYSTEM §3: no bundled font). The countdown in the **light** weight
(`sans-serif-light`) at 36 to 44sp with tabular figures. That is the Apple Clock look, and it still clears
the 14sp floor by a wide margin. Prayer names semibold. Nothing below 12sp, and that size only for the
column labels of a 4x2 strip.

**Day progress.** Five short bars, one per prayer (sunrise is not a prayer): prayed in green, next
outlined, still to come in the track colour. Built from resource ids switched per row, so the colours
still follow dark mode (lesson 130).

**Colour.** Existing widget tokens only (`WidgetColorsTest`). Green means the next prayer or the mark,
never decoration. No time of day gradient, for the reason in DESIGN_SYSTEM §4 (a moving background
cannot be contrast tested).

## Feasibility in RemoteViews (checked against the platform, not built)

- Glyphs: vector drawables, tinted in the layout XML so the launcher resolves the colour (follows the
  theme). Allowed.
- Light countdown: `Chronometer` with `android:fontFamily="sans-serif-light"`. Allowed. It shows
  H:MM:SS. It cannot show "4h 59m" without a redraw every minute, which this app will not pay for.
- Progress bars: five small views whose background resource is switched. Allowed.
- Responsive sizes: `RemoteViews(Map<SizeF, RemoteViews>)` on API 31+. Older phones keep one layout.
- Direction B's sun arc would need a bitmap drawn per refresh, whose colours would be baked in and go
  stale on a theme change. That is why B is not recommended.

## What would turn this into real research

Cheapest first: (1) show the A+ board to five people from the owner's mosque or family, on a phone,
and ask "what prayer is next and how long until it?" and "what do the little pictures mean?" Time to
answer and wrong answers are the measure. (2) Watch Play reviews after 1.5.0 for widget comments; the
daily review job already reads them. (3) Only with the owner's agreement and a privacy policy change:
a count of widgets placed, by size, no other detail.

Sources: Android "Provide flexible widget layouts" and "App widget design guidelines"
(developer.android.com); Apple HIG "Widgets"; Athan Pro help "My prayer times widget doesn't update
daily" (quanticapps.zendesk.com); Muslim Pro Play listing and help centre; Pray Watch, Minaret, Ezan
Vakti App Store listings.
