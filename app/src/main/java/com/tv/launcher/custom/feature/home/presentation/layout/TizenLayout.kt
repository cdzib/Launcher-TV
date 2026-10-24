package com.tv.launcher.custom.feature.home.presentation.layout

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
import com.tv.launcher.custom.R
import com.tv.launcher.custom.feature.apps.domain.model.AppEntry
import com.tv.launcher.custom.feature.home.presentation.components.AppIcon
import com.tv.launcher.custom.feature.home.presentation.components.LocalOpenApps
import com.tv.launcher.custom.feature.home.presentation.components.InstalledAppHero
import com.tv.launcher.custom.feature.home.presentation.components.InstalledAppCards
import com.tv.launcher.custom.feature.theme.domain.model.ThemeId
import kotlinx.collections.immutable.ImmutableList

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun TizenLayout(
    apps: ImmutableList<AppEntry>,
    firstItemFocus: FocusRequester,
    onLaunch: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onOpenApps = LocalOpenApps.current
    var showApps by remember { mutableStateOf(false) }
    val background = Color(0xFF061014)
    Row(modifier.fillMaxSize().background(background)) {
        Column(
            Modifier.width(52.dp).fillMaxHeight().padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TizenAction("", stringResource(R.string.launcher_settings), onOpenSettings, Modifier.size(44.dp), circular = true, icon = Icons.Outlined.AccountCircle)
            TizenAction("", stringResource(R.string.search_apps), onOpenApps, Modifier.size(44.dp), circular = true, icon = Icons.Outlined.Search)
            TizenAction("", stringResource(R.string.launcher_apps), { showApps = true }, Modifier.size(44.dp), circular = true, icon = Icons.Outlined.Apps)
            Spacer(Modifier.weight(1f))
            TizenAction("", stringResource(R.string.launcher_settings), onOpenSettings, Modifier.size(44.dp), circular = true, icon = Icons.Outlined.Settings)
        }
        BoxWithConstraints(Modifier.weight(1f).fillMaxHeight()) {
            val heroHeight = maxHeight * 0.46f
            val posterHeight = maxHeight * 0.17f
            val appSize = (maxHeight * 0.10f).coerceAtLeast(44.dp)
            Column(Modifier.fillMaxSize().padding(end = 16.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InstalledAppHero(apps.firstOrNull(), onLaunch, Modifier.fillMaxWidth().height(heroHeight))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    TizenAction(stringResource(R.string.google_tv_eyebrow), stringResource(R.string.google_tv_eyebrow), { showApps = false }, Modifier.width(112.dp).height(44.dp).focusRequester(firstItemFocus), selected = !showApps)
                    Spacer(Modifier.width(24.dp))
                    TizenAction(stringResource(R.string.launcher_apps), stringResource(R.string.launcher_apps), { showApps = true }, Modifier.width(112.dp).height(44.dp), selected = showApps)
                }
                if (!showApps) {
                    Text(stringResource(R.string.apps_for_you), color = Color(0xFFB5BFC4), fontSize = 13.sp)
                    InstalledAppCards(apps, onLaunch, posterHeight)
                } else {
                    Text(stringResource(R.string.your_apps), color = Color.White, fontSize = 18.sp)
                }
                LazyRow(contentPadding = PaddingValues(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        TizenAction("", stringResource(R.string.search_apps), onOpenApps, Modifier.size(appSize), selected = true, icon = Icons.Outlined.Search)
                    }
                    items(apps, key = AppEntry::packageName) { app ->
                        val shape = RoundedCornerShape(6.dp)
                        Surface(
                            onClick = { onLaunch(app.packageName) },
                            modifier = Modifier.size(appSize).semantics { contentDescription = app.label },
                            shape = ClickableSurfaceDefaults.shape(shape),
                            colors = ClickableSurfaceDefaults.colors(containerColor = Color(0xFF19242A)),
                            border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, Color.White), shape = shape)),
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                AppIcon(app.packageName, Modifier.fillMaxSize().padding(4.dp))
                            }
                        }
                    }
                }
                if (showApps) {
                    Text(stringResource(R.string.tizen_apps_hint), color = Color(0xFFB5BFC4), fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun TizenAction(label: String, description: String, onClick: () -> Unit, modifier: Modifier, selected: Boolean = false, circular: Boolean = false, icon: ImageVector? = null) {
    val shape = if (circular) CircleShape else RoundedCornerShape(50)
    Surface(
        onClick = onClick,
        modifier = modifier.semantics { contentDescription = description },
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) Color.White else Color.Transparent,
            contentColor = if (selected) Color(0xFF102027) else Color.White,
            focusedContainerColor = Color(0xFF267C97),
            focusedContentColor = Color.White
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, Color.White), shape = shape))
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (icon != null) Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = androidx.tv.material3.LocalContentColor.current)
            else Text(label, fontSize = 16.sp)
        }
    }
}

@Preview(name = "Tizen reference", device = "spec:width=960dp,height=540dp,dpi=160")
@Composable
private fun TizenHomeLayoutPreview() = LauncherLayoutPreview(ThemeId.TIZEN) { apps, focusRequester ->
    TizenLayout(apps, focusRequester, {}, {})
}
