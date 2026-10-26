package com.tv.launcher.custom

import com.tv.launcher.custom.core.ui.components.LauncherSection
import com.tv.launcher.custom.core.ui.components.resolveLauncherSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LauncherSectionTest {
    @Test
    fun `back returns secondary sections to home without leaving launcher`() {
        assertEquals(LauncherSection.HOME, LauncherSection.APPS.onBack())
        assertEquals(LauncherSection.HOME, LauncherSection.SEARCH.onBack())
        assertEquals(LauncherSection.HOME, LauncherSection.HOME.onBack())
    }

    @Test
    fun `maps existing Spanish and English section labels without treating settings as content`() {
        assertEquals(LauncherSection.HOME, resolveLauncherSection("Inicio"))
        assertEquals(LauncherSection.HOME, resolveLauncherSection("HOME"))
        assertEquals(LauncherSection.APPS, resolveLauncherSection("Aplicaciones"))
        assertEquals(LauncherSection.APPS, resolveLauncherSection("Apps"))
        assertEquals(LauncherSection.SEARCH, resolveLauncherSection("Buscar apps"))
        assertEquals(LauncherSection.SEARCH, resolveLauncherSection("Search"))
        assertNull(resolveLauncherSection("Ajustes"))
        assertNull(resolveLauncherSection("Personalizar"))
    }
}
