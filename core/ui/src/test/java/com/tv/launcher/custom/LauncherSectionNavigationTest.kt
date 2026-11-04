package com.tv.launcher.custom

import com.tv.launcher.custom.core.ui.components.InstalledAppsCanvasData
import com.tv.launcher.custom.core.ui.components.LauncherSection
import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherSectionNavigationTest {
    @Test
    fun `declarative navigation can return to home after apps and search`() {
        var section = LauncherSection.HOME
        val data = InstalledAppsCanvasData(
            apps = emptyList(), favorites = emptyList(), onLaunch = {},
            onOpenApps = { section = LauncherSection.APPS }, onOpenSettings = {},
            onUnavailableNavigation = { error("Unexpected destination: $it") },
            onOpenSearch = { section = LauncherSection.SEARCH },
            onOpenHome = { section = LauncherSection.HOME }
        )
        data.onNavClick("Apps")
        assertEquals(LauncherSection.APPS, section)
        data.onNavClick("Buscar")
        assertEquals(LauncherSection.SEARCH, section)
        data.onNavClick("Inicio")
        assertEquals(LauncherSection.HOME, section)
        data.withThemeAssets(emptyMap()).onNavClick("Search")
        assertEquals(LauncherSection.SEARCH, section)
        data.withThemeAssets(emptyMap()).onNavClick("Home")
        assertEquals(LauncherSection.HOME, section)
    }
}
