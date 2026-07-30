package com.sajdatime.core

import android.content.Context

/**
 * Display names for the shared enums.
 *
 * These used to be English strings held on the enum constants themselves, which meant the
 * prayer names on the main screen could never be translated without reopening every
 * screen that showed one. They are resources now.
 *
 * Kept in this file rather than PrayerModels.kt on purpose: that file has no android.*
 * imports and lifts straight into an iOS or Kotlin Multiplatform target. This one is the
 * Android-only shim, and an iOS port simply supplies its own.
 *
 * Two accessors for each enum, because both are genuinely needed:
 *   [labelRes]  for Compose, via stringResource — re-reads if the language changes
 *   [label]     for notifications, the watch tile and the PDF, which have no composition
 */

val PrayerSlot.labelRes: Int
    get() = when (this) {
        PrayerSlot.FAJR -> R.string.prayer_fajr
        PrayerSlot.SUNRISE -> R.string.prayer_sunrise
        PrayerSlot.DHUHR -> R.string.prayer_dhuhr
        PrayerSlot.ASR -> R.string.prayer_asr
        PrayerSlot.MAGHRIB -> R.string.prayer_maghrib
        PrayerSlot.ISHA -> R.string.prayer_isha
    }

fun PrayerSlot.label(context: Context): String = context.getString(labelRes)

val CalcMethod.labelRes: Int
    get() = when (this) {
        CalcMethod.AUTO -> R.string.method_auto
        CalcMethod.MUSLIM_WORLD_LEAGUE -> R.string.method_muslim_world_league
        CalcMethod.EGYPTIAN -> R.string.method_egyptian
        CalcMethod.KARACHI -> R.string.method_karachi
        CalcMethod.UMM_AL_QURA -> R.string.method_umm_al_qura
        CalcMethod.DUBAI -> R.string.method_dubai
        CalcMethod.MOON_SIGHTING -> R.string.method_moon_sighting
        CalcMethod.NORTH_AMERICA -> R.string.method_north_america
        CalcMethod.KUWAIT -> R.string.method_kuwait
        CalcMethod.QATAR -> R.string.method_qatar
        CalcMethod.SINGAPORE -> R.string.method_singapore
        CalcMethod.TURKEY -> R.string.method_turkey
        CalcMethod.JAFARI -> R.string.method_jafari
        CalcMethod.TEHRAN -> R.string.method_tehran
    }

fun CalcMethod.label(context: Context): String = context.getString(labelRes)
