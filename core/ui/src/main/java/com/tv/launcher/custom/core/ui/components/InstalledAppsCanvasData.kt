package com.tv.launcher.custom.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tv.launcher.custom.core.model.apps.AppEntry
import com.tv.launcher.custom.core.model.theme.ThemeBlock
import com.tv.launcher.custom.core.model.theme.ThemeBlockType

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
    override fun itemCount(block: ThemeBlock) = appsFor(block).size
    override fun label(block: ThemeBlock, index: Int) = appsFor(block).getOrNull(index)?.label.orEmpty()
    @Composable
    override fun Icon(block: ThemeBlock, index: Int, modifier: Modifier) {
        appsFor(block).getOrNull(index)?.let { AppIcon(it.packageName, modifier) }
    }
    override fun onItemClick(block: ThemeBlock, index: Int) {
        if (block.type == ThemeBlockType.HERO) onOpenApps()
        else appsFor(block).getOrNull(index)?.let { onLaunch(it.packageName) }
    }
    override fun onNavClick(label: String) {
        when {
            label.contains("ajust", true) || label.contains("config", true) || label.contains("setting", true) -> onOpenSettings()
            label.contains("apps", true) || label.contains("aplic", true) || label.contains("busc", true) || label.contains("search", true) -> onOpenApps()
            label.contains("inicio", true) || label.contains("home", true) -> Unit
            else -> onUnavailableNavigation(label)
        }
    }
}
