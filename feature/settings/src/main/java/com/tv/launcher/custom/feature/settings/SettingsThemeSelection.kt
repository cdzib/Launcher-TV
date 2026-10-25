package com.tv.launcher.custom.feature.settings

import com.tv.launcher.custom.core.model.theme.InstalledTheme

/** Browsing is local UI state; only the Apply action may change the persisted appearance. */
internal fun findSettingsPreviewTheme(
    themes: List<InstalledTheme>,
    selectedThemeId: String?,
    activeThemeId: String?
): InstalledTheme? = themes.firstOrNull { it.id == selectedThemeId }
    ?: themes.firstOrNull { it.id == activeThemeId }
