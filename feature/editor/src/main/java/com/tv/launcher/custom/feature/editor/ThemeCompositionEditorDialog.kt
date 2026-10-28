package com.tv.launcher.custom.feature.editor

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.tv.launcher.custom.core.ui.R
import com.tv.launcher.custom.core.model.theme.*
import com.tv.launcher.custom.core.ui.components.*
import com.tv.launcher.custom.core.theme.PlaceholderCanvasData

private const val MAX_BLOCKS = 12
private const val MAX_PER_TYPE = 3
private const val MAX_MEDIA_CONTENT_ITEMS = 20

internal fun canAddThemeBlock(composition: ThemeComposition, type: ThemeBlockType): Boolean =
    composition.blocks.size < MAX_BLOCKS && composition.blocks.count { it.type == type } < MAX_PER_TYPE

private val SWATCHES = listOf(
    0xFFFFFFFFL, 0xFF000000L, 0xFF111111L, 0xFF2D2D2DL, 0xFF0B1426L, 0xFF1A1A2EL, 0xFF1E88E5L, 0xFF00ACC1L,
    0xFF43A047L, 0xFFFDD835L, 0xFFFB8C00L, 0xFFE53935L, 0xFFB71C1CL, 0xFF8E24AAL, 0xFF6A1B9AL, 0xFF424242L
).map { it.toInt() }

private val ALIGNS = listOf("Izquierda" to BlockAlign.START, "Centro" to BlockAlign.CENTER, "Derecha" to BlockAlign.END)
private val ASPECTS = listOf("16:9" to CardAspect.WIDE, "Cuadrada 1:1" to CardAspect.SQUARE, "Vertical 2:3" to CardAspect.PORTRAIT, "Clásica 4:3" to CardAspect.CLASSIC, "Ultra 21:9" to CardAspect.ULTRAWIDE)
private val CARD_STYLES = listOf("Sólida" to CardStyle.FILLED, "Blanca" to CardStyle.LIGHT, "Cristal" to CardStyle.GLASS, "Contorno" to CardStyle.OUTLINE, "Sin fondo" to CardStyle.NONE)
private val LABELS = listOf("Oculto" to LabelPos.NONE, "Debajo" to LabelPos.BELOW, "Dentro" to LabelPos.INSIDE)
private val FOCUSES = listOf("Zoom" to FocusStyle.SCALE, "Borde" to FocusStyle.BORDER, "Zoom + borde" to FocusStyle.SCALE_BORDER, "Brillo" to FocusStyle.GLOW)
private val BG_STYLES = listOf("Sólido" to BgStyle.SOLID, "Degradado ↕" to BgStyle.VERTICAL, "Degradado ↘" to BgStyle.DIAGONAL, "Radial" to BgStyle.RADIAL, "Paisaje original" to BgStyle.LANDSCAPE)
private val TOP_STYLES = listOf("Pestañas píldora" to TopBarStyle.TABS_PILL, "Pestañas texto" to TopBarStyle.TABS_TEXT, "Íconos" to TopBarStyle.ICONS, "Mínima" to TopBarStyle.MINIMAL)
private val RAIL_STYLES = listOf("Lista" to RailStyle.LIST, "Íconos" to RailStyle.ICONS, "Íconos + texto" to RailStyle.ICONS_LABELS)
private val PLACEMENTS = listOf("Contenido" to BlockPlacement.CONTENT, "Anclado abajo" to BlockPlacement.BOTTOM)
private val YESNO = listOf("No" to false, "Sí" to true)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ThemeCompositionEditorDialog(
    name: String,
    onNameChange: (String) -> Unit,
    composition: ThemeComposition,
    onCompositionChange: (ThemeComposition) -> Unit,
    previewData: CanvasData = PlaceholderCanvasData,
    availableAssetPaths: List<String> = emptyList(),
    onImportAssets: () -> Unit = {},
    isEditing: Boolean = false,
    isSaving: Boolean = false,
    useDialog: Boolean = true,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val initialName = remember { name }
    val initialComposition = remember { composition }
    var showDiscard by remember { mutableStateOf(false) }
    val hasChanges = name != initialName || composition != initialComposition
    val requestClose: () -> Unit = {
        if (!isSaving) {
            if (hasChanges) showDiscard = true else onCancel()
        }
    }
    val editorContent: @Composable () -> Unit = {
        CompositionLocalProvider(androidx.tv.material3.LocalContentColor provides SettingsColors.text) {
            ThemeEditorContent(name, onNameChange, composition, onCompositionChange, previewData,
                availableAssetPaths, onImportAssets,
                isEditing, isSaving, hasChanges, initialName, initialComposition, onSave, requestClose)
            if (showDiscard) ThemeEditorConfirmation(
                title = stringResource(R.string.editor_discard_title),
                message = stringResource(R.string.editor_discard_message),
                confirmLabel = stringResource(R.string.editor_discard),
                onDismiss = { showDiscard = false },
                onConfirm = { showDiscard = false; onCancel() }
            )
        }
    }
    if (useDialog) {
        Dialog(onDismissRequest = requestClose, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false), content = editorContent)
    } else {
        androidx.activity.compose.BackHandler(onBack = requestClose)
        editorContent()
    }
}

// ───────────── Inspectores ─────────────

@Composable
internal fun GlobalInspector(composition: ThemeComposition, onChange: (ThemeComposition) -> Unit) {
    val style = composition.bgStyle ?: BgStyle.SOLID
    Text(stringResource(R.string.editor_background_spacing), fontSize = 20.sp)
    Picker("Tipo de fondo", BG_STYLES, style) { onChange(composition.copy(bgStyle = it)) }
    ColorRow("Color de fondo", composition.bgColor) { onChange(composition.copy(bgColor = it)) }
    if (style != BgStyle.SOLID && style != BgStyle.LANDSCAPE) ColorRow("Segundo color", composition.bgColor2) { onChange(composition.copy(bgColor2 = it)) }
    Stepper("Espacio entre secciones", composition.spacing ?: 16, 0, 64, 2, "dp") { onChange(composition.copy(spacing = it)) }
    Stepper("Margen general", composition.padding ?: 24, 0, 96, 4, "dp") { onChange(composition.copy(padding = it)) }
    composition.topPadding?.let { value ->
        Stepper(stringResource(R.string.editor_block_top_inset), value, 0, 96, 4, "dp") { onChange(composition.copy(topPadding = it)) }
    }
    WidgetInspector(composition, onChange)
}

@Composable
private fun WidgetInspector(composition: ThemeComposition, onChange: (ThemeComposition) -> Unit) {
    Text(stringResource(R.string.editor_widgets), fontSize = 20.sp)
    val anchors = listOf(
        stringResource(R.string.editor_anchor_top_start) to WidgetAnchor.TOP_START,
        stringResource(R.string.editor_anchor_top_end) to WidgetAnchor.TOP_END,
        stringResource(R.string.editor_anchor_bottom_start) to WidgetAnchor.BOTTOM_START,
        stringResource(R.string.editor_anchor_bottom_end) to WidgetAnchor.BOTTOM_END
    )
    val freeAnchor = WidgetAnchor.entries.firstOrNull { anchor -> composition.widgets.none { it.anchor == anchor } }
    if (freeAnchor != null) {
        SettingsButton(stringResource(R.string.editor_add_clock), onClick = {
            onChange(composition.copy(widgets = composition.widgets + ThemeWidget(ThemeWidgetType.CLOCK, anchor = freeAnchor, height = 120)))
        })
        SettingsButton(stringResource(R.string.editor_add_weather), onClick = {
            onChange(composition.copy(widgets = composition.widgets + ThemeWidget(ThemeWidgetType.WEATHER, anchor = freeAnchor)))
        })
    }
    composition.widgets.forEachIndexed { index, widget ->
        fun update(transform: (ThemeWidget) -> ThemeWidget) = onChange(composition.copy(
            widgets = composition.widgets.mapIndexed { itemIndex, item -> if (itemIndex == index) transform(item) else item }
        ))
        SettingsButton(stringResource(R.string.editor_remove_widget, index + 1), onClick = {
            onChange(composition.copy(widgets = composition.widgets.filterIndexed { itemIndex, _ -> itemIndex != index }))
        })
        Picker(stringResource(R.string.editor_widget_anchor), anchors.filter { (_, anchor) ->
            anchor == widget.anchor || composition.widgets.none { it.anchor == anchor }
        }, widget.anchor) { anchor -> update { it.copy(anchor = anchor) } }
        Stepper(stringResource(R.string.editor_widget_width), widget.width, 100, 400, 8, "dp") { width -> update { it.copy(width = width) } }
        Stepper(stringResource(R.string.editor_widget_height), widget.height, 60, 240, 8, "dp") { height -> update { it.copy(height = height) } }
        Picker(stringResource(R.string.editor_overlay_navigation), YESNO, widget.overlay) { value -> update { it.copy(overlay = value) } }
        Picker(stringResource(R.string.editor_layered_glass), YESNO, widget.glass) { value -> update { it.copy(glass = value) } }
        if (widget.overlay) {
            Stepper(stringResource(R.string.editor_widget_offset_x), widget.offsetX, 0, 360, 4, "dp") { value -> update { it.copy(offsetX = value) } }
            Stepper(stringResource(R.string.editor_widget_offset_y), widget.offsetY, 0, 360, 4, "dp") { value -> update { it.copy(offsetY = value) } }
        }
        if (widget.type == ThemeWidgetType.CLOCK) {
            Picker(stringResource(R.string.editor_widget_date), YESNO, widget.showDate) { value -> update { it.copy(showDate = value) } }
            Picker(stringResource(R.string.editor_widget_24_hour), YESNO, widget.use24Hour) { value -> update { it.copy(use24Hour = value) } }
        } else Text(stringResource(R.string.editor_widget_weather_hint), color = SettingsColors.muted)
    }
}

@Composable
internal fun TopBarInspector(composition: ThemeComposition, onChange: (ThemeComposition) -> Unit) {
    val cfg = composition.topBar
    if (cfg == null) {
        Text(stringResource(R.string.editor_top_disabled), color = SettingsColors.muted)
        SettingsButton(stringResource(R.string.editor_enable_top), onClick = { onChange(composition.copy(topBar = TopBarConfig())) })
        return
    }
    fun up(t: (TopBarConfig) -> TopBarConfig) = onChange(composition.copy(topBar = t(cfg)))
    SettingsButton(stringResource(R.string.editor_remove_top), onClick = { onChange(composition.copy(topBar = null)) })
    Picker(stringResource(R.string.editor_navigation_position), listOf(
        stringResource(R.string.editor_position_top) to TopBarPosition.TOP,
        stringResource(R.string.editor_position_bottom) to TopBarPosition.BOTTOM
    ), cfg.position) { value -> up { it.copy(position = value) } }
    Picker(stringResource(R.string.editor_layered_glass), YESNO, cfg.glass) { value -> up { it.copy(glass = value) } }
    Picker("Estilo", TOP_STYLES, cfg.style) { v -> up { it.copy(style = v) } }
    if (cfg.style != TopBarStyle.MINIMAL) {
        Field("Pestañas (coma). La primera es la activa", cfg.tabs.orEmpty(), "Inicio,Apps", 80) { v -> up { it.copy(tabs = v) } }
        Picker("Posición de pestañas", ALIGNS, cfg.align) { v -> up { it.copy(align = v) } }
    }
    Field("Logo / texto de marca", cfg.logo.orEmpty(), "Ej. Roku", 20) { v -> up { it.copy(logo = v.ifBlank { null }) } }
    Picker("Reloj", YESNO, cfg.showClock) { v -> up { it.copy(showClock = v) } }
    Picker("Botón buscar", YESNO, cfg.showSearch) { v -> up { it.copy(showSearch = v) } }
    Picker("Botón ajustes", YESNO, cfg.showSettings) { v -> up { it.copy(showSettings = v) } }
    ColorRow("Color de texto", cfg.textColor) { v -> up { it.copy(textColor = v) } }
    ColorRow("Fondo de la barra", cfg.bgColor) { v -> up { it.copy(bgColor = v) } }
    if (cfg.bgColor != null) Stepper("Opacidad del fondo", cfg.bgAlpha ?: 100, 0, 100, 10, "%") { v -> up { it.copy(bgAlpha = v) } }
}

@Composable
internal fun RailInspector(composition: ThemeComposition, onChange: (ThemeComposition) -> Unit) {
    val cfg = composition.rail
    if (cfg == null) {
        Text(stringResource(R.string.editor_rail_disabled), color = SettingsColors.muted)
        SettingsButton(stringResource(R.string.editor_enable_rail), onClick = { onChange(composition.copy(rail = RailConfig())) })
        return
    }
    fun up(t: (RailConfig) -> RailConfig) = onChange(composition.copy(rail = t(cfg)))
    SettingsButton(stringResource(R.string.editor_remove_rail), onClick = { onChange(composition.copy(rail = null)) })
    Picker(stringResource(R.string.editor_navigation_position), listOf(
        stringResource(R.string.editor_position_left) to RailPosition.LEFT,
        stringResource(R.string.editor_position_right) to RailPosition.RIGHT,
        stringResource(R.string.editor_position_bottom) to RailPosition.BOTTOM
    ), cfg.position) { value -> up { it.copy(position = value) } }
    Picker(stringResource(R.string.editor_layered_glass), YESNO, cfg.glass) { value -> up { it.copy(glass = value) } }
    Picker("Estilo", RAIL_STYLES, cfg.style) { v -> up { it.copy(style = v) } }
    Picker(stringResource(R.string.editor_overlay_navigation), YESNO, cfg.overlay) { v -> up { it.copy(overlay = v) } }
    Field("Opciones (coma). La primera es la activa", cfg.items.orEmpty(), "Inicio,Buscar,Apps", 120) { v -> up { it.copy(items = v) } }
    Stepper("Ancho", cfg.width ?: if (cfg.style == RailStyle.ICONS) 72 else 220, 56, 360, 8, "dp") { v -> up { it.copy(width = v) } }
    Stepper(stringResource(R.string.editor_widget_offset_y), cfg.topInset, 0, 360, 4, "dp") { v -> up { it.copy(topInset = v) } }
    Picker(stringResource(R.string.editor_rail_fixed_height), YESNO, cfg.height != null) { v -> up { it.copy(height = if (v) 326 else null) } }
    cfg.height?.let { height -> Stepper(stringResource(R.string.editor_widget_height), height, 120, 600, 8, "dp") { v -> up { it.copy(height = v) } } }
    Picker("Flotante (con margen)", YESNO, cfg.floating) { v -> up { it.copy(floating = v) } }
    Stepper("Esquinas", cfg.cornerRadius ?: if (cfg.floating) 24 else 0, 0, 60, 2, "dp") { v -> up { it.copy(cornerRadius = v) } }
    Stepper("Tamaño de texto", cfg.textSize ?: 16, 12, 28, 1, "sp") { v -> up { it.copy(textSize = v) } }
    Picker("Avatar de perfil", YESNO, cfg.showProfile) { v -> up { it.copy(showProfile = v) } }
    ColorRow("Color de texto", cfg.textColor) { v -> up { it.copy(textColor = v) } }
    ColorRow("Fondo del menú", cfg.bgColor) { v -> up { it.copy(bgColor = v) } }
    if (cfg.bgColor != null) Stepper("Opacidad del fondo", cfg.bgAlpha ?: 100, 0, 100, 5, "%") { v -> up { it.copy(bgAlpha = v) } }
}

@Composable
internal fun BlockInspector(
    block: ThemeBlock,
    index: Int,
    composition: ThemeComposition,
    availableAssetPaths: List<String>,
    canDuplicate: Boolean,
    onChange: (ThemeComposition) -> Unit,
    onSelect: (Int) -> Unit
) {
    fun update(t: (ThemeBlock) -> ThemeBlock) = onChange(composition.updateAt(index, t))
    val type = block.type
    val isCards = type == ThemeBlockType.APPS_ROW || type == ThemeBlockType.FAVORITES_ROW ||
            type == ThemeBlockType.RECENTS_ROW || type == ThemeBlockType.APPS_GRID

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ThemeEditorIconButton(Icons.Default.KeyboardArrowUp, stringResource(R.string.editor_move_up), Modifier.weight(1f), enabled = index > 0) {
            onChange(composition.move(index, index - 1)); onSelect(index - 1)
        }
        ThemeEditorIconButton(Icons.Default.KeyboardArrowDown, stringResource(R.string.editor_move_down), Modifier.weight(1f), enabled = index < composition.blocks.lastIndex) {
            onChange(composition.move(index, index + 1)); onSelect(index + 1)
        }
        ThemeEditorIconButton(Icons.Default.ContentCopy, stringResource(R.string.editor_duplicate), Modifier.weight(1f), enabled = canDuplicate) {
                onChange(composition.copy(blocks = composition.blocks.toMutableList().also { it.add(index + 1, block) }))
                onSelect(index + 1)
        }
        ThemeEditorIconButton(Icons.Default.DeleteOutline, stringResource(R.string.remove_block), Modifier.weight(1f), enabled = composition.blocks.size > 1) {
                onChange(composition.copy(blocks = composition.blocks.filterIndexed { i, _ -> i != index }))
                onSelect((index - 1).coerceAtLeast(0))
        }
    }
    SettingsButton(stringResource(R.string.editor_reset_section), icon = Icons.Default.RestartAlt, onClick = {
        update { type.defaultBlock().copy(appContent = block.appContent, startInset = block.startInset, endInset = block.endInset) }
    })
    Picker("Ubicación del bloque", PLACEMENTS, block.placement) { v -> update { it.copy(placement = v) } }

    if (block.appContent != null) {
        Text(stringResource(R.string.editor_dynamic_apps_hint), color = SettingsColors.muted)
        if (block.appContent == AppContent.FEATURED_APP) {
            Picker(stringResource(R.string.editor_layered_glass), YESNO, block.rCardStyle() == CardStyle.GLASS) { value ->
                update { it.copy(cardStyle = if (value) CardStyle.GLASS else CardStyle.FILLED) }
            }
        }
        if (block.appContent != AppContent.SHORTCUT_EDITOR) {
            Stepper(stringResource(R.string.editor_block_height), block.rItemH(), 28, 400, 4, "dp") { value ->
                update { it.copy(itemHeight = value) }
            }
        }
        Stepper(stringResource(R.string.editor_block_start_inset), block.startInset ?: 0, 0, 360, 4, "dp") { value ->
            update { it.copy(startInset = value) }
        }
        Stepper(stringResource(R.string.editor_block_end_inset), block.endInset ?: 0, 0, 360, 4, "dp") { value ->
            update { it.copy(endInset = value) }
        }
        return
    }

    when (type) {
        ThemeBlockType.TITLE -> {
            Field("Texto (vacío = nombre del diseño)", block.text.orEmpty(), "Título", 60) { v -> update { it.copy(text = v.ifBlank { null }) } }
            Stepper("Tamaño de texto", block.rTextSize(), 12, 72, 2, "sp") { v -> update { it.copy(textSize = v) } }
            Picker("Alineación", ALIGNS, block.rAlign()) { v -> update { it.copy(align = v) } }
            Stepper("Relleno", block.rPadding(), 0, 64, 4, "dp") { v -> update { it.copy(padding = v) } }
            ColorRow("Color del título", block.textColor) { v -> update { it.copy(textColor = v) } }
        }
        ThemeBlockType.SPACER -> Stepper("Altura", block.weight ?: 1, 1, 12) { v -> update { it.copy(weight = v) } }
        ThemeBlockType.HERO -> {
            Picker(stringResource(R.string.editor_layered_glass), YESNO, block.rCardStyle() == CardStyle.GLASS) { value ->
                update { it.copy(cardStyle = if (value) CardStyle.GLASS else CardStyle.FILLED) }
            }
            Field("Título (vacío = nombre del diseño)", block.text.orEmpty(), "Título", 60) { v -> update { it.copy(text = v.ifBlank { null }) } }
            Field("Subtítulo", block.subtitle.orEmpty(), "Descripción", 90) { v -> update { it.copy(subtitle = v.ifBlank { null }) } }
            Field("Botón (vacío = sin botón)", block.buttonText.orEmpty(), "Más información", 30) { v -> update { it.copy(buttonText = v.ifBlank { null }) } }
            Stepper("Alto del banner", block.rItemH(), 80, 400, 10, "dp") { v -> update { it.copy(itemHeight = v) } }
            Stepper("Tamaño del título", block.rTextSize(), 16, 72, 2, "sp") { v -> update { it.copy(textSize = v) } }
            Stepper("Esquinas", block.rCorner(), 0, 40, 2, "dp") { v -> update { it.copy(cornerRadius = v) } }
            Picker(stringResource(R.string.custom_layout_hero_image), assetOptions(availableAssetPaths, block.imageAsset), block.imageAsset.orEmpty()) {
                v -> update { it.copy(imageAsset = v.ifBlank { null }) }
            }
        }
        ThemeBlockType.CHIPS_ROW -> {
            Field("Etiquetas (separadas por coma)", block.text.orEmpty(), "Juegos,Música", 100) { v -> update { it.copy(text = v.ifBlank { null }) } }
            Picker("Colores variados", YESNO, block.multicolor ?: false) { v -> update { it.copy(multicolor = v) } }
            Stepper("Alto", block.rItemH(), 28, 80, 2, "dp") { v -> update { it.copy(itemHeight = v) } }
            Stepper("Tamaño de texto", block.rTextSize(), 10, 32, 1, "sp") { v -> update { it.copy(textSize = v) } }
            Stepper("Espacio entre chips", block.rSpacing(), 0, 48, 2, "dp") { v -> update { it.copy(spacing = v) } }
            Stepper("Esquinas", block.rCorner(), 0, 40, 2, "dp") { v -> update { it.copy(cornerRadius = v) } }
            Picker("Efecto de foco", FOCUSES, block.rFocusStyle()) { v -> update { it.copy(focusStyle = v) } }
            Stepper("Zoom al enfocar", block.focusScale ?: 112, 100, 150, 2, "%") { v -> update { it.copy(focusScale = v) } }
        }
        ThemeBlockType.MEDIA_ROW -> {
            Field("Título de sección (opcional)", block.text.orEmpty(), "Ej. Nuevos canales", 60) {
                value -> update { it.copy(text = value.ifBlank { null }) }
            }
            Stepper("Cantidad visible", block.maxItems ?: 8, 1, MAX_MEDIA_CONTENT_ITEMS) {
                value -> update { it.copy(maxItems = value) }
            }
            Picker(stringResource(R.string.custom_layout_media_item_image), assetOptions(availableAssetPaths), block.imageAsset.orEmpty()) {
                value -> update { it.copy(imageAsset = value.ifBlank { null }) }
            }
            block.contentItems.forEachIndexed { itemIndex, contentItem ->
                Text(stringResource(R.string.custom_layout_media_item, itemIndex + 1), color = SettingsColors.text, fontSize = 16.sp)
                Field(stringResource(R.string.custom_layout_media_item_title), contentItem.label, "Título", 60) { value ->
                    update { current -> current.copy(contentItems = current.contentItems.mapIndexed { index, item ->
                        if (index == itemIndex) item.copy(label = value) else item
                    }) }
                }
                Picker(
                    stringResource(R.string.custom_layout_media_item_image),
                    assetOptions(availableAssetPaths, contentItem.imageAsset),
                    contentItem.imageAsset
                ) { value ->
                    update { current -> current.copy(contentItems = current.contentItems.mapIndexed { index, item ->
                        if (index == itemIndex) item.copy(imageAsset = value) else item
                    }) }
                }
                Field(stringResource(R.string.custom_layout_media_item_package), contentItem.launchPackage.orEmpty(), "com.example.app", 200) { value ->
                    update { current -> current.copy(contentItems = current.contentItems.mapIndexed { index, item ->
                        if (index == itemIndex) item.copy(launchPackage = value.ifBlank { null }) else item
                    }) }
                }
                SettingsButton(stringResource(R.string.custom_layout_remove_media_item), icon = Icons.Default.DeleteOutline) {
                    update { current -> current.copy(contentItems = current.contentItems.filterIndexed { index, _ -> index != itemIndex }) }
                }
            }
            SettingsButton(
                stringResource(R.string.custom_layout_add_media_item),
                icon = Icons.Default.Add,
                enabled = block.contentItems.size < MAX_MEDIA_CONTENT_ITEMS && availableAssetPaths.isNotEmpty()
            ) {
                val initialAsset = availableAssetPaths.first()
                update { current -> current.copy(contentItems = current.contentItems + ThemeContentItem("Nuevo elemento", initialAsset)) }
            }
            if (availableAssetPaths.isEmpty()) {
                Text(stringResource(R.string.custom_layout_add_theme_images), color = SettingsColors.muted, fontSize = 13.sp)
            }
        }
        else -> if (isCards) {
            Field("Título de sección (opcional)", block.text.orEmpty(), "Ej. Apps favoritas", 40) { v -> update { it.copy(text = v.ifBlank { null }) } }
            Stepper("Tamaño del título", block.rTextSize(), 12, 40, 2, "sp") { v -> update { it.copy(textSize = v) } }
            Stepper("Cantidad", block.maxItems ?: 8, 1, if (type == ThemeBlockType.APPS_GRID) 48 else 20) { v -> update { it.copy(maxItems = v) } }
            if (type == ThemeBlockType.APPS_GRID) Stepper("Columnas", block.columns ?: 4, 2, 8) { v -> update { it.copy(columns = v) } }
            Stepper("Alto de tarjeta", block.rItemH(), 32, 320, 4, "dp") { v -> update { it.copy(itemHeight = v) } }
            Picker(
                if (type == ThemeBlockType.APPS_GRID) "Proporción" else "Formato de fila",
                ASPECTS,
                block.rAspect()
            ) { v -> update { it.copy(aspect = v) } }
            Picker("Estilo de tarjeta", CARD_STYLES, block.rCardStyle()) { v -> update { it.copy(cardStyle = v) } }
            Picker("Nombre de la app", LABELS, block.rLabelPos()) { v -> update { it.copy(labelPos = v) } }
            Stepper("Espacio entre tarjetas", block.rSpacing(), 0, 48, 2, "dp") { v -> update { it.copy(spacing = v) } }
            Stepper("Esquinas", block.rCorner(), 0, 60, 2, "dp") { v -> update { it.copy(cornerRadius = v) } }
            Picker("Efecto de foco", FOCUSES, block.rFocusStyle()) { v -> update { it.copy(focusStyle = v) } }
            Stepper("Zoom al enfocar", block.focusScale ?: 112, 100, 150, 2, "%") { v -> update { it.copy(focusScale = v) } }
        }
    }

    if (type != ThemeBlockType.SPACER) {
        Picker("Alineación", ALIGNS, block.rAlign()) { v -> update { it.copy(align = v) } }
        Stepper("Relleno interno", block.rPadding(), 0, 64, 4, "dp") { v -> update { it.copy(padding = v) } }
        ColorRow("Color de texto", block.textColor) { v -> update { it.copy(textColor = v) } }
        ColorRow("Fondo de la sección", block.bgColor) { v -> update { it.copy(bgColor = v) } }
    if (block.bgColor != null) Stepper("Opacidad del fondo", block.bgAlpha ?: 100, 0, 100, 10, "%") { v -> update { it.copy(bgAlpha = v) } }
    }
}

private fun assetOptions(paths: List<String>, selectedPath: String? = null): List<Pair<String, String>> {
    val availablePaths = (paths + listOfNotNull(selectedPath)).distinct()
    return listOf("Sin imagen" to "") + availablePaths.map { path -> path.substringAfterLast('/') to path }
}

// ───────────── Controles reutilizables ─────────────

@Composable
private fun Stepper(label: String, value: Int, min: Int, max: Int, step: Int = 1, suffix: String = "", onChange: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 14.sp, color = SettingsColors.muted)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            ThemeEditorIconButton(Icons.Default.Remove, stringResource(R.string.editor_decrease, label), Modifier.width(48.dp), enabled = value > min) { onChange((value - step).coerceAtLeast(min)) }
            Text("$value$suffix", Modifier.weight(1f), fontSize = 16.sp, textAlign = TextAlign.Center)
            ThemeEditorIconButton(Icons.Default.Add, stringResource(R.string.editor_increase, label), Modifier.width(48.dp), enabled = value < max) { onChange((value + step).coerceAtMost(max)) }
        }
    }
}

@Composable
private fun <T> Picker(label: String, options: List<Pair<String, T>>, current: T, onSelect: (T) -> Unit) {
    val i = options.indexOfFirst { it.second == current }.coerceAtLeast(0)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 14.sp, color = SettingsColors.muted)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            ThemeEditorIconButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.editor_previous, label), modifier = Modifier.width(48.dp)) { onSelect(options[(i - 1 + options.size) % options.size].second) }
            Text(options[i].first, Modifier.weight(1f), fontSize = 14.sp, textAlign = TextAlign.Center)
            ThemeEditorIconButton(Icons.AutoMirrored.Filled.ArrowForward, stringResource(R.string.editor_next, label), modifier = Modifier.width(48.dp)) { onSelect(options[(i + 1) % options.size].second) }
        }
    }
}

@Composable
internal fun Field(label: String, value: String, hint: String, maxLength: Int, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 16.sp)
        TextInput(value, hint, SettingsColors.surface, Modifier, maxLength, onChange)
    }
}

@Composable
private fun ColorRow(label: String, value: Int?, onChange: (Int?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 16.sp)
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Swatch(null, value == null) { onChange(null) }
            SWATCHES.forEach { c -> Swatch(c, value == c) { onChange(c) } }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun Swatch(color: Int?, selected: Boolean, onClick: () -> Unit) {
    val fill = color?.let { Color(it) } ?: SettingsColors.surface
    val description = if (color == null) stringResource(R.string.editor_color_auto)
        else stringResource(R.string.editor_color_value, Integer.toHexString(color).uppercase())
    Surface(
        onClick = onClick,
        modifier = Modifier.size(44.dp).semantics {
            contentDescription = description
        },
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        colors = ClickableSurfaceDefaults.colors(containerColor = fill, focusedContainerColor = fill),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.2f)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (selected || color == null) Icon(if (selected) Icons.Default.Check else Icons.Default.RestartAlt, null,
                Modifier.size(20.dp), tint = if (fill.luminance() > 0.5f) Color.Black else Color.White)
        }
    }
}

@Composable
private fun TextInput(value: String, hint: String, container: Color, modifier: Modifier, maxLength: Int, onChange: (String) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    Surface(
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp).onFocusChanged { focused = it.hasFocus }
            .border(2.dp, if (focused) SettingsColors.accent else SettingsColors.divider, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = SurfaceDefaults.colors(containerColor = container)
    ) {
        BasicTextField(
            value = value,
            onValueChange = { onChange(it.take(maxLength)) },
            singleLine = true,
            textStyle = TextStyle(color = SettingsColors.text, fontSize = 16.sp),
            cursorBrush = SolidColor(SettingsColors.accent),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
                .onPreviewKeyEvent { event ->
                    val direction = when (event.key) {
                        Key.DirectionDown -> FocusDirection.Down
                        Key.DirectionUp -> FocusDirection.Up
                        else -> null
                    }
                    if (direction != null) {
                        if (event.type == KeyEventType.KeyDown) {
                            keyboard?.hide()
                            focusManager.moveFocus(direction)
                        }
                        true
                    } else false
                },
            decorationBox = { field ->
                if (value.isBlank()) Text(hint, color = SettingsColors.muted, fontSize = 14.sp)
                field()
            }
        )
    }
}

// ───────────── Helpers ─────────────

internal fun ThemeComposition.move(from: Int, to: Int): ThemeComposition {
    if (from !in blocks.indices || to !in blocks.indices || from == to) return this
    return copy(blocks = blocks.toMutableList().also { list ->
        val block = list.removeAt(from)
        list.add(to, block)
    })
}

private fun ThemeComposition.updateAt(index: Int, transform: (ThemeBlock) -> ThemeBlock): ThemeComposition {
    val next = blocks.toMutableList()
    next[index] = transform(next[index])
    return copy(blocks = next)
}

internal fun ThemeBlockType.defaultBlock(): ThemeBlock = when (this) {
    ThemeBlockType.TITLE -> ThemeBlock(this)
    ThemeBlockType.APPS_ROW -> ThemeBlock(this, maxItems = 8)
    ThemeBlockType.APPS_GRID -> ThemeBlock(this, maxItems = 12, columns = 4)
    ThemeBlockType.FAVORITES_ROW -> ThemeBlock(this, maxItems = 6)
    ThemeBlockType.SPACER -> ThemeBlock(this, weight = 1)
    ThemeBlockType.HERO -> ThemeBlock(this, text = "Destacado", subtitle = "Descripción breve")
    ThemeBlockType.CHIPS_ROW -> ThemeBlock(this, text = "Juegos,Música,Deportes,Hogar", multicolor = true)
    ThemeBlockType.RECENTS_ROW -> ThemeBlock(this, text = "Recientes", maxItems = 5)
    ThemeBlockType.MEDIA_ROW -> ThemeBlock(this, text = "Contenido", maxItems = 8)
    ThemeBlockType.NAV_ROW -> ThemeBlock(this, text = "Inicio,Buscar,Apps", showSystemSettings = true, showPersonalization = true)
}

@Composable
internal fun ThemeBlockType.label(): String = when (this) {
    ThemeBlockType.TITLE -> stringResource(R.string.custom_layout_title_block)
    ThemeBlockType.APPS_ROW -> stringResource(R.string.custom_layout_apps_row)
    ThemeBlockType.APPS_GRID -> stringResource(R.string.custom_layout_apps_grid)
    ThemeBlockType.FAVORITES_ROW -> stringResource(R.string.custom_layout_favorites)
    ThemeBlockType.SPACER -> stringResource(R.string.custom_layout_spacer)
    ThemeBlockType.HERO -> "Banner"
    ThemeBlockType.CHIPS_ROW -> "Categorías"
    ThemeBlockType.RECENTS_ROW -> "Recientes"
    ThemeBlockType.MEDIA_ROW -> stringResource(R.string.custom_layout_media_row)
    ThemeBlockType.NAV_ROW -> "Navegación"
}
