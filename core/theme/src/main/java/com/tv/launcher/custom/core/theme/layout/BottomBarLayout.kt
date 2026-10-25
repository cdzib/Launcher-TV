package com.tv.launcher.custom.core.theme.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.tv.material3.Text
import androidx.compose.ui.res.stringResource
import com.tv.launcher.custom.core.ui.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tv.launcher.custom.core.ui.theme.LocalLauncherStyle
import com.tv.launcher.custom.core.model.apps.AppEntry
import com.tv.launcher.custom.core.ui.components.*
import kotlinx.collections.immutable.ImmutableList
import com.tv.launcher.custom.core.model.theme.ThemeId

private val FloatingBarShape = RoundedCornerShape(56.dp)
private const val BAR_ALPHA = 0.8f

/** webOS (barra flotante) y Tizen (tile enfocado se expande). */
@Composable
fun BottomBarLayout(
    apps: ImmutableList<AppEntry>,
    expandFocused: Boolean,
    firstItemFocus: FocusRequester,
    onLaunch: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalLauncherStyle.current.palette
    val featuredApp = apps.first()
    val barModifier = if (expandFocused) {
        Modifier
    } else {
        Modifier.clip(FloatingBarShape).background(palette.surface.copy(alpha = BAR_ALPHA))
    }

    Box(modifier.fillMaxSize().padding(horizontal = 72.dp, vertical = 48.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ClockText(fontSize = 72.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WifiStatusIndicator()
                LauncherNavigation(onOpenSettings)
            }
        }
        if (expandFocused) {
            Column(
                Modifier.align(Alignment.TopStart).padding(top = 48.dp).width(220.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text("Tizen", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = palette.onBackground)
                listOf("Buscar", "Inicio", "En vivo", "Apps").forEachIndexed { index, label ->
                    Text(label, fontSize = 18.sp, color = if (index == 1) palette.accent else palette.onMuted)
                }
            }
        }
        FeaturedAppHero(
            label = featuredApp.label,
            packageName = featuredApp.packageName,
            height = 280.dp,
            onClick = { onLaunch(featuredApp.packageName) },
            modifier = Modifier.align(Alignment.Center).fillMaxWidth(if (expandFocused) 0.88f else 0.92f)
                .padding(start = if (expandFocused) 180.dp else 0.dp),
            focusRequester = firstItemFocus
        )
        LazyRow(
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().then(barModifier),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = if (expandFocused) 16.dp else 24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            itemsIndexed(apps, key = { _, app -> app.packageName }) { _, app ->
                val focusModifier = Modifier
                if (expandFocused) {
                    ExpandingTile(app, { onLaunch(app.packageName) }, focusModifier)
                } else {
                    AppTile(app, 144.dp, 84.dp, { onLaunch(app.packageName) }, focusModifier, labelInside = true)
                }
            }
        }
    }
}

@Preview(name = "webOS - launcher", device = "spec:width=1920dp,height=1080dp,dpi=160")
@Composable
private fun WebOsLayoutPreview() = LauncherLayoutPreview(ThemeId.WEBOS) { apps, focusRequester ->
    BottomBarLayout(apps, false, focusRequester, {}, {})
}
