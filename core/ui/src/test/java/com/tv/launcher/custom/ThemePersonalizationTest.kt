package com.tv.launcher.custom

import com.tv.launcher.custom.core.model.theme.*
import com.tv.launcher.custom.core.ui.components.hasPersonalizationAction
import com.tv.launcher.custom.core.ui.components.InstalledAppsCanvasData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemePersonalizationTest {
    @Test
    fun `no agrega acceso si la barra ya permite personalizar`() {
        assertTrue(hasPersonalizationAction(ThemeComposition(emptyList(), topBar = TopBarConfig(tabs = "Inicio,Personalizar"))))
        assertTrue(hasPersonalizationAction(ThemeComposition(emptyList(), topBar = TopBarConfig(tabs = "Home,Customize"))))
    }

    @Test
    fun `reconoce temas y ajustes del launcher en el rail`() {
        listOf("Temas", "Themes", "Ajustes del launcher", "Personalización").forEach { label ->
            assertTrue(label, hasPersonalizationAction(ThemeComposition(emptyList(), rail = RailConfig(items = "Inicio,$label"))))
        }
    }

    @Test
    fun `reconoce accion existente en categorias`() {
        assertTrue(hasPersonalizationAction(ThemeComposition(listOf(ThemeBlock(ThemeBlockType.CHIPS_ROW, text = "Apps,Personalizar inicio")))))
    }

    @Test
    fun `ajustes del sistema no reemplaza personalizacion`() {
        assertFalse(hasPersonalizationAction(ThemeComposition(emptyList(), topBar = TopBarConfig(tabs = "Inicio,Apps,Ajustes"))))
        assertFalse(hasPersonalizationAction(ThemeComposition(emptyList(), rail = RailConfig(items = "Ajustes del sistema"))))
    }

    @Test
    fun `opcion oculta o texto decorativo no suprime el icono`() {
        assertFalse(hasPersonalizationAction(ThemeComposition(emptyList(), topBar = TopBarConfig(style = TopBarStyle.MINIMAL, tabs = "Personalizar"))))
        assertFalse(hasPersonalizationAction(ThemeComposition(listOf(ThemeBlock(ThemeBlockType.TITLE, text = "Mis temas")))))
    }

    @Test
    fun `acceso propio abre personalizacion y no ajustes del sistema incluso al cargar assets`() {
        val actions = mutableListOf<String>()
        val data = InstalledAppsCanvasData(
            apps = emptyList(), favorites = emptyList(), onLaunch = {}, onOpenApps = {},
            onOpenSettings = { actions += "launcher" }, onUnavailableNavigation = { actions += "unavailable" },
            onOpenPersonalization = { actions += "personalization" },
            onOpenSystemSettings = { actions += "system" }
        ).withThemeAssets(emptyMap())

        data.onNavClick("Personalización")
        data.onNavClick("Ajustes del launcher")
        data.onNavClick("Ajustes del sistema")

        assertEquals(listOf("personalization", "personalization", "system"), actions)
    }
}
