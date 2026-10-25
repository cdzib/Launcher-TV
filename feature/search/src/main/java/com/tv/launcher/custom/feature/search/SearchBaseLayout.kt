package com.tv.launcher.custom.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Text
import com.tv.launcher.custom.core.ui.R
import com.tv.launcher.custom.core.ui.theme.LocalLauncherStyle
import com.tv.launcher.custom.core.model.apps.AppEntry
import kotlinx.coroutines.delay

@Composable
internal fun SearchBaseLayout(
    apps: List<AppEntry>,
    filteredApps: List<AppEntry>,
    selectedApp: AppEntry?,
    query: String,
    keyboardVisible: Boolean,
    keyboardRequest: Int,
    fieldFocus: FocusRequester,
    onQueryChange: (String) -> Unit,
    onSelect: (String) -> Unit,
    onLaunch: (String) -> Unit,
    onDismiss: () -> Unit,
    onVoice: () -> Unit,
    onShowKeyboard: () -> Unit,
    onToggleKeyboard: () -> Unit
) {
    val style = LocalLauncherStyle.current
    val palette = style.palette
    var numericKeyboard by rememberSaveable { mutableStateOf(false) }
    val firstKeyFocus = remember { FocusRequester() }
    val firstResultFocus = remember { FocusRequester() }
    val characters = if (numericKeyboard) "0123456789" else "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val isPreview = LocalInspectionMode.current

    LaunchedEffect(Unit) {
        if (!isPreview) {
            delay(160)
            if (filteredApps.isNotEmpty()) firstResultFocus.requestFocus() else fieldFocus.requestFocus()
        }
    }
    LaunchedEffect(keyboardVisible, keyboardRequest) {
        if (keyboardVisible && !isPreview) {
            withFrameNanos { }
            firstKeyFocus.requestFocus()
        }
    }

    Row(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(listOf(palette.background, lerp(palette.surface, palette.background, .18f)))
        )
    ) {
        Column(
            Modifier.width(72.dp)
                .fillMaxHeight()
                .background(Color.Black.copy(alpha = .16f))
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "st.",
                color = palette.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.96).sp
            )
            Spacer(Modifier.weight(1f))
            SearchRailIconButton(
                icon = Icons.Outlined.Home,
                description = stringResource(R.string.studio_home),
                onClick = onDismiss,
                isSelected = false
            )
            SearchRailIconButton(
                icon = Icons.Outlined.Search,
                description = stringResource(R.string.search_apps),
                onClick = { fieldFocus.requestFocus() },
                isSelected = true
            )
            Spacer(Modifier.weight(1f))
            SearchRailIconButton(
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                description = stringResource(R.string.close),
                onClick = onDismiss,
                isSelected = false
            )
        }
        Column(
            Modifier.weight(1f)
                .fillMaxHeight()
                .padding(start = 32.dp, end = 32.dp, top = 26.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                Column(
                    Modifier.widthIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.studio_search_title),
                        color = palette.onBackground,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.9).sp
                    )
                    Text(
                        text = stringResource(R.string.search_base_subtitle),
                        color = palette.onMuted,
                        fontSize = 13.sp
                    )
                }
                SearchField(
                    query = query,
                    onQueryChange = onQueryChange,
                    focusRequester = fieldFocus,
                    keyboardVisible = keyboardVisible,
                    onToggleKeyboard = onToggleKeyboard,
                    onShowKeyboard = onShowKeyboard,
                    onVoice = onVoice,
                    modifier = Modifier.weight(1f),
                    variant = SearchFieldVariant.BASE
                )
            }
            if (!keyboardVisible) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(if (query.isBlank()) R.string.studio_installed_apps else R.string.studio_results),
                        color = palette.onBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.studio_app_count, filteredApps.size),
                        color = palette.onMuted,
                        fontSize = 14.sp
                    )
                }
                SearchHero(selectedApp, { selectedApp?.let { onLaunch(it.packageName) } })
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredApps.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SearchEmptyState(
                            text = stringResource(
                                if (apps.isEmpty()) R.string.studio_no_installed_apps else R.string.search_empty_results
                            ),
                            minHeight = 120.dp,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    itemsIndexed(filteredApps, key = { _, app -> app.packageName }) { index, app ->
                        SearchAppTile(
                            app = app,
                            onClick = { onLaunch(app.packageName) },
                            modifier = if (index == 0) Modifier.focusRequester(firstResultFocus) else Modifier,
                            height = 64.dp,
                            radius = 12.dp,
                            labelFontSize = 11.sp,
                            labelSpacing = 6.dp,
                            containerColor = palette.surface,
                            focusedContainerColor = lerp(palette.surface, palette.onBackground, .12f),
                            focusedBorderColor = palette.accent,
                            selectedBorderColor = palette.accent,
                            isSelected = selectedApp?.packageName == app.packageName,
                            focusedScale = 1f,
                            onFocused = { onSelect(app.packageName) }
                        )
                    }
                }
            }
            if (keyboardVisible) {
                BaseKeyboard(
                    characters = characters,
                    numeric = numericKeyboard,
                    firstKeyFocus = firstKeyFocus,
                    onCharacter = { onQueryChange(query + it) },
                    onSpace = { onQueryChange(query + " ") },
                    onToggleNumeric = { numericKeyboard = !numericKeyboard },
                    onBackspace = { onQueryChange(query.dropLast(1)) },
                    onRightEdge = {
                        if (filteredApps.isNotEmpty()) firstResultFocus.requestFocus() else fieldFocus.requestFocus()
                    }
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchHints(
                    hints = listOf(
                        "↔ ↕" to stringResource(R.string.search_hint_move),
                        "OK" to stringResource(R.string.search_hint_select),
                        "↩" to stringResource(R.string.search_hint_back)
                    )
                )
                Text(
                    text = stringResource(R.string.studio_local_only),
                    color = palette.onMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun BaseKeyboard(
    characters: String,
    numeric: Boolean,
    firstKeyFocus: FocusRequester,
    onCharacter: (Char) -> Unit,
    onSpace: () -> Unit,
    onToggleNumeric: () -> Unit,
    onBackspace: () -> Unit,
    onRightEdge: () -> Unit
) {
    val palette = LocalLauncherStyle.current.palette
    Column(
        Modifier.fillMaxWidth().padding(top = 2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (numeric) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                characters.forEachIndexed { index, character ->
                    BaseKey(
                        label = character.toString(),
                        firstKeyFocus = if (index == 0) firstKeyFocus else null,
                        onRightEdge = if (index == characters.lastIndex) onRightEdge else null,
                        onClick = { onCharacter(character) }
                    )
                }
                repeat(3) { Spacer(Modifier.weight(1f)) }
            }
        } else {
            characters.chunked(13).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    row.forEachIndexed { index, character ->
                        BaseKey(
                            label = character.toString(),
                            firstKeyFocus = if (character == 'A') firstKeyFocus else null,
                            onRightEdge = if (index == row.lastIndex) onRightEdge else null,
                            onClick = { onCharacter(character) }
                        )
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SearchKey(
                onClick = onSpace,
                modifier = Modifier.weight(1f),
                height = 30.dp,
                containerColor = palette.surface,
                contentColor = palette.onBackground,
                focusedContainerColor = palette.accent,
                focusedContentColor = palette.background,
                focusedBorderColor = palette.accent,
                borderColor = palette.onMuted.copy(alpha = .20f)
            ) {
                Text(
                    text = stringResource(R.string.studio_space),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            SearchKey(
                onClick = onToggleNumeric,
                modifier = Modifier.weight(1f),
                height = 30.dp,
                containerColor = palette.surface,
                contentColor = palette.onBackground,
                focusedContainerColor = palette.accent,
                focusedContentColor = palette.background,
                focusedBorderColor = palette.accent,
                borderColor = palette.onMuted.copy(alpha = .20f)
            ) {
                Text(
                    text = stringResource(if (numeric) R.string.studio_letters else R.string.studio_numbers),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            SearchKey(
                onClick = onBackspace,
                modifier = Modifier.weight(1f),
                height = 30.dp,
                containerColor = palette.surface,
                contentColor = palette.onBackground,
                focusedContainerColor = palette.accent,
                focusedContentColor = palette.background,
                focusedBorderColor = palette.accent,
                borderColor = palette.onMuted.copy(alpha = .20f)
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
private fun RowScope.BaseKey(
    label: String,
    firstKeyFocus: FocusRequester?,
    onRightEdge: (() -> Unit)?,
    onClick: () -> Unit
) {
    val palette = LocalLauncherStyle.current.palette
    SearchKey(
        onClick = onClick,
        modifier = Modifier.weight(1f).then(
            if (onRightEdge != null) {
                Modifier.onPreviewKeyEvent { event ->
                    if (event.key == Key.DirectionRight && event.type == KeyEventType.KeyDown) {
                        onRightEdge()
                        true
                    } else false
                }
            } else {
                Modifier
            }
        ),
        height = 32.dp,
        containerColor = palette.surface,
        contentColor = palette.onBackground,
        focusedContainerColor = palette.accent,
        focusedContentColor = palette.background,
        focusedBorderColor = palette.accent,
        borderColor = palette.onMuted.copy(alpha = .20f),
        focusRequester = firstKeyFocus
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
