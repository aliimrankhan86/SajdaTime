package com.sajdatime.app.ui.settings

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R
import com.sajdatime.app.ui.theme.ThemeChoice
import com.sajdatime.app.ui.theme.sajdaSurface
import kotlin.math.roundToInt

/*
 * DEBUG BUILD ONLY. Three candidate looks for the Settings screen, drawn from exactly the
 * rows, strings and handlers the shipping screen builds (see SettingsItem in SettingsScreen.kt).
 * Picked on an emulator with SettingsLookActivity. Nothing here can reach a release build.
 *
 * Rules all three keep: Material 3 roles only (no hex), the system font and the existing type
 * scale (weights adjusted, nothing below 14sp), start/end padding so right-to-left mirrors,
 * 48dp touch targets, and every row the shipping screen has.
 */

internal val MinimalLook: SettingsRenderer = { title, banners, sections -> MinimalSettings(title, banners, sections) }
internal val RefinedLook: SettingsRenderer = { title, banners, sections -> RefinedSettings(title, banners, sections) }
internal val PremiumLook: SettingsRenderer = { title, banners, sections -> PremiumSettings(title, banners, sections) }

// --- shared --------------------------------------------------------------------------------

/** Sentence case section titles. The shipping strings are capitals, written that way by the translators. */
@Composable
private fun sectionTitle(id: SettingsSectionId): String = stringResource(
    when (id) {
        SettingsSectionId.PRAYER_TIMES -> R.string.preview_group_prayer_times
        SettingsSectionId.APPEARANCE -> R.string.preview_group_appearance
        SettingsSectionId.REMINDERS -> R.string.preview_group_reminders
        SettingsSectionId.ABOUT -> R.string.preview_group_about
    },
)

/** Every row gets an icon, so text starts on one line down the whole screen. */
private fun iconFor(item: SettingsItem): ImageVector = when (item) {
    is SettingsItem.Link -> item.icon
    is SettingsItem.Toggle -> item.icon
    is SettingsItem.Theme -> Icons.Outlined.Contrast
} ?: when (item.key) {
    "version" -> Icons.Outlined.PhoneAndroid
    "disclaimer" -> Icons.Outlined.Info
    "privacy" -> Icons.Outlined.Lock
    "feedback" -> Icons.Outlined.MailOutline
    "usage_counts" -> Icons.Outlined.BarChart
    "charity" -> Icons.Outlined.FavoriteBorder
    "credits" -> Icons.Outlined.Person
    else -> Icons.Outlined.Code
}

/** Rows that only inform, folded away behind their title so About stops being a wall of text. */
private fun SettingsItem.isFoldable() = this is SettingsItem.Link && onClick == null && key in setOf("charity", "credits", "data")

private val themeLabels = mapOf(
    ThemeChoice.SYSTEM to R.string.theme_system,
    ThemeChoice.LIGHT to R.string.theme_light,
    ThemeChoice.DARK to R.string.theme_dark,
)

private val themeIcons = mapOf(
    ThemeChoice.SYSTEM to Icons.Outlined.BrightnessAuto,
    ThemeChoice.LIGHT to Icons.Outlined.LightMode,
    ThemeChoice.DARK to Icons.Outlined.DarkMode,
)

@Composable
private fun ScreenTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp)
            .semantics { heading() },
    )
}

@Composable
private fun Chevron(tint: Color = MaterialTheme.colorScheme.outline) {
    Spacer(Modifier.width(12.dp))
    Icon(
        Icons.AutoMirrored.Outlined.ArrowForwardIos,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(14.dp),
    )
}

@Composable
private fun CheckSwitch(checked: Boolean) {
    Switch(
        checked = checked,
        onCheckedChange = null,
        // The tick is the second signal for "on", beside the track colour.
        thumbContent = if (checked) {
            { Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(SwitchDefaults.IconSize)) }
        } else {
            null
        },
    )
}

/**
 * A row that is one tap away from its text. Expands in place, several can be open at once,
 * and a screen reader hears "expanded" or "collapsed".
 */
@Composable
private fun FoldRow(
    item: SettingsItem.Link,
    textStart: Dp,
    padding: Dp,
    leading: @Composable () -> Unit,
    titleStyle: androidx.compose.ui.text.TextStyle,
) {
    var open by rememberSaveable(item.key) { mutableStateOf(false) }
    val turn by animateFloatAsState(if (open) 180f else 0f, tween(200), label = "fold")
    val stateText = stringResource(if (open) R.string.preview_expanded else R.string.preview_collapsed)
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button) { open = !open }
                .semantics { stateDescription = stateText }
                .heightIn(min = 60.dp)
                .padding(horizontal = padding, vertical = 12.dp),
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
            Text(
                text = item.subtitle.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = textStart, end = padding, bottom = 14.dp),
            )
        }
    }
}

/**
 * A three-way choice with a thumb that slides to the selected option. Each option is a
 * radio button to a screen reader, and the set is one group. Labels wrap rather than clip
 * at large font sizes, and the thumb grows with them.
 */
@Composable
private fun SlidingSelector(
    current: ThemeChoice,
    onSelect: (ThemeChoice) -> Unit,
    track: Color,
    thumb: Color,
    raisedThumb: Boolean,
    selectedText: Color,
    unselectedText: Color,
    withIcons: Boolean,
    modifier: Modifier = Modifier,
) {
    val options = ThemeChoice.entries
    val index = options.indexOf(current)
    val position by animateFloatAsState(index.toFloat(), tween(240), label = "thumb")
    var width by remember { mutableIntStateOf(0) }
    val inset = 4.dp
    val insetPx = with(LocalDensity.current) { inset.roundToPx() }
    val pill = RoundedCornerShape(50)
    Box(
        modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(pill)
            .background(track)
            .onSizeChanged { width = it.width }
            .padding(inset)
            .selectableGroup(),
    ) {
        val step = (width - 2 * insetPx) / options.size.toFloat()
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(1f / options.size)
                // offset {} is direction-aware: in right-to-left the thumb slides the other way.
                .offset { IntOffset((step * position).roundToInt(), 0) }
                .then(if (raisedThumb) Modifier.sajdaSurface(pill, thumb) else Modifier.clip(pill).background(thumb)),
        )
        Row(Modifier.fillMaxWidth()) {
            options.forEach { choice ->
                val selected = choice == current
                val color = if (selected) selectedText else unselectedText
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clip(pill)
                        .selectable(selected = selected, role = Role.RadioButton) { onSelect(choice) }
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                ) {
                    if (withIcons) {
                        Icon(themeIcons.getValue(choice), contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.height(2.dp))
                    }
                    Text(
                        text = stringResource(themeLabels.getValue(choice)),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        ),
                        color = color,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

// --- Option A: Clean Minimal ---------------------------------------------------------------
// No cards. One flat list on the page, rows separated by hairlines inset to the text, sections
// separated by space and a green sentence case title. The least decoration that still groups.

@Composable
private fun MinimalSettings(title: String, banners: @Composable () -> Unit, sections: List<SettingsSection>) {
    val scheme = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 40.dp)) {
        ScreenTitle(title)
        banners()
        sections.forEach { section ->
            Text(
                text = sectionTitle(section.id),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = scheme.primary,
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 4.dp)
                    .semantics { heading() },
            )
            section.items.forEachIndexed { i, item ->
                if (i > 0) HorizontalDivider(Modifier.padding(start = 64.dp), color = scheme.outlineVariant)
                MinimalItem(item)
            }
        }
    }
}

@Composable
private fun MinimalItem(item: SettingsItem) {
    val scheme = MaterialTheme.colorScheme
    val icon: @Composable () -> Unit = {
        Icon(iconFor(item), contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(20.dp))
    }
    when (item) {
        is SettingsItem.Theme -> Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon()
                Text(item.title, style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.height(12.dp))
            // The stock Material 3 segmented button: familiar, outlined, a tick on the chosen one.
            // Full width under the title, not indented to the text: indented, "Follow phone"
            // wrapped onto two lines at the default font size.
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                ThemeChoice.entries.forEachIndexed { i, choice ->
                    SegmentedButton(
                        selected = choice == item.current,
                        onClick = { item.onSelect(choice) },
                        shape = SegmentedButtonDefaults.itemShape(i, ThemeChoice.entries.size),
                        label = { Text(stringResource(themeLabels.getValue(choice)), textAlign = TextAlign.Center) },
                    )
                }
            }
        }

        else -> {
            val onClick = (item as? SettingsItem.Link)?.onClick
            val toggle = item as? SettingsItem.Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        when {
                            toggle != null -> Modifier.clickable(role = Role.Switch) { toggle.onCheckedChange(!toggle.checked) }
                            onClick != null -> Modifier.clickable(role = Role.Button, onClick = onClick)
                            else -> Modifier
                        },
                    )
                    .heightIn(min = 64.dp)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
            ) {
                icon()
                val subtitle = (item as? SettingsItem.Link)?.subtitle ?: toggle?.subtitle
                if (item.key == "version") {
                    Text(item.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text(subtitle.orEmpty(), style = MaterialTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
                } else {
                    Column(Modifier.weight(1f)) {
                        Text(item.title, style = MaterialTheme.typography.bodyLarge)
                        subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant) }
                    }
                    if (toggle != null) {
                        Spacer(Modifier.width(12.dp))
                        Switch(checked = toggle.checked, onCheckedChange = null)
                    } else if (onClick != null) {
                        Chevron()
                    }
                }
            }
        }
    }
}

// --- Option B: Modern Refined --------------------------------------------------------------
// Inset grouped cards, a tinted tile behind each icon, medium weight titles, hairlines inside
// each card, a sliding theme selector, and the three information-only rows folded away.

private val RefinedTextStart = 66.dp // 16 padding + 36 tile + 14 gap

@Composable
private fun RefinedSettings(title: String, banners: @Composable () -> Unit, sections: List<SettingsSection>) {
    val scheme = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 40.dp)) {
        ScreenTitle(title)
        banners()
        sections.forEach { section ->
            Text(
                text = sectionTitle(section.id),
                style = MaterialTheme.typography.labelLarge,
                color = scheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 8.dp)
                    .semantics { heading() },
            )
            Column(
                Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .sajdaSurface(RoundedCornerShape(24.dp)),
            ) {
                section.items.forEachIndexed { i, item ->
                    if (i > 0) HorizontalDivider(Modifier.padding(start = RefinedTextStart), color = scheme.outlineVariant)
                    RefinedItem(item)
                }
            }
        }
    }
}

@Composable
private fun IconTile(icon: ImageVector, background: Color, tint: Color, shape: androidx.compose.ui.graphics.Shape, size: Dp) {
    Box(Modifier.size(size).clip(shape).background(background), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun RefinedItem(item: SettingsItem) {
    val scheme = MaterialTheme.colorScheme
    val leading: @Composable () -> Unit = {
        IconTile(iconFor(item), scheme.secondaryContainer, scheme.onSecondaryContainer, RoundedCornerShape(10.dp), 36.dp)
        Spacer(Modifier.width(14.dp))
    }
    val titleStyle = MaterialTheme.typography.titleMedium
    when {
        item is SettingsItem.Theme -> Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                leading()
                Text(item.title, style = titleStyle)
            }
            Spacer(Modifier.height(12.dp))
            SlidingSelector(
                current = item.current,
                onSelect = item.onSelect,
                track = scheme.surfaceContainerHigh,
                thumb = scheme.surface,
                raisedThumb = true,
                selectedText = scheme.onSurface,
                unselectedText = scheme.onSurfaceVariant,
                withIcons = false,
            )
        }

        item.isFoldable() -> FoldRow(item as SettingsItem.Link, RefinedTextStart, 16.dp, leading, titleStyle)

        else -> StandardRow(item, leading, titleStyle, horizontal = 16.dp, checkSwitch = true)
    }
}

/** The plain row B and C share: leading mark, title over subtitle, then a chevron or a switch. */
@Composable
private fun StandardRow(
    item: SettingsItem,
    leading: @Composable () -> Unit,
    titleStyle: androidx.compose.ui.text.TextStyle,
    horizontal: Dp,
    checkSwitch: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    val onClick = (item as? SettingsItem.Link)?.onClick
    val toggle = item as? SettingsItem.Toggle
    val subtitle = (item as? SettingsItem.Link)?.subtitle ?: toggle?.subtitle
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                when {
                    toggle != null -> Modifier.clickable(role = Role.Switch) { toggle.onCheckedChange(!toggle.checked) }
                    onClick != null -> Modifier.clickable(role = Role.Button, onClick = onClick)
                    else -> Modifier
                },
            )
            .heightIn(min = 64.dp)
            .padding(horizontal = horizontal, vertical = 12.dp),
    ) {
        leading()
        if (item.key == "version") {
            Text(item.title, style = titleStyle, modifier = Modifier.weight(1f))
            Text(subtitle.orEmpty(), style = MaterialTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
            return@Row
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(item.title, style = titleStyle)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant) }
        }
        if (toggle != null) {
            Spacer(Modifier.width(12.dp))
            if (checkSwitch) CheckSwitch(toggle.checked) else Switch(checked = toggle.checked, onCheckedChange = null)
        } else if (onClick != null) {
            Chevron()
        }
    }
}

// --- Option C: Premium Contemporary --------------------------------------------------------
// The setup that decides every prayer time becomes one green summary card at the top, values
// first and large. Below it, cards with round green icon badges, stronger section titles, a
// filled sliding theme selector with icons, folded information rows and the version as a footer.

private val PremiumTextStart = 72.dp // 16 padding + 40 badge + 16 gap

@Composable
private fun PremiumSettings(title: String, banners: @Composable () -> Unit, sections: List<SettingsSection>) {
    val scheme = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 40.dp)) {
        ScreenTitle(title)
        banners()
        sections.forEach { section ->
            if (section.id == SettingsSectionId.PRAYER_TIMES) {
                Spacer(Modifier.height(12.dp))
                PrayerSetupCard(sectionTitle(section.id), section.items.filterIsInstance<SettingsItem.Link>())
                return@forEach
            }
            Text(
                text = sectionTitle(section.id),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = scheme.onSurface,
                modifier = Modifier
                    .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 10.dp)
                    .semantics { heading() },
            )
            val version = section.items.firstOrNull { it.key == "version" } as? SettingsItem.Link
            val rows = section.items.filter { it.key != "version" }
            Column(
                Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .sajdaSurface(RoundedCornerShape(28.dp)),
            ) {
                rows.forEachIndexed { i, item ->
                    if (i > 0) HorizontalDivider(Modifier.padding(start = PremiumTextStart), color = scheme.outlineVariant)
                    PremiumItem(item)
                }
            }
            if (version != null) {
                Text(
                    text = stringResource(R.string.settings_value_pair, version.title, version.subtitle.orEmpty()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                )
            }
        }
    }
}

/** Location, school, method and adjustments: what is set, large, with what it is, small. */
@Composable
private fun PrayerSetupCard(heading: String, items: List<SettingsItem.Link>) {
    val scheme = MaterialTheme.colorScheme
    val ink = scheme.onPrimaryContainer
    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(scheme.primaryContainer)
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = heading,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = ink,
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp)
                .semantics { heading() },
        )
        items.forEachIndexed { i, item ->
            if (i > 0) HorizontalDivider(Modifier.padding(start = 62.dp, end = 20.dp), color = ink.copy(alpha = 0.14f))
            val lead = i == 0
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(item.onClick?.let { Modifier.clickable(role = Role.Button, onClick = it) } ?: Modifier)
                    .heightIn(min = 64.dp)
                    .padding(horizontal = 20.dp, vertical = if (lead) 14.dp else 10.dp),
            ) {
                Icon(item.icon ?: iconFor(item), contentDescription = null, tint = ink, modifier = Modifier.size(if (lead) 26.dp else 22.dp))
                Spacer(Modifier.width(if (lead) 16.dp else 20.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.title, style = MaterialTheme.typography.bodyMedium, color = ink)
                    Text(
                        text = item.subtitle.orEmpty(),
                        style = if (lead) {
                            MaterialTheme.typography.titleLarge
                        } else {
                            MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        },
                        color = ink,
                    )
                }
                if (item.onClick != null) Chevron(tint = ink)
            }
        }
    }
}

@Composable
private fun PremiumItem(item: SettingsItem) {
    val scheme = MaterialTheme.colorScheme
    val leading: @Composable () -> Unit = {
        IconTile(iconFor(item), scheme.primaryContainer, scheme.onPrimaryContainer, CircleShape, 40.dp)
        Spacer(Modifier.width(16.dp))
    }
    val titleStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
    when {
        item is SettingsItem.Theme -> Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                leading()
                Text(item.title, style = titleStyle)
            }
            Spacer(Modifier.height(14.dp))
            SlidingSelector(
                current = item.current,
                onSelect = item.onSelect,
                track = scheme.surfaceContainerHigh,
                thumb = scheme.primary,
                raisedThumb = false,
                selectedText = scheme.onPrimary,
                unselectedText = scheme.onSurfaceVariant,
                withIcons = true,
            )
        }

        item.isFoldable() -> FoldRow(item as SettingsItem.Link, PremiumTextStart, 16.dp, leading, titleStyle)

        else -> StandardRow(item, leading, titleStyle, horizontal = 16.dp, checkSwitch = true)
    }
}
