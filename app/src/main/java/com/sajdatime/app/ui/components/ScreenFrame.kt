package com.sajdatime.app.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp)
            .semantics { heading() },
    )
}
