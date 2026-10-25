package com.tv.launcher.custom.core.model.theme

data class InstalledTheme(
    val id: String,
    val name: String,
    val layout: ThemeId,
    val appearance: LauncherAppearance,
    val version: String? = null,
    val composition: ThemeComposition? = null
)
