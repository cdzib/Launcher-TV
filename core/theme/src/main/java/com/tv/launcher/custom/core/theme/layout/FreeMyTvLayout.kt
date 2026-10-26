package com.tv.launcher.custom.core.theme.layout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.tv.launcher.custom.core.model.apps.AppEntry
import com.tv.launcher.custom.core.model.theme.ThemeId
import com.tv.launcher.custom.core.ui.R
import com.tv.launcher.custom.core.ui.components.AppIcon
import com.tv.launcher.custom.core.ui.components.ClockText
import com.tv.launcher.custom.core.ui.components.LocalLauncherSectionContent
import com.tv.launcher.custom.core.ui.components.LocalOpenHome
import com.tv.launcher.custom.core.ui.components.LocalOpenApps
import com.tv.launcher.custom.core.ui.components.LocalOpenSearch
import com.tv.launcher.custom.core.ui.components.selectLauncherSectionOnFocus
import androidx.tv.material3.Button
import com.tv.launcher.custom.core.ui.components.touchClick
import com.tv.launcher.custom.core.ui.theme.LauncherTheme
import com.tv.launcher.custom.core.ui.theme.LocalBackgroundImageVisible
import com.tv.launcher.custom.core.ui.theme.LocalLauncherStyle
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val FREE_MY_TV_CLOCK_REFRESH_MILLIS = 60_000L
private const val FAVORITE_CARD_COUNT = 8
private val FreeMyTvDateFormat = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())

@Composable
fun FreeMyTvLayout(
    favoriteApps: List<AppEntry>,
    featuredApp: AppEntry?,
    firstItemFocusRequester: FocusRequester,
    onLaunch: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val style = LocalLauncherStyle.current
    val sectionContent = LocalLauncherSectionContent.current
    val openHome = LocalOpenHome.current
    val openApps = LocalOpenApps.current
    val openSearch = LocalOpenSearch.current
    val hasBackgroundImage = LocalBackgroundImageVisible.current
    val date = rememberLocalDate()
    val customizeDescription = "${stringResource(R.string.free_my_tv_customize)} ${stringResource(R.string.launcher_settings)}"

    BoxWithConstraints(
        modifier.fillMaxSize()
            .background(
                if (hasBackgroundImage) SolidColor(Color.Transparent)
                else Brush.linearGradient(
                    listOf(style.palette.background, style.palette.background.copy(alpha = 0.92f), Color(0xFF05070D))
                )
            )
    ) {
        val favoritesPanelHeight = (maxHeight * 0.29f).coerceAtLeast(120.dp)
        val panelVerticalPadding = (maxHeight * 0.02f).coerceIn(6.dp, 12.dp)
        val favoriteIconSize = (maxHeight * 0.11f).coerceIn(36.dp, 60.dp)
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(listOf(style.palette.accent.copy(alpha = 0.16f), Color.Transparent))
            )
        )
        Column(Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 20.dp)) {
            Row(
                Modifier.fillMaxWidth().height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(40.dp).background(style.palette.accent, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Tv, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text("Free My TV", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Surface(
                    onClick = onOpenSettings,
                    modifier = Modifier.height(48.dp).touchClick(onOpenSettings)
                        .semantics { contentDescription = customizeDescription },
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(50)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color(0xFF222431),
                        focusedContainerColor = Color(0xFF303342),
                        contentColor = Color.White
                    ),
                    border = ClickableSurfaceDefaults.border(
                        focusedBorder = Border(BorderStroke(2.dp, Color.White), shape = RoundedCornerShape(50))
                    )
                ) {
                    Row(
                        Modifier.padding(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(stringResource(R.string.free_my_tv_customize), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.width(20.dp))
                Text(date.format(FreeMyTvDateFormat), color = Color(0xFFB2B5C0), fontSize = 14.sp)
                Spacer(Modifier.width(12.dp))
                ClockText(fontSize = 24.sp, modifier = Modifier.semantics { contentDescription = "Hora actual" })
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = openHome, modifier = Modifier.selectLauncherSectionOnFocus("Home", openHome)) { Text(stringResource(R.string.studio_home)) }
                Button(onClick = openSearch, modifier = Modifier.selectLauncherSectionOnFocus("Search", openSearch)) { Text(stringResource(R.string.search_apps)) }
                Button(onClick = openApps, modifier = Modifier.selectLauncherSectionOnFocus("Apps", openApps)) { Text(stringResource(R.string.launcher_apps)) }
            }

            if (sectionContent != null) {
                Box(Modifier.fillMaxWidth().weight(1f)) { sectionContent() }
            } else {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.CenterStart) {
                if (featuredApp != null) {
                    Surface(
                        onClick = { onLaunch(featuredApp.packageName) },
                        modifier = Modifier.fillMaxWidth(0.7f).touchClick { onLaunch(featuredApp.packageName) }
                            .padding(top = 20.dp).padding(12.dp),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Color.Transparent,
                            focusedContainerColor = style.palette.surface.copy(alpha = 0.68f),
                            contentColor = Color.White
                        ),
                        border = ClickableSurfaceDefaults.border(
                            border = Border.None,
                            focusedBorder = Border(BorderStroke(2.dp, style.palette.accent), shape = RoundedCornerShape(20.dp))
                        ),
                        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                stringResource(R.string.free_my_tv_hero_eyebrow).uppercase(),
                                color = style.palette.accent, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                letterSpacing = 1.4.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                featuredApp.label,
                                color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Bold,
                                maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                stringResource(R.string.free_my_tv_hero_description, featuredApp.label),
                                color = Color(0xFFADB1BC), fontSize = 16.sp, maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    Text(
                        stringResource(R.string.free_my_tv_hero_empty),
                        color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }

            Column(
                Modifier.fillMaxWidth().height(favoritesPanelHeight)
                    .background(style.palette.surface.copy(alpha = 0.82f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = panelVerticalPadding)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.free_my_tv_favorites).uppercase(),
                        color = Color(0xFFB7BAC4), fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp, modifier = Modifier.weight(1f)
                    )
                    Text(
                        stringResource(R.string.free_my_tv_favorites_count, favoriteApps.size),
                        color = Color(0xFFB7BAC4), fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(favoriteApps.take(FAVORITE_CARD_COUNT), key = AppEntry::packageName) { app ->
                        val isFocusedFirstItem = app == favoriteApps.firstOrNull()
                        FavoriteAppCard(
                            app = app,
                            iconSize = favoriteIconSize,
                            modifier = Modifier.fillMaxHeight().aspectRatio(0.96f)
                                .then(if (isFocusedFirstItem) Modifier.focusRequester(firstItemFocusRequester) else Modifier),
                            onClick = { onLaunch(app.packageName) }
                        )
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun FavoriteAppCard(app: AppEntry, iconSize: androidx.compose.ui.unit.Dp, modifier: Modifier, onClick: () -> Unit) {
    val style = LocalLauncherStyle.current
    val isPreview = LocalInspectionMode.current
    val shape = RoundedCornerShape(20.dp)
    Surface(
        onClick = onClick,
        modifier = modifier.touchClick(onClick).semantics { contentDescription = app.label },
        shape = ClickableSurfaceDefaults.shape(shape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = style.palette.surface.copy(alpha = 0.96f),
            focusedContainerColor = style.palette.surface,
            contentColor = Color.White
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border.None,
            focusedBorder = Border(BorderStroke(2.dp, style.palette.accent), shape = shape)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f)
    ) {
        Column(
            Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isPreview) {
                Box(
                    Modifier.size(iconSize).background(style.palette.accent, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(app.label.take(2).uppercase(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                AppIcon(app.packageName, Modifier.size(iconSize))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                app.label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun rememberLocalDate(): LocalDate {
    val date = produceState(LocalDate.now()) {
        while (true) {
            value = LocalDate.now()
            delay(FREE_MY_TV_CLOCK_REFRESH_MILLIS)
        }
    }
    return date.value
}

@Preview(name = "Free My TV", device = "spec:width=960dp,height=540dp,dpi=160")
@Composable
private fun FreeMyTvLayoutPreview() {
    val apps = listOf(
        AppEntry("com.netflix", "Netflix"), AppEntry("com.youtube", "YouTube"),
        AppEntry("com.prime", "Prime Video"), AppEntry("com.disney", "Disney+")
    )
    LauncherTheme(ThemeId.FREE_MY_TV) {
        FreeMyTvLayout(apps, apps.first(), remember { FocusRequester() }, {}, {})
    }
}
