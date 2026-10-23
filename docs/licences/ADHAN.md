# Built-in adhan recordings

The audio files in `app/src/main/res/raw/` that the app offers as alarm sounds, one per school (1.5.0,
`app/src/main/java/com/sajdatime/app/data/BuiltInAdhan.kt`). They are credited in the app under
Settings → About → Adhan recordings.

Each file is byte for byte what Wikimedia Commons serves: **not modified**, not trimmed, not
normalised, not re-encoded. The SHA-1 of the download matched the SHA-1 Commons publishes for the
file, which is how "not modified" was checked. Re-run the check before replacing it.

Licences were read on the file pages and through the Commons API on **17 October 2026**.

## Sunni adhan (owner listened and approved, 17 Oct 2026)

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

## Shia adhan

| | |
|---|---|
| In the app | `res/raw/adhan_shia.ogg` (renamed only; the bytes are untouched), "Adhan", Shia users only |
| Title | AzaanMaahur.ogg |
| Source | https://commons.wikimedia.org/wiki/File:AzaanMaahur.ogg |
| Performer, author and uploader | Saeed Hatamzadeh-Varmazyar (سعید حاتم‌زاده ورمزیار), "own work", the page says "read by me" (خوانده شده توسط من), 10 Oct 2015 |
| Licence | CC BY-SA 4.0, https://creativecommons.org/licenses/by-sa/4.0/ (credit and licence link required; given in About) |
| Content | Checked by transcription (whisper.cpp, medium model, Arabic): a complete Shia adhan in order, including "ashhadu anna aliyyan waliyyullah", "hayya ala khayr al-amal" twice, and "la ilaha illallah" twice at the close |
| Quality | Studio: voice about 62 dB above the background, true silence between phrases, peak -2.7 dB (no clipping), -10.9 LUFS |
| Length, format | 2 min 48 s, Ogg Vorbis, about 109 kbps, 44.1 kHz stereo, 2,289,817 bytes |
| SHA-256 | `0e0433e77bed4ee15044169c4b735cac346836eeae11a36ea194228aaa18ac97` |
| SHA-1 (matches Commons) | `2f6cd3bc6110a77c4f21abad33f4c0986467aebc` |
| Modified | No (so the app is not an adaptation; editing it would make the edit CC BY-SA too) |

## Rules

- Credit only the files that ship. If one is removed, remove its row in About and its line here.
- The sources already rejected, and the candidates looked at, are in `docs/HANDOVER.md` §11, headings
  "Adhan sources rejected" and "More adhan candidates".
- **Considered and dropped (17 Oct 2026): "The Adhan - Muslim Call to Prayer - Aaqib Azeez.mp3"**,
  CC BY-SA 4.0, uploader Atcovi. Licence fine, but the file was replaced by its uploader on 11 Dec 2025
  (the 2022 original was a different, 3 min 55 s recording) and the page says "own work" by Atcovi
  while the file name names a reciter. The owner asked for one recording that is safe to use, so the
  CC0 one was preferred. (The Shia adhan is a different recording, above.)
