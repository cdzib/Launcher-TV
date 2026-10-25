package com.tv.launcher.custom.core.theme.layout

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.tv.launcher.custom.core.ui.R
import com.tv.launcher.custom.core.model.apps.AppEntry
import com.tv.launcher.custom.core.ui.components.*
import com.tv.launcher.custom.core.model.theme.ThemeId
import com.tv.launcher.custom.core.ui.theme.LocalBackgroundImageVisible
import com.tv.launcher.custom.core.ui.theme.LocalLauncherStyle
import kotlinx.collections.immutable.ImmutableList

/** webOS composition with installed apps and five persisted, configurable shortcuts. */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun WebOsLayout(
    apps: ImmutableList<AppEntry>,
    firstItemFocus: FocusRequester,
    onLaunch: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onOpenApps = LocalOpenApps.current
    val shortcuts = LocalShortcutPackages.current
    val setShortcut = LocalSetShortcut.current
    var editingSlot by remember { mutableStateOf<Int?>(null) }
    var showShortcutSlots by remember { mutableStateOf(false) }
    val palette = LocalLauncherStyle.current.palette
    val shortcutColors = listOf(palette.accent, palette.accent.copy(alpha = 0.82f), palette.accent.copy(alpha = 0.68f), palette.accent.copy(alpha = 0.54f), palette.accent.copy(alpha = 0.40f))
    val rootBackground = if (LocalBackgroundImageVisible.current) Color.Transparent else palette.background
    BoxWithConstraints(modifier.fillMaxSize().background(rootBackground)) {
        val bannerHeight = maxHeight * 0.35f
        val cardHeight = maxHeight * 0.16f
        val categoryHeight = (maxHeight * 0.085f).coerceAtLeast(44.dp)
        val appSize = (maxHeight * 0.095f).coerceAtLeast(48.dp)
        Column(Modifier.fillMaxSize().padding(top = 24.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.width(56.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    WebOsAction("", stringResource(R.string.launcher_settings), onOpenSettings, Modifier.size(44.dp), palette.accent, true, icon = Icons.Outlined.AccountCircle)
                    WebOsAction("", stringResource(R.string.launcher_settings), onOpenSettings, Modifier.size(44.dp), icon = Icons.Outlined.Settings)
                    WebOsAction("", stringResource(R.string.search_apps), onOpenApps, Modifier.size(44.dp), icon = Icons.Outlined.Search)
                }
                InstalledAppHero(resolveShortcut(apps, shortcuts, 0) ?: apps.firstOrNull(), onLaunch, Modifier.weight(1f).height(bannerHeight).padding(end = 20.dp))
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                shortcutColors.forEachIndexed { index, color ->
                    val app = resolveShortcut(apps, shortcuts, index)
                    val label = app?.label ?: stringResource(R.string.empty_shortcut)
                    WebOsAction(
                        label, label,
                        onClick = {
                            if (app == null) editingSlot = index else onLaunch(app.packageName)
                        },
                        modifier = Modifier.weight(1f).height(categoryHeight)
                            .then(if (index == 0) Modifier.focusRequester(firstItemFocus) else Modifier),
                        color = color,
                        onLongClick = { editingSlot = index }
                    )
                }
            }
            LazyRow(contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    WebOsAction("", stringResource(R.string.launcher_apps), onOpenApps, Modifier.size(appSize), palette.accent, icon = Icons.Outlined.Apps)
                }
                items(apps, key = AppEntry::packageName) { app ->
                    val shape = RoundedCornerShape(4.dp)
                    Surface(
                        onClick = { onLaunch(app.packageName) },
                        modifier = Modifier.size(appSize).semantics { contentDescription = app.label },
                        shape = ClickableSurfaceDefaults.shape(shape),
                        colors = ClickableSurfaceDefaults.colors(containerColor = palette.surface),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                        border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, Color.White), shape = shape))
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            AppIcon(app.packageName, Modifier.fillMaxSize().padding(4.dp))
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.your_apps), color = Color.White, fontSize = 14.sp)
                WebOsAction(stringResource(R.string.edit_shortcuts), stringResource(R.string.edit_shortcuts), { showShortcutSlots = true }, Modifier.width(160.dp).height(44.dp), palette.surface)
            }
            InstalledAppCards(apps, onLaunch, cardHeight, Modifier.padding(horizontal = 8.dp))
        }
    }
    if (showShortcutSlots) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showShortcutSlots = false }) {
            androidx.tv.material3.Surface(colors = androidx.tv.material3.SurfaceDefaults.colors(containerColor = palette.surface)) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.edit_shortcuts), color = Color.White, fontSize = 22.sp)
                    for (slot in 0..4) {
                        androidx.tv.material3.Button(onClick = { editingSlot = slot; showShortcutSlots = false }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.choose_shortcut, slot + 1))
                        }
                    }
                    androidx.tv.material3.Button(onClick = { showShortcutSlots = false }) { Text(stringResource(R.string.close)) }
                }
            }
        }
    }
    editingSlot?.let { slot ->
        AppSelectionDialog(apps, { packageName -> setShortcut(slot, packageName); editingSlot = null }, { editingSlot = null },
            title = stringResource(R.string.choose_shortcut, slot + 1),
            onClear = { setShortcut(slot, ""); editingSlot = null })
    }
}

@Composable
private fun WebOsAction(label: String, description: String, onClick: () -> Unit, modifier: Modifier, color: Color = Color.Black, circular: Boolean = false, onLongClick: (() -> Unit)? = null, icon: ImageVector? = null) {
    val shape = if (circular) CircleShape else RoundedCornerShape(4.dp)
    Surface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier.semantics { contentDescription = description },
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = color, contentColor = Color.White, focusedContainerColor = color),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, Color.White), shape = shape))
    ) {
        Box(Modifier.fillMaxSize().padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
            if (icon != null) Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = androidx.tv.material3.LocalContentColor.current)
            else Text(label, fontSize = 16.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
    }
}

@Preview(name = "webOS reference", device = "spec:width=960dp,height=540dp,dpi=160")
@Composable
private fun WebOsLayoutPreview() = LauncherLayoutPreview(ThemeId.WEBOS) { apps, focus ->
    WebOsLayout(apps, focus, {}, {})
}
