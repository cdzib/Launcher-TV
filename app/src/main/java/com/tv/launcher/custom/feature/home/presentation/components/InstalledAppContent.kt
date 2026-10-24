package com.tv.launcher.custom.feature.home.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.tv.launcher.custom.R
import com.tv.launcher.custom.core.theme.LauncherTheme
import com.tv.launcher.custom.core.theme.LocalLauncherStyle
import com.tv.launcher.custom.feature.apps.domain.model.AppEntry
import com.tv.launcher.custom.feature.theme.domain.model.ThemeId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val LocalShortcutPackages = staticCompositionLocalOf<Map<Int, String>> { emptyMap() }
val LocalSetShortcut = staticCompositionLocalOf<(Int, String) -> Unit> { { _, _ -> } }

internal fun resolveShortcut(apps: List<AppEntry>, shortcuts: Map<Int, String>, slot: Int): AppEntry? {
    if (slot !in 0..4) return null
    return if (shortcuts.containsKey(slot)) apps.firstOrNull { it.packageName == shortcuts[slot] } else apps.getOrNull(slot)
}

/** Only installed-package artwork. No promotional screenshots or synthetic destinations. */
@Composable
private fun InstalledAppArtwork(app: AppEntry, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val inspection = LocalInspectionMode.current
    val artwork by produceState<ImageBitmap?>(null, app.packageName, inspection) {
        value = null
        if (!inspection) value = withContext(Dispatchers.IO) {
            val manager = context.packageManager
            val banner = runCatching { manager.getApplicationBanner(app.packageName) }.getOrNull()
            val drawable = banner ?: runCatching { manager.getApplicationIcon(app.packageName) }.getOrNull()
            drawable?.let { runCatching { it.toBitmap(width = if (banner != null) 640 else 256, height = if (banner != null) 360 else 256).asImageBitmap() }.getOrNull() }
        }
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        artwork?.let { Image(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit) }
            ?: Text(app.label.take(2).uppercase(), color = Color.White, fontSize = 28.sp)
    }
}

@Composable
fun InstalledAppHero(app: AppEntry?, onLaunch: (String) -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalLauncherStyle.current.palette
    val openApps = LocalOpenApps.current
    val shape = RoundedCornerShape(8.dp)
    Surface(
        onClick = { if (app == null) openApps() else onLaunch(app.packageName) },
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = palette.surface, focusedContainerColor = palette.surface),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, Color.White), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
    ) {
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(palette.background, palette.surface, palette.accent.copy(alpha = 0.25f))))) {
            if (app != null) InstalledAppArtwork(app, Modifier.align(Alignment.CenterEnd).fillMaxWidth(0.55f).fillMaxHeight().padding(20.dp))
            Column(Modifier.align(Alignment.CenterStart).fillMaxWidth(0.5f).padding(28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(app?.label ?: stringResource(R.string.launcher_apps), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(stringResource(R.string.open_installed_app), color = Color(0xFFE0E0E0), fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun InstalledAppCards(apps: List<AppEntry>, onLaunch: (String) -> Unit, height: Dp, modifier: Modifier = Modifier) {
    LazyRow(modifier, horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(4.dp)) {
        items(apps, key = AppEntry::packageName) { app ->
            val shape = RoundedCornerShape(6.dp)
            Surface(
                onClick = { onLaunch(app.packageName) },
                modifier = Modifier.width(height * 1.75f).height(height),
                shape = ClickableSurfaceDefaults.shape(shape),
                colors = ClickableSurfaceDefaults.colors(containerColor = Color(0xFF202026), focusedContainerColor = Color(0xFF202026)),
                border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, Color.White), shape = shape)),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f)
            ) {
                Column(Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    InstalledAppArtwork(app, Modifier.weight(1f).fillMaxWidth())
                    Text(app.label, color = Color.White, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Preview(widthDp = 800, heightDp = 250)
@Composable
private fun InstalledAppHeroPreview() = LauncherTheme(ThemeId.WEBOS) {
    InstalledAppHero(AppEntry("preview.app", "Aplicación"), {}, Modifier.fillMaxSize())
}

@Preview(widthDp = 800, heightDp = 150)
@Composable
private fun InstalledAppCardsPreview() = LauncherTheme(ThemeId.WEBOS) {
    InstalledAppCards(listOf(AppEntry("preview.app", "Aplicación")), {}, 120.dp)
}
