package com.tv.launcher.custom.feature.home.presentation

import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import com.tv.launcher.custom.core.model.theme.ThemeBlockType
import com.tv.launcher.custom.core.model.theme.ThemeComposition
import com.tv.launcher.custom.core.theme.layout.*
import com.tv.launcher.custom.core.ui.components.InstalledAppsCanvasData
import com.tv.launcher.custom.core.ui.components.LocalOpenApps
import com.tv.launcher.custom.core.ui.theme.LayoutKind
import com.tv.launcher.custom.core.ui.theme.LocalLauncherStyle
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
    customThemeName: String = ""
) {
    val layout = LocalLauncherStyle.current.layout
    val firstItemFocus = remember { FocusRequester() }
    val onLaunch = { packageName: String -> onIntent(HomeIntent.Launch(packageName)) }
    val onOpenApps = LocalOpenApps.current
    val installedCanvasData = remember(state.apps, state.appearance.shortcuts, onIntent, onOpenSettings, onOpenApps, onUnavailableNavigation) {
        val favoritePackages = state.appearance.shortcuts.values.toSet()
        InstalledAppsCanvasData(state.apps, state.apps.filter { it.packageName in favoritePackages },
            onLaunch, onOpenApps, onOpenSettings, onUnavailableNavigation)
    }
    LaunchedEffect(layout, customComposition, state.apps) {
        val hasFocusableApps = customComposition?.blocks?.any { block ->
            when (block.type) {
                ThemeBlockType.APPS_ROW, ThemeBlockType.APPS_GRID -> state.apps.isNotEmpty()
                ThemeBlockType.FAVORITES_ROW -> state.apps.any { it.packageName in state.appearance.shortcuts.values }
                else -> false
            }
        } ?: true
        if (hasFocusableApps) runCatching { firstItemFocus.requestFocus() }
    }
    if (customComposition != null) {
        CustomThemeLayout(customThemeName, customComposition, installedCanvasData, firstItemFocus, onOpenSettings)
        return
    }
    when (layout) {
        LayoutKind.BOTTOM_FLOATING -> WebOsLayout(state.apps, firstItemFocus, onLaunch, onOpenSettings)
        LayoutKind.BOTTOM_EXPANDING -> BottomBarLayout(state.apps, true, firstItemFocus, onLaunch, onOpenSettings)
        LayoutKind.TOP_NAV -> TopNavLayout(state.apps, firstItemFocus, onLaunch, onOpenSettings)
        LayoutKind.SIDE_GRID -> SideGridLayout(state.apps, firstItemFocus, onLaunch, onOpenSettings)
        LayoutKind.ANDROID_GRID -> AndroidTvLayout(state.apps, firstItemFocus, onLaunch, onOpenSettings)
        LayoutKind.GOOGLE_HOME -> GoogleTvLayout(state.apps, firstItemFocus, onLaunch, onOpenSettings)
        LayoutKind.APPLE_HOME -> AppleTvLayout(state.apps, firstItemFocus, onLaunch, onOpenSettings)
        LayoutKind.TIZEN_HOME -> TizenLayout(state.apps, firstItemFocus, onLaunch, onOpenSettings)
    }
}
