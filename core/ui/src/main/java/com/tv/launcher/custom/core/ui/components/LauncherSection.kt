package com.tv.launcher.custom.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

enum class LauncherSection {
    HOME, APPS, SEARCH;

    fun onBack(): LauncherSection = HOME
}

/** Sub-filters of the Apps section used by theme navigation such as Películas, Para ti or Música. */
enum class AppsFilter { ALL, FOR_YOU, VIDEO, AUDIO, GAMES, FAVORITES, RECENTS }

val LocalLauncherSection = staticCompositionLocalOf { LauncherSection.HOME }
val LocalOpenHome = staticCompositionLocalOf<() -> Unit> { {} }

/** Host-owned feature content. Null keeps the theme's normal Home and its previews. */
val LocalLauncherSectionContent = staticCompositionLocalOf<(@Composable () -> Unit)?> { null }

fun resolveLauncherSection(label: String): LauncherSection? = when {
    label.contains("busc", true) || label.contains("search", true) -> LauncherSection.SEARCH
    label.contains("apps", true) || label.contains("aplic", true) -> LauncherSection.APPS
    label.contains("inicio", true) || label.contains("home", true) -> LauncherSection.HOME
    else -> null
}

private val DIACRITICS = Regex("\\p{Mn}+")

fun resolveAppsFilter(label: String): AppsFilter? {
    val key = java.text.Normalizer.normalize(label, java.text.Normalizer.Form.NFD)
        .replace(DIACRITICS, "")
        .lowercase()
    return when {
        key.contains("para ti") || key.contains("recomend") || key.contains("for you") -> AppsFilter.FOR_YOU
        key.contains("favorit") || key.contains("fav") -> AppsFilter.FAVORITES
        key.contains("reciente") || key.contains("recent") -> AppsFilter.RECENTS
        key.contains("music") -> AppsFilter.AUDIO
        key.contains("juego") || key.contains("game") -> AppsFilter.GAMES
        key.contains("pelic") || key.contains("serie") || key.contains("movie") ||
            key.contains("film") || key.contains("video") || key.contains("vivo") ||
            key.contains("deport") || key.contains("infantil") || key.contains("kids") -> AppsFilter.VIDEO
        else -> null
    }
}
