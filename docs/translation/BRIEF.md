# Translation brief: SajdaTime into Indonesian, Turkish and Urdu

Written 8 Oct 2026. Read this whole file before translating or reviewing. It is also the record of
how these three languages were made, so the next person knows what was decided and why.

## Who decided what

On 5 Oct 2026 the owner (Ali Imran Khan) decided that Urdu, Turkish and Indonesian would be added,
that he would **waive the native-speaker review** for them (he reads Urdu; nobody on the project can
check the other two) and that **an assistant does the translation**. He also approved these
safeguards, which are not optional:

1. The six highest-risk Urdu strings are spot-checked by the owner (list in `notes-ur.md`).
2. Method names stay untranslated (they are organisation names). Their one-line descriptions are
   translated.
3. Every translated language shows this line in About: *translated with AI help, may contain
   mistakes, tell us with Send feedback* (`about_translation_note`).
4. Tests: placeholder parity, plural forms, disclaimer structure (`tools/check-translation.py`
   and the Kotlin tests).
5. The translated consent text says the privacy policy page is in English (`consent_policy_english`).
6. Store listings are drafted but **never published** by an assistant.

An independent second model reviews each translation by back-translating it. Treat that as a
net, not a licence to be careless.

## What you are translating

Three English source files, each into one new folder:

| Source | Output (Indonesian `in`, Turkish `tr`, Urdu `ur`) |
|---|---|
| `app/src/main/res/values/strings.xml` | `app/src/main/res/values-<q>/strings.xml` |
| `core/src/main/res/values/strings.xml` | `core/src/main/res/values-<q>/strings.xml` |
| `wear/src/main/res/values/strings.xml` | `wear/src/main/res/values-<q>/strings.xml` |

Qualifier `<q>` is `in` for Indonesian (Android's own code; `values-id` is a lint error), `tr`,
`ur`. Keep every `name`, keep `translatable="false"` strings out of your file. Keep XML comments out
of the output except one header comment saying the file is an AI-assisted translation (see
`docs/translation/BRIEF.md`). In `core` you must also set `app_language_tag`:

- Indonesian: `in-ID`   Turkish: `tr-TR`   Urdu: `ur-PK` (Latin digits, which is what Pakistan uses)

You also write `docs/translation/notes-<tag>.md` (see the end).

Check your work yourself, as many times as it takes, with:

```
python3 tools/check-translation.py <q> <tag>      # e.g.  in in-ID
```

It must print `0 error(s)`. You may not run Gradle or edit any other file.

## Hard rules (these protect religious meaning and the app)

1. **Placeholders are code.** `%1$s`, `%2$d`, `%+d`, `%d`, `%%` must all appear, with the same
   numbering. You may move them (positional `%2$s ... %1$s` is fine). Never invent one, never drop one.
2. **Escapes.** Write `\'` for an apostrophe, `&amp;` for `&`, `\n` for a line break, `—` stays
   as it is in the source, and keep the `•` bullets and the same number of `\n` as the source.
   Never leave a bare `'` in the file.
3. **No grammar glued to a placeholder.** Turkish suffixes, Urdu postpositions and Indonesian
   particles must not be attached to `%1$s`/`%2$d` (a time or a name we cannot predict breaks vowel
   harmony and shaping). Restructure the sentence so the placeholder stands alone.
4. **Do not strengthen, soften or drop anything in the safety and legal text.** That means the
   disclaimer, the consent text, the exact-alarm and Do Not Disturb explanations, the polar notice and the
   method descriptions. Every hedge is deliberate and load-bearing: *some, many, usually, may, can,
   common in, followed by some mosques*. Keep them as hedges. "Follow your mosque", "I can simply be
   wrong", "no warranty", "Neither is wrong" must stay in force.
5. **The disclaimer keeps its shape**: seven paragraphs, same order, separated by `\n\n`, and the
   **last paragraph is the request to remember the author, his family and his parents in duas**,
   which appears there and nowhere else. Do not add a dua anywhere else, ever.
6. **Voice.** The disclaimer is written in the first person singular by one humble person who is not
   a Mufti or Aalim. Keep that voice. Elsewhere the app addresses the user politely and plainly.
7. **Never paraphrase a religious term into a descriptive phrase.** Use the standard term from the
   glossary below. If you believe the glossary is wrong, use it anyway and say so in the notes file.
8. **Method names stay as they are** (`method_*` names, shown as organisation names): Muslim World
   League, Egyptian General Authority, University of Karachi, Umm al-Qura, Moonsighting Committee,
   ISNA, Kemenag, MUIS, Diyanet, Jafari, Institute of Geophysics, Dubai, Kuwait, Qatar. Translate
   only the descriptive words around them (country names, "Indonesia and Singapore", "Turkey"). Their
   `_desc` lines are translated in full.
9. **Do not translate** `SajdaTime`, `Google Analytics`, `Firebase`, `Open-Meteo`, `adhan-java`,
   `Batoul Apps`, `PDF`, `Android`, `Ali Imran Khan`, the Arabic Bismillah, e-mail addresses and URLs.
10. **`Turn left` and `Turn right` (Qibla) are physical.** They tell the user to rotate their body.
    They are never mirrored, whatever the writing direction.
11. **Short UI strings stay short.** Buttons and tab labels must fit one line on a small phone. Prefer
    the shorter standard word.
12. **Numbers in literal text** use Latin digits (`5`, `14`), in every language including Urdu.
13. **Do not add** Unicode direction marks (LRM, RLM, FSI, PDI) or any invisible character. The app
    isolates names itself.

## Glossary (use these; flag disagreement in the notes file, do not silently change)

### Prayer names (these are in `core`, `prayer_*`) and times

| English | Indonesian (Kemenag) | Turkish (Diyanet's labels) | Urdu |
|---|---|---|---|
| Fajr | Subuh | İmsak | فجر |
| Sunrise | Terbit | Güneş | طلوعِ آفتاب |
| Dhuhr | Zuhur | Öğle | ظہر |
| Asr | Asar | İkindi | عصر |
| Maghrib | Magrib | Akşam | مغرب |
| Isha | Isya | Yatsı | عشاء |

Indonesian and Turkish labels are the ones the official timetables print (Kemenag: Imsak, Subuh,
Terbit, Dhuha, Zuhur, Asar, Magrib, Isya; Diyanet: İmsak, Güneş, Öğle, İkindi, Akşam, Yatsı). They
were confirmed through sites that publish those timetables, not from the official pages, which could
not be fetched from the cloud container. Say in the notes if you know a better standard label.

### Common religious and app terms

| English | Indonesian | Turkish | Urdu |
|---|---|---|---|
| Qibla | Kiblat | Kıble | قبلہ |
| Kaaba | Kakbah | Kâbe | کعبہ |
| Makkah | Mekah | Mekke | مکہ مکرمہ |
| Ummah | umat | ümmet | امت |
| Allah | Allah | Allah | اللہ |
| dua | doa | dua | دعا |
| adhan | azan | ezan | اذان |
| mosque | masjid | cami | مسجد |
| Mufti / Aalim / fiqh | mufti / ulama / fikih | müftü / âlim / fıkıh | مفتی / عالم / فقہ |
| scholars | ulama | âlimler | علمائے کرام |
| congregation | salat berjamaah / jemaah | cemaat | جماعت |
| Sunni / Shia | Sunni / Syiah | Sünni / Şii | سنی / شیعہ |
| school of thought | mazhab | mezhep | مکتبِ فکر |
| madhab | mazhab | mezhep | فقہ |
| Hanafi / Shafi'i / Maliki / Hanbali | Hanafi / Syafi'i / Maliki / Hanbali | Hanefi / Şafii / Maliki / Hanbeli | حنفی / شافعی / مالکی / حنبلی |
| Hijri / Islamic date | Hijriah | Hicri | ہجری |
| Ramadan / Eid | Ramadan / Id | Ramazan / Bayram | رمضان / عید |
| twilight | senja / mega (syafak) | şafak | شفق |

### Hijri months (`hijri_months`, twelve items, same order as the source)

- Indonesian: Muharam, Safar, Rabiulawal, Rabiulakhir, Jumadilawal, Jumadilakhir, Rajab, Syakban, Ramadan, Syawal, Zulkaidah, Zulhijah
- Turkish (Diyanet): Muharrem, Safer, Rebiülevvel, Rebiülahir, Cemaziyelevvel, Cemaziyelahir, Recep, Şaban, Ramazan, Şevval, Zilkade, Zilhicce
- Urdu: محرم، صفر، ربیع الاول، ربیع الثانی، جمادی الاول، جمادی الثانی، رجب، شعبان، رمضان، شوال، ذیقعد، ذوالحجہ

### Register

- Indonesian: formal and warm, `Anda`, standard Indonesian (KBBI), never slang.
- Turkish: polite `siz`, standard Istanbul Turkish, use the circumflex where the standard word has it (Kâbe, âlim).
- Urdu: formal `آپ`, standard Pakistani Urdu, Urdu punctuation (`۔` `؟` `،`), Naskh-readable plain text,
  no decorative diacritics except where the word is ambiguous without them.

## Notes file (`docs/translation/notes-<tag>.md`)

Write it for the owner, who is not technical and for Urdu can read the text. Include:

1. **The six highest-risk strings** with the English, your translation, and one line on why they are
   risky: `disclaimer_body`, the final dua paragraph (quote it), `method_moon_sighting_desc` plus
   `method_umm_al_qura_desc`, the prayer names, the Hijri months, `consent_body`. (Pick the six
   that carry most weight; the owner will read those.)
2. **Every judgement call** (a word choice where two standard options exist, a sentence restructured to
   keep a placeholder alone, a hedge you had to render with a different word).
3. **Anything in the English source that looks wrong, unclear or untranslatable.** Do not fix it;
   report it.
4. **Anything you are not sure is the standard term**, as a question for a native reader.

Be honest about uncertainty. A flagged doubt is useful; a confident guess is a trap.
