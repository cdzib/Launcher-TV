package com.tv.launcher.custom

import com.tv.launcher.custom.core.model.theme.RailConfig
import com.tv.launcher.custom.core.model.theme.RailStyle
import com.tv.launcher.custom.core.ui.components.shouldShowRailProfileAvatar
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeProfileTest {
    @Test
    fun `side stream conserva perfil del menu sin duplicar avatar`() {
        val rail = RailConfig(
            style = RailStyle.ICONS,
            items = "Perfil,Buscar,Apps,Ajustes",
            showProfile = true
        )

        assertFalse(shouldShowRailProfileAvatar(rail))
    }

    @Test
    fun `detecta perfil en los estilos de rail y en ambos idiomas`() {
        RailStyle.entries.forEach { style ->
            listOf("Perfil", " profile ", "PERFIL", "Mi perfil", "User profile").forEach { label ->
                assertFalse("$style - $label", shouldShowRailProfileAvatar(
                    RailConfig(style = style, items = "Inicio,$label,Apps", showProfile = true)
                ))
            }
        }
    }

    @Test
    fun `mantiene avatar solicitado cuando el menu no contiene perfil`() {
        assertTrue(shouldShowRailProfileAvatar(RailConfig(items = "Inicio,Buscar,Apps,Ajustes", showProfile = true)))
        assertTrue(shouldShowRailProfileAvatar(RailConfig(items = null, showProfile = true)))
    }

    @Test
    fun `no agrega avatar si el autor lo desactivo`() {
        assertFalse(shouldShowRailProfileAvatar(RailConfig(showProfile = false)))
        assertFalse(shouldShowRailProfileAvatar(RailConfig(items = "Perfil", showProfile = false)))
    }
}
