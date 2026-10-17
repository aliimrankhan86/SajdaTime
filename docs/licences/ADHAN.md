# Built-in adhan recording

The audio file in `app/src/main/res/raw/` that the app offers as an alarm sound to Sunni users (1.5.0,
`app/src/main/java/com/sajdatime/app/data/BuiltInAdhan.kt`). It is credited in the app under
Settings → About → Adhan recording.

The file is byte for byte what Wikimedia Commons serves: **not modified**, not trimmed, not
normalised, not re-encoded. The SHA-1 of the download matched the SHA-1 Commons publishes for the
file, which is how "not modified" was checked. Re-run the check before replacing it.

Licences were read on the file pages and through the Commons API on **17 October 2026**.

## The adhan

| | |
|---|---|
| In the app | `res/raw/adhan.ogg` (renamed only; the bytes are untouched), "Adhan" |
| Title | Beautiful adhan.ogg |
| Source | https://commons.wikimedia.org/wiki/File:Beautiful_adhan.ogg |
| Uploader | Adam-synagda (author on the page: Adam-synagda, "own work", recorded 29 Apr 2022) |
| Licence | CC0 1.0, https://creativecommons.org/publicdomain/zero/1.0/ (no credit required; credited anyway) |
| Length, format | 2 min 34 s, Ogg Vorbis, about 112 kbps, 44.1 kHz, 1,229,032 bytes |
| SHA-256 | `35fe06b08fe80505c550c33fed8a783fa9901ddc81ac884958b4be048f5b2a79` |
| SHA-1 (matches Commons) | `a1fa4fd942401922c5c3301816384aae86956522` |
| Modified | No |

## Rules

- Credit only the files that ship. If one is removed, remove its row in About and its line here.
- No Shia recording ships until one with a written licence exists. The sources already rejected, and
  why, are in `docs/HANDOVER.md` §11, heading "Adhan sources rejected".
- **Considered and dropped (17 Oct 2026): "The Adhan - Muslim Call to Prayer - Aaqib Azeez.mp3"**,
  CC BY-SA 4.0, uploader Atcovi. Licence fine, but the file was replaced by its uploader on 11 Dec 2025
  (the 2022 original was a different, 3 min 55 s recording) and the page says "own work" by Atcovi
  while the file name names a reciter. The owner asked for one recording that is safe to use, so the
  CC0 one ships alone.
