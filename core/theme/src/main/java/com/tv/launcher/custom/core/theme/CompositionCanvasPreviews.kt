package com.tv.launcher.custom.core.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.tv.launcher.custom.core.model.theme.RailConfig
import com.tv.launcher.custom.core.model.theme.RailPosition
import com.tv.launcher.custom.core.model.theme.ThemeId
import com.tv.launcher.custom.core.model.theme.TopBarConfig
import com.tv.launcher.custom.core.model.theme.TopBarPosition
import com.tv.launcher.custom.core.ui.components.LocalCanvasWeatherContent
import com.tv.launcher.custom.core.ui.theme.LauncherTheme

private class MinimalistPresetProvider : PreviewParameterProvider<ThemePreset> {
    override val values = ThemePresets.all.filter { it.name in setOf("Órbita", "Bruma", "Horizonte", "Estudio") }.asSequence()
}

@Preview(name = "Minimalist presets", widthDp = 960, heightDp = 540)
@Composable
private fun MinimalistPresetPreview(@PreviewParameter(MinimalistPresetProvider::class) preset: ThemePreset) = LauncherTheme(ThemeId.WEBOS) {
    CompositionCanvas(preset.composition, PlaceholderCanvasData, Modifier.fillMaxSize())
}

@Preview(name = "VIDAA hero", widthDp = 960, heightDp = 540)
@Composable
private fun CompositionCanvasVidaaPreview() = LauncherTheme(ThemeId.WEBOS) {
    CompositionCanvas(ThemePresets.byName("VIDAA")!!, PlaceholderCanvasData, Modifier.fillMaxSize())
}

@Preview(name = "Horizonte without favorites", widthDp = 960, heightDp = 540)
@Composable
private fun MinimalistEmptyFavoritesPreview() = LauncherTheme(ThemeId.WEBOS) {
    val emptyData = object : com.tv.launcher.custom.core.ui.components.CanvasData by PlaceholderCanvasData {
        override fun itemCount(block: com.tv.launcher.custom.core.model.theme.ThemeBlock) = 0
    }
    CompositionCanvas(ThemePresets.byName("Horizonte")!!, emptyData, Modifier.fillMaxSize())
}

@Preview(widthDp = 960, heightDp = 540)
@Preview(name = "Reference aspect", widthDp = 960, heightDp = 422)
@Composable
private fun CompositionCanvasLiquidGlassPreview() = LauncherTheme(ThemeId.WEBOS) {
    CompositionLocalProvider(LocalCanvasWeatherContent provides { modifier ->
        Text("Mérida\n31°C · Despejado", modifier.padding(16.dp), color = Color.White)
    }) {
        CompositionCanvas(ThemePresets.byName("macOS Liquid Glass")!!, PlaceholderCanvasData, Modifier.fillMaxSize())
    }
}

@Preview(name = "Weather disabled", widthDp = 960, heightDp = 540)
@Composable
private fun CompositionCanvasLiquidGlassWeatherDisabledPreview() = LauncherTheme(ThemeId.WEBOS) {
    CompositionCanvas(ThemePresets.byName("macOS Liquid Glass")!!, PlaceholderCanvasData, Modifier.fillMaxSize())
}

@Preview(widthDp = 960, heightDp = 540)
@Composable
private fun CompositionCanvasRightRailBottomBarPreview() = LauncherTheme(ThemeId.WEBOS) {
    CompositionCanvas(
        defaultCompositionFor(ThemeId.ANDROID_TV).copy(
            rail = RailConfig(position = RailPosition.RIGHT, width = 160, overlay = true),
            topBar = TopBarConfig(position = TopBarPosition.BOTTOM)
        ), PlaceholderCanvasData, Modifier.fillMaxSize(),
        sectionContent = { Text("Apps", color = Color.White) }
    )
}
