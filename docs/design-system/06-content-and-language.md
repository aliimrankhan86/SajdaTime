# 6. Content and language

How SajdaTime speaks, in every language, and how text behaves right to left. Words are part of the
design: most of what users meet is a sentence, not a shape.

## 6.1 Voice

Warm, plain, honest about limits. Second person ("your location"). The disclaimer alone speaks as the
developer ("I can simply be wrong"). Never salesy, never cute, never preachy.

| Do | Not |
|---|---|
| "Only you can see it." | "Your data is secured with industry leading protection" |
| "Prayer alerts can arrive an hour late, and Fajr may not arrive at all." | "Enable exact alarms for an optimal experience" |
| "Where a difference remains, follow your mosque." | "Our times are accurate" |
| "Nothing is tracked until you switch this on." | "Start your journey!" |
| "Sorry if you celebrate on another day: Eid Mubarak to you then too." | Assuming everyone's Eid is today |

## 6.2 House style (English)

From the top of `strings.xml`: plain words, short sentences, **no em dashes, no semicolons, UK
spelling**. Plus:

- **Sentence case everywhere**: titles, buttons, tabs, section labels ("Prayer alerts", "Save as PDF").
- **No hyphens** in app text, except inside names that need them (Eid al-Fitr, ir-Rahman, Rabi
  al-Awwal, Open-Meteo). Rephrase instead ("one tap", not "one-tap").
- **Full stops** end sentences. Titles, buttons, row titles, chips and pills have none. A row subtitle
  that is a sentence has one ("Opens the store page."); a fragment does not.
- **Buttons start with a verb**, two to five words: "Allow location", "Shift the date". Never "Tap
  here", "Click", "OK" alone (the time picker's "Set time" is the pattern).
- **Questions as titles** for decisions and doors: "Where are you praying?", "Different from your
  mosque?", "Delete your prayer record?".
- **Separators are strings, never code**: " · " (`S:settings_value_pair`), ", " (`S:list_separator`).
  Word order differs by language.
- **Counts use plurals** resources (Arabic has six forms).
- Ellipsis "…" in progress lines ("Finding your location…").

Known exceptions in strings: `S:adjustment_none` is "—" on purpose (the "no change" symbol);
`S:location_permission_denied` says "tap Allow location" (rule breach, recorded).

## 6.3 Words we never use

| Never | Why | Instead |
|---|---|---|
| missed, late, owed, debt, excused, qada (English, prayers) | Judges private worship | Ticks and "not ticked"; "fasts to make up" |
| valid, must, may (as a ruling), "you can pray now" | The app is not a religious authority | State the time; "follow your mosque" |
| imsak, stop eating | Not our ruling | "Suhoor ends" (at Fajr, as calculated), "Iftar" (at Maghrib) |
| tracking (in the usage question) | Misleads | "usage counts" |
| error, failed (alone), invalid | Blames | What happened and what to do |
| amazing, premium, best, ultimate | Hype | Say what it does |
| Any ask for a rating, share, review or donation in our words | The owner's rule | Google's own review card, rarely (`05-screens-and-features.md` §5.9), and the passive rows in About |

Sky names are times of day (Dawn, Day, Evening, Night), never prayer or fasting names.

## 6.4 Numbers, times and dates

| Thing | Format | Example |
|---|---|---|
| Clock time | The phone's 12 or 24 hour setting, in the app's language; never bidi isolated | 5:37 am, 17:37 |
| Countdown on screen | Tabular hh:mm:ss | 01:23:45 |
| Remaining, spoken | Hours and minutes | 2h 14m, 48m |
| Gregorian date | Short weekday, day, short month, no year | Thu 31 Jul |
| Month heading | Month and year | October 2026 |
| Hijri date | Day, month name, year | 16 Safar 1448 |
| Minutes offset | Always signed; none is "—" | +5 min, −3 min |
| Degrees | Digits with the sign attached | 118° |
| Distance | km, or miles in the US, UK, Liberia, Myanmar (phone region, never the app language) | About 4,512 km |
| Digits | Western in every language, including Arabic (`ar-u-nu-latn`) | |

Use `TimeFormat` (`app/.../notify/TimeFormat.kt`) and the plural resources; never build these strings
in code.

## 6.5 Religious names

Exactly as in the strings, never simplified: Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha, Jumu'ah,
Jama'ah; Hanafi, Shafi'i, Maliki, Hanbali; Sunni, Shia (Jafari, Ithna Ashari). Method names are the
organisations' names. Hijri months: Muharram, Safar, Rabi al-Awwal, Rabi al-Thani, Jumada al-Ula,
Jumada al-Akhirah, Rajab, Shaban, Ramadan, Shawwal, Dhu al-Qadah, Dhu al-Hijjah. "Birth of the Prophet
ﷺ" keeps the ligature. The Bismillah is shown in Arabic and spoken in full.

## 6.6 Languages

| Language | Folder | Direction | Notes |
|---|---|---|---|
| English (UK) | `values` | LTR | Source of truth |
| Urdu | `values-ur` | RTL | Noto Naskh lines stand about 4dp taller |
| Arabic | `values-ar` | RTL | Tag `ar-u-nu-latn` keeps Western digits |
| Turkish | `values-tr` | LTR | Dotted and dotless i: never uppercase in code |
| Indonesian | `values-in` | LTR | Must be `in`, not `id` (Android passes the legacy code) |

Rules:

- **The app follows the phone's language when it has the words**, else English, LTR. English is never
  laid out right to left.
- **In app picker on every Android version**, "Phone language" first. Anything naming a language goes
  through `AppLocale.wrap` at the moment of use.
- **Every language is packaged** (`bundle.language.enableSplit = false`), or the picker cannot switch.
- **Claude translates and checks its own work**; no native review (owner, 9 Oct 2026). Religious names
  and the disclaimer get extra care; the disclaimer is never softened in any language. Never tell users
  a translation awaits review.
- **A new string** lands in all five languages in the same commit (`TranslationIntegrityTest`).
- **Text grows**: allow 30 to 40% longer than English in buttons and rows; never fix a width to English.

## 6.7 Right to left

What mirrors: layout order, margins, alignment, chevrons and forward arrows (`AutoMirrored`), the tab
order, row start and end, the sky mark's position.

What never mirrors: clock times and countdowns, the Qibla dial and its letters, the week and history
rings (clockwise like a clock), sun glyphs, numbers, the PDF's table digits.

Text inside RTL: isolate Latin place names and the Hijri month name so word order holds; never isolate
clock times (the meridiem lands on the right side as rendered).

Check with `./gradlew :app:installRtl` as a **layout** check (margins, chevrons, order, clipping).
English words in an RTL paragraph always look broken; ignore the prose and never show those
screenshots to the owner as findings. Then check the real Urdu build.

## 6.8 Accessibility wording

- Screen reader labels are sentences a person would say: "Asr in 2h 14m", "Fajr, Next, 5:37 am",
  "Dhuhr alert on, notification".
- An icon button's label is its action ("Close", "Update location, London").
- Decorative icons have no label. Pictures that carry meaning (the Kaaba, the sky mark) are labelled
  or described by the text beside them.
