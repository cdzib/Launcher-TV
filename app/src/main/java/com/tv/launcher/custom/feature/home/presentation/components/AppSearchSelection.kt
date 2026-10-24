package com.tv.launcher.custom.feature.home.presentation.components

import com.tv.launcher.custom.core.theme.LayoutKind
import com.tv.launcher.custom.feature.apps.domain.model.AppEntry

internal fun filterSearchApps(apps: List<AppEntry>, query: String): List<AppEntry> =
    apps.filter { it.label.contains(query.trim(), ignoreCase = true) }

internal fun resolveSelectedSearchApp(apps: List<AppEntry>, selectedPackage: String?): AppEntry? =
    apps.firstOrNull { it.packageName == selectedPackage } ?: apps.firstOrNull()

internal fun shouldUseFocusSearchLayout(layout: LayoutKind): Boolean =
    layout == LayoutKind.SIDE_GRID || layout == LayoutKind.APPLE_HOME
