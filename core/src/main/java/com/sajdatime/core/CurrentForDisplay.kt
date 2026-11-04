package com.sajdatime.core

import java.time.Instant

/**
 * The prayer a screen marks as current. Before today's Fajr the engine answers yesterday's Isha, which must not be marked: nothing is
 * current until Fajr. One rule for the phone's Times screen, the widgets and the watch (plain Kotlin, so an iPhone version can reuse it);
 * it used to be written three times (Opus review, 4 Nov 2026).
 */
fun currentForDisplay(current: PrayerSlot?, now: Instant, fajr: Instant): PrayerSlot? =
    current?.takeIf { !now.isBefore(fajr) }
