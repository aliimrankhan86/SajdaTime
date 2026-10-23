package com.sajdatime.app

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.sajdatime.app.ui.settings.SettingsItem
import com.sajdatime.app.ui.settings.SettingsList
import com.sajdatime.app.ui.settings.SettingsSection
import com.sajdatime.app.ui.settings.SettingsSectionId
import com.sajdatime.app.ui.theme.SajdaTimeTheme
import org.junit.Rule
import org.junit.Test

/**
 * What a screen reader is given for a Settings switch row (research D3, 17 Oct 2026). TalkBack
 * reads this semantics tree; the emulator's TalkBack does not log its speech, so the tree is
 * asserted directly. Before the fix the row was a clickable with a Switch role and no state.
 */
class SettingsSemanticsDeviceTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun a_switch_row_is_one_toggleable_stop_that_reports_on_and_off() {
        compose.setContent {
            SajdaTimeTheme {
                var on by remember { mutableStateOf(false) }
                SettingsList(
                    title = "Settings",
                    banners = {},
                    sections = listOf(
                        SettingsSection(
                            SettingsSectionId.REMINDERS,
                            "Reminders",
                            listOf(SettingsItem.Toggle("ongoing", "Next prayer in notifications", "A silent notification", on, { on = it })),
                        ),
                    ),
                )
            }
        }
        val row = compose.onNode(isToggleable() and hasText("Next prayer in notifications") and hasClickAction())
        row.assertIsOff()
        row.performClick()
        row.assertIsOn()
    }
}
