package com.tv.launcher.custom.feature.home.presentation.components

import com.tv.launcher.custom.feature.theme.data.InstalledTheme

/** Browsing is local UI state; only the Apply action may change the persisted appearance. */
internal fun findSettingsPreviewTheme(
    themes: List<InstalledTheme>,
    selectedThemeId: String?,
    activeThemeId: String?
): InstalledTheme? = themes.firstOrNull { it.id == selectedThemeId }
    ?: themes.firstOrNull { it.id == activeThemeId }
