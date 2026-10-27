package com.tv.launcher.custom.feature.settings

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.focus.onFocusChanged
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
import com.tv.launcher.custom.core.ui.R
import com.tv.launcher.custom.core.ui.theme.LauncherTheme
import com.tv.launcher.custom.core.ui.theme.toStyle
import com.tv.launcher.custom.core.model.apps.AppEntry
import com.tv.launcher.custom.core.ui.components.*
import com.tv.launcher.custom.core.ui.labelResource
import com.tv.launcher.custom.core.model.theme.*
import com.tv.launcher.custom.core.theme.PlaceholderCanvasData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private enum class SettingsCategory(@StringRes val title: Int, val icon: ImageVector) {
    HOME(R.string.launcher_home, Icons.Default.Home),
    THEMES(R.string.theme_picker_title, Icons.Default.GridView),
    BACKGROUND(R.string.appearance_background, Icons.Default.Wallpaper),
    APPEARANCE(R.string.settings_appearance, Icons.Default.Tune),
    WEATHER(R.string.weather_settings_title, Icons.Default.Cloud),
    DEVICE_TOOLS(R.string.device_tools_title, Icons.Default.Build)
}

@Composable
fun LauncherSettingsDialog(
    appearance: LauncherAppearance,
    onIntent: (AppearanceAction) -> Unit,
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
    previewData: CanvasData = PlaceholderCanvasData,
    onOpenDeviceTools: () -> Unit = {},
    onOpenWeatherSettings: () -> Unit = {},
    weatherContent: (@Composable () -> Unit)? = null,
    deviceToolsContent: (@Composable () -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        LauncherSettingsContent(
            appearance, onIntent, onDismiss, onChooseBackgroundImage, onImportTheme,
            onExportTheme, onOpenOnlineThemes, onCreateCustomTheme, installedThemes,
            onEditInstalledTheme, onApplyInstalledTheme, onDeleteInstalledTheme, apps, previewData,
            onOpenDeviceTools, onOpenWeatherSettings,
            weatherContent = weatherContent, deviceToolsContent = deviceToolsContent
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun LauncherSettingsContent(
    appearance: LauncherAppearance,
    onIntent: (AppearanceAction) -> Unit,
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
    onOpenDeviceTools: () -> Unit,
    onOpenWeatherSettings: () -> Unit,
    initialCategory: SettingsCategory = SettingsCategory.HOME,
    weatherContent: (@Composable () -> Unit)? = null,
    deviceToolsContent: (@Composable () -> Unit)? = null
) {
    var category by remember { mutableStateOf(initialCategory) }
    val isThemeLibraryVisible = category == SettingsCategory.THEMES
    BackHandler(enabled = isThemeLibraryVisible) { category = SettingsCategory.HOME }
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
    val themeFocusRequesters = remember(installedThemes.map { it.id }) {
        installedThemes.associate { it.id to FocusRequester() }
    }
    val categoryFocus = remember { SettingsCategory.entries.associateWith { FocusRequester() } }
    val isInspection = LocalInspectionMode.current
    LaunchedEffect(isThemeLibraryVisible) {
        if (!isInspection && category == SettingsCategory.THEMES) contentFocus.requestFocus()
    }
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
                    Text(stringResource(R.string.settings_active_theme_name, activeName), color = SettingsColors.muted, fontSize = 14.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsButton(stringResource(R.string.import_theme_package), icon = Icons.Default.FileDownload,
                        onClick = onImportTheme)
                    SettingsButton(stringResource(R.string.settings_explore_themes), icon = Icons.Default.Language,
                        onClick = onOpenOnlineThemes)
                }
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
                        if (item == SettingsCategory.DEVICE_TOOLS) {
                            Text(stringResource(R.string.settings_advanced), color = SettingsColors.muted,
                                fontSize = 12.sp, modifier = Modifier.padding(top = 16.dp, start = 16.dp))
                        }
                        SettingsButton(
                            stringResource(item.title), selected = category == item,
                            icon = item.icon, containerColor = Color.Transparent,
                            modifier = Modifier.fillMaxWidth().then(
                                if (index == 0) Modifier.focusRequester(initialFocus) else Modifier
                            ).focusRequester(categoryFocus.getValue(item))
                                 .onFocusChanged {
                                     if (it.isFocused && category != item) {
                                         category = item
                                     }
                                 }
                                 .focusProperties {
                                     right = if (item == category && item != SettingsCategory.HOME) contentFocus else FocusRequester.Default
                                     if (index > 0) up = categoryFocus.getValue(SettingsCategory.entries[index - 1])
                                     if (index < SettingsCategory.entries.lastIndex) down = categoryFocus.getValue(SettingsCategory.entries[index + 1])
                                 },
                            onClick = { category = item }
                        )
                    }
                }
                Box(Modifier.fillMaxHeight().width(1.dp).background(SettingsColors.divider))
                // These short panels stay composed so D-pad entry targets remain attached after scrolling.
                categoryState.SaveableStateProvider(category.name) {
                    when (category) {
                        SettingsCategory.HOME, SettingsCategory.THEMES -> Column(
                            Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(4.dp),
                            verticalArrangement = Arrangement.spacedBy(if (compact) 12.dp else 16.dp)
                         ) {
                            if (!isThemeLibraryVisible) {
                                SettingsHeading(stringResource(R.string.launcher_home), stringResource(R.string.settings_theme_intro))
                                SettingsThemePreview(appearance, activeTheme?.composition, activeName, apps, safePreviewData,
                                    Modifier.fillMaxWidth().heightIn(max = 240.dp).aspectRatio(16f / 9f),
                                    assetUris = activeTheme?.assetUris.orEmpty())
                                Text(activeName, color = SettingsColors.text, fontSize = 20.sp)
                            } else {
                             Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                                 horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                 Column(Modifier.weight(1f)) {
                                     SettingsHeading(stringResource(R.string.settings_my_themes),
                                         stringResource(R.string.settings_theme_library_intro))
                                 }
                                 SettingsButton(stringResource(R.string.editor_create_title), icon = Icons.Default.Add,
                                     modifier = Modifier.then(if (installedThemes.isEmpty()) Modifier.focusRequester(contentFocus) else Modifier), onClick = onCreateCustomTheme)
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
                             installedThemes.chunked(if (compact) 2 else 3).forEach { themes ->
                                 Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                     themes.forEach { theme ->
                                         key(theme.id) {
                                             val isThemeActive = theme.id == appearance.installedThemeId
                                             val themeIndex = installedThemes.indexOfFirst { it.id == theme.id }
                                             val columnCount = if (compact) 2 else 3
                                             Column(Modifier.weight(1f)
                                                 .background(SettingsColors.surface, RoundedCornerShape(16.dp))
                                                 .border(1.dp, if (isThemeActive) SettingsColors.accent else SettingsColors.divider, RoundedCornerShape(16.dp))
                                                 .padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                                 SettingsContentButton(
                                                     onClick = { selectedThemeId = theme.id; showOptions = true },
                                                     modifier = Modifier.fillMaxWidth()
                                                         .then(if (themeIndex == 0) Modifier.focusRequester(contentFocus) else Modifier)
                                                         .focusRequester(themeFocusRequesters.getValue(theme.id))
                                                         .onFocusChanged { if (it.isFocused) selectedThemeId = theme.id }
                                                         .focusProperties {
                                                             if (themeIndex % columnCount == 0) left = categoryFocus.getValue(category)
                                                             else left = themeFocusRequesters.getValue(installedThemes[themeIndex - 1].id)
                                                             if (themeIndex % columnCount < columnCount - 1 && themeIndex < installedThemes.lastIndex)
                                                                 right = themeFocusRequesters.getValue(installedThemes[themeIndex + 1].id)
                                                         }
                                                 ) {
                                                 Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                                 SettingsThemePreview(theme.appearance, theme.composition, theme.name,
                                                     apps, safePreviewData, Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                                                         .clip(RoundedCornerShape(8.dp)), assetUris = theme.assetUris)
                                                 Text(theme.name, color = SettingsColors.text, fontSize = 16.sp,
                                                     maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                 }
                                                 }
                                                 if (isThemeActive) Row(verticalAlignment = Alignment.CenterVertically,
                                                     horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                     Icon(Icons.Default.Check, stringResource(R.string.settings_theme_active),
                                                         Modifier.size(20.dp), tint = SettingsColors.accent)
                                                     Text(stringResource(R.string.settings_theme_active), color = SettingsColors.accent, fontSize = 12.sp)
                                                 }
                                             }
                                         }
                                     }
                                     repeat((if (compact) 2 else 3) - themes.size) { Spacer(Modifier.weight(1f)) }
                                 }
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
                                    Modifier.fillMaxWidth(0.7f).aspectRatio(16f / 9f).clip(RoundedCornerShape(12.dp)),
                                    assetUris = activeTheme?.assetUris.orEmpty())
                            }
                            Column {
                                SettingsActionRow {
                                    SettingsButton(stringResource(R.string.choose_background_image), icon = Icons.Default.AddPhotoAlternate,
                                        modifier = Modifier.focusRequester(contentFocus).focusProperties { left = categoryFocus.getValue(category) },
                                        onClick = onChooseBackgroundImage)
                                    if (appearance.backgroundImageUri != null || appearance.backgroundColorArgb != null) {
                                        SettingsButton(stringResource(R.string.reset_background), icon = Icons.Default.RestartAlt,
                                            onClick = { onIntent(AppearanceAction.ClearCustomBackground) })
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
                                            onClick = { onIntent(AppearanceAction.SelectBackground(choice)) }
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
                                            onClick = { onIntent(AppearanceAction.SelectBackgroundColor(argb)) }) {
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
                                    Modifier.fillMaxWidth(0.7f).aspectRatio(16f / 9f).clip(RoundedCornerShape(12.dp)),
                                    assetUris = activeTheme?.assetUris.orEmpty())
                            }
                            SettingsSectionTitle(stringResource(R.string.appearance_accent))
                            Column {
                                SettingsActionRow {
                                    AccentChoice.entries.forEach { choice ->
                                        SettingsVisualChoice(settingsChoiceLabel(choice.labelResource(), choice == AccentChoice.PROFILE),
                                            appearance.accent == choice,
                                            modifier = if (choice == AccentChoice.PROFILE) Modifier.focusRequester(contentFocus)
                                                .focusProperties { left = categoryFocus.getValue(category) } else Modifier,
                                            onClick = { onIntent(AppearanceAction.SelectAccent(choice)) }) {
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
                                            appearance.tileShape == choice, onClick = { onIntent(AppearanceAction.SelectTileShape(choice)) }) {
                                            Box(Modifier.size(64.dp, 40.dp)
                                                .background(SettingsColors.accent, RoundedCornerShape(appearance.copy(tileShape = choice).toStyle().tileRadius / 2)))
                                        }
                                    }
                                }
                            }
                        }
                        SettingsCategory.WEATHER -> if (weatherContent != null) Box(
                            Modifier.weight(1f).fillMaxHeight().focusRequester(contentFocus)
                                .focusProperties { left = categoryFocus.getValue(category) }
                        ) { weatherContent() } else Column(
                            Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(4.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SettingsHeading(stringResource(R.string.weather_settings_title), stringResource(R.string.weather_settings_description))
                            Text(stringResource(R.string.weather_city_search), color = SettingsColors.muted)
                            SettingsButton(stringResource(R.string.weather_settings_title), icon = Icons.Default.Tune,
                                modifier = Modifier.fillMaxWidth().focusRequester(contentFocus), onClick = onOpenWeatherSettings)
                        }
                        SettingsCategory.DEVICE_TOOLS -> if (deviceToolsContent != null) Box(
                            Modifier.weight(1f).fillMaxHeight().focusRequester(contentFocus)
                                .focusProperties { left = categoryFocus.getValue(category) }
                        ) { deviceToolsContent() } else Column(
                            Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(4.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SettingsHeading(stringResource(R.string.device_tools_title), stringResource(R.string.device_tools_description))
                            Text(stringResource(R.string.device_tools_shizuku_unavailable), color = SettingsColors.muted)
                            SettingsButton(stringResource(R.string.device_tools_title), icon = Icons.Default.Security,
                                modifier = Modifier.fillMaxWidth().focusRequester(contentFocus), onClick = onOpenDeviceTools)
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
                SettingsButton(stringResource(R.string.apply_theme), icon = Icons.Default.Check,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showOptions = false; onApplyInstalledTheme(previewTheme.id) })
                SettingsButton(stringResource(R.string.settings_edit_design), icon = Icons.Default.Edit,
                    modifier = Modifier.fillMaxWidth(), enabled = previewTheme.composition != null,
                    onClick = { showOptions = false; onEditInstalledTheme(previewTheme.id) })
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
                Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)), assetUris = theme.assetUris)
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
            persistentListOf(), PlaceholderCanvasData, {}, {}, initialCategory = category)
    }
}
