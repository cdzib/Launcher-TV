package com.tv.launcher.custom.core.theme.layout

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import com.tv.launcher.custom.core.ui.components.CanvasData
import com.tv.launcher.custom.core.ui.components.hasPersonalizationAction
import com.tv.launcher.custom.core.ui.components.isPersonalizationLabel
import com.tv.launcher.custom.core.theme.CompositionCanvas
import com.tv.launcher.custom.core.model.theme.ThemeComposition
import com.tv.launcher.custom.core.model.theme.TopBarStyle
import com.tv.launcher.custom.core.model.theme.ThemeBlock
import com.tv.launcher.custom.core.model.theme.ThemeBlockType
import com.tv.launcher.custom.core.model.theme.ThemeId
import com.tv.launcher.custom.core.model.theme.TopBarConfig
import com.tv.launcher.custom.core.model.theme.RailConfig
import com.tv.launcher.custom.core.model.theme.RailStyle
import com.tv.launcher.custom.core.theme.PlaceholderCanvasData
import com.tv.launcher.custom.core.ui.theme.LauncherTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CustomThemeLayout(
    name: String,
    composition: ThemeComposition,
    canvasData: CanvasData,
    firstItemFocusRequester: FocusRequester,
    onOpenSettings: () -> Unit,
    onOpenPersonalization: () -> Unit = onOpenSettings,
    assetUris: Map<String, String> = emptyMap()
) {
    val configuredNavigation = buildList {
        addAll(composition.topBar?.takeUnless { it.style == TopBarStyle.MINIMAL }?.tabs.orEmpty().split(','))
        addAll(composition.rail?.items.orEmpty().split(','))
    }.map(String::trim)
    val hasExplicitSystemSettingsAction = configuredNavigation.any {
        !isPersonalizationLabel(it) && (it.contains("ajust", true) || it.contains("config", true) || it.contains("setting", true))
    }
    val topBarConfig = composition.topBar
    val hasSystemSettingsAction = hasExplicitSystemSettingsAction || when (topBarConfig?.style) {
        null, TopBarStyle.MINIMAL, TopBarStyle.ICONS -> false
        else -> topBarConfig.showSettings
    }
    CompositionCanvas(
        composition = composition,
        data = canvasData.withThemeAssets(assetUris),
        modifier = Modifier.fillMaxSize(),
        title = name,
        interactive = true,
        transparentBackground = true,
        firstItemFocusRequester = firstItemFocusRequester,
        onOpenPersonalization = onOpenPersonalization.takeUnless { hasPersonalizationAction(composition) },
        onOpenSystemSettingsFallback = onOpenSettings.takeUnless { hasSystemSettingsAction }
    )
}

@Preview(name = "Tema sin personalización · Icono junto a Ajustes", widthDp = 960, heightDp = 540)
@Composable
private fun CustomThemeMissingPersonalizationPreview() = CustomThemePersonalizationPreview("Inicio,Apps")

@Preview(name = "Tema con personalización · Sin duplicado", widthDp = 960, heightDp = 540)
@Composable
private fun CustomThemeExistingPersonalizationPreview() = CustomThemePersonalizationPreview("Inicio,Apps,Personalizar")

@Preview(name = "Buscar declarado · Acceso visible", widthDp = 960, heightDp = 540)
@Composable
private fun CustomThemeExplicitSearchPreview() = CustomThemePersonalizationPreview("Inicio,Buscar,Apps")

@Preview(name = "Barra de iconos · Búsqueda habilitada", widthDp = 960, heightDp = 540)
@Composable
private fun CustomThemeSearchIconPreview() = CustomThemePersonalizationPreview("Inicio,Apps", TopBarStyle.ICONS)

@Composable
private fun CustomThemePersonalizationPreview(tabs: String, style: TopBarStyle = TopBarStyle.TABS_PILL) = LauncherTheme(ThemeId.WEBOS) {
    CustomThemeLayout(
        name = "Inicio",
        composition = ThemeComposition(
            blocks = listOf(ThemeBlock(ThemeBlockType.TITLE, text = "Inicio")),
            topBar = TopBarConfig(style = style, tabs = tabs, showClock = false)
        ),
        canvasData = PlaceholderCanvasData,
        firstItemFocusRequester = remember { FocusRequester() },
        onOpenSettings = {},
        onOpenPersonalization = {}
    )
}

@Preview(name = "Side Stream · Un solo perfil", widthDp = 960, heightDp = 540)
@Composable
private fun CustomThemeSingleProfilePreview() = LauncherTheme(ThemeId.WEBOS) {
    CustomThemeLayout(
        name = "Inicio",
        composition = ThemeComposition(
            blocks = listOf(ThemeBlock(ThemeBlockType.TITLE, text = "Inicio")),
            topBar = TopBarConfig(style = TopBarStyle.MINIMAL, showSettings = false, showClock = false),
            rail = RailConfig(style = RailStyle.ICONS, items = "Perfil,Buscar,Apps,Ajustes", showProfile = true, width = 72)
        ),
        canvasData = PlaceholderCanvasData,
        firstItemFocusRequester = remember { FocusRequester() },
        onOpenSettings = {},
        onOpenPersonalization = {}
    )
}
