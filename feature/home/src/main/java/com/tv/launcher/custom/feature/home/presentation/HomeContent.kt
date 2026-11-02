package com.tv.launcher.custom.feature.home.presentation

import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import com.tv.launcher.custom.core.model.theme.ThemeBlockType
import com.tv.launcher.custom.core.model.theme.ThemeComposition
import com.tv.launcher.custom.core.theme.layout.CustomThemeLayout
import com.tv.launcher.custom.core.ui.components.AppsFilter
import com.tv.launcher.custom.core.ui.components.InstalledAppsCanvasData
import com.tv.launcher.custom.core.ui.components.LocalOpenApps
import com.tv.launcher.custom.feature.home.presentation.state.HomeIntent
import com.tv.launcher.custom.feature.home.presentation.state.HomeUiState

/** Feature entry point: root navigation is supplied by the application, never by a sibling feature. */
@Composable
fun HomeContent(
    state: HomeUiState.Success,
    onIntent: (HomeIntent) -> Unit,
    onOpenSettings: () -> Unit,
    onUnavailableNavigation: (String) -> Unit = {},
    customComposition: ThemeComposition? = null,
    customThemeName: String = "",
    customThemeAssets: Map<String, String> = emptyMap(),
    onOpenPersonalization: () -> Unit = onOpenSettings,
    onOpenSearch: () -> Unit = {},
    onOpenAppsFiltered: (AppsFilter) -> Unit,
    onOpenProfile: () -> Unit = onOpenPersonalization,
    onOpenNotifications: () -> Unit = onOpenSettings
) {
    val firstItemFocus = remember { FocusRequester() }
    val onLaunch = { packageName: String -> onIntent(HomeIntent.Launch(packageName)) }
    val onOpenApps = LocalOpenApps.current
    val onOpenHome = com.tv.launcher.custom.core.ui.components.LocalOpenHome.current
    val section = com.tv.launcher.custom.core.ui.components.LocalLauncherSection.current
    val installedCanvasData = remember(state.apps, state.appearance.shortcuts, state.appearance.recentPackages, onIntent, onOpenSettings, onOpenApps, onUnavailableNavigation, onOpenPersonalization, onOpenSearch, onOpenHome) {
        val favoritePackages = state.appearance.shortcuts.values.toSet()
        val recents = state.appearance.recentPackages.mapNotNull { packageName ->
            state.apps.firstOrNull { it.packageName == packageName }
        }
        InstalledAppsCanvasData(state.apps, state.apps.filter { it.packageName in favoritePackages },
            onLaunch, onOpenApps, onOpenSettings, onUnavailableNavigation,
            onOpenPersonalization = onOpenPersonalization,
            onOpenSearch = onOpenSearch,
            onOpenHome = onOpenHome,
            onOpenSystemSettings = onOpenSettings,
            recents = recents,
            onOpenAppsFiltered = onOpenAppsFiltered,
            onOpenProfile = onOpenProfile,
            onOpenNotifications = onOpenNotifications)
    }
    LaunchedEffect(customComposition, state.apps) {
        if (section != com.tv.launcher.custom.core.ui.components.LauncherSection.HOME) return@LaunchedEffect
        val hasFocusableApps = customComposition?.blocks?.any { block ->
            when (block.type) {
                ThemeBlockType.APPS_ROW, ThemeBlockType.APPS_GRID -> state.apps.isNotEmpty()
                ThemeBlockType.FAVORITES_ROW -> state.apps.any { it.packageName in state.appearance.shortcuts.values }
                ThemeBlockType.RECENTS_ROW -> state.appearance.recentPackages.isNotEmpty()
                ThemeBlockType.MEDIA_ROW -> block.contentItems.any { item -> item.launchPackage in state.apps.map { it.packageName }.toSet() }
                else -> false
            }
        } ?: true
        if (hasFocusableApps) runCatching { firstItemFocus.requestFocus() }
    }
    if (customComposition != null) {
        CustomThemeLayout(
            customThemeName, customComposition, installedCanvasData, firstItemFocus,
            onOpenSettings, onOpenPersonalization, customThemeAssets
        )
        return
    }
    CustomThemeLayout(
        state.themeId.name,
        com.tv.launcher.custom.core.theme.defaultCompositionFor(state.themeId),
        installedCanvasData, firstItemFocus, onOpenSettings, onOpenPersonalization
    )
}
