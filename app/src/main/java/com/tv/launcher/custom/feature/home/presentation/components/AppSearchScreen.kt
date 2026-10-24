package com.tv.launcher.custom.feature.home.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.*
import com.tv.launcher.custom.R
import com.tv.launcher.custom.core.theme.LauncherTheme
import com.tv.launcher.custom.feature.apps.domain.model.AppEntry
import com.tv.launcher.custom.feature.theme.domain.model.ThemeId

// Studio is intentionally independent of imported theme palettes: the approved search
// surface stays graphite even when Home uses a light or highly saturated profile.
private object StudioColors {
    val background = Color(0xFF181B23)
    val glow = Color(0xFF2C3445)
    val surface = Color(0xFF252B38)
    val hero = Color(0xFF242B3A)
    val text = Color(0xFFF4F5F8)
    val muted = Color(0xFFB4BDCE)
    val focus = Color(0xFFD6E4FF)
}

@Composable
fun AppSearchDialog(apps: List<AppEntry>, onLaunch: (String) -> Unit, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        AppSearchScreen(apps, onLaunch, onDismiss)
    }
}

@Composable
internal fun AppSearchScreen(
    apps: List<AppEntry>,
    onLaunch: (String) -> Unit,
    onDismiss: () -> Unit,
    initialQuery: String = ""
) {
    var query by rememberSaveable { mutableStateOf(initialQuery) }
    var selectedPackage by rememberSaveable { mutableStateOf<String?>(null) }
    var focusedSearchKey by remember { mutableStateOf('A') }
    var isInputFocused by remember { mutableStateOf(false) }
    var isNumericKeyboard by rememberSaveable { mutableStateOf(false) }
    val keyboardCharacters = if (isNumericKeyboard) "0123456789" else "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val filteredApps = remember(apps, query) { filterSearchApps(apps, query) }
    val selectedApp = resolveSelectedSearchApp(filteredApps, selectedPackage)
    val keyboardFocus = remember { FocusRequester() }
    val inputFocus = remember { FocusRequester() }
    val resultsFocus = remember { FocusRequester() }
    val isPreview = LocalInspectionMode.current

    // Commit the fallback, so clearing a filter does not restore an obsolete selection.
    LaunchedEffect(selectedApp?.packageName) { selectedPackage = selectedApp?.packageName }
    LaunchedEffect(Unit) { if (!isPreview) keyboardFocus.requestFocus() }

    Row(
        Modifier.fillMaxSize()
            .background(Brush.linearGradient(listOf(StudioColors.background, StudioColors.glow)))
            .onPreviewKeyEvent { event ->
                if (event.key == Key.Escape && event.type == KeyEventType.KeyUp) {
                    onDismiss()
                    true
                } else false
            }
    ) {
        Column(
            Modifier.width(72.dp).fillMaxHeight().background(Color.Black.copy(alpha = .12f)).padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("st.", color = StudioColors.text, fontSize = 24.sp)
            Spacer(Modifier.weight(1f))
            StudioIconButton(Icons.Outlined.Home, stringResource(R.string.studio_home), onDismiss)
            StudioIconButton(Icons.Outlined.Search, stringResource(R.string.search_apps), { inputFocus.requestFocus() }, isSelected = true)
            Spacer(Modifier.weight(1f))
            StudioIconButton(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.close), onDismiss)
        }
        Column(Modifier.weight(1f).fillMaxHeight().padding(32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                Column(Modifier.weight(.38f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.studio_search_title), color = StudioColors.text, fontSize = 32.sp)
                    Text(stringResource(R.string.studio_search_subtitle), color = StudioColors.muted, fontSize = 14.sp)
                    Row(
                        Modifier.fillMaxWidth().background(StudioColors.surface, RoundedCornerShape(12.dp))
                            .border(if (isInputFocused) 2.dp else 1.dp, if (isInputFocused) StudioColors.focus else StudioColors.glow, RoundedCornerShape(12.dp))
                            .padding(start = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Search, null, tint = StudioColors.muted, modifier = Modifier.size(20.dp))
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            textStyle = TextStyle(color = StudioColors.text, fontSize = 16.sp),
                            cursorBrush = Brush.linearGradient(listOf(StudioColors.focus, StudioColors.focus)),
                            modifier = Modifier.weight(1f).padding(12.dp).focusRequester(inputFocus)
                                .onFocusChanged { isInputFocused = it.isFocused }
                                .onPreviewKeyEvent { event ->
                                    if (event.key == Key.DirectionDown) {
                                        if (event.type == KeyEventType.KeyDown) keyboardFocus.requestFocus()
                                        true
                                    } else false
                                },
                            decorationBox = { field ->
                                if (query.isEmpty()) Text(stringResource(R.string.search_apps_hint), color = StudioColors.muted, fontSize = 14.sp, maxLines = 1)
                                field()
                            }
                        )
                        StudioIconButton(Icons.Outlined.Close, stringResource(R.string.studio_clear_search), { query = "" })
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier.weight(1f).fillMaxWidth().focusRequester(keyboardFocus).onPreviewKeyEvent { event ->
                            val index = keyboardCharacters.indexOf(focusedSearchKey)
                            if (event.key == Key.DirectionRight && (index % 6 == 5 || index == keyboardCharacters.lastIndex)) {
                                if (event.type == KeyEventType.KeyDown) {
                                    if (filteredApps.isNotEmpty()) resultsFocus.requestFocus() else inputFocus.requestFocus()
                                }
                                true
                            } else false
                        },
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(keyboardCharacters.toList(), key = { it }) { character ->
                            StudioButton(
                                onClick = { query += character },
                                modifier = Modifier.fillMaxWidth()
                                    .onFocusChanged { if (it.isFocused) focusedSearchKey = character },
                                compact = true
                            ) { Text(character.toString(), fontSize = 16.sp) }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StudioButton({ query += " " }, Modifier.weight(1f), compact = true) { Text(stringResource(R.string.studio_space), fontSize = 12.sp) }
                        StudioButton({ isNumericKeyboard = !isNumericKeyboard }, Modifier.weight(1f), compact = true) {
                            Text(stringResource(if (isNumericKeyboard) R.string.studio_letters else R.string.studio_numbers), fontSize = 12.sp)
                        }
                        StudioButton({ query = query.dropLast(1) }, Modifier.weight(1f), compact = true) {
                            Icon(Icons.AutoMirrored.Outlined.Backspace, stringResource(R.string.studio_delete), tint = LocalContentColor.current, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Column(Modifier.weight(.62f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(if (query.isBlank()) R.string.studio_installed_apps else R.string.studio_results), color = StudioColors.text, fontSize = 16.sp)
                        Text(stringResource(R.string.studio_app_count, filteredApps.size), color = StudioColors.muted, fontSize = 14.sp)
                    }
                    Column(
                        Modifier.fillMaxWidth().height(184.dp)
                            .background(Brush.linearGradient(listOf(StudioColors.hero, StudioColors.surface)), RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StudioAppIcon(selectedApp, Modifier.size(44.dp))
                            Text(stringResource(if (selectedApp == null) R.string.studio_no_results else R.string.studio_selected), color = StudioColors.muted, fontSize = 12.sp)
                        }
                        Text(selectedApp?.label ?: stringResource(R.string.no_matching_apps), color = StudioColors.text, fontSize = 24.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(stringResource(if (selectedApp == null) R.string.studio_try_another_name else R.string.studio_ready_to_open), modifier = Modifier.weight(1f), color = StudioColors.muted, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            StudioButton(
                                { selectedApp?.let { onLaunch(it.packageName) } },
                                enabled = selectedApp != null,
                                primary = true
                            ) { Text(stringResource(R.string.studio_open), fontSize = 14.sp) }
                        }
                    }
                    if (filteredApps.isEmpty()) {
                        Text(stringResource(if (apps.isEmpty()) R.string.studio_no_installed_apps else R.string.studio_try_another_name), color = StudioColors.muted, fontSize = 16.sp)
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier.weight(1f).fillMaxWidth().focusRequester(resultsFocus),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            items(filteredApps, key = AppEntry::packageName) { app ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val isSelected = selectedApp?.packageName == app.packageName
                                    Surface(
                                        onClick = { onLaunch(app.packageName) },
                                        modifier = Modifier.fillMaxWidth().height(64.dp)
                                            .onFocusChanged { if (it.isFocused) selectedPackage = app.packageName }
                                            .semantics { contentDescription = app.label; selected = isSelected },
                                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                                        colors = ClickableSurfaceDefaults.colors(containerColor = StudioColors.surface, focusedContainerColor = StudioColors.glow),
                                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
                                        border = ClickableSurfaceDefaults.border(
                                            border = if (isSelected) Border(BorderStroke(2.dp, StudioColors.focus), shape = RoundedCornerShape(12.dp)) else Border.None,
                                            focusedBorder = Border(BorderStroke(3.dp, StudioColors.focus), shape = RoundedCornerShape(12.dp))
                                        )
                                    ) {
                                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { StudioAppIcon(app, Modifier.size(40.dp)) }
                                    }
                                    Text(app.label, color = if (isSelected) StudioColors.text else StudioColors.muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.studio_remote_hint), color = StudioColors.muted, fontSize = 12.sp)
                Text(stringResource(R.string.studio_local_only), color = StudioColors.muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun StudioButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    compact: Boolean = false,
    primary: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 44.dp),
        enabled = enabled,
        shape = ButtonDefaults.shape(RoundedCornerShape(8.dp)),
        colors = ButtonDefaults.colors(
            containerColor = if (primary) StudioColors.focus else StudioColors.surface,
            contentColor = if (primary) StudioColors.background else StudioColors.text,
            focusedContainerColor = StudioColors.focus,
            focusedContentColor = StudioColors.background
        ),
        scale = ButtonDefaults.scale(focusedScale = 1f),
        border = ButtonDefaults.border(focusedBorder = Border(BorderStroke(2.dp, Color.White), shape = RoundedCornerShape(8.dp))),
        contentPadding = PaddingValues(horizontal = if (compact) 4.dp else 16.dp, vertical = 8.dp),
        content = content
    )
}

@Composable
private fun StudioIconButton(icon: ImageVector, description: String, onClick: () -> Unit, isSelected: Boolean = false) {
    StudioButton(onClick, Modifier.size(44.dp).semantics { selected = isSelected }, compact = true, primary = isSelected) {
        Icon(icon, description, Modifier.size(20.dp), tint = LocalContentColor.current)
    }
}

@Composable
private fun StudioAppIcon(app: AppEntry?, modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Icon(Icons.Outlined.Apps, null, tint = StudioColors.muted, modifier = Modifier.fillMaxSize().padding(8.dp))
        if (app != null && !LocalInspectionMode.current) key(app.packageName) { AppIcon(app.packageName, Modifier.fillMaxSize()) }
    }
}

@Preview(name = "Studio · Apps", widthDp = 960, heightDp = 540)
@Composable
private fun AppSearchContentPreview() = LauncherTheme(ThemeId.WEBOS) {
    AppSearchScreen(listOf(AppEntry("sample.video", "Video"), AppEntry("sample.music", "Music"), AppEntry("sample.library", "Una biblioteca con nombre muy largo")), {}, {})
}

@Preview(name = "Studio · Sin coincidencias", widthDp = 960, heightDp = 540)
@Composable
private fun AppSearchNoResultsPreview() = LauncherTheme(ThemeId.WEBOS) {
    AppSearchScreen(listOf(AppEntry("sample.video", "Video")), {}, {}, initialQuery = "xyz")
}

@Preview(name = "Studio · Sin apps", widthDp = 1280, heightDp = 720)
@Composable
private fun AppSearchEmptyPreview() = LauncherTheme(ThemeId.WEBOS) {
    AppSearchScreen(emptyList(), {}, {})
}

@Preview(name = "Studio · Nombre largo", widthDp = 960, heightDp = 540, fontScale = 1.15f)
@Composable
private fun AppSearchLongLabelPreview() = LauncherTheme(ThemeId.WEBOS) {
    AppSearchScreen(listOf(AppEntry("sample.library", "Una biblioteca multimedia con nombre muy largo")), {}, {})
}
