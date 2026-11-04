package com.tv.launcher.custom.feature.editor

import com.tv.launcher.custom.core.model.theme.LauncherAppearance
import com.tv.launcher.custom.core.model.theme.ThemeComposition

data class ThemeCreationDraft(
    val name: String,
    val appearance: LauncherAppearance,
    val composition: ThemeComposition
)

/** A theme base is copied exactly; native layouts cannot be inferred from a preset. */
fun createThemeCreationDraft(
    name: String,
    appearance: LauncherAppearance,
    composition: ThemeComposition?
): ThemeCreationDraft {
    requireNotNull(composition) { "A native layout cannot be copied as an editable composition" }
    return ThemeCreationDraft(name, appearance.copy(installedThemeId = null), composition)
}
