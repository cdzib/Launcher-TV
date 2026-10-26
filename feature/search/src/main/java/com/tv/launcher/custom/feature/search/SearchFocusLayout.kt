package com.tv.launcher.custom.feature.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Text
import com.tv.launcher.custom.core.ui.R
import com.tv.launcher.custom.core.ui.theme.LocalLauncherStyle
import com.tv.launcher.custom.core.model.apps.AppEntry

@Composable
internal fun SearchFocusLayout(
    apps: List<AppEntry>,
    filteredApps: List<AppEntry>,
    query: String,
    keyboardVisible: Boolean,
    keyboardRequest: Int,
    fieldFocus: FocusRequester,
    onQueryChange: (String) -> Unit,
    onSelect: (String) -> Unit,
    onLaunch: (String) -> Unit,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    onVoice: () -> Unit,
    onShowKeyboard: () -> Unit,
    onToggleKeyboard: () -> Unit,
    embedded: Boolean = false,
    selectedPackage: String? = null
) {
    val style = LocalLauncherStyle.current
    val palette = style.palette
    val firstResultFocus = remember { FocusRequester() }
    val firstKeyFocus = remember { FocusRequester() }
    val isPreview = LocalInspectionMode.current
    val panelBackground = lerp(palette.surface, palette.background, .9f)

    LaunchedEffect(Unit) {
        if (!isPreview && !embedded && !keyboardVisible) {
            withFrameNanos { }
            if (filteredApps.isNotEmpty()) firstResultFocus.requestFocus() else fieldFocus.requestFocus()
        }
    }
    LaunchedEffect(keyboardVisible, keyboardRequest) {
        if (!isPreview && (!embedded || keyboardRequest > 0) && (keyboardVisible || keyboardRequest > 0)) {
            withFrameNanos { }
            if (keyboardVisible) firstKeyFocus.requestFocus() else fieldFocus.requestFocus()
        }
    }

    Row(Modifier.fillMaxSize().background(palette.background)) {
        if (!embedded) FocusRail(
            modifier = Modifier.fillMaxWidth(.27f).fillMaxHeight(),
            onHome = onDismiss,
            onApps = onDismiss,
            onSearch = { fieldFocus.requestFocus() },
            onSettings = onOpenSettings
        )
        Column(Modifier.weight(1f).fillMaxHeight().background(panelBackground)) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                Column(Modifier.fillMaxSize()) {
                    Column(
                        Modifier.fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 26.dp, bottom = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SearchField(
                            query = query,
                            onQueryChange = onQueryChange,
                            focusRequester = fieldFocus,
                            keyboardVisible = keyboardVisible,
                            onToggleKeyboard = onToggleKeyboard,
                            onShowKeyboard = onShowKeyboard,
                            onVoice = onVoice,
                            modifier = Modifier.widthIn(max = 620.dp).fillMaxWidth(),
                            variant = SearchFieldVariant.FOCUS
                        )
                        Spacer(Modifier.height(8.dp))
                        SearchMeta(
                            countText = stringResource(R.string.search_count_of_total, filteredApps.size, apps.size),
                            modeText = stringResource(
                                if (keyboardVisible) R.string.search_mode_keyboard else R.string.search_mode_voice
                            )
                        )
                    }
                    if (apps.isNotEmpty()) {
                        Row(
                            Modifier.fillMaxWidth()
                                .padding(start = 24.dp, end = 24.dp, top = 2.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            apps.take(4).forEach { app ->
                                SearchChip(
                                    label = app.label,
                                    onClick = {
                                        onQueryChange(app.label)
                                        fieldFocus.requestFocus()
                                    },
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(start = 24.dp, end = 78.dp, top = 4.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (filteredApps.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                SearchEmptyState(
                                    text = stringResource(
                                        if (apps.isEmpty()) R.string.search_empty_apps else R.string.search_empty_query
                                    ),
                                    minHeight = 140.dp,
                                    fontSize = 15.sp
                                )
                            }
                        } else {
                            itemsIndexed(filteredApps, key = { _, app -> app.packageName }) { index, app ->
                                SearchAppTile(
                                    app = app,
                                    onClick = { onLaunch(app.packageName) },
                                    modifier = if (app.packageName == (selectedPackage ?: filteredApps.firstOrNull()?.packageName)) Modifier.focusRequester(firstResultFocus) else Modifier,
                                    height = 56.dp,
                                    radius = style.tileRadius,
                                    labelFontSize = 12.sp,
                                    labelSpacing = 7.dp,
                                    containerColor = lerp(palette.background, Color.White, .06f),
                                    focusedContainerColor = lerp(palette.surface, Color.White, .12f),
                                    focusedBorderColor = palette.onBackground,
                                    selectedBorderColor = null,
                                    isSelected = false,
                                    focusedScale = style.focusScale,
                                    onFocused = { onSelect(app.packageName) }
                                )
                            }
                        }
                    }
                    if (keyboardVisible) {
                        FocusKeyboard(
                            firstKeyFocus = firstKeyFocus,
                            onCharacter = { onQueryChange(query + it) },
                            onSpace = { onQueryChange(query + " ") },
                            onBackspace = { onQueryChange(query.dropLast(1)) }
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SearchHints(
                            hints = listOf(
                                "＋" to stringResource(R.string.search_hint_move),
                                "OK" to stringResource(R.string.search_hint_open),
                                "↩" to stringResource(R.string.search_hint_back_short)
                            ),
                            fontSize = 11.sp
                        )
                        Text(
                            text = stringResource(R.string.search_focus_footer_note),
                            color = palette.onMuted,
                            fontSize = 11.sp
                        )
                    }
                }
                if (!keyboardVisible) {
                    AlphabetIndex(
                        Modifier.align(Alignment.TopEnd)
                            .padding(top = 120.dp, end = 22.dp)
                            .height(360.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusRail(
    modifier: Modifier,
    onHome: () -> Unit,
    onApps: () -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit
) {
    val style = LocalLauncherStyle.current
    val palette = style.palette
    Box(modifier.padding(18.dp)) {
        Column(
            Modifier.fillMaxSize()
                .background(
                    palette.surface.copy(alpha = .92f),
                    RoundedCornerShape(style.tileRadius * .85f)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(style.nameRes),
                color = palette.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.96).sp,
                modifier = Modifier.padding(bottom = 18.dp)
            )
            FocusRailItem(stringResource(R.string.launcher_home), onHome)
            FocusRailItem(stringResource(R.string.launcher_apps), onApps)
            FocusRailItem(stringResource(R.string.studio_search_title), onSearch, active = true)
            Spacer(Modifier.weight(1f))
            FocusRailItem(stringResource(R.string.search_nav_settings), onSettings)
        }
    }
}

@Composable
private fun FocusRailItem(label: String, onClick: () -> Unit, active: Boolean = false) {
    val style = LocalLauncherStyle.current
    val palette = style.palette
    val shape = RoundedCornerShape((style.tileRadius * .45f).coerceAtLeast(4.dp))
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 42.dp),
        shape = ButtonDefaults.shape(shape),
        colors = ButtonDefaults.colors(
            containerColor = if (active) palette.accent else Color.Transparent,
            contentColor = if (active) palette.background else palette.onMuted,
            focusedContainerColor = palette.accent,
            focusedContentColor = palette.background
        ),
        scale = ButtonDefaults.scale(focusedScale = 1f),
        border = ButtonDefaults.border(focusedBorder = Border(BorderStroke(2.dp, palette.onBackground), shape = shape)),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AlphabetIndex(modifier: Modifier = Modifier) {
    val palette = LocalLauncherStyle.current.palette
    Column(
        modifier.clearAndSetSemantics {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
    ) {
        "ABCDEFGHIJKLMNÑOPQRSTUVWXYZ".forEach { letter ->
            Text(
                text = letter.toString(),
                color = palette.onMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 13.5.sp
            )
        }
    }
}

@Composable
private fun FocusKeyboard(
    firstKeyFocus: FocusRequester,
    onCharacter: (Char) -> Unit,
    onSpace: () -> Unit,
    onBackspace: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FocusKeyRow("QWERTYUIOP", firstKeyFocus, onCharacter)
        FocusKeyRow("ASDFGHJKLÑ", null, onCharacter)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            "ZXCVBNM".forEach { character ->
                FocusKey(
                    onClick = { onCharacter(character) },
                    modifier = Modifier.weight(1f),
                    label = character.toString()
                )
            }
            FocusKey(
                onClick = onSpace,
                modifier = Modifier.weight(2f),
                label = stringResource(R.string.studio_space)
            )
            FocusKey(
                onClick = onBackspace,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.Backspace,
                    stringResource(R.string.studio_delete),
                    Modifier.size(16.dp),
                    tint = LocalContentColor.current
                )
            }
        }
    }
}

@Composable
private fun FocusKeyRow(characters: String, firstKeyFocus: FocusRequester?, onCharacter: (Char) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        characters.forEachIndexed { index, character ->
            FocusKey(
                onClick = { onCharacter(character) },
                modifier = Modifier.weight(1f),
                label = character.toString(),
                focusRequester = if (index == 0) firstKeyFocus else null
            )
        }
    }
}

@Composable
private fun FocusKey(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    focusRequester: FocusRequester? = null,
    content: (@Composable () -> Unit)? = null
) {
    val palette = LocalLauncherStyle.current.palette
    val keyBackground = lerp(palette.background, Color.White, .06f)
    SearchKey(
        onClick = onClick,
        modifier = modifier,
        height = 34.dp,
        radius = 7.dp,
        containerColor = keyBackground,
        contentColor = palette.onBackground,
        focusedContainerColor = keyBackground,
        focusedContentColor = palette.onBackground,
        focusedBorderColor = palette.onBackground,
        borderColor = Color.White.copy(alpha = .08f),
        focusRequester = focusRequester
    ) {
        if (content != null) {
            content()
        } else {
            Text(label.orEmpty(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
