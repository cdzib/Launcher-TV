package com.tv.launcher.custom

import com.tv.launcher.custom.core.model.theme.TopBarConfig
import com.tv.launcher.custom.core.model.theme.TopBarStyle
import com.tv.launcher.custom.core.ui.components.InstalledAppsCanvasData
import com.tv.launcher.custom.core.ui.components.getRenderedTopBarTabs
import com.tv.launcher.custom.core.ui.components.isSearchLabel
import com.tv.launcher.custom.core.ui.components.shouldShowTopBarSearchButton
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeSearchNavigationTest {
    @Test
    fun `buscar declarado no desaparece al habilitar el icono de busqueda`() {
        listOf(TopBarStyle.TABS_PILL, TopBarStyle.TABS_TEXT, TopBarStyle.ICONS).forEach { style ->
            listOf("Buscar", "Search").forEach { label ->
                listOf(true, false).forEach { showSearch ->
                    val config = TopBarConfig(style = style, tabs = "Inicio,$label,Apps", showSearch = showSearch)
                    assertEquals("$style $label $showSearch", 1, countSearchActions(config))
                }
            }
        }
    }

    @Test
    fun `icono implicito aparece una vez en barras de texto y de iconos`() {
        listOf(TopBarStyle.TABS_PILL, TopBarStyle.TABS_TEXT, TopBarStyle.ICONS).forEach { style ->
            assertEquals(style.name, 1, countSearchActions(TopBarConfig(style = style, tabs = "Inicio,Apps", showSearch = true)))
            assertEquals(style.name, 0, countSearchActions(TopBarConfig(style = style, tabs = "Inicio,Apps", showSearch = false)))
        }
    }

    @Test
    fun `barra minima respeta su configuracion sin inventar acceso`() {
        assertEquals(0, countSearchActions(TopBarConfig(style = TopBarStyle.MINIMAL)))
    }

    @Test
    fun `buscar y apps invocan acciones independientes conservando assets`() {
        val actions = mutableListOf<String>()
        val data = InstalledAppsCanvasData(
            apps = emptyList(), favorites = emptyList(), onLaunch = {}, onOpenSettings = {},
            onOpenApps = { actions += "apps" }, onOpenSearch = { actions += "search" },
            onUnavailableNavigation = { actions += "unavailable" }
        ).withThemeAssets(emptyMap())

        data.onNavClick("Buscar")
        data.onNavClick("Search")
        data.onNavClick("Apps")

        assertEquals(listOf("search", "search", "apps"), actions)
    }

    private fun countSearchActions(config: TopBarConfig): Int =
        getRenderedTopBarTabs(config).count(::isSearchLabel) + if (shouldShowTopBarSearchButton(config)) 1 else 0
}
