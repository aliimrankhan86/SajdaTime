# SajdaTime 1.7.0 store review

4 November 2026 · `claude/1-7-0`

## Listing text

The approved English is in [LISTING.md](LISTING.md). Commit `c838834` records that wording unchanged alongside the translations. The full English description is 3,950 characters. The release notes are 407 characters. The existing note above the approved text gives 286 for the release notes.

The separate Claude reviewer can find these sections in [LISTING-LOCALES.md](LISTING-LOCALES.md). Counts include spaces and line breaks, excluding the final line break and Markdown fences.

| Section | Full description (limit 4,000) | What's new (limit 500) |
|---|---:|---:|
| Arabic (ar), 1.7.0 | 3,615 | 351 |
| Urdu (ur), 1.7.0 | 3,903 | 349 |
| Turkish (tr-TR), 1.7.0 | 3,978 | 402 |
| Indonesian (id-ID), 1.7.0 | 3,995 | 411 |
| Malay (ms-MY), 1.7.0 | 3,985 | 390 |
| Bengali (bn-BD), 1.7.0 | 3,974 | 329 |
| French (fr-FR), 1.7.0 | 3,999 | 436 |
| Hindi (hi-IN), 1.7.0 | 3,940 | 390 |

I checked the new copy against the approved English, and the prayer names, schools and app labels against the Android strings where available. I kept the previously reviewed disclaimer and dua paragraphs as written. The mechanical diff review confirmed the prior sections were untouched and caught the old Bengali spelling of Maliki, which I corrected in the 1.7.0 text.

The wording I would look at most closely in the separate language review is the sky band in Bengali (আকাশের ফালি), the equivalent of the mosque adjustment in Malay, and the register of the Hindi release notes, especially चाश्त for Duha. Malay, French and Hindi use the terminology of their previous listings because the app does not have those languages. The French full description has one character to spare by the local count.

The ledger baseline before editing was `0 to review, 2913 already reviewed`. It returned status 1 before the translation commit and again after it because these same 16 blocks await review. The ledger was left unstamped.

```text
NEW     listing/### Arabic (ar), 1.7.0/Full description
NEW     listing/### Arabic (ar), 1.7.0/What's new
NEW     listing/### Bengali (bn-BD), 1.7.0/Full description
NEW     listing/### Bengali (bn-BD), 1.7.0/What's new
NEW     listing/### French (fr-FR), 1.7.0/Full description
NEW     listing/### French (fr-FR), 1.7.0/What's new
NEW     listing/### Hindi (hi-IN), 1.7.0/Full description
NEW     listing/### Hindi (hi-IN), 1.7.0/What's new
NEW     listing/### Indonesian (id-ID), 1.7.0/Full description
NEW     listing/### Indonesian (id-ID), 1.7.0/What's new
NEW     listing/### Malay (ms-MY), 1.7.0/Full description
NEW     listing/### Malay (ms-MY), 1.7.0/What's new
NEW     listing/### Turkish (tr-TR), 1.7.0/Full description
NEW     listing/### Turkish (tr-TR), 1.7.0/What's new
NEW     listing/### Urdu (ur), 1.7.0/Full description
NEW     listing/### Urdu (ur), 1.7.0/What's new
16 to review, 2913 already reviewed
```

There is no dedicated listing text check in `tools/`. I checked the fenced blocks and their displayed counts directly. `tools/check-translation.py` reported zero errors and zero warnings for Arabic, Bengali, Indonesian, Turkish and Urdu Android resources. The four targeted Gradle suites passed: Design Tokens (11), Design Rules (12), Colour Contrast (11), and Translation Integrity (9).

## Phone pictures for Ali to approve

I built the debug app and its capture helpers from `63bb233` and verified the emulator reported 1.7.0, build 11. I chose Automatic and Continue after clearing app data on `sajdastore` (Android 16, 1080 × 1920), then used the project's London and My prayers seeds. The four seed invocations passed their readback checks. The three widgets were placed through the launcher. I used Android’s dark mode with Follow phone for the final Ramadan pictures. Bengali uses the English set.

I used two launcher pictures so the Today strip, wide Next prayer and full Today widget are readable. The launcher clock is staged at 6:30 while the live countdown keeps ticking during placement. To stay within eight English pictures, the separate timetable, alerts and Suhoor settings pictures give their slots to the Today widget, theme selector and mosque dialog. The Ramadan picture still shows the timetable button.

| Picture | English | Arabic | Urdu | Turkish | Indonesian | What I checked |
|---|---|---|---|---|---|---|
| Times | [EN](upload/phone/en-GB/01-times-light.png) | [AR](upload/phone/ar/01-times-light.png) | [UR](upload/phone/ur/01-times-light.png) | [TR](upload/phone/tr-TR/01-times-light.png) | [ID](upload/phone/id-ID/01-times-light.png) | London, sky band, next prayer countdown and a single filled Now marker on Dhuhr with time remaining. |
| My prayers | [EN](upload/phone/en-GB/02-my-prayers.png) | [AR](upload/phone/ar/02-my-prayers.png) | [UR](upload/phone/ur/02-my-prayers.png) | [TR](upload/phone/tr-TR/02-my-prayers.png) | [ID](upload/phone/id-ID/02-my-prayers.png) | Fajr and Dhuhr ticked, the weekly rings, and the local Pause tracking button. Arabic uses Thursday so the week is visible. |
| Qibla | [EN](upload/phone/en-GB/03-qibla.png) | [AR](upload/phone/ar/03-qibla.png) | [UR](upload/phone/ur/03-qibla.png) | [TR](upload/phone/tr-TR/03-qibla.png) | [ID](upload/phone/id-ID/03-qibla.png) | Compass, Kaaba, London and a 58° right turn from a displayed heading of 61°. |
| Ramadan in dark | [EN](upload/phone/en-GB/04-ramadan-dark.png) | [AR](upload/phone/ar/04-ramadan-dark.png) | [UR](upload/phone/ur/04-ramadan-dark.png) | [TR](upload/phone/tr-TR/04-ramadan-dark.png) | [ID](upload/phone/id-ID/04-ramadan-dark.png) | Day 15, aligned Suhoor and Iftar tiles, Iftar marked Next, countdown, fasting duration and the full timetable button. |
| Today strip and Next prayer | [EN](upload/phone/en-GB/05-widgets.png) | — | — | — | — | Five aligned columns in the strip and a six row list in the wide Next widget. Both mark current Asr. The countdown is for Maghrib. |
| Today widget | [EN](upload/phone/en-GB/06-widget-today.png) | — | — | — | — | Date and London, countdown to Maghrib, six times, and a single Now label on Asr. |
| Settings | [EN](upload/phone/en-GB/07-settings-theme.png) | — | — | — | — | Follow phone selected with Dark as the other choice on a light phone. The Duha switch is visible and off. |
| Match your mosque | [EN](upload/phone/en-GB/08-match-your-mosque.png) | — | — | — | — | Fajr at 3:22 am beside its adjusted 3:25 am, +3 min, with aligned minute controls and the mosque board button. |

I opened each of the 24 captures and compared sibling rows and tiles with the independent reviewer. The tracker rows are evenly spaced. The Ramadan tiles share a width and height within each language. The widget columns and mosque minute controls line up. The floating navigation covers some lower rows on Times at 9:16, and the Indonesian time remaining wraps. Arabic and Urdu mirror on the emulator except for the thin Now stripe, which stays on the physical left. The mosque dialog wraps the adjusted Fajr time and shows part of the last row at the bottom of its scroll area. These are visible in the app itself.

The store asset script copied the phone PNGs through byte for byte. The release script's optional screenshot step still uses the old English shot list, so rerunning that step would reintroduce old slots.

## Checks still pending

Ali's picture approval and the separate Claude language review are pending. I checked right to left layout in the Arabic and Urdu app captures. I did not check these pictures on a real phone, localised widgets, double size text, TalkBack or Play Console previews. I did not rerun the full release gate or capture new watch pictures.

The public Play listing still showed 23 October 2026 as its update date when checked on 4 November. Nothing was sent to Google Play and no Play Console controls were used. No app code, signing files, or files under `private/` or `.codex/` were committed.
