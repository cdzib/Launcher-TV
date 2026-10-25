package com.tv.launcher.custom.core.ui.components

import com.tv.launcher.custom.core.model.theme.TopBarConfig
import com.tv.launcher.custom.core.model.theme.TopBarStyle

fun isSearchLabel(label: String): Boolean =
    label.contains("busc", ignoreCase = true) || label.contains("search", ignoreCase = true)

fun isSystemSettingsLabel(label: String): Boolean = !isPersonalizationLabel(label) && (
    label.contains("ajust", ignoreCase = true) || label.contains("config", ignoreCase = true) ||
        label.contains("setting", ignoreCase = true))

fun getRenderedTopBarTabs(config: TopBarConfig): List<String> {
    if (config.style == TopBarStyle.MINIMAL) return emptyList()
    return config.tabs.orEmpty().split(',').map(String::trim).filter(String::isNotEmpty).filterNot { label ->
        isSystemSettingsLabel(label) && config.showSettings
    }
}

fun shouldShowTopBarSearchButton(config: TopBarConfig): Boolean =
    config.showSearch && config.style != TopBarStyle.MINIMAL &&
        config.tabs.orEmpty().split(',').none(::isSearchLabel)
