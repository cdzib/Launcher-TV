package com.tv.launcher.custom.feature.home.presentation.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import com.tv.launcher.custom.feature.apps.domain.model.AppEntry
import com.tv.launcher.custom.feature.home.presentation.components.AppIcon
import com.tv.launcher.custom.feature.theme.data.CanvasData
import com.tv.launcher.custom.feature.theme.data.CompositionCanvas
import com.tv.launcher.custom.feature.theme.data.ThemeBlock
import com.tv.launcher.custom.feature.theme.data.ThemeBlockType
import com.tv.launcher.custom.feature.theme.data.ThemeComposition

class InstalledAppsCanvasData(
    private val apps: List<AppEntry>,
    private val favorites: List<AppEntry>,
    private val onLaunch: (String) -> Unit,
    private val onOpenApps: () -> Unit,
    private val onOpenSettings: () -> Unit,
    private val onUnavailableNavigation: (String) -> Unit
) : CanvasData {
    private fun appsFor(block: ThemeBlock): List<AppEntry> = when (block.type) {
        ThemeBlockType.FAVORITES_ROW -> favorites
        ThemeBlockType.RECENTS_ROW -> emptyList()
        else -> apps
    }

    override fun itemCount(block: ThemeBlock): Int = appsFor(block).size

    override fun label(block: ThemeBlock, index: Int): String = appsFor(block).getOrNull(index)?.label.orEmpty()

    @Composable
    override fun Icon(block: ThemeBlock, index: Int, modifier: Modifier) {
        appsFor(block).getOrNull(index)?.let { AppIcon(it.packageName, modifier) }
    }

    override fun onItemClick(block: ThemeBlock, index: Int) {
        if (block.type == ThemeBlockType.HERO) {
            onOpenApps()
        } else {
            appsFor(block).getOrNull(index)?.let { onLaunch(it.packageName) }
        }
    }

    override fun onNavClick(label: String) {
        when {
            label.contains("ajust", ignoreCase = true) || label.contains("config", ignoreCase = true) ||
                label.contains("setting", ignoreCase = true) -> onOpenSettings()
            label.contains("apps", ignoreCase = true) || label.contains("aplic", ignoreCase = true) ||
                label.contains("busc", ignoreCase = true) || label.contains("search", ignoreCase = true) -> onOpenApps()
            label.contains("inicio", ignoreCase = true) || label.contains("home", ignoreCase = true) -> Unit
            else -> onUnavailableNavigation(label)
        }
    }
}

@Composable
fun CustomThemeLayout(
    name: String,
    composition: ThemeComposition,
    canvasData: CanvasData,
    firstItemFocusRequester: FocusRequester,
    onOpenSettings: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        CompositionCanvas(
            composition = composition,
            data = canvasData,
            modifier = Modifier.fillMaxSize(),
            title = name,
            interactive = true,
            transparentBackground = true,
            firstItemFocusRequester = firstItemFocusRequester
        )
    }
}
