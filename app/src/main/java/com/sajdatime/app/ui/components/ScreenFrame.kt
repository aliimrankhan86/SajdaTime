package com.sajdatime.app.ui.components

import com.sajdatime.app.ui.theme.Space
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/*
 * The two pieces every top-level screen shares (Quiet Glass, 17 Oct 2026), so the three tabs
 * open the same way and end the same way.
 */

/**
 * The measured height of the floating tab bar, margins included. Provided by MainScaffold,
 * which measures the bar rather than assuming it: at a large font size its labels grow and so
 * does the bar, and a fixed number would hide the last row under it.
 */
val LocalBottomBarClearance = compositionLocalOf { 88.dp }

/** The last thing in every scrolling screen: room to scroll the final row clear of the bar. */
@Composable
fun BottomBarClearance() {
    Spacer(Modifier.height(LocalBottomBarClearance.current))
}

/**
 * The large title every tab opens with, left aligned (mirrored in right to left), 20dp in from
 * the screen edge so it lines up with the content of the cards below it.
 */
@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        modifier = modifier
            .padding(start = 20.dp, end = 20.dp, top = Space.Paragraph, bottom = Space.Tight)
            .semantics { heading() },
    )
}

/**
 * The reading column (1.6, Q18 and Q20): at most 600dp wide and centred by its parent (a Column with
 * CenterHorizontally, or a Box with a Top Center alignment). On a phone in portrait it changes nothing;
 * a tablet, a phone in landscape or an unfolded foldable gets the centred column.
 */
fun Modifier.readingWidth(): Modifier = widthIn(max = 600.dp).fillMaxWidth()
