package com.tv.launcher.custom.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged

fun Modifier.selectLauncherSectionOnFocus(label: String, onSelect: () -> Unit): Modifier =
    if (resolveLauncherSection(label) == null) this else onFocusChanged {
        if (it.isFocused) onSelect()
    }

enum class LauncherSection {
    HOME, APPS, SEARCH;

    fun onBack(): LauncherSection = HOME
}

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
