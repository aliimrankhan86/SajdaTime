package com.sajdatime.app.review

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.google.android.play.core.review.ReviewManagerFactory
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * When the app may show Google Play's own rating card (owner's decision, 17 Oct 2026, which
 * reversed the old "no rate prompt" rule; research in build/ux-research-input/FINDINGS.md §3).
 *
 * He asked for a weekly reminder "or other best practice". The research says no to weekly, on
 * Google's own words: asking "more than once during a short period of time (for example, less
 * than a month) might not always display a dialog", the app is never told whether it showed, and
 * Play policy names forced or deceptive rating pop ups. So: Google's card only, no copy of ours
 * (nothing to translate, no religious pressure possible), at a calm moment, rarely.
 *
 * Deliberately NOT here, and must not be added:
 *  - a "Do you like the app?" question first. Google forbids asking anything before the card.
 *  - a button that calls the card. Google says no; Settings opens the store page instead.
 *  - a usage counts event. The closed event list is the owner's, and the Console shows ratings.
 *  - gating on the user's settings or opinions. Only the moment is chosen, never the person.
 *
 * Pure rules live in [ReviewTiming] so they are unit tested; this file is the storage and the call.
 */
object ReviewPrompt {

    /**
     * Called by the Times screen once it has been on screen, settled, for a few seconds.
     * [calmScreen] is the caller's word that no sheet, dialog or warning notice is showing.
     * Never throws, never blocks: the card is a nicety and the app must not notice its absence.
     */
    fun onTimesShown(activity: Activity, calmScreen: Boolean, todayTimes: List<Instant>, fajr: Instant?, sunrise: Instant?) {
        runCatching {
            val prefs = prefs(activity)
            val today = LocalDate.now(ZoneId.systemDefault()).toEpochDay()
            val edit = prefs.edit()
            if (!prefs.contains(ELIGIBLE_FROM)) edit.putLong(ELIGIBLE_FROM, today)
            if (prefs.getLong(LAST_OPEN_DAY, -1) != today) {
                edit.putLong(LAST_OPEN_DAY, today)
                edit.putInt(OPEN_DAYS, (prefs.getInt(OPEN_DAYS, 0) + 1).coerceAtMost(10))
            }
            edit.apply()

            val due = ReviewTiming.isDue(
                today = today,
                eligibleFrom = prefs.getLong(ELIGIBLE_FROM, today),
                openDays = prefs.getInt(OPEN_DAYS, 0),
                askCount = prefs.getInt(ASK_COUNT, 0),
                lastAskedDay = prefs.getLong(LAST_ASKED_DAY, Long.MIN_VALUE / 2),
            )
            if (!due || !calmScreen || !ReviewTiming.isCalmMoment(Instant.now(), todayTimes, fajr, sunrise)) return

            // Counted before the call: Google never says whether the card showed, so every
            // attempt is an ask.
            prefs.edit()
                .putInt(ASK_COUNT, prefs.getInt(ASK_COUNT, 0) + 1)
                .putLong(LAST_ASKED_DAY, today)
                .apply()

            val manager = ReviewManagerFactory.create(activity)
            manager.requestReviewFlow().addOnCompleteListener { task ->
                // No Play Store, sideloaded, offline, quota spent: all land here as a failure
                // or a no-op card, and Google's instruction is to change nothing for the user.
                if (task.isSuccessful && !activity.isFinishing && !activity.isDestroyed) {
                    runCatching { manager.launchReviewFlow(activity, task.result) }
                }
            }
        }
    }

    /** The Settings row. The store app if there is one, the web page if not. */
    fun openStorePage(context: Context) {
        val id = "com.sajdatime.app"
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$id".toUri())) }
            .recoverCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$id".toUri()))
            }
    }

    // SharedPreferences rather than the settings DataStore: five counters nobody else reads, and
    // the sharedpref domain is already excluded from backup and transfer (data_extraction_rules.xml),
    // so they stay on the phone, survive updates and reset on reinstall like everything else.
    private fun prefs(context: Context) = context.getSharedPreferences("review_timing", Context.MODE_PRIVATE)

    private const val ELIGIBLE_FROM = "eligible_from_day"
    private const val OPEN_DAYS = "open_days"
    private const val LAST_OPEN_DAY = "last_open_day"
    private const val ASK_COUNT = "ask_count"
    private const val LAST_ASKED_DAY = "last_asked_day"
}

/** The rules, with no Android in them. Every number has its reason beside it. */
object ReviewTiming {
    /** Kept this long: alerts and widgets are use too, so days kept, not days opened. */
    const val DAYS_KEPT = 14
    /** Opened on this many separate days: the smallest sign the person came back on purpose. */
    const val OPEN_DAYS = 3
    /** Never more than this many asks, ever. Apple's own yearly cap; under any likely Play quota. */
    const val MAX_ASKS = 3
    /** Days between asks: at most one a season, far outside Google's "less than a month". */
    const val DAYS_BETWEEN = 120

    fun isDue(today: Long, eligibleFrom: Long, openDays: Int, askCount: Int, lastAskedDay: Long): Boolean =
        today - eligibleFrom >= DAYS_KEPT &&
            openDays >= OPEN_DAYS &&
            askCount < MAX_ASKS &&
            today - lastAskedDay >= DAYS_BETWEEN

    /**
     * Not near a prayer: never from 20 minutes before any of today's times to 30 minutes after,
     * and never from an hour before Fajr until sunrise (the alarm and the waking up). Missing
     * times mean no location, which is never calm.
     */
    fun isCalmMoment(now: Instant, todayTimes: List<Instant>, fajr: Instant?, sunrise: Instant?): Boolean {
        if (todayTimes.isEmpty() || fajr == null || sunrise == null) return false
        if (!now.isBefore(fajr.minus(Duration.ofMinutes(60))) && now.isBefore(sunrise)) return false
        return todayTimes.none { t ->
            !now.isBefore(t.minus(Duration.ofMinutes(20))) && now.isBefore(t.plus(Duration.ofMinutes(30)))
        }
    }
}
