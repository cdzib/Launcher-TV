package com.tv.launcher.custom.feature.theme.domain.model

import com.tv.launcher.custom.R

enum class AccentChoice { PROFILE, BLUE, PURPLE, GREEN, ORANGE }
enum class BackgroundChoice { PROFILE, MIDNIGHT, GRAPHITE, PLUM }
enum class TileShapeChoice { PROFILE, ROUNDED, SQUARE }

fun AccentChoice.labelResource(): Int = when (this) {
    AccentChoice.PROFILE -> R.string.appearance_profile_default
    AccentChoice.BLUE -> R.string.accent_blue
    AccentChoice.PURPLE -> R.string.accent_purple
    AccentChoice.GREEN -> R.string.accent_green
    AccentChoice.ORANGE -> R.string.accent_orange
}

fun BackgroundChoice.labelResource(): Int = when (this) {
    BackgroundChoice.PROFILE -> R.string.appearance_profile_default
    BackgroundChoice.MIDNIGHT -> R.string.background_midnight
    BackgroundChoice.GRAPHITE -> R.string.background_graphite
    BackgroundChoice.PLUM -> R.string.background_plum
}

fun TileShapeChoice.labelResource(): Int = when (this) {
    TileShapeChoice.PROFILE -> R.string.appearance_profile_default
    TileShapeChoice.ROUNDED -> R.string.tile_shape_rounded
    TileShapeChoice.SQUARE -> R.string.tile_shape_square
}

data class LauncherAppearance(
    val themeId: ThemeId = ThemeId.WEBOS,
    val accent: AccentChoice = AccentChoice.PROFILE,
    val background: BackgroundChoice = BackgroundChoice.PROFILE,
    val tileShape: TileShapeChoice = TileShapeChoice.PROFILE,
    val shortcuts: Map<Int, String> = emptyMap(),
    val backgroundImageUri: String? = null,
    val backgroundColorArgb: Long? = null,
    val accentColorArgb: Long? = null,
    val installedThemeId: String? = null
)
