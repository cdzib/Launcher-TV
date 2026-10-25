package com.tv.launcher.custom.core.ui.components

import com.tv.launcher.custom.core.model.theme.ThemeBlockType
import com.tv.launcher.custom.core.model.theme.ThemeComposition
import com.tv.launcher.custom.core.model.theme.TopBarStyle

private val PERSONALIZATION_LABEL = Regex(
    "\\b(personal[\\p{L}\\p{N}_]*|customiz[\\p{L}\\p{N}_]*|customis[\\p{L}\\p{N}_]*|temas?|themes?|launcher)\\b",
    RegexOption.IGNORE_CASE
)

fun isPersonalizationLabel(label: String): Boolean = PERSONALIZATION_LABEL.containsMatchIn(label)

/** Only actionable, visible navigation can replace the fallback personalization icon. */
fun hasPersonalizationAction(composition: ThemeComposition): Boolean {
    val topBarLabels = composition.topBar?.takeUnless { it.style == TopBarStyle.MINIMAL }?.tabs.orEmpty()
    val railLabels = composition.rail?.items.orEmpty()
    val chipLabels = composition.blocks.filter { it.type == ThemeBlockType.CHIPS_ROW }.mapNotNull { it.text }
    return (listOf(topBarLabels, railLabels) + chipLabels)
        .flatMap { it.split(',') }.any(::isPersonalizationLabel)
}
