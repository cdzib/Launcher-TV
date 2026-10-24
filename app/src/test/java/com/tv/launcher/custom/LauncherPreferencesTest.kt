package com.tv.launcher.custom

import androidx.datastore.preferences.core.mutablePreferencesOf
import com.tv.launcher.custom.feature.theme.data.setLauncherAppearance
import com.tv.launcher.custom.feature.theme.data.toLauncherAppearance
import com.tv.launcher.custom.feature.theme.domain.model.LauncherAppearance
import com.tv.launcher.custom.feature.theme.domain.model.ThemeId
import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherPreferencesTest {
    @Test
    fun `apariencia conserva color e imagen y el modo elegido elimina la otra fuente`() {
        val preferences = mutablePreferencesOf()
        val colorAppearance = LauncherAppearance(
            backgroundColorArgb = 0xFF123456L,
            accentColorArgb = 0xFFABCDEF,
            installedThemeId = "f0d3e5a2-743c-4a1e-90cb-28775a60acb2"
        )
        preferences.setLauncherAppearance(colorAppearance)
        assertEquals(colorAppearance, preferences.toLauncherAppearance())

        val imageAppearance = colorAppearance.copy(
            backgroundImageUri = "content://wallpaper/one",
            backgroundColorArgb = null
        )
        preferences.setLauncherAppearance(imageAppearance)
        assertEquals(imageAppearance, preferences.toLauncherAppearance())
    }

    @Test
    fun `las preferencias conservan posiciones y accesos vacios`() {
        val appearance = LauncherAppearance(themeId = ThemeId.WEBOS, shortcuts = mapOf(0 to "app.video", 1 to "", 4 to "app.music"))
        val preferences = mutablePreferencesOf()
        preferences.setLauncherAppearance(appearance)
        assertEquals(appearance, preferences.toLauncherAppearance())
    }

    @Test
    fun `una escritura nueva elimina asignaciones antiguas y conserva defaults compatibles`() {
        val preferences = mutablePreferencesOf()
        preferences.setLauncherAppearance(LauncherAppearance(shortcuts = mapOf(4 to "old.app")))
        preferences.setLauncherAppearance(LauncherAppearance(shortcuts = mapOf(0 to "new.app")))
        assertEquals(mapOf(0 to "new.app"), preferences.toLauncherAppearance().shortcuts)
        assertEquals(emptyMap<Int, String>(), mutablePreferencesOf().toLauncherAppearance().shortcuts)
    }
}
