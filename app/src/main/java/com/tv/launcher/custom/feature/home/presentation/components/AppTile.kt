package com.tv.launcher.custom.feature.home.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.tv.launcher.custom.core.theme.LocalLauncherStyle
import com.tv.launcher.custom.feature.apps.domain.model.AppEntry

private const val ICON_RATIO_BELOW = 0.55f
private const val ICON_RATIO_INSIDE = 0.40f

@Composable
fun AppTile(
    app: AppEntry,
    width: Dp,
    height: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    labelInside: Boolean = false
) {
    val style = LocalLauncherStyle.current
    val shape = RoundedCornerShape(style.tileRadius)
    var focused by remember { mutableStateOf(false) }
    val iconSize = height * if (labelInside) ICON_RATIO_INSIDE else ICON_RATIO_BELOW

    Column(
        modifier = Modifier.onFocusChanged { focused = it.hasFocus },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            onClick = onClick,
            modifier = modifier.size(width, height),
            shape = ClickableSurfaceDefaults.shape(shape),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = style.palette.surface,
                focusedContainerColor = style.palette.surface
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = style.focusScale),
            border = ClickableSurfaceDefaults.border(
                focusedBorder = Border(BorderStroke(4.dp, style.palette.onBackground), shape = shape)
            )
        ) {
            Column(
                modifier = Modifier.size(width, height),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
            ) {
                AppIcon(app.packageName, Modifier.size(iconSize))
                if (labelInside) AppLabel(app.label, width, focused)
            }
        }
        if (!labelInside) AppLabel(app.label, width, focused)
    }
}

@Composable
private fun AppLabel(label: String, width: Dp, focused: Boolean) {
    val palette = LocalLauncherStyle.current.palette
    Text(
        text = label,
        modifier = Modifier.width(width),
        color = if (focused) palette.onBackground else palette.onMuted,
        fontSize = 22.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center
    )
}
