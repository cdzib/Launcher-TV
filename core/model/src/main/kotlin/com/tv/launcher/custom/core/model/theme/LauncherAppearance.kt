package com.tv.launcher.custom.core.model.theme

enum class AccentChoice { PROFILE, BLUE, PURPLE, GREEN, ORANGE }
enum class BackgroundChoice { PROFILE, MIDNIGHT, GRAPHITE, PLUM }
enum class TileShapeChoice { PROFILE, ROUNDED, SQUARE }

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
