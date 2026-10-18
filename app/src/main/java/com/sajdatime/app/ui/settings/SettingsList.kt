package com.sajdatime.app.ui.settings

import com.sajdatime.app.ui.theme.Space
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R
import com.sajdatime.app.ui.components.BottomBarClearance
import com.sajdatime.app.ui.components.ScreenTitle
import com.sajdatime.app.ui.theme.ThemeChoice
import com.sajdatime.app.ui.theme.sajdaSurface
import kotlin.math.roundToInt

/*
 * How the Settings screen is drawn (Quiet Glass, chosen by the owner on 17 Oct 2026 as
 * "option B, Modern Refined", from three emulator-tested candidates).
 *
 * Why it looks like this, against the screen it replaced:
 *  - Inset grouped cards stay: grouping by a border is faster than grouping by reading.
 *  - Every row has an icon on a soft green tile, so text starts on one line down the screen.
 *    The old About group had no icons and its text edge jumped.
 *  - Titles are medium weight, descriptions regular grey: before, the two differed only in
 *    colour, so a scan landed on nothing.
 *  - Hairlines between rows, inset to the text.
 *  - Section titles in sentence case, grey: the capitals were letterspaced green shouting.
 *  - "Free, forever", "Made by" and "Data and libraries" fold behind their titles, because
 *    About was eight equal rows of paragraphs and read like a document, not a menu.
 *  - The theme is a sliding three way selector, not three chips that looked like filters.
 *
 * Rejected: no cards at all ("Clean Minimal": grouping then rests on spacing alone, and its
 * stock segmented button wrapped "Follow phone" at a large font); a green summary card for the
 * prayer setup ("Premium Contemporary": primaryContainer means next prayer and facing the
 * Qibla, and using it here dilutes that). Screenshots of all three: HANDOVER §11.
 */

@Composable
internal fun SettingsList(
    title: String,
    banners: @Composable () -> Unit,
    sections: List<SettingsSection>,
) {
    val scheme = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenTitle(title)
        banners()
        sections.forEach { section ->
            Text(
                text = section.title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = scheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(start = 32.dp, end = 32.dp, top = Space.Section, bottom = Space.Related)
                    .semantics { heading() },
            )
            Column(
                Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .sajdaSurface(RoundedCornerShape(24.dp)),
            ) {
                section.items.forEachIndexed { i, item ->
                    if (i > 0) HorizontalDivider(Modifier.padding(start = TextStart), color = scheme.outlineVariant)
                    SettingsItemRow(item)
                }
            }
        }
        Spacer(Modifier.height(Space.Group))
        BottomBarClearance()
    }
}

/** 16 padding + 36 tile + 14 gap: where every row's text starts, and where dividers begin. */
private val TextStart = 66.dp

/** Every row gets an icon. The About rows have none in the data, so they get one here. */
private fun iconFor(item: SettingsItem): ImageVector = when (item) {
    is SettingsItem.Link -> item.icon
    is SettingsItem.Toggle -> item.icon
    is SettingsItem.Theme -> Icons.Outlined.Contrast
} ?: when (item.key) {
    "version" -> Icons.Outlined.PhoneAndroid
    "disclaimer" -> Icons.Outlined.Info
    "privacy" -> Icons.Outlined.Lock
    "feedback" -> Icons.Outlined.MailOutline
    "rate" -> Icons.Outlined.StarOutline
    "share" -> Icons.Outlined.Share
    "usage_counts" -> Icons.Outlined.BarChart
    "charity" -> Icons.Outlined.FavoriteBorder
    "credits" -> Icons.Outlined.Person
    "adhan" -> Icons.Outlined.MusicNote
    else -> Icons.Outlined.Code
}

/** Rows that only inform, folded behind their title. Nothing that does something is folded. */
private fun SettingsItem.isFoldable() =
    this is SettingsItem.Link && onClick == null && key in setOf("charity", "credits", "data", "adhan")

@Composable
private fun SettingsItemRow(item: SettingsItem) {
    val scheme = MaterialTheme.colorScheme
    val leading: @Composable () -> Unit = {
        Box(
            Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(scheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(iconFor(item), contentDescription = null, tint = scheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
    }
    val titleStyle = MaterialTheme.typography.titleMedium
    when {
        item is SettingsItem.Theme -> Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                leading()
                Text(item.title, style = titleStyle)
            }
            Spacer(Modifier.height(Space.Paragraph))
            ThemeSelector(item.current, item.onSelect)
        }

        item.isFoldable() -> FoldRow(item as SettingsItem.Link, leading, titleStyle)

        else -> PlainRow(item, leading, titleStyle)
    }
}

/** Leading tile, title over description, then a chevron (opens something) or a switch. */
@Composable
private fun PlainRow(item: SettingsItem, leading: @Composable () -> Unit, titleStyle: TextStyle) {
    val scheme = MaterialTheme.colorScheme
    val onClick = (item as? SettingsItem.Link)?.onClick
    val toggle = item as? SettingsItem.Toggle
    val subtitle = (item as? SettingsItem.Link)?.subtitle ?: toggle?.subtitle
    val haptics = LocalHapticFeedback.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                when {
                    // toggleable, not clickable: a clickable with a Switch role never told a
                    // screen reader whether it was on or off (research D3).
                    toggle != null -> Modifier.toggleable(value = toggle.checked, role = Role.Switch) { on ->
                        haptics.performHapticFeedback(if (on) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
                        toggle.onCheckedChange(on)
                    }
                    onClick != null -> Modifier.clickable(role = Role.Button, onClick = onClick)
                    else -> Modifier
                },
            )
            .heightIn(min = 64.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        leading()
        // The version is a value, not a sentence: one line, title and number at either end.
        if (item.key == "version") {
            Text(item.title, style = titleStyle, modifier = Modifier.weight(1f))
            Text(subtitle.orEmpty(), style = MaterialTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
            return@Row
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.Tight)) {
            Text(item.title, style = titleStyle)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant) }
        }
        if (toggle != null) {
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = toggle.checked,
                onCheckedChange = null,
                // The tick is a second signal for "on", beside the track colour.
                thumbContent = if (toggle.checked) {
                    { Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize)) }
                } else {
                    null
                },
            )
        } else if (onClick != null) {
            Spacer(Modifier.width(12.dp))
            Icon(
                Icons.AutoMirrored.Outlined.ArrowForwardIos,
                contentDescription = null,
                tint = scheme.outline,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

/**
 * A row that opens in place to show its text. Several can be open at once, and a screen
 * reader hears "expanded" or "collapsed".
 */
@Composable
private fun FoldRow(item: SettingsItem.Link, leading: @Composable () -> Unit, titleStyle: TextStyle) {
    var open by rememberSaveable(item.key) { mutableStateOf(false) }
    val turn by animateFloatAsState(if (open) 180f else 0f, tween(200), label = "fold")
    val stateText = stringResource(if (open) R.string.state_expanded else R.string.state_collapsed)
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button) { open = !open }
                .semantics { stateDescription = stateText }
                .heightIn(min = 60.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            leading()
            Text(item.title, style = titleStyle, modifier = Modifier.weight(1f))
            Icon(
                Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.rotate(turn),
            )
        }
        AnimatedVisibility(open) {
            Column(Modifier.padding(start = TextStart, end = 16.dp, bottom = Space.Group)) {
                Text(
                    text = item.subtitle.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // One line of inline links, in the paragraph's own type and line height, 8dp below it
                // (DESIGN_SYSTEM §2). Three 48dp rows read as loose and uneven (owner, 17 Oct 2026).
                // Inline text links are the platform's pattern for credits; a phone with no browser
                // must not crash, so the opening is caught.
                if (item.links.isNotEmpty()) {
                    val uriHandler = LocalUriHandler.current
                    val linkStyle = TextLinkStyles(
                        SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline),
                    )
                    val links = buildAnnotatedString {
                        item.links.forEachIndexed { i, (label, url) ->
                            if (i > 0) append("  ·  ")
                            withLink(
                                LinkAnnotation.Url(url, linkStyle) { runCatching { uriHandler.openUri(url) } },
                            ) { append(label.replace(' ', '\u00A0')) } // a label never breaks in two
                        }
                    }
                    Text(
                        text = links,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Space.Related),
                    )
                }
            }
        }
    }
}

/**
 * Light, dark, or follow the phone, as one choice with a thumb that slides to it.
 *
 * Here rather than in a dialog because, unlike every other setting, the result is visible the
 * instant it is tapped. Each option is a radio button to a screen reader and the set is one
 * group. Labels wrap rather than clip at a large font size, and the thumb grows with them.
 * "Follow the phone" is first and is the default; on a phone with no dark preference it
 * resolves to light, which is what the label promises.
 */
@Composable
private fun ThemeSelector(current: ThemeChoice, onSelect: (ThemeChoice) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val options = ThemeChoice.entries
    val labels = mapOf(
        ThemeChoice.SYSTEM to R.string.theme_system,
        ThemeChoice.LIGHT to R.string.theme_light,
        ThemeChoice.DARK to R.string.theme_dark,
    )
    val position by animateFloatAsState(options.indexOf(current).toFloat(), tween(240), label = "thumb")
    var width by remember { mutableIntStateOf(0) }
    val inset = 4.dp
    val insetPx = with(LocalDensity.current) { inset.roundToPx() }
    val haptics = LocalHapticFeedback.current
    val pill = RoundedCornerShape(50)
    Box(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(pill)
            .background(scheme.surfaceContainerHigh)
            .onSizeChanged { width = it.width }
            .padding(inset)
            .selectableGroup(),
    ) {
        val step = (width - 2 * insetPx) / options.size.toFloat()
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(1f / options.size)
                // offset {} follows the layout direction: in right to left the thumb slides left.
                .offset { IntOffset((step * position).roundToInt(), 0) }
                .sajdaSurface(pill, scheme.surface),
        )
        Row(Modifier.fillMaxWidth()) {
            options.forEach { choice ->
                val selected = choice == current
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clip(pill)
                        .selectable(selected = selected, role = Role.RadioButton) {
                            if (!selected) {
                                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                onSelect(choice)
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = stringResource(labels.getValue(choice)),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        ),
                        color = if (selected) scheme.onSurface else scheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
