package com.sajdatime.core

/**
 * Unicode isolation for text the app did not write.
 *
 * Every string in `values/` is authored by us and, the day a translation ships, by a
 * translator — so it is in the same language as the sentence around it and the bidi
 * algorithm has nothing to get wrong. City names are the exception. They arrive from the
 * Open-Meteo geocoder or from whatever the user typed, and they are usually Latin script
 * *even for a user reading the app in Arabic or Urdu*: someone in Lahore sees "Lahore,
 * Pakistan" because that is what the geocoder returns.
 *
 * A Latin run inside a right-to-left sentence is exactly the case the Unicode bidirectional
 * algorithm resolves by looking at the characters on either side of it, and the characters
 * on either side are the translator's. So the comma inside "Cairo, Egypt", and the full stop
 * that ends the sentence containing it, get reordered — the same fault [AppLocale] already
 * documents for digits, where "17 Safar 1448" came out as "١٤٤٨ Safar ١٧" and read as a
 * different date. A mangled city name is the milder version of the same bug, but it lands in
 * the PDF a user prints and pins up, so it is worth the two characters.
 *
 * FSI (U+2068) opens an isolate whose direction is auto-detected from its first strong
 * character; PDI (U+2069) closes it. Inside the isolate the run resolves on its own, and to
 * the surrounding text the whole thing counts as one neutral object that cannot be split.
 *
 * Deliberately not `androidx.core.text.BidiFormatter`, which does the same job: it needs a
 * `Context` to pick a locale, it cannot run in a JVM unit test, and it adds nothing here
 * because FSI auto-detects direction rather than needing to be told. Two characters and no
 * Android dependency means `core` keeps building for the watch and for the JVM, and
 * `BidiTest` can assert the behaviour without an emulator.
 *
 * In English this is invisible: both characters are zero-width and an all-Latin sentence
 * resolves identically with or without them. It cannot regress the app as it ships today.
 */
private const val FIRST_STRONG_ISOLATE = '⁨'
private const val POP_DIRECTIONAL_ISOLATE = '⁩'

/**
 * Wraps externally-sourced text so the surrounding sentence cannot reorder it.
 *
 * Blank stays blank: isolating nothing would put two invisible characters where a caller is
 * about to substitute a fallback string, and `ifBlank {}` would then no longer see it.
 */
fun String.bidiIsolated(): String =
    if (isBlank()) this else "$FIRST_STRONG_ISOLATE$this$POP_DIRECTIONAL_ISOLATE"
