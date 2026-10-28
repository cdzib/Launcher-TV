package com.tv.launcher.custom.core.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tv.launcher.custom.core.model.theme.CardStyle
import com.tv.launcher.custom.core.model.theme.ThemeBlockType
import com.tv.launcher.custom.core.model.theme.ThemeComposition
import com.tv.launcher.custom.core.ui.theme.LauncherStyle

/** App collections supply section surfaces independently of preset names. */
internal fun ThemeComposition.resolveSectionStyle(base: LauncherStyle): LauncherStyle {
    val block = blocks.firstOrNull { it.type == ThemeBlockType.APPS_GRID }
        ?: blocks.firstOrNull { it.type == ThemeBlockType.APPS_ROW }
        ?: blocks.firstOrNull { it.usesCardLayout() }
        ?: return base
    return base.copy(
        tileRadius = block.rCorner().dp,
        focusScale = block.rFocusScale(),
        glassSurfaces = block.rCardStyle() == CardStyle.GLASS,
        declarativeSurfaces = true,
        palette = base.palette.copy(
            background = bgColor?.let { Color(it) } ?: base.palette.background,
            surface = block.bgColor?.let { Color(it).copy(alpha = (block.bgAlpha ?: 100) / 100f) }
                ?: base.palette.surface,
            onBackground = block.textColor?.let { Color(it) } ?: base.palette.onBackground,
            onMuted = if (block.rCardStyle() == CardStyle.GLASS)
                (block.textColor?.let { Color(it) } ?: base.palette.onBackground).copy(alpha = .9f)
                else base.palette.onMuted
        )
    )
}
