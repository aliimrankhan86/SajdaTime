package com.sajdatime.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.sajdatime.app.ui.Destination
import com.sajdatime.app.ui.GlassTabBar
import com.sajdatime.app.ui.components.splitsAWord
import com.sajdatime.app.ui.settings.SettingsItem
import com.sajdatime.app.ui.settings.SettingsList
import com.sajdatime.app.ui.settings.SettingsSection
import com.sajdatime.app.ui.settings.SettingsSectionId
import com.sajdatime.app.ui.settings.StepperRow
import com.sajdatime.app.ui.theme.SajdaTimeTheme
import com.sajdatime.app.ui.theme.ThemeChoice
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

/**
 * Two faults found on the emulator on 19 Oct 2026 that reading the code had missed: words split across
 * lines at the largest text size, and Match your mosque's plus and minus saying nothing to TalkBack.
 * Each check fails if its fix is taken out (HANDOVER §15 lessons 159 and 162).
 */
class LargeTextAndTalkBackDeviceTest {
    @get:Rule val compose = createComposeRule()

    /** The app's words in the language the test device is in, so the checks are not English only. */
    private fun word(id: Int, vararg args: Any): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *args)

    /** [content] 320dp wide at [scale] times the text size; 2.0 as `wm density 540` and `font_scale 2.0` showed it. */
    private fun atText(scale: Float, content: @Composable () -> Unit) = compose.setContent {
        val density = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = scale)) {
            SajdaTimeTheme { Box(Modifier.width(320.dp)) { content() } }
        }
    }

    private fun layoutOf(text: String): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        compose.onNode(hasText(text)).fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        return results.single()
    }

    private fun assertWhole(text: String) {
        val layout = layoutOf(text)
        val ends = (0 until layout.lineCount - 1).map { layout.getLineEnd(it) }
        assertFalse("\"$text\" is split inside a word, lines end at $ends", splitsAWord(text, ends))
    }

    @Test
    fun the_tab_labels_never_split_a_word_at_the_largest_text() {
        atText(2f) { GlassTabBar(current = Destination.SETTINGS, onSelect = {}) }
        assertWhole(word(R.string.nav_settings))
    }

    /**
     * At the phone's Small text size the label keeps its own size: it once grew to 1.18 times it
     * (review, 19 Oct 2026). Checked by width, because the layout reports the style it was given,
     * not the size auto size chose.
     */
    @Test
    fun at_small_text_the_tab_labels_keep_their_own_size() {
        val label = word(R.string.nav_settings)
        var expected = 0f
        atText(0.85f) {
            val style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                .copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            expected = androidx.compose.ui.text.rememberTextMeasurer().measure(label, style).size.width.toFloat()
            GlassTabBar(current = Destination.SETTINGS, onSelect = {})
        }
        val layout = layoutOf(label)
        assertEquals("width of \"$label\" at Small text", expected, layout.getLineRight(0) - layout.getLineLeft(0), 2f)
    }

    @Test
    fun the_theme_options_never_split_a_word_at_the_largest_text() {
        atText(2f) {
            SettingsList(
                title = "Settings",
                banners = {},
                sections = listOf(
                    SettingsSection(SettingsSectionId.APPEARANCE, "Appearance", listOf(SettingsItem.Theme("Theme", ThemeChoice.SYSTEM) {})),
                ),
            )
        }
        assertWhole(word(R.string.theme_system))
    }

    @Test
    fun match_your_mosque_tells_a_screen_reader_the_new_value() {
        compose.setContent {
            SajdaTimeTheme {
                var minutes by remember { mutableIntStateOf(0) }
                StepperRow(
                    label = "Fajr",
                    value = if (minutes == 0) stringResource(R.string.adjustment_none) else stringResource(R.string.adjustment_minutes, minutes),
                    canDecrease = true,
                    canIncrease = true,
                    onDecrease = { minutes-- },
                    onIncrease = { minutes++ },
                )
            }
        }
        val separator = word(R.string.list_separator)
        compose.onNodeWithContentDescription("Fajr" + separator + word(R.string.settings_adjustments_none))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
        compose.onNodeWithContentDescription(word(R.string.action_increase, "Fajr")).performClick()
        compose.onNodeWithContentDescription("Fajr" + separator + word(R.string.adjustment_minutes, 1)).assertExists()
    }
}
