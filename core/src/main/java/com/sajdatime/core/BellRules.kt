package com.sajdatime.core

/**
 * What the bell on a prayer row changes (1.6.0 step 3, `STEP-3-DESIGN.md` §5 and §14; Ali's Q6).
 *
 * Plain maps in, maps out, so the app applies each result in one settings edit (a crash between two
 * writes would otherwise leave a prayer silent with no memory of its style) and the rule carries to
 * iPhone unchanged. Generic over the alert style, so `:core` does not need the app's `AlertStyle`.
 */
data class BellState<S>(
    /** The prayers with an alert, and its style. Absent means off. */
    val alertFor: Map<PrayerSlot, S>,
    /** The style each prayer last had before it went off, so turning it on restores it. */
    val lastStyle: Map<PrayerSlot, S>,
    /** The prayers ticked for "Reminder before prayer". */
    val reminderFor: Set<PrayerSlot>,
    /** Reminders the bell took away, which the bell (or Undo) gives back. Nothing else does. */
    val bellRemovedReminder: Set<PrayerSlot>,
)

/** The four wordings of the message shown when a bell goes off (§4). */
enum class BellMessage { OFF, OFF_AND_REMINDER, OFF_FAJR_RAMADAN, OFF_AND_REMINDER_FAJR_RAMADAN }

/** Which way a bell turned on while the phone blocks notifications goes (§14.2). */
enum class PermissionDoor { NONE, ASK, OPEN_SETTINGS }

object BellRules {

    /**
     * The bell turns [slot] off. Its style is remembered, and its "minutes before" reminder moves to
     * [BellState.bellRemovedReminder] even when [reminderMinutes] is 0, so a prayer silenced here gets
     * no heads up if the user later sets minutes. The Boolean says whether a reminder that would
     * actually have fired went off, which picks the message.
     */
    fun <S> off(state: BellState<S>, slot: PrayerSlot, reminderMinutes: Int): Pair<BellState<S>, Boolean> {
        val style = state.alertFor[slot] ?: return state to false
        val hadReminder = slot in state.reminderFor
        return state.copy(
            alertFor = state.alertFor - slot,
            lastStyle = state.lastStyle + (slot to style),
            reminderFor = state.reminderFor - slot,
            bellRemovedReminder = if (hadReminder) state.bellRemovedReminder + slot else state.bellRemovedReminder,
        ) to (hadReminder && reminderMinutes > 0)
    }

    /** The bell (or Undo) turns [slot] on: its last style, or [default], and the reminder the bell took. */
    fun <S> on(state: BellState<S>, slot: PrayerSlot, default: S): BellState<S> {
        if (slot in state.alertFor) return state
        val giveBack = slot in state.bellRemovedReminder
        return state.copy(
            alertFor = state.alertFor + (slot to (state.lastStyle[slot] ?: default)),
            reminderFor = if (giveBack) state.reminderFor + slot else state.reminderFor,
            bellRemovedReminder = state.bellRemovedReminder - slot,
        )
    }

    /**
     * Settings' alerts dialog sets [slot] to [style] (null is Off). Off there also remembers the style,
     * so a bell turned on later restores what the user last chose, but it never touches the reminder:
     * only the bell carries Q6 (§14.9).
     */
    fun <S> setFromSettings(state: BellState<S>, slot: PrayerSlot, style: S?): BellState<S> {
        val current = state.alertFor[slot]
        return if (style == null) {
            state.copy(
                alertFor = state.alertFor - slot,
                lastStyle = if (current != null) state.lastStyle + (slot to current) else state.lastStyle,
            )
        } else {
            state.copy(alertFor = state.alertFor + (slot to style))
        }
    }

    /** Ticking or unticking [slot] in the reminder dialog is the user's own choice; the bell forgets it. */
    fun <S> setReminderFromSettings(state: BellState<S>, slot: PrayerSlot, on: Boolean): BellState<S> =
        state.copy(
            reminderFor = if (on) state.reminderFor + slot else state.reminderFor - slot,
            bellRemovedReminder = state.bellRemovedReminder - slot,
        )

    /**
     * [suhoorTonightOrToday]: the suhoor reminder is on and today or tomorrow is in Ramadan on the
     * user's calendar. Tomorrow too, because the evening before 1 Ramadan the reminder already fires
     * for the next Fajr (§14.8).
     */
    fun message(slot: PrayerSlot, reminderRemoved: Boolean, suhoorTonightOrToday: Boolean): BellMessage {
        val ramadanFajr = slot == PrayerSlot.FAJR && suhoorTonightOrToday
        return when {
            ramadanFajr && reminderRemoved -> BellMessage.OFF_AND_REMINDER_FAJR_RAMADAN
            ramadanFajr -> BellMessage.OFF_FAJR_RAMADAN
            reminderRemoved -> BellMessage.OFF_AND_REMINDER
            else -> BellMessage.OFF
        }
    }

    /**
     * The door chosen *before* launching anything (§14.2), so a second "Don't allow" is never answered by
     * sending the user to Settings. [systemCanAsk]: Android 13 or later and the permission not granted.
     * [rationale]: `shouldShowRequestPermissionRationale`. [askedBefore]: the app has asked at least once.
     * A phone below 13, or one where the permission is granted but the app's notifications are switched
     * off, can only be fixed in the phone's settings.
     */
    fun permissionDoor(blocked: Boolean, systemCanAsk: Boolean, rationale: Boolean, askedBefore: Boolean): PermissionDoor =
        when {
            !blocked -> PermissionDoor.NONE
            !systemCanAsk -> PermissionDoor.OPEN_SETTINGS
            !askedBefore || rationale -> PermissionDoor.ASK
            else -> PermissionDoor.OPEN_SETTINGS
        }
}
