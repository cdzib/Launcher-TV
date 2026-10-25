package com.tv.launcher.custom.core.theme.layout

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AppleTvLayout(apps: ImmutableList<AppEntry>, firstItemFocus: FocusRequester, onLaunch: (String) -> Unit, onOpenSettings: () -> Unit, modifier: Modifier = Modifier, onOpenPersonalization: () -> Unit = onOpenSettings) {
    val openApps = LocalOpenApps.current
    val openSearch = LocalOpenSearch.current
    val imageVisible = LocalBackgroundImageVisible.current
    val palette = LocalLauncherStyle.current.palette
    BoxWithConstraints(modifier.fillMaxSize().then(if (imageVisible) Modifier else Modifier.background(palette.background))) {
        val heroHeight = maxHeight * 0.70f
        val posterHeight = maxHeight * 0.19f
        InstalledAppHero(apps.firstOrNull(), onLaunch, Modifier.fillMaxWidth().height(heroHeight).padding(start = 220.dp, end = 16.dp, top = 16.dp))
        Row(Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(
                Modifier.width(184.dp).fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(Color(0xE62A211C)).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("TV", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                ClockText(14.sp)
                ReferenceAction(stringResource(R.string.search_apps), openSearch, Modifier.fillMaxWidth().height(44.dp))
                ReferenceAction(stringResource(R.string.launcher_apps), openApps, Modifier.fillMaxWidth().height(44.dp).focusRequester(firstItemFocus), selected = true)
                ReferenceAction(stringResource(R.string.launcher_settings), onOpenSettings, Modifier.fillMaxWidth().height(44.dp))
                LauncherPersonalizationButton(onOpenPersonalization)
                Text(stringResource(R.string.your_apps), fontSize = 13.sp, color = Color(0xFFC7BDB5))
                apps.take(3).forEach { app ->
                    ReferenceAction(app.label, { onLaunch(app.packageName) }, Modifier.fillMaxWidth().height(44.dp))
                }
            }
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.Bottom) {
                Text(stringResource(R.string.your_apps), fontSize = 13.sp, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
                InstalledAppCards(apps, onLaunch, posterHeight)
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AndroidTvLayout(apps: ImmutableList<AppEntry>, firstItemFocus: FocusRequester, onLaunch: (String) -> Unit, onOpenSettings: () -> Unit, modifier: Modifier = Modifier, onOpenPersonalization: () -> Unit = onOpenSettings) {
    val openApps = LocalOpenApps.current
    val imageVisible = LocalBackgroundImageVisible.current
    val palette = LocalLauncherStyle.current.palette
    BoxWithConstraints(modifier.fillMaxSize().then(if (imageVisible) Modifier else Modifier.background(palette.background))) {
        val appHeight = (maxHeight * 0.12f).coerceAtLeast(48.dp)
        val posterHeight = maxHeight * 0.23f
        Column(Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ReferenceHeader("Android TV", firstItemFocus, openApps, onOpenSettings, onOpenPersonalization)
            InstalledAppHero(apps.firstOrNull(), onLaunch, Modifier.fillMaxWidth().weight(1f))
            Text(stringResource(R.string.your_apps), color = Color.White, fontSize = 14.sp)
            ReferenceApps(apps, onLaunch, 112.dp, appHeight)
            Text(stringResource(R.string.apps_for_you), color = Color(0xFFB7C2D2), fontSize = 13.sp)
            InstalledAppCards(apps, onLaunch, posterHeight)
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun GoogleTvLayout(apps: ImmutableList<AppEntry>, firstItemFocus: FocusRequester, onLaunch: (String) -> Unit, onOpenSettings: () -> Unit, modifier: Modifier = Modifier, onOpenPersonalization: () -> Unit = onOpenSettings) {
    val openApps = LocalOpenApps.current
    val imageVisible = LocalBackgroundImageVisible.current
    val palette = LocalLauncherStyle.current.palette
    BoxWithConstraints(modifier.fillMaxSize().then(if (imageVisible) Modifier else Modifier.background(palette.background))) {
        val posterHeight = maxHeight * 0.20f
        val appHeight = (maxHeight * 0.11f).coerceAtLeast(48.dp)
        Column(Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ReferenceHeader("Google TV", firstItemFocus, openApps, onOpenSettings, onOpenPersonalization)
            InstalledAppHero(apps.firstOrNull(), onLaunch, Modifier.fillMaxWidth().weight(1f))
            Text(stringResource(R.string.apps_for_you), color = Color.White, fontSize = 13.sp)
            InstalledAppCards(apps, onLaunch, posterHeight)
            Text(stringResource(R.string.your_apps), color = Color.White, fontSize = 14.sp)
            ReferenceApps(apps, onLaunch, 104.dp, appHeight)
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun TopNavLayout(apps: ImmutableList<AppEntry>, firstItemFocus: FocusRequester, onLaunch: (String) -> Unit, onOpenSettings: () -> Unit, modifier: Modifier = Modifier, onOpenPersonalization: () -> Unit = onOpenSettings) {
    val openApps = LocalOpenApps.current
    val openSearch = LocalOpenSearch.current
    val imageVisible = LocalBackgroundImageVisible.current
    val palette = LocalLauncherStyle.current.palette
    BoxWithConstraints(modifier.fillMaxSize().then(if (imageVisible) Modifier else Modifier.background(palette.background))) {
        val bannerHeight = maxHeight * 0.40f
        val posterHeight = maxHeight * 0.17f
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InstalledAppHero(apps.firstOrNull(), onLaunch, Modifier.fillMaxWidth().height(bannerHeight))
            Row(Modifier.fillMaxWidth().padding(horizontal = 32.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ReferenceAction("", openSearch, Modifier.size(44.dp).focusRequester(firstItemFocus), description = stringResource(R.string.search_apps), icon = Icons.Outlined.Search)
                ReferenceAction("", openApps, Modifier.size(44.dp), selected = true, description = stringResource(R.string.launcher_apps), icon = Icons.Outlined.Apps)
                Box(Modifier.weight(1f)) { ReferenceApps(apps, onLaunch, 80.dp, 48.dp) }
                ReferenceAction("", onOpenSettings, Modifier.size(44.dp), description = stringResource(R.string.launcher_settings), icon = Icons.Outlined.Settings)
                LauncherPersonalizationButton(onOpenPersonalization)
            }
            Text(stringResource(R.string.your_apps), Modifier.padding(horizontal = 32.dp), color = Color(0xFFCCCCCC), fontSize = 13.sp)
            Column(Modifier.padding(horizontal = 32.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InstalledAppCards(apps.take((apps.size + 1) / 2), onLaunch, posterHeight)
                InstalledAppCards(apps.drop((apps.size + 1) / 2), onLaunch, posterHeight)
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SideGridLayout(apps: ImmutableList<AppEntry>, firstItemFocus: FocusRequester, onLaunch: (String) -> Unit, onOpenSettings: () -> Unit, modifier: Modifier = Modifier, onOpenPersonalization: () -> Unit = onOpenSettings) {
    val openApps = LocalOpenApps.current
    val openSearch = LocalOpenSearch.current
    val imageVisible = LocalBackgroundImageVisible.current
    val palette = LocalLauncherStyle.current.palette
    BoxWithConstraints(modifier.fillMaxSize().then(if (imageVisible) Modifier else Modifier.background(palette.background))) {
        Row(Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 24.dp), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            Column(Modifier.fillMaxWidth(0.34f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Roku", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))
                ReferenceAction(stringResource(R.string.launcher_apps), openApps, Modifier.fillMaxWidth().height(44.dp), selected = true, square = true)
                ReferenceAction(stringResource(R.string.search_apps), openSearch, Modifier.fillMaxWidth().height(44.dp), square = true)
                ReferenceAction(stringResource(R.string.launcher_settings), onOpenSettings, Modifier.fillMaxWidth().height(44.dp), square = true)
                LauncherPersonalizationButton(onOpenPersonalization)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    ClockText(16.sp)
                    Text(stringResource(R.string.installed_apps_count, apps.size), Modifier.padding(start = 16.dp), fontSize = 13.sp, color = Color(0xFFE0D2ED))
                }
                LazyVerticalGrid(columns = GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(8.dp)) {
                    itemsIndexed(apps, key = { _, app -> app.packageName }) { index, app ->
                        ReferenceAppCard(app, onLaunch, Modifier.fillMaxWidth().aspectRatio(1.3f).then(if (index == 0) Modifier.focusRequester(firstItemFocus) else Modifier), square = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferenceHeader(title: String, focus: FocusRequester, openApps: () -> Unit, openSettings: () -> Unit, onOpenPersonalization: () -> Unit) {
    val openSearch = LocalOpenSearch.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 18.sp, color = Color.White)
        ReferenceAction(stringResource(R.string.search_apps), openSearch, Modifier.height(44.dp).focusRequester(focus))
        ReferenceAction(stringResource(R.string.launcher_apps), openApps, Modifier.height(44.dp), selected = true)
        Spacer(Modifier.weight(1f))
        ReferenceAction("", openSettings, Modifier.size(44.dp), description = stringResource(R.string.launcher_settings), icon = Icons.Outlined.Settings)
        LauncherPersonalizationButton(onOpenPersonalization)
        ClockText(16.sp)
    }
}

@Composable
private fun ReferenceAction(label: String, onClick: () -> Unit, modifier: Modifier, selected: Boolean = false, square: Boolean = false, description: String = label, icon: ImageVector? = null) {
    val palette = LocalLauncherStyle.current.palette
    val shape = RoundedCornerShape(if (square) 2.dp else 24.dp)
    Surface(
        onClick = onClick,
        modifier = modifier.semantics { contentDescription = description },
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = if (selected) palette.accent else Color.Transparent, contentColor = if (selected) palette.onBackground else palette.onBackground),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, Color.White), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f)
    ) {
        Box(
            if (icon != null) Modifier.fillMaxSize() else Modifier.fillMaxHeight().padding(horizontal = 12.dp),
            contentAlignment = if (icon != null) Alignment.Center else Alignment.CenterStart
        ) {
            if (icon != null) Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = androidx.tv.material3.LocalContentColor.current)
            else Text(label, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ReferenceApps(apps: List<AppEntry>, onLaunch: (String) -> Unit, width: Dp, height: Dp) {
    LazyRow(contentPadding = PaddingValues(4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(apps, key = AppEntry::packageName) { app -> ReferenceAppCard(app, onLaunch, Modifier.size(width, height)) }
    }
}

@Composable
private fun ReferenceAppCard(app: AppEntry, onLaunch: (String) -> Unit, modifier: Modifier, square: Boolean = false) {
    val palette = LocalLauncherStyle.current.palette
    val shape = RoundedCornerShape(if (square) 2.dp else 6.dp)
    Surface(
        onClick = { onLaunch(app.packageName) },
        modifier = modifier.semantics { contentDescription = app.label },
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(containerColor = palette.surface, focusedContainerColor = palette.surface, focusedContentColor = palette.onBackground),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(3.dp, Color.White), shape = shape)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f)
    ) {
        Column(Modifier.fillMaxSize().padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            AppIcon(app.packageName, Modifier.weight(1f).fillMaxWidth())
            Text(app.label, color = palette.onBackground, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Preview(name = "Apple TV", device = "spec:width=960dp,height=540dp,dpi=160")
@Composable
private fun AppleReferencePreview() = LauncherLayoutPreview(ThemeId.APPLE_TV) { apps, focus -> AppleTvLayout(apps, focus, {}, {}) }

@Preview(name = "Android TV", device = "spec:width=960dp,height=540dp,dpi=160")
@Composable
private fun AndroidReferencePreview() = LauncherLayoutPreview(ThemeId.ANDROID_TV) { apps, focus -> AndroidTvLayout(apps, focus, {}, {}) }

@Preview(name = "Google TV", device = "spec:width=960dp,height=540dp,dpi=160")
@Composable
private fun GoogleReferencePreview() = LauncherLayoutPreview(ThemeId.GOOGLE_TV) { apps, focus -> GoogleTvLayout(apps, focus, {}, {}) }

@Preview(name = "Fire TV", device = "spec:width=960dp,height=540dp,dpi=160")
@Composable
private fun FireReferencePreview() = LauncherLayoutPreview(ThemeId.FIRE_TV) { apps, focus -> TopNavLayout(apps, focus, {}, {}) }

@Preview(name = "Roku", device = "spec:width=960dp,height=540dp,dpi=160")
@Composable
private fun RokuReferencePreview() = LauncherLayoutPreview(ThemeId.ROKU) { apps, focus -> SideGridLayout(apps, focus, {}, {}) }
