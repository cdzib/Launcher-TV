package com.tv.launcher.custom.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Border
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.tv.launcher.custom.core.ui.R
import com.tv.launcher.custom.core.ui.theme.LocalLauncherStyle

/** Focusable hero treatment for the first installed app; no fictional media recommendations. */
@Composable
fun FeaturedAppHero(
    label: String,
    packageName: String,
    height: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null
) {
    val palette = LocalLauncherStyle.current.palette
    val heroShape = RoundedCornerShape(24.dp)
    val focusModifier = if (focusRequester == null) Modifier else Modifier.focusRequester(focusRequester)

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = height)
            .then(focusModifier),
        shape = ClickableSurfaceDefaults.shape(heroShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = palette.surface,
            focusedContainerColor = palette.surface,
            contentColor = palette.onBackground
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.03f),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(3.dp, palette.accent), shape = heroShape)
        )
    ) {
        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp))) {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.horizontalGradient(
                        listOf(palette.surface, palette.accent.copy(alpha = 0.32f), palette.background)
                    )
                )
            )
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(packageName, Modifier.size(100.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(label, fontSize = 36.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(stringResource(R.string.open_installed_app), color = palette.onMuted, fontSize = 18.sp)
                }
            }
        }
    }
}
