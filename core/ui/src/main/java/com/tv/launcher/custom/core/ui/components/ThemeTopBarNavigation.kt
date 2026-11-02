package com.tv.launcher.custom.core.ui.components

import com.tv.launcher.custom.core.model.theme.TopBarConfig
import com.tv.launcher.custom.core.model.theme.TopBarStyle

fun isSearchLabel(label: String): Boolean =
    label.contains("busc", ignoreCase = true) || label.contains("search", ignoreCase = true)

fun isProfileLabel(label: String): Boolean =
    label.contains("perfil", ignoreCase = true) || label.contains("profile", ignoreCase = true) ||
        label.contains("cuenta", ignoreCase = true) || label.contains("account", ignoreCase = true)

fun isNotificationsLabel(label: String): Boolean =
    label.contains("notific", ignoreCase = true) || label.contains("notification", ignoreCase = true) ||
        label.contains("avis", ignoreCase = true)

/** Sections the launcher can actually open. Themes keep their visual identity, not dead buttons. */
fun hasLauncherAction(label: String): Boolean =
    resolveLauncherSection(label) != null || resolveAppsFilter(label) != null ||
        isSystemSettingsLabel(label) || isPersonalizationLabel(label) ||
        isProfileLabel(label) || isNotificationsLabel(label)

fun isSystemSettingsLabel(label: String): Boolean = !isPersonalizationLabel(label) && (
    label.contains("ajust", ignoreCase = true) || label.contains("config", ignoreCase = true) ||
        label.contains("setting", ignoreCase = true))

fun getRenderedTopBarTabs(config: TopBarConfig): List<String> {
    if (config.style == TopBarStyle.MINIMAL) return emptyList()
    return config.tabs.orEmpty().split(',').map(String::trim).filter(String::isNotEmpty).filterNot { label ->
        isSystemSettingsLabel(label) && config.showSettings
    }.filter(::hasLauncherAction)
}

fun shouldShowTopBarSearchButton(config: TopBarConfig): Boolean =
    config.showSearch && config.style != TopBarStyle.MINIMAL &&
        config.tabs.orEmpty().split(',').none(::isSearchLabel)
