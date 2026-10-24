package com.tv.launcher.custom.feature.home.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Text
import com.tv.launcher.custom.R
import com.tv.launcher.custom.core.theme.LauncherTheme
import com.tv.launcher.custom.core.theme.toStyle
import com.tv.launcher.custom.feature.apps.domain.model.AppEntry
import com.tv.launcher.custom.feature.home.presentation.state.HomeIntent
import com.tv.launcher.custom.feature.theme.data.*
import com.tv.launcher.custom.feature.theme.domain.model.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private enum class SettingsCategory(@StringRes val title: Int, val icon: ImageVector) {
    THEMES(R.string.theme_picker_title, Icons.Default.GridView),
    BACKGROUND(R.string.appearance_background, Icons.Default.Wallpaper),
    APPEARANCE(R.string.settings_appearance, Icons.Default.Tune)
}

// This utility panel intentionally stays neutral even when the launcher theme changes.
internal object SettingsColors {
    val background = Color(0xFF12171E)
    val surface = Color(0xFF202731)
    val selected = Color(0xFF263242)
    val accent = Color(0xFFB9D5FF)
    val primary = Color(0xFFD9E8FD)
    val onPrimary = Color(0xFF172536)
    val text = Color(0xFFF4F6F8)
    val muted = Color(0xFFB7C0CE)
    val divider = Color.White.copy(alpha = 0.08f)
    val destructive = Color(0xFFFFB4AB)
}

@Composable
fun LauncherSettingsDialog(
    appearance: LauncherAppearance,
    onIntent: (HomeIntent) -> Unit,
    onDismiss: () -> Unit,
    onChooseBackgroundImage: () -> Unit = {},
    onImportTheme: () -> Unit = {},
    onExportTheme: () -> Unit = {},
    onOpenOnlineThemes: () -> Unit = {},
    onCreateCustomTheme: () -> Unit = {},
    installedThemes: List<InstalledTheme> = emptyList(),
    onEditInstalledTheme: (String) -> Unit = {},
    onApplyInstalledTheme: (String) -> Unit = {},
    onDeleteInstalledTheme: (String) -> Unit = {},
    apps: ImmutableList<AppEntry> = persistentListOf(),
    previewData: CanvasData = PlaceholderCanvasData
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        LauncherSettingsContent(
            appearance, onIntent, onDismiss, onChooseBackgroundImage, onImportTheme,
            onExportTheme, onOpenOnlineThemes, onCreateCustomTheme, installedThemes,
            onEditInstalledTheme, onApplyInstalledTheme, onDeleteInstalledTheme, apps, previewData
        )
    }
}

@Composable
private fun LauncherSettingsContent(
    appearance: LauncherAppearance,
    onIntent: (HomeIntent) -> Unit,
    onDismiss: () -> Unit,
    onChooseBackgroundImage: () -> Unit,
    onImportTheme: () -> Unit,
    onExportTheme: () -> Unit,
    onOpenOnlineThemes: () -> Unit,
    onCreateCustomTheme: () -> Unit,
    installedThemes: List<InstalledTheme>,
    onEditInstalledTheme: (String) -> Unit,
    onApplyInstalledTheme: (String) -> Unit,
    onDeleteInstalledTheme: (String) -> Unit,
    apps: ImmutableList<AppEntry>,
    previewData: CanvasData,
    initialCategory: SettingsCategory = SettingsCategory.THEMES
) {
    var category by remember { mutableStateOf(initialCategory) }
    var selectedThemeId by remember(appearance.installedThemeId) { mutableStateOf(appearance.installedThemeId) }
    val activeTheme = installedThemes.firstOrNull { it.id == appearance.installedThemeId }
    val activeName = activeTheme?.name ?: stringResource(appearance.themeId.toStyle().nameRes)
    val previewTheme = findSettingsPreviewTheme(installedThemes, selectedThemeId, appearance.installedThemeId)
    val isActive = previewTheme == null || previewTheme.id == appearance.installedThemeId
    val previewAppearance = if (isActive) appearance else previewTheme!!.appearance
    val previewName = previewTheme?.name ?: activeName
    var showOptions by remember { mutableStateOf(false) }
    var deleteTheme by remember { mutableStateOf<InstalledTheme?>(null) }
    val initialFocus = remember { FocusRequester() }
    val categoryState = rememberSaveableStateHolder()
    val contentFocus = remember { FocusRequester() }
    val categoryFocus = remember { SettingsCategory.entries.associateWith { FocusRequester() } }
    val isInspection = LocalInspectionMode.current
    val safePreviewData = remember(previewData) {
        object : CanvasData by previewData {
            override fun onItemClick(block: ThemeBlock, index: Int) = Unit
            override fun onNavClick(label: String) = Unit
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val compact = maxWidth < 1100.dp
        val panelPadding = if (compact) 24.dp else 32.dp
        Column(
            Modifier.fillMaxSize().background(SettingsColors.background)
                .windowInsetsPadding(WindowInsets.safeDrawing).padding(panelPadding)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.settings_personalization), color = SettingsColors.text,
                        fontSize = if (compact) 24.sp else 28.sp, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.settings_subtitle), color = SettingsColors.muted, fontSize = 14.sp)
                }
                SettingsButton(stringResource(R.string.close), icon = Icons.Default.Close, onClick = onDismiss)
            }
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(SettingsColors.divider))
            Spacer(Modifier.height(16.dp))
            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(
                    Modifier.width(if (compact) 168.dp else 208.dp).fillMaxHeight()
                        .verticalScroll(rememberScrollState()).padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SettingsCategory.entries.forEachIndexed { index, item ->
                        SettingsButton(
                            stringResource(item.title), selected = category == item,
                            icon = item.icon, containerColor = Color.Transparent,
                            modifier = Modifier.fillMaxWidth().then(
                                if (index == 0) Modifier.focusRequester(initialFocus) else Modifier
                            ).focusRequester(categoryFocus.getValue(item)).focusProperties { right = contentFocus },
                            onClick = { category = item }
                        )
                    }
                }
                Box(Modifier.fillMaxHeight().width(1.dp).background(SettingsColors.divider))
                // These short panels stay composed so D-pad entry targets remain attached after scrolling.
                categoryState.SaveableStateProvider(category.name) {
                    when (category) {
                        SettingsCategory.THEMES -> Column(
                            Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(4.dp),
                            verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 16.dp)
                        ) {
                            Column {
                                SettingsHeading(
                                    stringResource(if (isActive) R.string.settings_current_theme else R.string.theme_import_preview),
                                    if (compact) null else stringResource(R.string.settings_theme_intro)
                                )
                            }
                            Column {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                    SettingsThemePreview(
                                        previewAppearance, previewTheme?.composition, previewName,
                                        apps, safePreviewData,
                                        Modifier.weight(if (compact) 1.1f else 1.9f).aspectRatio(16f / 9f).clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, SettingsColors.divider, RoundedCornerShape(12.dp))
                                    )
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp)) {
                                        Text(previewName, color = SettingsColors.text, fontSize = if (compact) 20.sp else 24.sp,
                                            fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        if (isActive) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Icon(Icons.Default.Check, null, Modifier.size(18.dp), tint = SettingsColors.accent)
                                                Text(stringResource(R.string.settings_theme_active), color = SettingsColors.accent, fontSize = 14.sp)
                                            }
                                        }
                                        if (!compact) Text(stringResource(R.string.settings_theme_description), color = SettingsColors.muted, fontSize = 14.sp)
                                        if (!isActive) {
                                            SettingsButton(stringResource(R.string.apply_theme), primary = true,
                                                icon = Icons.Default.Check, modifier = Modifier.fillMaxWidth().focusRequester(contentFocus)
                                                    .focusProperties { left = categoryFocus.getValue(category) },
                                                onClick = { previewTheme?.let { onApplyInstalledTheme(it.id) } })
                                        } else {
                                            SettingsButton(
                                                stringResource(if (previewTheme != null) R.string.settings_edit_design else R.string.custom_layout_title),
                                                primary = true, icon = Icons.Default.Edit, modifier = Modifier.fillMaxWidth().focusRequester(contentFocus)
                                                    .focusProperties { left = categoryFocus.getValue(category) },
                                                onClick = { previewTheme?.let { onEditInstalledTheme(it.id) } ?: onCreateCustomTheme() }
                                            )
                                        }
                                        SettingsButton(stringResource(R.string.settings_more_options), icon = Icons.Default.MoreHoriz,
                                            modifier = Modifier.fillMaxWidth().focusProperties { left = categoryFocus.getValue(category) }, onClick = { showOptions = true })
                                    }
                                }
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(stringResource(R.string.settings_my_themes), fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium, color = SettingsColors.text)
                                    Text(installedThemes.size.toString(), fontSize = 14.sp, color = SettingsColors.muted)
                                }
                            }
                            if (installedThemes.isEmpty()) {
                                Text(stringResource(R.string.settings_empty_themes), fontSize = 14.sp, color = SettingsColors.muted)
                            }
                            Column {
                                LazyRow(contentPadding = PaddingValues(8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    items(installedThemes, key = { it.id }) { theme ->
                                        SettingsThemeTile(
                                            theme, theme.id == appearance.installedThemeId, theme.id == previewTheme?.id,
                                            apps, safePreviewData, onClick = { selectedThemeId = theme.id }
                                        )
                                    }
                                    item(key = "create") {
                                        SettingsVisualChoice(stringResource(R.string.custom_layout_title), selected = false,
                                            modifier = Modifier.heightIn(min = 80.dp), onClick = onCreateCustomTheme) {
                                            Icon(Icons.Default.Add, null, Modifier.size(24.dp), tint = SettingsColors.accent)
                                        }
                                    }
                                }
                            }
                            Column {
                                SettingsActionRow {
                                    SettingsButton(stringResource(R.string.import_theme_package), icon = Icons.Default.FileDownload, onClick = onImportTheme)
                                    SettingsButton(stringResource(R.string.settings_explore_themes), icon = Icons.Default.Language, onClick = onOpenOnlineThemes)
                                }
                            }
                        }
                        SettingsCategory.BACKGROUND -> Column(
                            Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(4.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SettingsHeading(stringResource(R.string.appearance_background), stringResource(R.string.settings_background_intro))
                            Column {
                                SettingsThemePreview(appearance, activeTheme?.composition,
                                    activeName, apps, safePreviewData,
                                    Modifier.fillMaxWidth(0.7f).aspectRatio(16f / 9f).clip(RoundedCornerShape(12.dp)))
                            }
                            Column {
                                SettingsActionRow {
                                    SettingsButton(stringResource(R.string.choose_background_image), icon = Icons.Default.AddPhotoAlternate,
                                        modifier = Modifier.focusRequester(contentFocus).focusProperties { left = categoryFocus.getValue(category) },
                                        onClick = onChooseBackgroundImage)
                                    if (appearance.backgroundImageUri != null || appearance.backgroundColorArgb != null) {
                                        SettingsButton(stringResource(R.string.reset_background), icon = Icons.Default.RestartAlt,
                                            onClick = { onIntent(HomeIntent.ClearCustomBackground) })
                                    }
                                }
                            }
                            SettingsSectionTitle(stringResource(R.string.settings_preset_backgrounds))
                            Column {
                                SettingsActionRow {
                                    BackgroundChoice.entries.forEach { choice ->
                                        SettingsVisualChoice(
                                            label = settingsChoiceLabel(choice.labelResource(), choice == BackgroundChoice.PROFILE),
                                            selected = appearance.background == choice && appearance.backgroundImageUri == null && appearance.backgroundColorArgb == null,
                                            onClick = { onIntent(HomeIntent.SelectBackground(choice)) }
                                        ) {
                                            Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(8.dp))
                                                .background(appearance.copy(background = choice, backgroundColorArgb = null).toStyle().palette.background)
                                                .border(1.dp, SettingsColors.muted.copy(alpha = 0.25f), RoundedCornerShape(8.dp)))
                                        }
                                    }
                                }
                            }
                            SettingsSectionTitle(stringResource(R.string.settings_solid_colors))
                            Column {
                                SettingsActionRow {
                                    listOf(
                                        R.string.settings_color_night to 0xFF101820L,
                                        R.string.background_graphite to 0xFF202124L,
                                        R.string.settings_color_slate to 0xFF263238L,
                                        R.string.background_plum to 0xFF311B2BL,
                                        R.string.settings_color_petrol to 0xFF003B46L
                                    ).forEach { (label, argb) ->
                                        SettingsVisualChoice(stringResource(label), appearance.backgroundColorArgb == argb,
                                            onClick = { onIntent(HomeIntent.SelectBackgroundColor(argb)) }) {
                                            Box(Modifier.size(40.dp).clip(CircleShape).background(Color(argb))
                                                .border(1.dp, SettingsColors.muted.copy(alpha = 0.4f), CircleShape))
                                        }
                                    }
                                }
                            }
                        }
                        SettingsCategory.APPEARANCE -> Column(
                            Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(4.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SettingsHeading(stringResource(R.string.settings_appearance), stringResource(R.string.settings_appearance_intro))
                            Column {
                                SettingsThemePreview(appearance, activeTheme?.composition,
                                    activeName, apps, safePreviewData,
                                    Modifier.fillMaxWidth(0.7f).aspectRatio(16f / 9f).clip(RoundedCornerShape(12.dp)))
                            }
                            SettingsSectionTitle(stringResource(R.string.appearance_accent))
                            Column {
                                SettingsActionRow {
                                    AccentChoice.entries.forEach { choice ->
                                        SettingsVisualChoice(settingsChoiceLabel(choice.labelResource(), choice == AccentChoice.PROFILE),
                                            appearance.accent == choice,
                                            modifier = if (choice == AccentChoice.PROFILE) Modifier.focusRequester(contentFocus)
                                                .focusProperties { left = categoryFocus.getValue(category) } else Modifier,
                                            onClick = { onIntent(HomeIntent.SelectAccent(choice)) }) {
                                            Box(Modifier.size(40.dp).clip(CircleShape)
                                                .background(appearance.copy(accent = choice, accentColorArgb = null).toStyle().palette.accent))
                                        }
                                    }
                                }
                            }
                            SettingsSectionTitle(stringResource(R.string.appearance_tile_shape))
                            Column {
                                SettingsActionRow {
                                    TileShapeChoice.entries.forEach { choice ->
                                        SettingsVisualChoice(settingsChoiceLabel(choice.labelResource(), choice == TileShapeChoice.PROFILE),
                                            appearance.tileShape == choice, onClick = { onIntent(HomeIntent.SelectTileShape(choice)) }) {
                                            Box(Modifier.size(64.dp, 40.dp)
                                                .background(SettingsColors.accent, RoundedCornerShape(appearance.copy(tileShape = choice).toStyle().tileRadius / 2)))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.settings_remote_hint), color = SettingsColors.muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(16.dp), tint = SettingsColors.muted)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.settings_back_hint), color = SettingsColors.muted, fontSize = 12.sp)
            }
        }
    }

    LaunchedEffect(Unit) { if (!isInspection) initialFocus.requestFocus() }

    if (showOptions) {
        SettingsActionDialog(previewName, onDismiss = { showOptions = false }) {
            if (previewTheme != null) {
                SettingsButton(stringResource(R.string.settings_edit_design), icon = Icons.Default.Edit,
                    modifier = Modifier.fillMaxWidth(), onClick = { showOptions = false; onEditInstalledTheme(previewTheme.id) })
            }
            if (isActive) {
                SettingsButton(stringResource(R.string.export_active_theme), icon = Icons.Default.FileUpload,
                    modifier = Modifier.fillMaxWidth(), onClick = { showOptions = false; onExportTheme() })
            }
            if (previewTheme != null) {
                SettingsButton(stringResource(R.string.delete_theme), icon = Icons.Default.DeleteOutline,
                    destructive = true, modifier = Modifier.fillMaxWidth(),
                    onClick = { showOptions = false; deleteTheme = previewTheme })
            }
        }
    }
    deleteTheme?.let { theme ->
        SettingsActionDialog(stringResource(R.string.settings_delete_title), onDismiss = { deleteTheme = null }) {
            Text(stringResource(R.string.settings_delete_message, theme.name), color = SettingsColors.muted, fontSize = 16.sp)
            SettingsButton(stringResource(R.string.delete_theme), icon = Icons.Default.DeleteOutline, destructive = true,
                modifier = Modifier.fillMaxWidth(), onClick = { deleteTheme = null; onDeleteInstalledTheme(theme.id) })
        }
    }
}

@Composable
private fun settingsChoiceLabel(@StringRes resource: Int, isThemeDefault: Boolean): String =
    stringResource(if (isThemeDefault) R.string.settings_theme_default else resource)

@Composable
private fun SettingsHeading(title: String, subtitle: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, color = SettingsColors.text, fontSize = 22.sp, fontWeight = FontWeight.Medium)
        if (subtitle != null) Text(subtitle, color = SettingsColors.muted, fontSize = 14.sp)
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(title, color = SettingsColors.text, fontSize = 20.sp, fontWeight = FontWeight.Medium)
}

@Composable
private fun SettingsActionRow(content: @Composable RowScope.() -> Unit) {
    // Small groups retain their controls while the row scrolls horizontally.
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
internal fun SettingsButton(
    label: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    primary: Boolean = false,
    destructive: Boolean = false,
    icon: ImageVector? = null,
    containerColor: Color = SettingsColors.surface,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val contentColor = when {
        primary -> SettingsColors.onPrimary
        destructive -> SettingsColors.destructive
        else -> SettingsColors.text
    }
    Button(
        onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = 48.dp).semantics { this.selected = selected },
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        shape = ButtonDefaults.shape(RoundedCornerShape(12.dp)),
        scale = ButtonDefaults.scale(focusedScale = 1.03f),
        colors = ButtonDefaults.colors(
            containerColor = when { primary -> SettingsColors.primary; selected -> SettingsColors.selected; else -> containerColor },
            contentColor = contentColor,
            focusedContainerColor = if (primary) SettingsColors.primary else SettingsColors.selected,
            focusedContentColor = contentColor
        ),
        border = ButtonDefaults.border(
            border = if (selected) Border(BorderStroke(1.dp, SettingsColors.accent)) else Border.None,
            focusedBorder = Border(BorderStroke(3.dp, SettingsColors.text), inset = (-4).dp)
        )
    ) {
        if (icon != null) {
            Icon(icon, null, Modifier.size(20.dp), tint = contentColor)
            if (label.isNotEmpty()) Spacer(Modifier.width(8.dp))
        }
        if (label.isNotEmpty()) Text(label, fontSize = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SettingsVisualChoice(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    visual: @Composable () -> Unit
) {
    Button(
        onClick = onClick, modifier = modifier.width(144.dp).semantics { this.selected = selected },
        contentPadding = PaddingValues(12.dp), shape = ButtonDefaults.shape(RoundedCornerShape(12.dp)),
        scale = ButtonDefaults.scale(focusedScale = 1.03f),
        colors = ButtonDefaults.colors(containerColor = SettingsColors.surface, contentColor = SettingsColors.text,
            focusedContainerColor = SettingsColors.selected, focusedContentColor = SettingsColors.text),
        border = ButtonDefaults.border(
            border = if (selected) Border(BorderStroke(1.dp, SettingsColors.accent)) else Border.None,
            focusedBorder = Border(BorderStroke(3.dp, SettingsColors.text), inset = (-4).dp)
        )
    ) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            visual()
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (selected) Icon(Icons.Default.Check, null, Modifier.size(16.dp), tint = SettingsColors.accent)
                Text(label, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun SettingsThemeTile(
    theme: InstalledTheme,
    isActive: Boolean,
    isSelected: Boolean,
    apps: ImmutableList<AppEntry>,
    data: CanvasData,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick, modifier = Modifier.width(144.dp).semantics { selected = isSelected },
        contentPadding = PaddingValues(0.dp), shape = ButtonDefaults.shape(RoundedCornerShape(12.dp)),
        scale = ButtonDefaults.scale(focusedScale = 1.03f),
        colors = ButtonDefaults.colors(containerColor = SettingsColors.surface, contentColor = SettingsColors.text,
            focusedContainerColor = SettingsColors.selected, focusedContentColor = SettingsColors.text),
        border = ButtonDefaults.border(
            border = if (isSelected) Border(BorderStroke(1.dp, SettingsColors.accent)) else Border.None,
            focusedBorder = Border(BorderStroke(3.dp, SettingsColors.text), inset = (-4).dp)
        )
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
            SettingsThemePreview(theme.appearance, theme.composition, theme.name, apps, data,
                Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)))
            Row(Modifier.align(Alignment.BottomStart).fillMaxWidth().background(Color(0xE612171E)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(theme.name, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                if (isActive) {
                    Icon(Icons.Default.CheckCircle, stringResource(R.string.settings_theme_active), Modifier.size(18.dp), tint = SettingsColors.accent)
                }
            }
        }
    }
}

@Composable
private fun SettingsActionDialog(title: String, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val cancelFocus = remember { FocusRequester() }
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.widthIn(max = 480.dp).clip(RoundedCornerShape(24.dp))
                .background(SettingsColors.background).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(title, color = SettingsColors.text, fontSize = 24.sp, fontWeight = FontWeight.Medium)
            content()
            SettingsButton(stringResource(R.string.cancel), modifier = Modifier.fillMaxWidth().focusRequester(cancelFocus), onClick = onDismiss)
        }
        // Destructive actions never receive initial focus.
        LaunchedEffect(Unit) { cancelFocus.requestFocus() }
    }
}

@Preview(name = "TV · Temas", widthDp = 1280, heightDp = 720)
@Composable
private fun LauncherSettingsThemesPreview() = LauncherSettingsPreviewContent(SettingsCategory.THEMES)

@Preview(name = "TV · Biblioteca vacía", widthDp = 960, heightDp = 540)
@Composable
private fun LauncherSettingsEmptyPreview() = LauncherSettingsPreviewContent(SettingsCategory.THEMES, emptyList())

@Preview(name = "TV · Fondo", widthDp = 1280, heightDp = 720)
@Composable
private fun LauncherSettingsBackgroundPreview() = LauncherSettingsPreviewContent(SettingsCategory.BACKGROUND)

@Preview(name = "TV · Apariencia", widthDp = 960, heightDp = 540, fontScale = 1.2f)
@Composable
private fun LauncherSettingsAppearancePreview() = LauncherSettingsPreviewContent(SettingsCategory.APPEARANCE)

private val settingsPreviewThemes = listOf(
    InstalledTheme("preview-aurora", "Aurora", ThemeId.ANDROID_TV,
        LauncherAppearance(themeId = ThemeId.ANDROID_TV, installedThemeId = "preview-aurora"),
        composition = ThemeComposition(
            blocks = listOf(ThemeBlock(ThemeBlockType.HERO, text = "Tu momento de desconectar.", subtitle = "Todo lo que te gusta, en un solo lugar."),
                ThemeBlock(ThemeBlockType.APPS_ROW, maxItems = 5, text = "Tus aplicaciones")),
            topBar = TopBarConfig(), bgColor = 0xFF243F4BL.toInt()
        )),
    InstalledTheme("preview-duna", "Duna", ThemeId.GOOGLE_TV,
        LauncherAppearance(themeId = ThemeId.GOOGLE_TV, backgroundColorArgb = 0xFF665E58L),
        composition = ThemeComposition(listOf(ThemeBlock(ThemeBlockType.TITLE, text = "Duna"), ThemeBlock(ThemeBlockType.APPS_GRID, maxItems = 8, columns = 4)))),
    InstalledTheme("preview-night", "Nocturno", ThemeId.ROKU, LauncherAppearance(themeId = ThemeId.ROKU))
)

@Composable
private fun LauncherSettingsPreviewContent(category: SettingsCategory, themes: List<InstalledTheme> = settingsPreviewThemes) {
    val appearance = themes.firstOrNull()?.appearance ?: LauncherAppearance()
    LauncherTheme(appearance) {
        LauncherSettingsContent(appearance, {}, {}, {}, {}, {}, {}, {}, themes, {}, {}, {},
            persistentListOf(), PlaceholderCanvasData, initialCategory = category)
    }
}
