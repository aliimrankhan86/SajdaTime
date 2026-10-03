package com.sajdatime.app.data

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Optional, opt-in usage counts. The whole design is in docs/ANALYTICS_PLAN.md; the rules that
 * must never be loosened are these.
 *
 * 1. **Off means off.** Until the user opts in, nothing here talks to Firebase. The manifest
 *    also tells Firebase not to collect, because the SDK starts with the app regardless.
 * 2. **A fixed, closed set of events.** Only what [Screen], [SetupStep] and [Permission] can
 *    express can ever be sent. There is no free-text path, no user property, no timestamp.
 * 3. **Never anything that reveals religious belief.** Not the sect, madhab, calculation
 *    method, alert settings, city or coordinates, and not the *shape* of what is sent either.
 *    The madhab step is shown to Sunni users only, so it has no [SetupStep] — logging it, or
 *    any step only one sect sees, would reveal the sect. A test enforces this.
 *
 * Adding any event, parameter or user property needs the owner's agreement and a privacy
 * policy change first.
 */
interface UsageSink {
    fun setCollection(enabled: Boolean)
    fun reset()
    fun event(name: String, params: Map<String, String>)
}

/** The main screens. Wire names are fixed words, never anything the user typed or chose. */
enum class Screen(val wire: String) { TIMES("times"), QIBLA("qibla"), SETTINGS("settings") }

/**
 * Setup steps every user passes through. **Deliberately no MADHAB** (Sunni users only), and no
 * WELCOME or CONSENT (they come before the user has agreed to anything).
 */
enum class SetupStep(val wire: String) {
    PERMISSION("permission"),
    SECT("sect"),
    METHOD("method"),
    CONFIRM("confirm"),
    FINISH("finish"),
}

/** Whether a permission is allowed. Only ever a yes or no, never the location itself. */
enum class Permission(val wire: String) {
    NOTIFICATIONS("notifications"),
    LOCATION("location"),

    /** A *state at the end of setup*, not a decision: there is no result callback for it. */
    EXACT_ALARM("exact_alarm"),
}

class UsageCounts(private val sink: UsageSink, private val allowed: Boolean) {

    @Volatile
    private var on = false
    private var lastScreen: Screen? = null
    private val seenSteps = mutableSetOf<SetupStep>()

    /**
     * Applies the user's saved choice. Safe to call with every settings change and every
     * launch. Turning it off (a real true-to-false change) also clears Firebase's local data
     * and its random ID. A user who never opted in never causes any Firebase call at all.
     */
    fun apply(enabled: Boolean) {
        if (!allowed || enabled == on) return
        on = enabled
        if (enabled) {
            sink.setCollection(true)
        } else {
            sink.setCollection(false)
            sink.reset()
            lastScreen = null
            seenSteps.clear()
        }
    }

    /** One view per change of main screen. A repeat of the same screen is not sent again. */
    fun screen(screen: Screen) {
        if (!on || screen == lastScreen) return
        lastScreen = screen
        sink.event("screen_view", mapOf("screen_name" to screen.wire))
    }

    /** Once per step per run, so Back and recomposition cannot make one user's path look longer. */
    fun setupStep(step: SetupStep) {
        if (!on || !seenSteps.add(step)) return
        sink.event("setup_step", mapOf("step" to step.wire))
    }

    fun permission(permission: Permission, granted: Boolean) {
        if (!on) return
        sink.event(
            "permission_result",
            mapOf("permission" to permission.wire, "granted" to granted.toString()),
        )
    }
}

/**
 * The only class that touches Firebase. `lazy` means the SDK's analytics object is not even
 * requested until the user has opted in and the first call is made.
 */
class FirebaseUsageSink(context: Context) : UsageSink {
    private val analytics by lazy { FirebaseAnalytics.getInstance(context.applicationContext) }

    override fun setCollection(enabled: Boolean) = analytics.setAnalyticsCollectionEnabled(enabled)

    override fun reset() = analytics.resetAnalyticsData()

    override fun event(name: String, params: Map<String, String>) {
        val bundle = Bundle()
        params.forEach { (key, value) -> bundle.putString(key, value) }
        analytics.logEvent(name, bundle)
    }
}
