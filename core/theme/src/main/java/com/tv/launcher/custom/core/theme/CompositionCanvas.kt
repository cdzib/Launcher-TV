package com.tv.launcher.custom.core.theme

import com.tv.launcher.custom.core.model.theme.*
import com.tv.launcher.custom.core.ui.components.CanvasData

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.onFocusChanged
import com.tv.launcher.custom.core.ui.components.selectLauncherSectionOnFocus
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Palette
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.tv.launcher.custom.core.ui.components.LauncherPersonalizationButton
import com.tv.launcher.custom.core.ui.components.ClockText as SharedClockText
import com.tv.launcher.custom.core.ui.components.isPersonalizationLabel
import com.tv.launcher.custom.core.ui.components.shouldShowRailProfileAvatar
import com.tv.launcher.custom.core.ui.components.getRenderedTopBarTabs
import com.tv.launcher.custom.core.ui.components.isSystemSettingsLabel as isSettingsLabel
import com.tv.launcher.custom.core.ui.components.shouldShowTopBarSearchButton
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Glow
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.tv.launcher.custom.core.ui.theme.LocalLauncherStyle

private const val REFERENCE_WIDTH_DP = 960f
private val LocalCanvasScale = compositionLocalOf { 1f }

@Composable private fun Int.d(): Dp = (this * LocalCanvasScale.current).dp
@Composable private fun Int.s(): TextUnit = (this * LocalCanvasScale.current).sp

object PlaceholderCanvasData : CanvasData {
    private val colors = listOf(0xFFE50914, 0xFFFF0000, 0xFF00A8E1, 0xFF4285F4, 0xFF9146FF, 0xFF1DB954, 0xFFFF9800, 0xFF607D8B)
        .map { Color(it) }

    override fun itemCount(block: ThemeBlock) = when (block.type) {
        ThemeBlockType.FAVORITES_ROW -> 4
        ThemeBlockType.RECENTS_ROW -> 5
        else -> 40
    }
    override fun label(block: ThemeBlock, index: Int) = "App ${index + 1}"

    @Composable
    override fun Icon(block: ThemeBlock, index: Int, modifier: Modifier) {
        val c = colors[index % colors.size]
        if (block.type == ThemeBlockType.RECENTS_ROW) {
            Box(modifier.background(Brush.linearGradient(listOf(c, c.copy(alpha = 0.35f)))))
        } else {
            Box(modifier, contentAlignment = Alignment.Center) {
                Box(Modifier.fillMaxSize(0.55f).background(c, RoundedCornerShape(20)))
            }
        }
    }
}

@Composable
fun CompositionCanvas(
    composition: ThemeComposition,
    data: CanvasData,
    modifier: Modifier = Modifier,
    title: String = "",
    interactive: Boolean = false,
    selectedBlock: Int = -1,
    transparentBackground: Boolean = false,
    firstItemFocusRequester: FocusRequester? = null,
    onOpenPersonalization: (() -> Unit)? = null,
    onOpenSystemSettingsFallback: (() -> Unit)? = null,
    sectionContent: (@Composable () -> Unit)? = null
) {
    val palette = LocalLauncherStyle.current.palette
    val settingsInTopBar = composition.topBar?.let { config ->
        topBarShowsSettings(config) || (config.style != TopBarStyle.MINIMAL && config.tabs.orEmpty().split(',').any(::isSettingsLabel))
    } ?: false
    val personalizationInRail = composition.rail != null && (composition.topBar == null ||
        (!settingsInTopBar && composition.rail?.items.orEmpty().split(',').any(::isSettingsLabel)))
    BoxWithConstraints(modifier) {
        val k = (maxWidth.value / REFERENCE_WIDTH_DP).coerceAtLeast(if (interactive) 0.5f else 0.05f)
        CompositionLocalProvider(LocalCanvasScale provides k) {
            val c1 = composition.bgColor?.let { Color(it) } ?: palette.background
            val c2 = composition.bgColor2?.let { Color(it) } ?: c1
            val brush = when (composition.bgStyle ?: BgStyle.SOLID) {
                BgStyle.SOLID -> SolidColor(c1)
                BgStyle.VERTICAL -> Brush.verticalGradient(listOf(c1, c2))
                BgStyle.DIAGONAL -> Brush.linearGradient(listOf(c1, c2))
                BgStyle.RADIAL -> Brush.radialGradient(listOf(c2, c1))
            }
            val canvasBackground: Brush = if (
                transparentBackground && composition.bgColor == null && composition.bgColor2 == null && composition.bgStyle == null
            ) SolidColor(Color.Transparent) else brush
            Box(Modifier.fillMaxSize().background(canvasBackground).clipToBounds()) {
                Row(Modifier.fillMaxSize()) {
                    composition.rail?.let {
                        Rail(it, data, interactive, onOpenPersonalization.takeIf { personalizationInRail },
                            onOpenSystemSettingsFallback.takeIf { composition.topBar == null })
                    }
                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        composition.topBar?.let { TopBar(it, data, interactive, onOpenPersonalization.takeUnless { personalizationInRail }, onOpenSystemSettingsFallback) }
                        if (composition.topBar == null && composition.rail == null && (onOpenPersonalization != null || onOpenSystemSettingsFallback != null)) {
                            Row(Modifier.fillMaxWidth().padding(horizontal = 24.d(), vertical = 8.d()), horizontalArrangement = Arrangement.spacedBy(12.d(), Alignment.End)) {
                                onOpenSystemSettingsFallback?.let { GlyphButton("Ajustes", palette.onBackground, interactive, it) }
                                onOpenPersonalization?.let { LauncherPersonalizationButton(it) }
                            }
                        }
                        val pad = (composition.padding ?: 24).d()
                        val contentBlocks = composition.blocks.withIndex().filter { it.value.placement == BlockPlacement.CONTENT }
                        val bottomBlocks = composition.blocks.withIndex().filter { it.value.placement == BlockPlacement.BOTTOM }
                        val hasTitleBlock = contentBlocks.any { it.value.type == ThemeBlockType.TITLE }
                        val firstFocusableBlock = composition.blocks.indexOfFirst { block ->
                            block.type in setOf(
                                ThemeBlockType.APPS_ROW,
                                ThemeBlockType.APPS_GRID,
                                ThemeBlockType.FAVORITES_ROW,
                                ThemeBlockType.RECENTS_ROW,
                                ThemeBlockType.MEDIA_ROW
                            ) && data.itemCount(block) > 0
                                && data.isItemClickable(block, 0)
                        }
                        if (sectionContent != null) {
                            Box(Modifier.fillMaxWidth().weight(1f).padding(pad)) { sectionContent() }
                        } else Column(Modifier.fillMaxWidth().weight(1f)) {
                            Column(
                                Modifier.fillMaxWidth().weight(1f)
                                    .then(if (interactive) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                                    .padding(
                                        start = pad,
                                        end = pad,
                                        top = if (composition.topBar == null && composition.rail == null && !hasTitleBlock) 48.d() else pad / 2,
                                        bottom = pad / 2
                                    ),
                                verticalArrangement = Arrangement.spacedBy((composition.spacing ?: 16).d())
                            ) {
                                contentBlocks.forEach { (i, block) ->
                                    BlockView(
                                        block = block,
                                        title = title,
                                        data = data,
                                        interactive = interactive,
                                        selected = i == selectedBlock,
                                        firstItemFocusRequester = firstItemFocusRequester.takeIf { i == firstFocusableBlock }
                                    )
                                }
                            }
                            if (bottomBlocks.isNotEmpty()) {
                                Column(
                                    Modifier.fillMaxWidth().padding(horizontal = pad, vertical = pad / 2),
                                    verticalArrangement = Arrangement.spacedBy((composition.spacing ?: 16).d())
                                ) {
                                    bottomBlocks.forEach { (i, block) ->
                                        BlockView(
                                            block = block,
                                            title = title,
                                            data = data,
                                            interactive = interactive,
                                            selected = i == selectedBlock,
                                            firstItemFocusRequester = firstItemFocusRequester.takeIf { i == firstFocusableBlock }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                if (sectionContent == null && composition.topBar == null && composition.rail == null && composition.blocks.none { it.type == ThemeBlockType.TITLE }) {
                    Text(
                        title,
                        Modifier.align(Alignment.TopStart)
                            .fillMaxWidth()
                            .heightIn(min = 48.d())
                            .padding(horizontal = (composition.padding ?: 24).d(), vertical = 10.d()),
                        color = palette.onBackground,
                        fontSize = 22.s(),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ───────────── Tile: superficie con foco configurable ─────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun Tile(
    interactive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    shape: Shape,
    container: Color,
    contentColor: Color,
    focusStyle: FocusStyle,
    focusScale: Float,
    focusColor: Color,
    onFocus: () -> Unit = {},
    content: @Composable () -> Unit
) {
    if (!interactive) {
        Surface(modifier, shape = shape, colors = SurfaceDefaults.colors(containerColor = container, contentColor = contentColor)) { content() }
    } else {
        val scaled = focusStyle != FocusStyle.BORDER
        val bordered = focusStyle == FocusStyle.BORDER || focusStyle == FocusStyle.SCALE_BORDER
        Surface(
            onClick = onClick,
            modifier = modifier.onFocusChanged { if (it.hasFocus) onFocus() },
            shape = ClickableSurfaceDefaults.shape(shape),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = container, contentColor = contentColor,
                focusedContainerColor = container, focusedContentColor = contentColor,
                pressedContainerColor = container, pressedContentColor = contentColor
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = if (scaled) focusScale else 1f),
            border = ClickableSurfaceDefaults.border(
                focusedBorder = if (bordered) Border(BorderStroke(3.dp, focusColor), shape = shape) else Border.None
            ),
            glow = ClickableSurfaceDefaults.glow(
                focusedGlow = if (focusStyle == FocusStyle.GLOW) Glow(focusColor.copy(alpha = 0.45f), 20.dp) else Glow.None
            )
        ) { content() }
    }
}

// ───────────── Bloques ─────────────

private fun BlockAlign.h() = when (this) {
    BlockAlign.START -> Alignment.Start
    BlockAlign.CENTER -> Alignment.CenterHorizontally
    BlockAlign.END -> Alignment.End
}

private fun BlockAlign.box() = when (this) {
    BlockAlign.START -> Alignment.BottomStart
    BlockAlign.CENTER -> Alignment.BottomCenter
    BlockAlign.END -> Alignment.BottomEnd
}

@Composable
private fun BlockView(
    block: ThemeBlock,
    title: String,
    data: CanvasData,
    interactive: Boolean,
    selected: Boolean,
    firstItemFocusRequester: FocusRequester?
) {
    val palette = LocalLauncherStyle.current.palette
    val shape = RoundedCornerShape(block.rCorner().d())
    val bg = block.bgColor?.let { Color(it).copy(alpha = (block.bgAlpha ?: 100) / 100f) } ?: Color.Transparent
    val textColor = block.textColor?.let { Color(it) } ?: palette.onBackground
    Column(
        Modifier.fillMaxWidth()
            .then(if (selected) Modifier.border(2.dp, palette.onBackground, shape) else Modifier)
            .background(bg, shape)
            .padding(block.rPadding().d()),
        horizontalAlignment = block.rAlign().h(),
        verticalArrangement = Arrangement.spacedBy(8.d())
    ) {
        val heading = block.text?.takeIf { it.isNotBlank() }
        when (block.type) {
            ThemeBlockType.TITLE -> Text(
                heading ?: title,
                Modifier.fillMaxWidth(),
                fontSize = block.rTextSize().s(),
                color = textColor,
                textAlign = when (block.rAlign()) {
                    BlockAlign.START -> TextAlign.Start
                    BlockAlign.CENTER -> TextAlign.Center
                    BlockAlign.END -> TextAlign.End
                }
            )
            ThemeBlockType.SPACER -> Spacer(Modifier.height(((block.weight ?: 1) * 12).d()))
            ThemeBlockType.HERO -> Hero(block, title, data, interactive)
            ThemeBlockType.CHIPS_ROW -> Chips(block, data, interactive)
            else -> {
                val contentAlignment = when (block.rAlign()) {
                    BlockAlign.START -> Alignment.Start
                    BlockAlign.CENTER -> Alignment.CenterHorizontally
                    BlockAlign.END -> Alignment.End
                }
                if (heading != null && block.supportsSectionHeader()) {
                    Text(
                        heading,
                        Modifier.fillMaxWidth(),
                        fontSize = block.rTextSize().s(),
                        color = textColor,
                        textAlign = when (block.rAlign()) {
                            BlockAlign.START -> TextAlign.Start
                            BlockAlign.CENTER -> TextAlign.Center
                            BlockAlign.END -> TextAlign.End
                        }
                    )
                }
                if (block.type == ThemeBlockType.APPS_GRID) {
                    CardsGrid(block, data, interactive, firstItemFocusRequester)
                } else {
                    val rowAlignment = when (block.rAlign()) {
                        BlockAlign.START -> Alignment.Start
                        BlockAlign.CENTER -> Alignment.CenterHorizontally
                        BlockAlign.END -> Alignment.End
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = when (block.rAlign()) {
                        BlockAlign.START -> Arrangement.Start
                        BlockAlign.CENTER -> Arrangement.Center
                        BlockAlign.END -> Arrangement.End
                    }) {
                        CardsRow(block, data, interactive, firstItemFocusRequester)
                    }
                }
            }
        }
    }
}

@Composable
private fun CardsRow(block: ThemeBlock, data: CanvasData, interactive: Boolean, firstItemFocusRequester: FocusRequester?) {
    val n = minOf(block.maxItems ?: 8, data.itemCount(block))
    val padding = block.rPadding().d()
    Row(
        Modifier.then(if (interactive) Modifier.horizontalScroll(rememberScrollState()) else Modifier)
            .padding(horizontal = padding, vertical = 8.d()),
        horizontalArrangement = Arrangement.spacedBy(block.rSpacing().d())
    ) {
        repeat(n) { index ->
            AppCard(block, index, data, interactive, firstItemFocusRequester.takeIf { index == 0 })
        }
    }
}

    @Composable
private fun CardsGrid(block: ThemeBlock, data: CanvasData, interactive: Boolean, firstItemFocusRequester: FocusRequester?) {
    val cols = (block.columns ?: 4).coerceIn(2, 8)
    val n = minOf(block.maxItems ?: 12, data.itemCount(block))
    BoxWithConstraints {
        val cellWidth = ((maxWidth.value - (2 * block.rPadding()) - ((cols - 1) * block.rSpacing())) / cols).coerceAtLeast(1f)
        val computedHeight = (cellWidth / block.rAspect().ratio).toInt().coerceIn(32, 320)
        val effectiveBlock = block.copy(itemHeight = block.itemHeight ?: computedHeight)
        Column(
            Modifier.padding(block.rPadding().d()),
            verticalArrangement = Arrangement.spacedBy(block.rSpacing().d())
        ) {
            (0 until n).chunked(cols).forEach { rowIdx ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(block.rSpacing().d())) {
                    rowIdx.forEach { index ->
                        AppCard(effectiveBlock, index, data, interactive, firstItemFocusRequester.takeIf { index == 0 })
                    }
                }
            }
        }
    }
}

@Composable
private fun AppCard(
    block: ThemeBlock,
    index: Int,
    data: CanvasData,
    interactive: Boolean,
    focusRequester: FocusRequester?
) {
    val palette = LocalLauncherStyle.current.palette
    val h = block.rItemH()
    val w = (h * block.rAspect().ratio).toInt()
    val style = block.rCardStyle()
    val container = when (style) {
        CardStyle.FILLED -> palette.surface
        CardStyle.LIGHT -> Color.White
        CardStyle.GLASS -> Color.White.copy(alpha = 0.14f)
        CardStyle.OUTLINE, CardStyle.NONE -> Color.Transparent
    }
    val textColor = block.textColor?.let { Color(it) } ?: palette.onBackground
    val shape = RoundedCornerShape(block.rCorner().d())
    val pos = block.rLabelPos()
    Column(
        Modifier.width(w.d()),
        horizontalAlignment = when (block.rAlign()) {
            BlockAlign.START -> Alignment.Start
            BlockAlign.CENTER -> Alignment.CenterHorizontally
            BlockAlign.END -> Alignment.End
        }
    ) {
        Tile(
            interactive = interactive && data.isItemClickable(block, index),
            onClick = { data.onItemClick(block, index) },
            modifier = Modifier.size(w.d(), h.d())
                .then(focusRequester?.takeIf { data.isItemClickable(block, index) }?.let(Modifier::focusRequester) ?: Modifier)
                .then(if (style == CardStyle.OUTLINE) Modifier.border(1.dp, palette.onMuted, shape) else Modifier),
            shape = shape,
            container = container,
            contentColor = if (style == CardStyle.LIGHT) Color.Black else textColor,
            focusStyle = block.rFocusStyle(),
            focusScale = block.rFocusScale(),
            focusColor = textColor
        ) {
            Box(Modifier.fillMaxSize()) {
                data.Icon(block, index, Modifier.fillMaxSize())
                if (pos == LabelPos.INSIDE) {
                    Text(
                        data.label(block, index),
                        Modifier.align(Alignment.BottomStart).fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.68f), RoundedCornerShape(bottomStart = block.rCorner().d(), bottomEnd = block.rCorner().d()))
                            .padding(horizontal = 8.d(), vertical = 6.d()),
                        fontSize = 12.s(), color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (pos == LabelPos.BELOW) {
            Text(
                data.label(block, index), Modifier.padding(top = 6.d()),
                fontSize = 12.s(), color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun Hero(block: ThemeBlock, title: String, data: CanvasData, interactive: Boolean) {
    val palette = LocalLauncherStyle.current.palette
    val shape = RoundedCornerShape(block.rCorner().d())
    val base = block.bgColor?.let { Color(it) } ?: palette.surface
    val textColor = block.textColor?.let { Color(it) } ?: Color.White
    val a = block.rAlign()
    Box(
        Modifier.fillMaxWidth().height(block.rItemH().d()).clip(shape)
            .background(Brush.horizontalGradient(listOf(base, palette.background)))
    ) {
        data.HeroBackground(block, Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)))))
        Column(
            Modifier.align(a.box()).padding(block.rPadding().d().coerceAtLeast(24.d())),
            horizontalAlignment = a.h(),
            verticalArrangement = Arrangement.spacedBy(6.d())
        ) {
            Text(block.text?.takeIf { it.isNotBlank() } ?: title, fontSize = block.rTextSize().s(), color = textColor, fontWeight = FontWeight.SemiBold)
            block.subtitle?.takeIf { it.isNotBlank() }?.let { Text(it, fontSize = 14.s(), color = textColor.copy(alpha = 0.75f)) }
            block.buttonText?.takeIf { it.isNotBlank() }?.let { label ->
                Tile(
                    interactive, { data.onItemClick(block, 0) }, Modifier, RoundedCornerShape(50),
                    Color.White.copy(alpha = 0.22f), Color.White, FocusStyle.SCALE_BORDER, 1.06f, Color.White
                ) { Text(label, Modifier.padding(horizontal = 16.d(), vertical = 7.d()), fontSize = 13.s(), color = Color.White) }
            }
        }
    }
}

private val CHIP_COLORS = listOf(0xFF1F8F8B, 0xFF6B4FBB, 0xFFB04FB0, 0xFF1F7BB8, 0xFF5E8F2F).map { Color(it) }

@Composable
private fun Chips(block: ThemeBlock, data: CanvasData, interactive: Boolean) {
    val palette = LocalLauncherStyle.current.palette
    val labels = remember(block.text) {
        (block.text ?: "Categoría 1,Categoría 2,Categoría 3").split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(6)
    }
    if (labels.isEmpty()) return
    val textColor = block.textColor?.let { Color(it) } ?: Color.White
    BoxWithConstraints {
        val chipWidth = ((maxWidth - block.rSpacing().d() * (labels.size - 1)) / labels.size).coerceAtLeast(1.dp)
        Row(Modifier.fillMaxWidth().padding(vertical = 6.d()), horizontalArrangement = Arrangement.spacedBy(block.rSpacing().d())) {
            labels.forEachIndexed { i, label ->
                val c = if (block.multicolor == true) CHIP_COLORS[i % CHIP_COLORS.size]
                else block.bgColor?.let { Color(it) } ?: palette.surface
                Tile(
                    interactive, { data.onNavClick(label) }, Modifier.width(chipWidth).height(block.rItemH().d()),
                    RoundedCornerShape(block.rCorner().d()), c, textColor, block.rFocusStyle(), block.rFocusScale(), Color.White
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(label, fontSize = block.rTextSize().s(), color = textColor, maxLines = 1)
                    }
                }
            }
        }
    }
}

// ───────────── Barra superior ─────────────

private fun iconForLabel(label: String): ImageVector? {
    val l = label.lowercase()
    return when {
        isPersonalizationLabel(label) -> Icons.Filled.Palette
        "busc" in l || "search" in l -> Icons.Filled.Search
        "inicio" in l || "home" in l -> Icons.Filled.Home
        "ajust" in l || "config" in l || "setting" in l -> Icons.Filled.Settings
        "vivo" in l || "live" in l -> Icons.Filled.LiveTv
        "apps" in l || "aplic" in l -> Icons.Filled.Apps
        "fav" in l -> Icons.Filled.Star
        "notif" in l -> Icons.Filled.Notifications
        "perfil" in l || "profile" in l -> Icons.Filled.Person
        "juego" in l || "game" in l -> Icons.Filled.SportsEsports
        "músi" in l || "music" in l -> Icons.Filled.MusicNote
        "deport" in l -> Icons.Filled.SportsSoccer
        "tienda" in l || "store" in l -> Icons.Filled.Store
        "biblio" in l || "library" in l -> Icons.Filled.VideoLibrary
        "google tv" in l -> Icons.Filled.LiveTv
        else -> null
    }
}

@Composable
private fun NavigationIcon(label: String, tint: Color, modifier: Modifier = Modifier) {
    // Unknown custom destinations get a neutral icon, never an arbitrary initial.
    Icon(iconForLabel(label) ?: Icons.Filled.Widgets, contentDescription = null, modifier = modifier, tint = tint)
}

private fun topBarShowsSettings(cfg: TopBarConfig): Boolean = when (cfg.style) {
    TopBarStyle.MINIMAL -> false
    TopBarStyle.ICONS -> cfg.tabs.orEmpty().split(',').any(::isSettingsLabel)
    else -> cfg.showSettings
}

@Composable
private fun GlyphButton(label: String, color: Color, interactive: Boolean, onClick: () -> Unit) {
    Tile(
        interactive,
        onClick,
        Modifier.selectLauncherSectionOnFocus(label) { if (interactive) onClick() }.semantics { contentDescription = label },
        CircleShape,
        Color.Transparent,
        color,
        FocusStyle.SCALE_BORDER,
        1.1f,
        color
    ) {
        Box(Modifier.size(44.d()), contentAlignment = Alignment.Center) {
            NavigationIcon(label, color, Modifier.size(24.d()))
        }
    }
}

@Composable
private fun TopBar(cfg: TopBarConfig, data: CanvasData, interactive: Boolean, onOpenPersonalization: (() -> Unit)? = null, onOpenSystemSettingsFallback: (() -> Unit)? = null) {
    val activeSection = com.tv.launcher.custom.core.ui.components.LocalLauncherSection.current
    val palette = LocalLauncherStyle.current.palette
    val text = cfg.textColor?.let { Color(it) } ?: palette.onBackground
    val muted = text.copy(alpha = 0.65f)
    var current by remember { mutableIntStateOf(0) }
    val bg = cfg.bgColor?.let { Color(it).copy(alpha = (cfg.bgAlpha ?: 100) / 100f) } ?: Color.Transparent
    val renderedTabs = getRenderedTopBarTabs(cfg)
    Row(
        Modifier.fillMaxWidth().background(bg).padding(horizontal = 32.d(), vertical = 14.d()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.d())
    ) {
        cfg.logo?.takeIf { it.isNotBlank() }?.let { Text(it, fontSize = 22.s(), color = text, fontWeight = FontWeight.Bold) }
        if (shouldShowTopBarSearchButton(cfg)) GlyphButton("Buscar", text, interactive) { data.onNavClick("Buscar") }
        if (cfg.align != BlockAlign.START) Spacer(Modifier.weight(1f))
        if (cfg.style != TopBarStyle.MINIMAL) {
            renderedTabs.forEachIndexed { i, label ->
                val destination = com.tv.launcher.custom.core.ui.components.resolveLauncherSection(label)
                val selected = if (interactive && destination != null) destination == activeSection else i == current
                TopTab(label, selected, cfg.style, text, muted, interactive, { current = i; if (interactive && destination != null) data.onNavClick(label) }) { data.onNavClick(label) }
            }
        }
        if (cfg.align != BlockAlign.END) Spacer(Modifier.weight(1f))
        if (cfg.showClock) SharedClockText(fontSize = 18.s(), color = text)
        if (topBarShowsSettings(cfg)) {
            GlyphButton("Ajustes", text, interactive) { data.onNavClick("Ajustes") }
        }
        onOpenSystemSettingsFallback?.let { GlyphButton("Ajustes", text, interactive, it) }
        onOpenPersonalization?.let { LauncherPersonalizationButton(it) }
    }
}

@Composable
private fun TopTab(
    label: String, selected: Boolean, style: TopBarStyle, text: Color, muted: Color,
    interactive: Boolean, onFocus: () -> Unit, onClick: () -> Unit
) {
    when (style) {
        TopBarStyle.TABS_PILL -> Tile(
            interactive, onClick, Modifier, RoundedCornerShape(50),
            if (selected) Color.White.copy(alpha = 0.95f) else Color.Transparent,
            if (selected) Color.Black else muted, FocusStyle.SCALE, 1.08f, text, onFocus
        ) {
            Text(label, Modifier.padding(horizontal = 16.d(), vertical = 6.d()), fontSize = 16.s(), color = if (selected) Color.Black else muted)
        }
        TopBarStyle.TABS_TEXT -> Tile(
            interactive, onClick, Modifier, RoundedCornerShape(8.d()), Color.Transparent, text,
            FocusStyle.SCALE, 1.06f, text, onFocus
        ) {
            Column(Modifier.padding(horizontal = 8.d(), vertical = 4.d()), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, fontSize = 16.s(), color = if (selected) text else muted, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                Box(Modifier.height(2.d()).width(24.d()).background(if (selected) text else Color.Transparent))
            }
        }
        TopBarStyle.ICONS -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Tile(
                interactive, onClick, Modifier, CircleShape,
                if (selected) text.copy(alpha = 0.2f) else Color.Transparent, text, FocusStyle.SCALE_BORDER, 1.1f, text, onFocus
            ) { Box(Modifier.size(40.d()), contentAlignment = Alignment.Center) { NavigationIcon(label, text, Modifier.size(24.d())) } }
            if (selected) Text(label, fontSize = 11.s(), color = text)
        }
        TopBarStyle.MINIMAL -> {}
    }
}

// ───────────── Menú lateral ─────────────

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun Rail(cfg: RailConfig, data: CanvasData, interactive: Boolean, onOpenPersonalization: (() -> Unit)? = null, onOpenSystemSettingsFallback: (() -> Unit)? = null) {
    val activeSection = com.tv.launcher.custom.core.ui.components.LocalLauncherSection.current
    val palette = LocalLauncherStyle.current.palette
    val items = cfg.items.orEmpty().split(",").map { it.trim() }.filter { it.isNotEmpty() }
    var current by remember { mutableIntStateOf(0) }
    val text = cfg.textColor?.let { Color(it) } ?: palette.onBackground
    val muted = text.copy(alpha = 0.7f)
    val w = cfg.width ?: when (cfg.style) { RailStyle.ICONS -> 72; else -> 220 }
    val shape = RoundedCornerShape((cfg.cornerRadius ?: if (cfg.floating) 24 else 0).d())
    val bg = cfg.bgColor?.let { Color(it).copy(alpha = (cfg.bgAlpha ?: 100) / 100f) } ?: Color.Transparent
    val size = (cfg.textSize ?: 16).s()
    Column(
        Modifier.fillMaxHeight().padding(if (cfg.floating) 16.d() else 0.d()).width(w.d())
            .clip(shape).background(bg, shape).padding(horizontal = 12.d(), vertical = 16.d()),
        verticalArrangement = Arrangement.spacedBy(6.d()),
        horizontalAlignment = if (cfg.style == RailStyle.ICONS) Alignment.CenterHorizontally else Alignment.Start
    ) {
        if (shouldShowRailProfileAvatar(cfg)) {
            Box(Modifier.size(36.d()).background(palette.surface, CircleShape), contentAlignment = Alignment.Center) {
                NavigationIcon("Perfil", text, Modifier.size(22.d()))
            }
            Spacer(Modifier.height(6.d()))
        }
        items.forEachIndexed { i, label ->
            val destination = com.tv.launcher.custom.core.ui.components.resolveLauncherSection(label)
            val sel = if (interactive && destination != null) destination == activeSection else i == current
            val fg = if (sel && cfg.style != RailStyle.ICONS) Color.Black else muted
            val container = if (sel) Color.White.copy(alpha = if (cfg.style == RailStyle.ICONS) 0.2f else 0.92f) else Color.Transparent
            when (cfg.style) {
                RailStyle.ICONS -> Tile(
                    interactive, { data.onNavClick(label) }, Modifier, CircleShape, container, text,
                    FocusStyle.SCALE_BORDER, 1.1f, text, { current = i; if (interactive && destination != null) data.onNavClick(label) }
                ) { Box(Modifier.size(44.d()), contentAlignment = Alignment.Center) { NavigationIcon(label, text, Modifier.size(24.d())) } }
                else -> Tile(
                    interactive, { data.onNavClick(label) }, Modifier.fillMaxWidth(), RoundedCornerShape(10.d()), container, fg,
                    FocusStyle.BORDER, 1f, text, { current = i; if (interactive && destination != null) data.onNavClick(label) }
                ) {
                    Row(Modifier.padding(horizontal = 12.d(), vertical = 10.d()), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.d())) {
                        if (cfg.style == RailStyle.ICONS_LABELS) {
                            val iconSize = cfg.textSize ?: 16
                            NavigationIcon(label, fg, Modifier.size(iconSize.d().coerceAtMost(28.dp)))
                        }
                        Text(label, fontSize = size, color = fg, maxLines = 1)
                    }
                }
            }
            if (i == items.indexOfLast(::isSettingsLabel)) {
                onOpenPersonalization?.let { LauncherPersonalizationButton(it) }
            }
        }
        onOpenSystemSettingsFallback?.let { GlyphButton("Ajustes", text, interactive, it) }
        if (items.none(::isSettingsLabel)) onOpenPersonalization?.let { LauncherPersonalizationButton(it) }
    }
}
