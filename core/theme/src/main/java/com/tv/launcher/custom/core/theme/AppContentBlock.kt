package com.tv.launcher.custom.core.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.tv.launcher.custom.core.model.theme.*
import com.tv.launcher.custom.core.ui.R
import com.tv.launcher.custom.core.ui.components.*
import com.tv.launcher.custom.core.ui.theme.LocalLauncherStyle

/** Reusable installed-app content selected by the ZIP, not by a theme/layout identity. */
@Composable
internal fun AppContentBlock(block: ThemeBlock, data: CanvasData, interactive: Boolean,
    scale: Float, firstItemFocus: FocusRequester?) {
    val apps = data.installedApps()
    val shortcuts = LocalShortcutPackages.current
    val setShortcut = LocalSetShortcut.current
    val palette = LocalLauncherStyle.current.palette
    var editingSlot by remember { mutableStateOf<Int?>(null) }
    var showSlots by remember { mutableStateOf(false) }
    val height = (block.rItemH() * scale).dp
    fun launch(packageName: String) {
        val index = apps.indexOfFirst { it.packageName == packageName }
        if (interactive && index >= 0) data.onItemClick(ThemeBlock(ThemeBlockType.APPS_ROW), index)
    }
    when (block.appContent) {
        AppContent.FEATURED_APP -> InstalledAppHero(resolveShortcut(apps, shortcuts, 0) ?: apps.firstOrNull(),
            ::launch, Modifier.fillMaxWidth().height(height), interactive, scale)
        AppContent.SHORTCUTS -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy((12 * scale).dp)) {
            repeat(5) { slot ->
                val app = resolveShortcut(apps, shortcuts, slot)
                val label = app?.label ?: stringResource(R.string.empty_shortcut)
                val shape = RoundedCornerShape((4 * scale).dp)
                val color = palette.accent.copy(alpha = listOf(1f, 0.82f, 0.68f, 0.54f, 0.40f)[slot])
                Surface(onClick = { if (interactive) { if (app == null) editingSlot = slot else launch(app.packageName) } },
                    onLongClick = { if (interactive) editingSlot = slot },
                    modifier = Modifier.weight(1f).height(height).focusProperties { canFocus = interactive }
                        .then(if (slot == 0 && firstItemFocus != null) Modifier.focusRequester(firstItemFocus) else Modifier)
                        .semantics { contentDescription = label },
                    shape = ClickableSurfaceDefaults.shape(shape),
                    colors = ClickableSurfaceDefaults.colors(containerColor = color, focusedContainerColor = color),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f),
                    border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, Color.White), shape = shape))) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(label, color = Color.White, fontSize = (16 * scale).sp, maxLines = 1)
                    }
                }
            }
        }
        AppContent.APP_ICONS -> LazyRow(horizontalArrangement = Arrangement.spacedBy((12 * scale).dp)) {
            item {
                Surface(onClick = { if (interactive) data.onNavClick("Apps") },
                    modifier = Modifier.size(height).focusProperties { canFocus = interactive },
                    colors = ClickableSurfaceDefaults.colors(containerColor = palette.accent),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape((4 * scale).dp))) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Apps, stringResource(R.string.launcher_apps), Modifier.size((24 * scale).dp), tint = palette.onBackground)
                    }
                }
            }
            items(apps, key = { it.packageName }) { app ->
                val shape = RoundedCornerShape((4 * scale).dp)
                Surface(onClick = { launch(app.packageName) }, modifier = Modifier.size(height)
                    .focusProperties { canFocus = interactive }.semantics { contentDescription = app.label },
                    shape = ClickableSurfaceDefaults.shape(shape),
                    colors = ClickableSurfaceDefaults.colors(containerColor = palette.surface),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                    border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, Color.White), shape = shape))) {
                    AppIcon(app.packageName, Modifier.fillMaxSize().padding((4 * scale).dp))
                }
            }
        }
        AppContent.SHORTCUT_EDITOR -> Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.your_apps), color = palette.onBackground, fontSize = (14 * scale).sp)
            Surface(onClick = { if (interactive) showSlots = true },
                modifier = Modifier.width((160 * scale).dp).height((44 * scale).dp).focusProperties { canFocus = interactive },
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape((4 * scale).dp)),
                colors = ClickableSurfaceDefaults.colors(containerColor = palette.surface)) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.edit_shortcuts), color = palette.onBackground, fontSize = (14 * scale).sp)
                }
            }
        }
        AppContent.APP_CARDS -> InstalledAppCards(apps, ::launch, height, interactive = interactive, contentScale = scale)
        AppContent.APP_ICON_ROW -> LazyRow(horizontalArrangement = Arrangement.spacedBy((12 * scale).dp)) {
            items((0 until data.itemCount(block)).toList()) { index ->
                val label = data.label(block, index)
                val shape = RoundedCornerShape((6 * scale).dp)
                Surface(onClick = { if (interactive) data.onItemClick(block, index) }, modifier = Modifier.size(height)
                    .focusProperties { canFocus = interactive && data.isItemClickable(block, index) }.semantics { contentDescription = label },
                    shape = ClickableSurfaceDefaults.shape(shape),
                    colors = ClickableSurfaceDefaults.colors(containerColor = palette.surface),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f),
                    border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, Color.White), shape = shape))) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        data.Icon(block, index, Modifier.fillMaxSize().padding((4 * scale).dp))
                    }
                }
            }
        }
        null -> Unit
    }
    if (interactive && showSlots) androidx.compose.ui.window.Dialog(onDismissRequest = { showSlots = false }) {
        Column(Modifier.background(palette.surface).padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(5) { slot -> SettingsButton(stringResource(R.string.choose_shortcut, slot + 1), onClick = {
                showSlots = false; editingSlot = slot
            }) }
            SettingsButton(stringResource(R.string.cancel), onClick = { showSlots = false })
        }
    }
    if (interactive) editingSlot?.let { slot ->
        AppSelectionDialog(apps, { setShortcut(slot, it); editingSlot = null }, { editingSlot = null },
            title = stringResource(R.string.choose_shortcut, slot + 1),
            onClear = { setShortcut(slot, ""); editingSlot = null })
    }
}
