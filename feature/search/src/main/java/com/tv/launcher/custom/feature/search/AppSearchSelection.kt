package com.tv.launcher.custom.feature.search

import com.tv.launcher.custom.core.ui.theme.LayoutKind
import com.tv.launcher.custom.core.model.apps.AppEntry

internal fun filterSearchApps(apps: List<AppEntry>, query: String): List<AppEntry> =
    apps.filter { it.label.contains(query.trim(), ignoreCase = true) }

internal fun resolveSelectedSearchApp(apps: List<AppEntry>, selectedPackage: String?): AppEntry? =
    apps.firstOrNull { it.packageName == selectedPackage } ?: apps.firstOrNull()

internal fun shouldUseFocusSearchLayout(layout: LayoutKind): Boolean =
    layout == LayoutKind.SIDE_GRID || layout == LayoutKind.APPLE_HOME
