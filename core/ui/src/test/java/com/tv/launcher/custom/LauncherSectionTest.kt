package com.tv.launcher.custom

import com.tv.launcher.custom.core.ui.components.AppsFilter
import com.tv.launcher.custom.core.ui.components.LauncherSection
import com.tv.launcher.custom.core.ui.components.hasLauncherAction
import com.tv.launcher.custom.core.ui.components.resolveAppsFilter
import com.tv.launcher.custom.core.ui.components.resolveLauncherSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @Test
    fun `only navigation labels with a real destination stay visible`() {
        assertTrue(hasLauncherAction("Inicio"))
        assertTrue(hasLauncherAction("Apps"))
        assertTrue(hasLauncherAction("Buscar"))
        assertTrue(hasLauncherAction("Ajustes"))
        assertTrue(hasLauncherAction("Personalización"))
        assertTrue(hasLauncherAction("Perfil"))
        assertTrue(hasLauncherAction("Notificaciones"))
        assertTrue(hasLauncherAction("Películas"))
        assertTrue(hasLauncherAction("Series"))
        assertTrue(hasLauncherAction("Para ti"))
        assertTrue(hasLauncherAction("Deportes"))
        assertFalse(hasLauncherAction("Clima"))
    }

    @Test
    fun `content labels open the apps section with the right filter`() {
        assertEquals(AppsFilter.FOR_YOU, resolveAppsFilter("Para ti"))
        assertEquals(AppsFilter.FOR_YOU, resolveAppsFilter("Recomendados"))
        assertEquals(AppsFilter.FAVORITES, resolveAppsFilter("Favoritos"))
        assertEquals(AppsFilter.RECENTS, resolveAppsFilter("Recientes"))
        assertEquals(AppsFilter.VIDEO, resolveAppsFilter("Películas"))
        assertEquals(AppsFilter.VIDEO, resolveAppsFilter("Series"))
        assertEquals(AppsFilter.VIDEO, resolveAppsFilter("Películas y TV"))
        assertEquals(AppsFilter.VIDEO, resolveAppsFilter("Deportes"))
        assertEquals(AppsFilter.AUDIO, resolveAppsFilter("Música"))
        assertEquals(AppsFilter.GAMES, resolveAppsFilter("Juegos"))
        assertNull(resolveAppsFilter("Ajustes"))
    }
}
