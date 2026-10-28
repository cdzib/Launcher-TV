package com.tv.launcher.custom.feature.search

import com.tv.launcher.custom.core.ui.components.sectionSurfaceBackground

import com.tv.launcher.custom.core.ui.components.AppIcon

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.tv.launcher.custom.core.ui.R
import com.tv.launcher.custom.core.ui.theme.LocalLauncherStyle
import com.tv.launcher.custom.core.model.apps.AppEntry

internal enum class SearchFieldVariant { BASE, FOCUS }

@Composable
internal fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    focusRequester: FocusRequester,
    keyboardVisible: Boolean,
    onToggleKeyboard: () -> Unit,
    onShowKeyboard: () -> Unit,
    onVoice: () -> Unit,
    modifier: Modifier = Modifier,
    variant: SearchFieldVariant = SearchFieldVariant.BASE
) {
    val style = LocalLauncherStyle.current
    val palette = style.palette
    val focusVariant = variant == SearchFieldVariant.FOCUS
    val shape = RoundedCornerShape(if (style.declarativeSurfaces) style.tileRadius else if (focusVariant) (style.tileRadius * .55f).coerceAtLeast(10.dp) else 12.dp)
    var focused by remember { mutableStateOf(false) }
    val borderColor = if (focused) palette.accent else palette.onMuted.copy(alpha = if (focusVariant) .26f else .24f)
    val fieldBackground = if (focusVariant) palette.background.copy(alpha = .52f) else palette.surface
    val queryStyle = TextStyle(
        color = palette.onBackground,
        fontSize = if (focusVariant) 22.sp else 16.sp,
        fontWeight = if (focusVariant) FontWeight.Medium else FontWeight.Normal,
        letterSpacing = if (focusVariant) (-0.22).sp else 0.sp
    )
    val placeholderStyle = TextStyle(
        color = palette.onMuted.copy(alpha = .8f),
        fontSize = if (focusVariant) 19.sp else 14.sp
    )
    val actionSize = if (focusVariant) 38.dp else 34.dp
    val actionIconSize = if (focusVariant) 17.dp else 16.dp
    val actionGap = if (focusVariant) 8.dp else 10.dp
    val actionRadius = if (focusVariant) (style.tileRadius * .4f).coerceAtLeast(7.dp) else 8.dp

    Row(
        modifier
            .heightIn(min = if (focusVariant) 58.dp else 52.dp)
            .then(if (style.glassSurfaces) Modifier.sectionSurfaceBackground(shape) else Modifier.background(fieldBackground, shape))
            .border(if (focused) 2.dp else 1.dp, borderColor, shape)
            .padding(start = if (focusVariant) 18.dp else 12.dp, end = if (focusVariant) 12.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (focusVariant) 12.dp else 10.dp)
    ) {
        Icon(
            Icons.Outlined.Search,
            null,
            tint = palette.onMuted,
            modifier = Modifier.size(if (focusVariant) 22.dp else 20.dp)
        )
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            readOnly = true,
            singleLine = true,
            textStyle = queryStyle,
            cursorBrush = SolidColor(palette.accent),
            modifier = Modifier.weight(1f)
                .focusRequester(focusRequester)
                .onFocusChanged { focused = it.isFocused }
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when {
                        event.key == Key.Enter || event.key == Key.NumPadEnter ||
                            event.key == Key.DirectionCenter || event.key == Key.DirectionDown -> {
                            onShowKeyboard()
                            true
                        }
                        event.key == Key.Backspace -> {
                            onQueryChange(query.dropLast(1))
                            true
                        }
                        !event.isCtrlPressed && !event.isAltPressed && !event.isMetaPressed &&
                            event.utf16CodePoint >= 32 && event.utf16CodePoint != 127 -> {
                            onQueryChange(query + event.utf16CodePoint.toChar())
                            true
                        }
                        else -> false
                    }
                },
            decorationBox = { inner ->
                Box {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(if (focusVariant) R.string.search_focus_hint else R.string.search_apps_hint),
                            style = placeholderStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    inner()
                }
            }
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(actionGap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchFieldAction(
                icon = Icons.Outlined.Mic,
                description = stringResource(R.string.search_voice),
                onClick = onVoice,
                size = actionSize,
                iconSize = actionIconSize,
                radius = actionRadius
            )
            SearchFieldAction(
                icon = Icons.Outlined.Keyboard,
                description = stringResource(if (keyboardVisible) R.string.search_keyboard_hide else R.string.search_keyboard_show),
                onClick = onToggleKeyboard,
                size = actionSize,
                iconSize = actionIconSize,
                radius = actionRadius,
                selected = keyboardVisible
            )
            SearchFieldAction(
                icon = Icons.Outlined.Close,
                description = stringResource(R.string.studio_clear_search),
                onClick = { onQueryChange("") },
                size = actionSize,
                iconSize = actionIconSize,
                radius = actionRadius
            )
        }
    }
}

@Composable
private fun SearchFieldAction(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    size: Dp,
    iconSize: Dp,
    radius: Dp,
    selected: Boolean = false
) {
    val palette = LocalLauncherStyle.current.palette
    val shape = RoundedCornerShape(radius)
    Button(
        onClick = onClick,
        modifier = Modifier.size(size),
        shape = ButtonDefaults.shape(shape),
        colors = ButtonDefaults.colors(
            containerColor = if (selected) palette.accent.copy(alpha = .18f) else Color.Transparent,
            contentColor = if (selected) palette.accent else palette.onMuted,
            focusedContainerColor = palette.accent,
            focusedContentColor = palette.background
        ),
        scale = ButtonDefaults.scale(focusedScale = 1f),
        border = ButtonDefaults.border(focusedBorder = Border(BorderStroke(2.dp, palette.onBackground), shape = shape)),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(icon, description, Modifier.size(iconSize), tint = LocalContentColor.current)
    }
}

@Composable
internal fun SearchAppIcon(app: AppEntry?, modifier: Modifier) {
    val muted = LocalLauncherStyle.current.palette.onMuted
    Box(modifier, contentAlignment = Alignment.Center) {
        Icon(Icons.Outlined.Apps, null, tint = muted, modifier = Modifier.fillMaxSize().padding(8.dp))
        if (app != null && !LocalInspectionMode.current) {
            androidx.compose.runtime.key(app.packageName) { AppIcon(app.packageName, Modifier.fillMaxSize()) }
        }
    }
}

@Composable
internal fun SearchAppTile(
    app: AppEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 64.dp,
    radius: Dp = 12.dp,
    labelFontSize: TextUnit = 11.sp,
    labelSpacing: Dp = 6.dp,
    containerColor: Color,
    focusedContainerColor: Color,
    focusedBorderColor: Color,
    selectedBorderColor: Color? = null,
    isSelected: Boolean = false,
    focusedScale: Float = 1f,
    onFocused: () -> Unit = {}
) {
    val palette = LocalLauncherStyle.current.palette
    var focused by remember { mutableStateOf(false) }
    val style = LocalLauncherStyle.current
    val shape = RoundedCornerShape(if (style.declarativeSurfaces) style.tileRadius else radius)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth()
                .height(height)
                .onFocusChanged { state ->
                    focused = state.isFocused
                    if (state.isFocused) onFocused()
                }
                .semantics {
                    contentDescription = app.label
                    selected = isSelected
                },
            shape = ClickableSurfaceDefaults.shape(shape),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = if (style.glassSurfaces) Color.Transparent else containerColor,
                focusedContainerColor = if (style.glassSurfaces) Color.Transparent else focusedContainerColor
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = focusedScale),
            border = ClickableSurfaceDefaults.border(
                border = if (isSelected && selectedBorderColor != null) {
                    Border(BorderStroke(2.dp, selectedBorderColor), shape = shape)
                } else {
                    Border.None
                },
                focusedBorder = Border(BorderStroke(3.dp, focusedBorderColor), shape = shape)
            )
        ) {
            Box(Modifier.fillMaxSize().sectionSurfaceBackground(shape), contentAlignment = Alignment.Center) {
                SearchAppIcon(app, Modifier.size(height * .625f))
            }
        }
        Text(
            text = app.label,
            modifier = Modifier.fillMaxWidth().padding(top = labelSpacing, start = 2.dp, end = 2.dp),
            color = if (focused) palette.onBackground else palette.onMuted,
            fontSize = labelFontSize,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun SearchKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 32.dp,
    radius: Dp = 8.dp,
    containerColor: Color,
    contentColor: Color,
    focusedContainerColor: Color,
    focusedContentColor: Color,
    focusedBorderColor: Color,
    borderColor: Color,
    focusRequester: FocusRequester? = null,
    content: @Composable () -> Unit
) {
    val style = LocalLauncherStyle.current
    val shape = RoundedCornerShape(if (style.declarativeSurfaces) style.tileRadius else radius)
    val keyModifier = modifier.height(height).then(
        if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier
    )
    Surface(
        onClick = onClick,
        modifier = keyModifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (style.glassSurfaces) Color.Transparent else containerColor,
            contentColor = if (style.glassSurfaces) style.palette.onBackground else contentColor,
            focusedContainerColor = if (style.glassSurfaces) Color.Transparent else focusedContainerColor,
            focusedContentColor = if (style.glassSurfaces) style.palette.onBackground else focusedContentColor
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        border = ClickableSurfaceDefaults.border(
            border = Border(BorderStroke(1.dp, borderColor), shape = shape),
            focusedBorder = Border(BorderStroke(2.dp, focusedBorderColor), shape = shape)
        )
    ) {
        Box(Modifier.fillMaxSize().sectionSurfaceBackground(shape), contentAlignment = Alignment.Center) { content() }
    }
}

@Composable
internal fun SearchChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalLauncherStyle.current.palette
    val style = LocalLauncherStyle.current
    val shape = RoundedCornerShape(if (style.declarativeSurfaces) style.tileRadius else 999.dp)
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 34.dp),
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (style.glassSurfaces) Color.Transparent else palette.background.copy(alpha = .42f),
            contentColor = palette.onMuted,
            focusedContainerColor = if (style.glassSurfaces) Color.Transparent else palette.background.copy(alpha = .42f),
            focusedContentColor = palette.onBackground
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        border = ClickableSurfaceDefaults.border(
            border = Border(BorderStroke(1.dp, palette.onMuted.copy(alpha = .26f)), shape = shape),
            focusedBorder = Border(BorderStroke(3.dp, palette.onBackground), shape = shape)
        )
    ) {
        Box(Modifier.sectionSurfaceBackground(shape).padding(horizontal = 14.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
internal fun SearchPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val palette = LocalLauncherStyle.current.palette
    val shape = RoundedCornerShape(8.dp)
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 36.dp),
        enabled = enabled,
        shape = ButtonDefaults.shape(shape),
        colors = ButtonDefaults.colors(
            containerColor = palette.accent,
            contentColor = palette.background,
            focusedContainerColor = palette.accent,
            focusedContentColor = palette.background
        ),
        scale = ButtonDefaults.scale(focusedScale = 1f),
        border = ButtonDefaults.border(focusedBorder = Border(BorderStroke(3.dp, palette.onBackground), shape = shape)),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun SearchMeta(countText: String, modeText: String, modifier: Modifier = Modifier) {
    val palette = LocalLauncherStyle.current.palette
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(countText, color = palette.onMuted, fontSize = 11.sp)
        Text("·", color = palette.onMuted, fontSize = 11.sp)
        Text(modeText, color = palette.onMuted, fontSize = 11.sp)
    }
}

@Composable
internal fun SearchHero(app: AppEntry?, onLaunch: () -> Unit, modifier: Modifier = Modifier) {
    val style = LocalLauncherStyle.current
    val palette = style.palette
    val shape = RoundedCornerShape(if (style.declarativeSurfaces) style.tileRadius else 16.dp)
    Row(
        modifier.fillMaxWidth()
            .height(72.dp)
            .then(if (style.glassSurfaces) Modifier.sectionSurfaceBackground(shape) else Modifier.background(
                Brush.linearGradient(listOf(lerp(palette.surface, palette.background, .32f), palette.surface)),
                shape
            ))
            .border(1.dp, Color.White.copy(alpha = .08f), shape)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SearchAppIcon(app, Modifier.size(38.dp))
        Text(
            text = stringResource(if (app == null) R.string.studio_no_results else R.string.studio_selected),
            color = palette.onMuted,
            fontSize = 11.sp
        )
        Text(
            text = app?.label ?: stringResource(R.string.no_matching_apps),
            color = palette.onBackground,
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.38).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(if (app == null) R.string.studio_try_another_name else R.string.studio_ready_to_open),
            color = palette.onMuted,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
        if (app != null) {
            SearchPrimaryButton(stringResource(R.string.studio_open), onLaunch)
        }
    }
}

@Composable
internal fun SearchEmptyState(
    text: String,
    modifier: Modifier = Modifier,
    minHeight: Dp = 120.dp,
    fontSize: TextUnit = 14.sp
) {
    val palette = LocalLauncherStyle.current.palette
    Box(
        modifier.fillMaxWidth().heightIn(min = minHeight),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = palette.onMuted, fontSize = fontSize, textAlign = TextAlign.Center)
    }
}

@Composable
internal fun SearchRailIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val palette = LocalLauncherStyle.current.palette
    val shape = RoundedCornerShape(8.dp)
    Button(
        onClick = onClick,
        modifier = modifier.size(44.dp).semantics { selected = isSelected },
        shape = ButtonDefaults.shape(shape),
        colors = ButtonDefaults.colors(
            containerColor = if (isSelected) palette.accent else Color.Transparent,
            contentColor = if (isSelected) palette.background else palette.onMuted,
            focusedContainerColor = palette.accent,
            focusedContentColor = palette.background
        ),
        scale = ButtonDefaults.scale(focusedScale = 1f),
        border = ButtonDefaults.border(focusedBorder = Border(BorderStroke(3.dp, palette.onBackground), shape = shape)),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(icon, description, Modifier.size(20.dp), tint = LocalContentColor.current)
    }
}

@Composable
internal fun SearchHints(
    hints: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 12.sp
) {
    val palette = LocalLauncherStyle.current.palette
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        hints.forEach { (key, label) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.height(20.dp)
                        .widthIn(min = 20.dp)
                        .border(1.dp, palette.onMuted.copy(alpha = .55f), RoundedCornerShape(5.dp))
                        .padding(horizontal = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(key, color = palette.onBackground, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(5.dp))
                Text(label, color = palette.onMuted, fontSize = fontSize)
            }
        }
    }
}
