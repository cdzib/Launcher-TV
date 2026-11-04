package com.tv.launcher.custom.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.tv.launcher.custom.core.model.theme.ThemeId
import com.tv.launcher.custom.core.ui.theme.LauncherTheme

/** Host-owned weather content. No network, permissions or feature dependencies in the renderer. */
val LocalCanvasWeatherContent = compositionLocalOf<(@Composable (Modifier) -> Unit)?> { null }

/** Host-supplied static, pre-blurred background. Never samples foreground children. */
data class GlassBackdrop(val image: ImageBitmap, val origin: Offset, val size: IntSize)
val LocalGlassBackdrop = compositionLocalOf<GlassBackdrop?> { null }

/** Leaves legacy surfaces untouched; declarative glass samples only the host backdrop. */
@Composable
fun Modifier.sectionSurfaceBackground(shape: Shape): Modifier =
    if (com.tv.launcher.custom.core.ui.theme.LocalLauncherStyle.current.glassSurfaces)
        glassSurfaceBackground(shape) else this

/** API-26-safe approximation: aligned scenery crop, violet frost, sheen and a fine rim. */
@Composable
fun Modifier.glassSurfaceBackground(shape: Shape = RoundedCornerShape(16.dp)): Modifier {
    val backdrop = LocalGlassBackdrop.current
    var position by remember { mutableStateOf(Offset.Zero) }
    return clip(shape)
        .onGloballyPositioned { position = it.positionInRoot() }
        .drawBehind {
            if (backdrop != null && backdrop.size.width > 0 && backdrop.size.height > 0) {
                val image = backdrop.image
                val scale = maxOf(backdrop.size.width.toFloat() / image.width, backdrop.size.height.toFloat() / image.height)
                val width = (image.width * scale).toInt()
                val height = (image.height * scale).toInt()
                val relative = position - backdrop.origin
                drawImage(image, dstOffset = IntOffset(
                    ((backdrop.size.width - width) / 2f - relative.x).toInt(),
                    ((backdrop.size.height - height) / 2f - relative.y).toInt()), dstSize = IntSize(width, height))
            }
            drawRect(if (backdrop == null) Color(0xFF343441).copy(alpha = 0.78f) else Color(0xFF776C8C).copy(alpha = 0.36f))
        }
        .background(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.025f), Color.White.copy(alpha = 0.08f))), shape)
        .border(0.8.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.58f), Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.28f))), shape)
}

@Composable
fun GlassSurface(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.glassSurfaceBackground(), content = content)
}

@Preview
@Composable
private fun GlassSurfacePreview() = LauncherTheme(ThemeId.WEBOS) {
    GlassSurface { Text("12:45", Modifier.padding(24.dp), color = Color.White) }
}
