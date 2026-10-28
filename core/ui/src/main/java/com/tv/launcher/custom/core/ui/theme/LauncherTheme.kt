package com.tv.launcher.custom.core.ui.theme

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import com.tv.launcher.custom.core.ui.R
import com.tv.launcher.custom.core.model.theme.*

enum class LayoutKind { BOTTOM_FLOATING, BOTTOM_EXPANDING, TOP_NAV, SIDE_GRID, ANDROID_GRID, GOOGLE_HOME, APPLE_HOME, TIZEN_HOME, FREE_MY_TV }

@Immutable
data class LauncherPalette(
    val background: Color,
    val surface: Color,
    val accent: Color,
    val onBackground: Color,
    val onMuted: Color
)

@Immutable
data class LauncherStyle(
    val palette: LauncherPalette,
    val tileRadius: Dp,
    val focusScale: Float,
    val layout: LayoutKind,
    @StringRes val nameRes: Int,
    val glassSurfaces: Boolean = false,
    val declarativeSurfaces: Boolean = false
)

private val Light = Color(0xFFF2F4F7)
private val Muted = Color(0xFFB4BCC8)

fun ThemeId.toStyle(): LauncherStyle = when (this) {
    ThemeId.WEBOS -> LauncherStyle(
        LauncherPalette(Color(0xFF08070D), Color(0xFF211A2B), Color(0xFF37D9C5), Light, Muted),
        30.dp, 1.10f, LayoutKind.BOTTOM_FLOATING, R.string.theme_webos
    )
    ThemeId.FIRE_TV -> LauncherStyle(
        LauncherPalette(Color(0xFF111111), Color(0xFF242424), Color(0xFF53B8FF), Light, Color(0xFF9AA7B4)),
        12.dp, 1.08f, LayoutKind.TOP_NAV, R.string.theme_fire_tv
    )
    ThemeId.ROKU -> LauncherStyle(
        LauncherPalette(Color(0xFF35134D), Color(0xFF361955), Color.White, Color.White, Color(0xFFE5D4F5)),
        10.dp, 1.06f, LayoutKind.SIDE_GRID, R.string.theme_roku
    )
    ThemeId.TIZEN -> LauncherStyle(
        LauncherPalette(Color(0xFF08090B), Color(0xFF17191D), Color(0xFF6B7BFF), Light, Muted),
        28.dp, 1.00f, LayoutKind.TIZEN_HOME, R.string.theme_tizen
    )
    ThemeId.ANDROID_TV -> LauncherStyle(
        LauncherPalette(Color(0xFF071326), Color(0xFF17243A), Color(0xFF8AB4F8), Light, Muted),
        16.dp, 1.06f, LayoutKind.ANDROID_GRID, R.string.theme_android_tv
    )
    ThemeId.GOOGLE_TV -> LauncherStyle(
        LauncherPalette(Color(0xFF101827), Color(0xFF1B2940), Color(0xFFA8C7FA), Light, Muted),
        24.dp, 1.10f, LayoutKind.GOOGLE_HOME, R.string.theme_google_tv
    )
    ThemeId.APPLE_TV -> LauncherStyle(
        LauncherPalette(Color(0xFF090807), Color(0xFF30231B), Color(0xFFFFC078), Light, Color(0xFFB8ACA2)),
        16.dp, 1.08f, LayoutKind.APPLE_HOME, R.string.theme_apple_tv
    )
    ThemeId.FREE_MY_TV -> LauncherStyle(
        LauncherPalette(Color(0xFF090D19), Color(0xFF20232D), Color(0xFF168CFF), Light, Muted),
        20.dp, 1.05f, LayoutKind.FREE_MY_TV, R.string.theme_free_my_tv
    )
}

fun LauncherAppearance.toStyle(): LauncherStyle {
    val base = themeId.toStyle()
    val accent = when (accent) {
        AccentChoice.PROFILE -> base.palette.accent
        AccentChoice.BLUE -> Color(0xFF4FC3F7)
        AccentChoice.PURPLE -> Color(0xFFBB86FC)
        AccentChoice.GREEN -> Color(0xFF81C784)
        AccentChoice.ORANGE -> Color(0xFFFFB74D)
    }
    val background = when (background) {
        BackgroundChoice.PROFILE -> base.palette.background
        BackgroundChoice.MIDNIGHT -> Color(0xFF05070B)
        BackgroundChoice.GRAPHITE -> Color(0xFF202124)
        BackgroundChoice.PLUM -> Color(0xFF24152F)
    }
    val radius = when (tileShape) {
        TileShapeChoice.PROFILE -> base.tileRadius
        TileShapeChoice.ROUNDED -> 28.dp
        TileShapeChoice.SQUARE -> 4.dp
    }
    return base.copy(
        palette = base.palette.copy(
            background = backgroundColorArgb?.let { Color(it.toInt()) } ?: background,
            accent = accentColorArgb?.let { Color(it.toInt()) } ?: accent
        ),
        tileRadius = radius
    )
}

val LocalLauncherStyle = staticCompositionLocalOf<LauncherStyle> {
    error("LauncherTheme no provisto")
}

val LocalBackgroundImageVisible = staticCompositionLocalOf { false }

@Composable
fun LauncherTheme(themeId: ThemeId, content: @Composable () -> Unit) {
    LauncherTheme(LauncherAppearance(themeId), content)
}

@Composable
fun LauncherTheme(appearance: LauncherAppearance, content: @Composable () -> Unit) {
    val style = remember(appearance) { appearance.toStyle() }
    CompositionLocalProvider(LocalLauncherStyle provides style) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = style.palette.accent,
                background = style.palette.background,
                surface = style.palette.surface,
                onBackground = style.palette.onBackground,
                onSurface = style.palette.onBackground
            ),
            content = content
        )
    }
}
