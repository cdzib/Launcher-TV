package com.tv.launcher.custom.core.ui.components

import com.tv.launcher.custom.core.model.theme.RailConfig

private val PROFILE_LABEL = Regex("\\b(perfil|profile)\\b", RegexOption.IGNORE_CASE)

/** Keep the navigation entry; omit only the optional decorative avatar. */
fun shouldShowRailProfileAvatar(config: RailConfig): Boolean =
    config.showProfile && config.items.orEmpty().split(',').none { PROFILE_LABEL.containsMatchIn(it) }
