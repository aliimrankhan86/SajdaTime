# Step 4 Timetable sheet: emulator screenshot results

Device: emulator-5554, debug build com.sajdatime.app. Real clock 24 Oct 2026, London, approximate area label.
Settings and what is visible in each picture. "Cut off" means text clipped by an edge or container.

## Pictures

- 1-light-823.png: normal settings, light, Timetable sheet open. Three columns of times (Fajr/Sunrise/Dhuhr, Asr/Maghrib/Isha). Today card (Sat 24 Oct) shows its "Today" pill on the heading row. No text cut off, no word broken.
- 2-dark-823.png: dark (uimode night yes), normal font, sheet open. Three columns. "Today" pill visible under the month heading. No text cut off, no word broken.
- 3-urdu-823.png: Urdu (app locale ur), light, normal font, sheet open. Right-to-left layout, three columns. Today card shows a round "آج" badge instead of the English "Today" pill. Times keep English AM/PM. No text cut off, no word broken.
- 4-text1.5-823.png: font scale 1.5, normal density, sheet open. Two columns of times (Fajr/Sunrise, Dhuhr/Asr, Maghrib/Isha). "Save as PDF" button on its own full-width row. "Today" pill visible on the Today card heading row. No text cut off, no word broken.
- 4b-text1.5-times.png: font scale 1.5, Times screen, sheet closed, no scroll needed. "Today" heading and "Timetable" button visible in full. Bottom bar labels at this size: "My prayers" wraps to two lines. No clipping of the Timetable button.
- 5-text2.0-320dp.png: density 540 (320dp width) + font scale 2.0, sheet open. One column of times. Save as PDF button sits on its own row. "Today" pill visible on the Today card heading row. No text cut off, no word broken.
- 5b-text2.0-320dp-times.png: density 540 + font 2.0, Times screen, sheet closed, no scroll needed. "Today" heading and "Timetable" pill visible in full. Bottom bar labels ("Times", "Qibla", "My prayers", "Settings") are cramped and the "Times" and "Qibla" labels run into each other.
- 5c-text2.0-320dp-urdu.png: density 540 + font 2.0 + Urdu, sheet open. Right-to-left, one column of times. Round "آج" badge on the Today card. Content continues below the bottom edge (scrollable list). No text cut off at the top; no word broken.
- 6-31oct-light.png: emulator date 31 Oct 2026 12:00 (auto_time 0), normal settings, light, sheet open. Sat 31 Oct card with "Today" pill, then "November 2026" heading and Sun 1 Nov card below it. Three columns. No text cut off.
- 6b-31oct-november.png: same as 6, list scrolled (swipe 540 1800 to 540 1000). Shows November 2026 cards (Sun 1 Nov, Mon 2 Nov, Tue 3 Nov, Wed 4 Nov, Thu 5 Nov), three columns. Top card partly scrolled under the Save as PDF row. No text cut off.
- 7-chooser-over-sheet.png: normal settings, light, sheet open, "Save as PDF" tapped. Chooser "Save your timetable" slides over the bottom of the sheet with three options: "Today only", "The next 7 days", "This whole month". All option text fully visible. No range chosen.
- 7b-after-back.png: after one BACK press from 7. Chooser gone, Timetable sheet still open underneath (Save as PDF button and Sat 24 Oct card visible).

## Column counts

- 3 columns: 1-light, 2-dark, 3-urdu, 6-31oct-light, 6b-31oct-november, 7 and 7b (sheet behind chooser)
- 2 columns: 4-text1.5
- 1 column: 5-text2.0-320dp, 5c-text2.0-320dp-urdu

## UI dump at normal settings (1.0), sheet open

content-desc of today's card, verbatim:
Today, Sat 24 Oct. Fajr 6:01 am. Sunrise 7:40 am. Dhuhr 12:45 pm. Asr 3:17 pm. Maghrib 6:10 pm. Isha 7:15 pm.

## Notes on the process

- The first dark attempt used a wrong tap pattern and captured the Times screen; 2-dark-823.png was retaken with the sheet open. Final file is the sheet.
- After 5c the sheet was closed with BACK (not in the spec, harmless).
- Save as PDF was tapped only for 7; no range was chosen and no file was saved.

## Restore state (done at end)

Night mode no, font scale 1.0, density reset (420), app locale empty, auto_time 1, app force-stopped.

## Q8 and Ramadan pictures (main session, 24 Oct 2026)

Clock set to 22 Feb 2027 12:00 (day 15 of Ramadan 1448), London, Ramadan card on.
- 9a-ramadan-table-light.png / 10a-ramadan-table-dark.png: the approved table, as shipped on this branch, with the
  shared header (title now headlineSmall, the X). Today's row (15, Mon 22 Feb) marked.
- 9b-ramadan-cards-light.png / 10b-ramadan-cards-dark.png: the Q8 card prototype, built from a TEMPORARY local
  edit (RamadanCardsPreviewList swapped in for the table, plus scroll to today), reverted before commit; not in any
  commit. Suhoor ends and Iftar in primary and SemiBold, Fast line under the grid, Today pill.
- 11-ramadan-chooser.png: Save as PDF on the Ramadan table now opens the chooser (Ali, 24 Oct "Show the choices"):
  Today only, The next 7 days, This whole month, Ramadan 1448, all reachable in Ramadan.
Clock restored to automatic afterwards.

## Found in passing, not step 4

At font 1.5 the bottom bar's "My prayers" wraps to two lines; at 320dp and 2.0 "Times" and "Qibla" run into each
other (4b, 5b). The tab bar is step 1's; recorded in HANDOVER §11 as a leftover.
