package com.tv.launcher.custom.core.data.theme

import com.tv.launcher.custom.core.model.theme.BackgroundChoice
import com.tv.launcher.custom.core.model.theme.LauncherAppearance
import com.tv.launcher.custom.core.model.theme.TileShapeChoice

fun ThemeManifest.toAppearance(
    backgroundImageUri: String? = null,
    installedThemeId: String? = null
): LauncherAppearance = LauncherAppearance(
    themeId = layout,
    background = BackgroundChoice.PROFILE,
    tileShape = when (tileShape) {
        "rounded" -> TileShapeChoice.ROUNDED
        "square" -> TileShapeChoice.SQUARE
        else -> TileShapeChoice.PROFILE
    },
    backgroundImageUri = backgroundImageUri,
    backgroundColorArgb = (background as? ThemeManifestBackground.Solid)?.argb,
    accentColorArgb = accentArgb,
    installedThemeId = installedThemeId
)
