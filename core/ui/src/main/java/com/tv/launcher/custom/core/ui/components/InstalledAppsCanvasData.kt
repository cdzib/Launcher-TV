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
    private val onUnavailableNavigation: (String) -> Unit,
    private val assetUris: Map<String, String> = emptyMap(),
    private val onOpenSystemSettings: () -> Unit = {},
    private val onOpenPersonalization: () -> Unit = onOpenSettings,
    private val onOpenSearch: () -> Unit = onOpenApps
) : CanvasData {
    private fun appsFor(block: ThemeBlock): List<AppEntry> = when (block.type) {
        ThemeBlockType.FAVORITES_ROW -> favorites
        ThemeBlockType.RECENTS_ROW -> emptyList()
        ThemeBlockType.MEDIA_ROW -> emptyList()
        else -> apps
    }
    override fun itemCount(block: ThemeBlock) = if (block.type == ThemeBlockType.MEDIA_ROW) {
        mediaItemsFor(block).size
    } else {
        appsFor(block).size
    }
    override fun label(block: ThemeBlock, index: Int) = if (block.type == ThemeBlockType.MEDIA_ROW) {
        mediaItemsFor(block).getOrNull(index)?.label.orEmpty()
    } else {
        appsFor(block).getOrNull(index)?.label.orEmpty()
    }
    @Composable
    override fun Icon(block: ThemeBlock, index: Int, modifier: Modifier) {
        when (block.type) {
            ThemeBlockType.HERO -> block.imageAsset?.let { ThemeAssetImage(assetUris[it], modifier) }
            ThemeBlockType.MEDIA_ROW -> mediaItemsFor(block).getOrNull(index)?.imageAsset?.let { path ->
                ThemeAssetImage(assetUris[path], modifier)
            }
            else -> appsFor(block).getOrNull(index)?.let { AppIcon(it.packageName, modifier) }
        }
    }
    @Composable
    override fun HeroBackground(block: ThemeBlock, modifier: Modifier) {
        block.imageAsset?.let { ThemeAssetImage(assetUris[it], modifier) }
    }
    override fun isItemClickable(block: ThemeBlock, index: Int): Boolean = when (block.type) {
        ThemeBlockType.MEDIA_ROW -> mediaItemsFor(block).getOrNull(index)?.launchPackage != null
        else -> true
    }
    override fun onItemClick(block: ThemeBlock, index: Int) {
        when (block.type) {
            ThemeBlockType.HERO -> onOpenApps()
            ThemeBlockType.MEDIA_ROW -> mediaItemsFor(block).getOrNull(index)?.launchPackage
                ?.takeIf { packageName -> apps.any { it.packageName == packageName } }
                ?.let(onLaunch)
            else -> appsFor(block).getOrNull(index)?.let { onLaunch(it.packageName) }
        }
    }
    override fun onNavClick(label: String) {
        when {
            isPersonalizationLabel(label) -> onOpenPersonalization()
            label.contains("ajust", true) || label.contains("config", true) || label.contains("setting", true) -> onOpenSystemSettings()
            label.contains("apps", true) || label.contains("aplic", true) -> onOpenApps()
            label.contains("busc", true) || label.contains("search", true) -> onOpenSearch()
            label.contains("inicio", true) || label.contains("home", true) -> Unit
            else -> onUnavailableNavigation(label)
        }
    }
    override fun onSystemSettingsClick() = onOpenSystemSettings()
    override fun onPersonalizationClick() = onOpenPersonalization()

    override fun withThemeAssets(assetUris: Map<String, String>): CanvasData = InstalledAppsCanvasData(
        apps = apps,
        favorites = favorites,
        onLaunch = onLaunch,
        onOpenApps = onOpenApps,
        onOpenSettings = onOpenSettings,
        onUnavailableNavigation = onUnavailableNavigation,
        assetUris = assetUris,
        onOpenSystemSettings = onOpenSystemSettings,
        onOpenPersonalization = onOpenPersonalization,
        onOpenSearch = onOpenSearch
    )

    private fun mediaItemsFor(block: ThemeBlock) = block.contentItems
        .filter { item -> item.launchPackage == null || apps.any { it.packageName == item.launchPackage } }
        .take(block.maxItems ?: block.contentItems.size)
}
