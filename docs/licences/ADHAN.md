# Built-in adhan recordings

The audio files in `app/src/main/res/raw/` that the app offers as alarm sounds, one per school (1.5.0,
`app/src/main/java/com/sajdatime/app/data/BuiltInAdhan.kt`). They are credited in the app under
Settings → About → Adhan recordings.

**CHANGED 7 Nov 2026 (Ali's decision, after listening: "sunni 1.75x is fine for shia 1.5x is fine ... these
are only adhan we are going to use onwards").** The files in the app are now SHORTENED copies, because most
people do not want the full 2.5 minutes. Nothing else was changed: no words cut, no normalising, no
filters. Method (ffmpeg): Sunni sped up 1.75x with `atempo` (pitch unchanged); Shia: pauses longer than
1.2 s shortened to 0.8 s (`silenceremove`), then 1.5x with `atempo`. Both re-encoded to Opus in Ogg, 96 kbps
stereo (this Mac's ffmpeg has no Vorbis encoder; Android plays Opus in Ogg from Android 5, minSdk is 24).
The original Commons files are unmodified in git history (commit 33caf91, `res/raw/`) and their hashes
are kept below as "source". **Licence effect:** the Sunni source is CC0, so editing it is free. The Shia
source is CC BY-SA 4.0, so the shortened copy is an adaptation: it must say it was changed (About now says
"Both are shortened for this app") and stays CC BY-SA 4.0 (the credit and licence link already given).
Not checked by transcription again: Ali listened to both versions and approved them.

Licences were read on the file pages and through the Commons API on **17 October 2026**.

## Sunni adhan (owner listened and approved, 17 Oct 2026)

| | |
|---|---|
| In the app | `res/raw/adhan.ogg` (shortened, see top), "Adhan" |
| Title | Beautiful adhan.ogg |
| Source | https://commons.wikimedia.org/wiki/File:Beautiful_adhan.ogg |
| Uploader | Adam-synagda (author on the page: Adam-synagda, "own work", recorded 29 Apr 2022) |
| Licence | CC0 1.0, https://creativecommons.org/publicdomain/zero/1.0/ (no credit required; credited anyway) |
| Length, format in the app | 1 min 28 s, Opus in Ogg, 96 kbps, 48 kHz stereo, 976,404 bytes (source: 2 min 34 s, Vorbis, 1,229,032 bytes) |
| SHA-256 in the app | `449ca062d64564c5face0407c5856618cc4a865ec4d3a52757eeabc58e7c3c2e` |
| SHA-256 source | `35fe06b08fe80505c550c33fed8a783fa9901ddc81ac884958b4be048f5b2a79` |
| SHA-1 (matches Commons) | `a1fa4fd942401922c5c3301816384aae86956522` |
| Modified | Yes, shortened (above). CC0 allows it. |

## Shia adhan

| | |
|---|---|
| In the app | `res/raw/adhan_shia.ogg` (shortened, see top), "Adhan", Shia users only |
| Title | AzaanMaahur.ogg |
| Source | https://commons.wikimedia.org/wiki/File:AzaanMaahur.ogg |
| Performer, author and uploader | Saeed Hatamzadeh-Varmazyar (سعید حاتم‌زاده ورمزیار), "own work", the page says "read by me" (خوانده شده توسط من), 10 Oct 2015 |
| Licence | CC BY-SA 4.0, https://creativecommons.org/licenses/by-sa/4.0/ (credit and licence link required; given in About) |
| Content | Checked by transcription (whisper.cpp, medium model, Arabic): a complete Shia adhan in order, including "ashhadu anna aliyyan waliyyullah", "hayya ala khayr al-amal" twice, and "la ilaha illallah" twice at the close |
| Quality | Studio: voice about 62 dB above the background, true silence between phrases, peak -2.7 dB (no clipping), -10.9 LUFS |
| Length, format in the app | 1 min 34 s, Opus in Ogg, 96 kbps, 48 kHz stereo, 1,045,913 bytes (source: 2 min 48 s, Vorbis, 2,289,817 bytes) |
| SHA-256 in the app | `9738206467fc4e5cf412d98545f88cef9a8a60433aa9547f8bf10d40e4793565` |
| SHA-256 source | `0e0433e77bed4ee15044169c4b735cac346836eeae11a36ea194228aaa18ac97` |
| SHA-1 (matches Commons) | `2f6cd3bc6110a77c4f21abad33f4c0986467aebc` |
| Modified | Yes, shortened (above). The copy is an adaptation, so it stays CC BY-SA 4.0 and About says it was shortened. |

## Rules

- Credit only the files that ship. If one is removed, remove its row in About and its line here.
- The sources already rejected, and the candidates looked at, are in `docs/HANDOVER.md` §11, headings
  "Adhan sources rejected" and "More adhan candidates".
- **Considered and dropped (17 Oct 2026): "The Adhan - Muslim Call to Prayer - Aaqib Azeez.mp3"**,
  CC BY-SA 4.0, uploader Atcovi. Licence fine, but the file was replaced by its uploader on 11 Dec 2025
  (the 2022 original was a different, 3 min 55 s recording) and the page says "own work" by Atcovi
  while the file name names a reciter. The owner asked for one recording that is safe to use, so the
  CC0 one was preferred. (The Shia adhan is a different recording, above.)
