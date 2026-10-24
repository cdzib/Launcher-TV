package com.tv.launcher.custom.feature.home.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.tv.launcher.custom.core.theme.LocalLauncherStyle
import com.tv.launcher.custom.feature.apps.domain.model.AppEntry

private val CollapsedWidth = 148.dp
private val ExpandedWidth = 340.dp
private const val EXPAND_MILLIS = 200

@Composable
fun ExpandingTile(
    app: AppEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val style = LocalLauncherStyle.current
    var focused by remember { mutableStateOf(false) }
    val width by animateDpAsState(
        targetValue = if (focused) ExpandedWidth else CollapsedWidth,
        animationSpec = tween(EXPAND_MILLIS),
        label = "tileWidth"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .onFocusChanged { focused = it.hasFocus }
            .width(width)
            .height(CollapsedWidth),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(style.tileRadius)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = style.palette.surface,
            focusedContainerColor = style.palette.accent
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = style.focusScale)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(app.packageName, Modifier.size(72.dp))
            if (focused) {
                Spacer(Modifier.width(20.dp))
                Text(
                    text = app.label,
                    color = style.palette.onBackground,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
