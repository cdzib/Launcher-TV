package com.tv.launcher.custom.core.model.theme

/** Appearance commands shared by independent settings and application hosts. */
sealed interface AppearanceAction {
    data object ClearCustomBackground : AppearanceAction
    data class SelectBackground(val value: BackgroundChoice) : AppearanceAction
    data class SelectBackgroundColor(val argb: Long) : AppearanceAction
    data class SelectAccent(val value: AccentChoice) : AppearanceAction
    data class SelectTileShape(val value: TileShapeChoice) : AppearanceAction
}
