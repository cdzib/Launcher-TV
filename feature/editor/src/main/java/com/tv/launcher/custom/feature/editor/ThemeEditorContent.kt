package com.tv.launcher.custom.feature.editor

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewSidebar
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Text
import com.tv.launcher.custom.core.ui.R
import com.tv.launcher.custom.core.ui.theme.LauncherTheme
import com.tv.launcher.custom.core.theme.*
import com.tv.launcher.custom.core.model.theme.*
import com.tv.launcher.custom.core.ui.components.*

internal enum class ThemeEditorCategory(@StringRes val label: Int, val icon: ImageVector) {
    DESIGN(R.string.editor_design, Icons.Default.Dashboard),
    SECTIONS(R.string.editor_sections, Icons.Default.Layers),
    APPEARANCE(R.string.settings_appearance, Icons.Default.Palette)
}

@Composable
internal fun ThemeEditorContent(
    name: String,
    onNameChange: (String) -> Unit,
    composition: ThemeComposition,
    onCompositionChange: (ThemeComposition) -> Unit,
    previewData: CanvasData,
    availableAssetPaths: List<String>,
    onImportAssets: () -> Unit,
    isEditing: Boolean,
    isSaving: Boolean,
    hasChanges: Boolean,
    initialName: String,
    initialComposition: ThemeComposition,
    onSave: () -> Unit,
    onRequestClose: () -> Unit,
    initialCategory: ThemeEditorCategory = ThemeEditorCategory.DESIGN
) {
    var category by remember { mutableStateOf(initialCategory) }
    var selected by remember { mutableIntStateOf(0) }
    var inspectorTarget by remember { mutableStateOf<Int?>(null) }
    var showAddSection by remember { mutableStateOf(false) }
    var showExpandedPreview by remember { mutableStateOf(false) }
    var showReset by remember { mutableStateOf(false) }
    val initialFocus = remember { FocusRequester() }
    val contentFocus = remember { FocusRequester() }
    val categoryFocus = remember { ThemeEditorCategory.entries.associateWith { FocusRequester() } }
    val inspectorState = rememberSaveableStateHolder()
    val isPreview = LocalInspectionMode.current
    val safeData = remember(previewData) {
        object : CanvasData by previewData {
            override fun onItemClick(block: ThemeBlock, index: Int) = Unit
            override fun onNavClick(label: String) = Unit
        }
    }
    val safeSelected = selected.coerceIn(0, composition.blocks.lastIndex.coerceAtLeast(0))
    var previousInspectorTarget by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(composition.blocks.size) { selected = safeSelected }
    LaunchedEffect(Unit) { if (!isPreview) initialFocus.requestFocus() }
    LaunchedEffect(inspectorTarget) {
        if (previousInspectorTarget != inspectorTarget && !isPreview) contentFocus.requestFocus()
        previousInspectorTarget = inspectorTarget
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(SettingsColors.background), contentAlignment = Alignment.Center) {
        val compact = maxWidth < 1100.dp
        Column(
            Modifier.fillMaxSize().background(SettingsColors.background)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(if (compact) 24.dp else 32.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.editor_breadcrumb), color = SettingsColors.muted, fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(stringResource(if (isEditing) R.string.edit_theme_title else R.string.editor_create_title),
                            color = SettingsColors.text, fontSize = if (compact) 24.sp else 28.sp, fontWeight = FontWeight.SemiBold)
                        Text(stringResource(if (hasChanges) R.string.editor_unsaved else R.string.editor_draft),
                            color = SettingsColors.muted, fontSize = 12.sp,
                            modifier = Modifier.background(SettingsColors.surface, RoundedCornerShape(24.dp)).padding(horizontal = 12.dp, vertical = 8.dp))
                    }
                    Text(stringResource(if (isEditing) R.string.editor_editing_theme else R.string.editor_creating_theme, name), color = SettingsColors.muted, fontSize = 14.sp)
                }
                SettingsButton(stringResource(R.string.cancel), icon = Icons.Default.Close, enabled = !isSaving, onClick = onRequestClose)
            }
            Spacer(Modifier.height(16.dp))
            EditorDivider()
            Spacer(Modifier.height(16.dp))
            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(Modifier.width(if (compact) 168.dp else 208.dp).fillMaxHeight().verticalScroll(rememberScrollState()).padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeEditorCategory.entries.forEachIndexed { index, item ->
                        SettingsButton(stringResource(item.label), icon = item.icon, selected = category == item,
                            containerColor = Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                                .then(if (index == 0) Modifier.focusRequester(initialFocus) else Modifier)
                                .focusRequester(categoryFocus.getValue(item)).focusProperties {
                                     right = contentFocus
                                },
                            enabled = !isSaving,
                            onClick = { category = item; inspectorTarget = null })
                    }
                    if (!compact) Text(stringResource(R.string.editor_sidebar_note), color = SettingsColors.muted, fontSize = 12.sp,
                        modifier = Modifier.padding(top = 32.dp))
                }
                Box(Modifier.width(1.dp).fillMaxHeight().background(SettingsColors.divider))
                Column(Modifier.weight(1.55f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.editor_preview_title), color = SettingsColors.text, fontSize = 18.sp)
                        if (!compact) {
                            Row(
                                Modifier.clip(RoundedCornerShape(50)).background(SettingsColors.surface).padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(Modifier.size(7.dp).background(Color(0xFF7ED9A0), CircleShape))
                                Text(stringResource(R.string.editor_live), color = SettingsColors.muted, fontSize = 12.sp)
                            }
                        }
                        Spacer(Modifier.weight(1f))
                    }
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        ThemeEditorPreview(name, composition, safeData, if (category == ThemeEditorCategory.SECTIONS) safeSelected else -1,
                            Modifier.fillMaxWidth().aspectRatio(16f / 9f).then(
                                if (compact) Modifier else Modifier.widthIn(max = 760.dp)
                            ))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.editor_preview_footer), color = SettingsColors.muted, fontSize = 12.sp)
                        SettingsButton(stringResource(R.string.editor_expand), icon = Icons.Default.OpenInFull,
                            containerColor = Color.Transparent, onClick = { showExpandedPreview = true })
                    }
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SettingsColors.surface).padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Info, null, Modifier.size(20.dp), tint = SettingsColors.accent)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(stringResource(when (category) {
                                ThemeEditorCategory.DESIGN -> R.string.editor_design_context
                                ThemeEditorCategory.SECTIONS -> R.string.editor_sections_context
                                ThemeEditorCategory.APPEARANCE -> R.string.editor_appearance_context
                            }), color = SettingsColors.text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text(stringResource(when (category) {
                                ThemeEditorCategory.DESIGN -> R.string.editor_design_hint
                                ThemeEditorCategory.SECTIONS -> R.string.editor_sections_hint
                                ThemeEditorCategory.APPEARANCE -> R.string.editor_appearance_hint
                            }), color = SettingsColors.muted, fontSize = 12.sp)
                        }
                    }
                }
                Column(Modifier.weight(1f).fillMaxHeight()) {
                    // Keep each category's scroll position without keeping detached focus requesters.
                    inspectorState.SaveableStateProvider("${category.name}:$inspectorTarget") {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(4.dp)
                            .focusRequester(contentFocus), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            when (category) {
                                ThemeEditorCategory.DESIGN -> {
                                     Text(stringResource(R.string.editor_theme_details), color = SettingsColors.text, fontSize = 20.sp)
                                    Field(stringResource(R.string.editor_theme_name), name, stringResource(R.string.custom_layout_name_hint), 40, onNameChange)
                                    Text(stringResource(R.string.editor_name_hint), color = SettingsColors.muted, fontSize = 12.sp)
                                    SettingsButton(
                                        stringResource(R.string.custom_layout_add_theme_images),
                                        icon = Icons.Default.AddPhotoAlternate,
                                        enabled = !isSaving,
                                        onClick = onImportAssets
                                    )
                                    if (availableAssetPaths.isNotEmpty()) {
                                        Text(
                                            stringResource(R.string.custom_layout_theme_images) + ": " + availableAssetPaths.joinToString { it.substringAfterLast('/') },
                                            color = SettingsColors.muted,
                                            fontSize = 12.sp
                                        )
                                    }
                                     EditorDivider()
                                      Text(stringResource(R.string.editor_composition), color = SettingsColors.text, fontSize = 20.sp)
                                      Text(stringResource(R.string.editor_declarative_templates_hint), color = SettingsColors.muted, fontSize = 14.sp)
                                      ThemePresets.all.forEach { preset ->
                                          ThemeEditorTemplate(preset, composition == preset.composition, safeData, enabled = !isSaving) {
                                              onCompositionChange(preset.composition)
                                              selected = 0
                                              inspectorTarget = null
                                          }
                                      }
                                }
                                ThemeEditorCategory.SECTIONS -> {
                                    if (inspectorTarget != null) {
                                        SettingsButton(stringResource(R.string.editor_back_sections), icon = Icons.AutoMirrored.Filled.ArrowBack,
                                            modifier = Modifier.fillMaxWidth(), onClick = { inspectorTarget = null })
                                        when (val target = inspectorTarget) {
                                            -2 -> TopBarInspector(composition, onCompositionChange)
                                            -3 -> RailInspector(composition, onCompositionChange)
                                            else -> composition.blocks.getOrNull(target ?: -1)?.let { block ->
                                                Text(block.type.label(), color = SettingsColors.text, fontSize = 20.sp)
                                                BlockInspector(block, target!!, composition, availableAssetPaths,
                                                    canAddThemeBlock(composition, block.type), onCompositionChange) {
                                                    selected = it; inspectorTarget = it
                                                }
                                            }
                                        }
                                    } else {
                                        Text(stringResource(R.string.editor_sections_title), color = SettingsColors.text, fontSize = 20.sp)
                                        Text(stringResource(R.string.editor_sections_count, composition.blocks.size), color = SettingsColors.muted, fontSize = 12.sp)
                                        composition.blocks.forEachIndexed { index, block ->
                                            SettingsButton(block.text?.takeIf { it.isNotBlank() } ?: block.type.label(),
                                                icon = block.type.editorIcon(), selected = index == safeSelected, enabled = !isSaving,
                                                modifier = Modifier.fillMaxWidth().onFocusChanged { if (it.isFocused) selected = index },
                                                onClick = { selected = index; inspectorTarget = index })
                                        }
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            ThemeEditorIconButton(Icons.Default.KeyboardArrowUp, stringResource(R.string.editor_move_up),
                                                Modifier.weight(1f), enabled = safeSelected > 0 && !isSaving) {
                                                onCompositionChange(composition.move(safeSelected, safeSelected - 1)); selected = safeSelected - 1
                                            }
                                            ThemeEditorIconButton(Icons.Default.KeyboardArrowDown, stringResource(R.string.editor_move_down),
                                                Modifier.weight(1f), enabled = safeSelected < composition.blocks.lastIndex && !isSaving) {
                                                onCompositionChange(composition.move(safeSelected, safeSelected + 1)); selected = safeSelected + 1
                                            }
                                        }
                                        SettingsButton(stringResource(R.string.editor_add_section), icon = Icons.Default.Add,
                                            modifier = Modifier.fillMaxWidth(), enabled = composition.blocks.size < 12 && !isSaving,
                                            onClick = { showAddSection = true })
                                        EditorDivider()
                                        Text(stringResource(R.string.editor_navigation), color = SettingsColors.text, fontSize = 16.sp)
                                        SettingsButton(stringResource(R.string.editor_top_bar), icon = Icons.Default.WebAsset,
                                            modifier = Modifier.fillMaxWidth(), enabled = !isSaving, onClick = { inspectorTarget = -2 })
                                        SettingsButton(stringResource(R.string.editor_side_menu), icon = Icons.AutoMirrored.Filled.ViewSidebar,
                                            modifier = Modifier.fillMaxWidth(), enabled = !isSaving, onClick = { inspectorTarget = -3 })
                                    }
                                }
                                ThemeEditorCategory.APPEARANCE -> GlobalInspector(composition, onCompositionChange)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            EditorDivider()
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.editor_footer_hint), color = SettingsColors.muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                SettingsButton(stringResource(R.string.editor_reset), icon = Icons.Default.RestartAlt,
                    enabled = hasChanges && !isSaving, onClick = { showReset = true })
                if (isSaving) CircularProgressIndicator(Modifier.size(20.dp), color = SettingsColors.accent, strokeWidth = 2.dp)
                SettingsButton(stringResource(if (isSaving) R.string.editor_saving else if (isEditing) R.string.save_theme_changes else R.string.editor_save),
                    icon = if (isSaving) null else Icons.Default.Check, primary = true,
                    enabled = name.isNotBlank() && !isSaving, onClick = onSave)
            }
        }
    }
    if (showAddSection) ThemeEditorAddSectionDialog(composition, onDismiss = { showAddSection = false }) { type ->
        if (canAddThemeBlock(composition, type)) {
            selected = composition.blocks.size
            inspectorTarget = null
            onCompositionChange(composition.copy(blocks = composition.blocks + type.defaultBlock()))
            showAddSection = false
        }
    }
    if (showReset) ThemeEditorConfirmation(stringResource(R.string.editor_reset_title), stringResource(R.string.editor_reset_message),
        stringResource(R.string.editor_reset), onDismiss = { showReset = false }, onConfirm = {
            onNameChange(initialName); onCompositionChange(initialComposition); selected = 0; inspectorTarget = null; showReset = false
        })
    if (showExpandedPreview) Dialog(onDismissRequest = { showExpandedPreview = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(Modifier.fillMaxSize(.9f).background(SettingsColors.background, RoundedCornerShape(24.dp)).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.End) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                ThemeEditorPreview(name, composition, safeData, -1, Modifier.aspectRatio(16f / 9f, matchHeightConstraintsFirst = true))
            }
            ThemeEditorDismissButton { showExpandedPreview = false }
        }
    }
}

@Composable
private fun ThemeEditorPreview(name: String, composition: ThemeComposition, data: CanvasData, selection: Int, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(12.dp)).background(Color.Black).border(1.dp, SettingsColors.divider, RoundedCornerShape(12.dp))) {
        CompositionCanvas(composition, data, Modifier.fillMaxSize(), title = name, interactive = false, selectedBlock = selection)
    }
}

@Composable
private fun ThemeEditorTemplate(preset: ThemePreset, selected: Boolean, data: CanvasData, enabled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    // Real miniatures, not branding glyphs; selecting a template only updates the draft.
    androidx.tv.material3.Button(
        onClick = onClick, enabled = enabled, modifier = modifier.fillMaxWidth().semantics { this.selected = selected },
        shape = androidx.tv.material3.ButtonDefaults.shape(RoundedCornerShape(12.dp)),
        contentPadding = PaddingValues(12.dp),
        scale = androidx.tv.material3.ButtonDefaults.scale(focusedScale = 1.02f),
        colors = androidx.tv.material3.ButtonDefaults.colors(containerColor = if (selected) SettingsColors.selected else SettingsColors.surface,
            contentColor = SettingsColors.text, focusedContainerColor = SettingsColors.selected, focusedContentColor = SettingsColors.text),
        border = androidx.tv.material3.ButtonDefaults.border(
            border = if (selected) androidx.tv.material3.Border(androidx.compose.foundation.BorderStroke(1.dp, SettingsColors.accent)) else androidx.tv.material3.Border.None,
            focusedBorder = androidx.tv.material3.Border(androidx.compose.foundation.BorderStroke(3.dp, SettingsColors.text), inset = (-4).dp))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ThemeEditorPreview(preset.name, preset.composition, data, -1, Modifier.width(64.dp).aspectRatio(16f / 9f))
            Text(preset.name, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            if (selected) Icon(Icons.Default.Check, null, Modifier.size(20.dp), tint = SettingsColors.accent)
        }
    }
}

@Composable
internal fun ThemeEditorIconButton(icon: ImageVector, description: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    SettingsButton("", icon = icon, modifier = modifier.semantics { contentDescription = description }, enabled = enabled, onClick = onClick)
}

@Composable
private fun EditorDivider() = Box(Modifier.fillMaxWidth().height(1.dp).background(SettingsColors.divider))

private fun ThemeBlockType.editorIcon(): ImageVector = when (this) {
    ThemeBlockType.CLOCK -> Icons.Default.Title
    ThemeBlockType.TITLE -> Icons.Default.Title
    ThemeBlockType.HERO -> Icons.Default.Image
    ThemeBlockType.SPACER -> Icons.Default.SpaceBar
    ThemeBlockType.APPS_GRID -> Icons.Default.Apps
    ThemeBlockType.FAVORITES_ROW -> Icons.Default.StarOutline
    ThemeBlockType.RECENTS_ROW -> Icons.Default.History
    ThemeBlockType.CHIPS_ROW -> Icons.Default.Category
    ThemeBlockType.APPS_ROW -> Icons.Default.ViewCarousel
    ThemeBlockType.MEDIA_ROW -> Icons.Default.VideoLibrary
    ThemeBlockType.NAV_ROW -> Icons.Default.Menu
}

@Composable
private fun ThemeEditorAddSectionDialog(composition: ThemeComposition, onDismiss: () -> Unit, onAdd: (ThemeBlockType) -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(Modifier.width(400.dp).heightIn(max = 440.dp).background(SettingsColors.background, RoundedCornerShape(24.dp)).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.editor_add_section), color = SettingsColors.text, fontSize = 24.sp)
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeBlockType.entries.forEach { type ->
                    SettingsButton(type.label(), icon = type.editorIcon(), modifier = Modifier.fillMaxWidth(),
                        enabled = canAddThemeBlock(composition, type), onClick = { onAdd(type) })
                }
            }
            ThemeEditorDismissButton(onDismiss)
        }
    }
}

@Composable
internal fun ThemeEditorConfirmation(title: String, message: String, confirmLabel: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(Modifier.width(440.dp).background(SettingsColors.background, RoundedCornerShape(24.dp)).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(title, color = SettingsColors.text, fontSize = 24.sp)
            Text(message, color = SettingsColors.muted, fontSize = 16.sp)
            SettingsButton(confirmLabel, modifier = Modifier.fillMaxWidth(), destructive = true, onClick = onConfirm)
            ThemeEditorDismissButton(onDismiss)
        }
    }
}

@Composable
private fun ThemeEditorDismissButton(onDismiss: () -> Unit) {
    val focus = remember { FocusRequester() }
    val isPreview = LocalInspectionMode.current
    SettingsButton(stringResource(R.string.close), modifier = Modifier.focusRequester(focus), onClick = onDismiss)
    LaunchedEffect(Unit) { if (!isPreview) focus.requestFocus() }
}

@Preview(name = "Crear tema · Diseño", widthDp = 960, heightDp = 540)
@Composable
private fun ThemeEditorDesignPreview() = ThemeEditorPreviewContent(ThemeEditorCategory.DESIGN)

@Preview(name = "Crear tema · Secciones", widthDp = 1280, heightDp = 720)
@Composable
private fun ThemeEditorSectionsPreview() = ThemeEditorPreviewContent(ThemeEditorCategory.SECTIONS)

@Preview(name = "Crear tema · Apariencia", widthDp = 960, heightDp = 540)
@Composable
private fun ThemeEditorAppearancePreview() = ThemeEditorPreviewContent(ThemeEditorCategory.APPEARANCE)

@Preview(name = "Crear tema · Guardando", widthDp = 960, heightDp = 540)
@Composable
private fun ThemeEditorSavingPreview() = ThemeEditorPreviewContent(ThemeEditorCategory.DESIGN, saving = true)

@Composable
private fun ThemeEditorPreviewContent(category: ThemeEditorCategory, saving: Boolean = false) = LauncherTheme(ThemeId.WEBOS) {
    val composition = ThemePresets.all.first().composition
    ThemeEditorContent(
        "Mi tema", {}, composition, {}, PlaceholderCanvasData, emptyList(), {},
        false, saving, true, "Mi tema", composition, {}, {}, category
    )
}
