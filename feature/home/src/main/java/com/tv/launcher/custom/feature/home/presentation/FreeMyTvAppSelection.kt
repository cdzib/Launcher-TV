package com.tv.launcher.custom.feature.home.presentation

import com.tv.launcher.custom.core.model.apps.AppEntry

private const val MAX_FREE_MY_TV_FAVORITES = 8

internal fun resolveFreeMyTvFeaturedApp(apps: List<AppEntry>): AppEntry? =
    apps.firstOrNull { it.label.contains("netflix", ignoreCase = true) } ?: apps.firstOrNull()

internal fun resolveFreeMyTvFavoriteApps(
    apps: List<AppEntry>,
    shortcuts: Map<Int, String>,
    maxItems: Int = MAX_FREE_MY_TV_FAVORITES
): List<AppEntry> {
    val installedAppsByPackage = apps.associateBy(AppEntry::packageName)
    val favorites = shortcuts.toSortedMap().values.mapNotNull(installedAppsByPackage::get).distinct()
    return (favorites.ifEmpty { apps }).take(maxItems.coerceAtLeast(0))
}
