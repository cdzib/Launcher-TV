package com.tv.launcher.custom

import com.tv.launcher.custom.feature.home.presentation.components.findSettingsPreviewTheme
import com.tv.launcher.custom.feature.theme.data.InstalledTheme
import com.tv.launcher.custom.feature.theme.domain.model.LauncherAppearance
import com.tv.launcher.custom.feature.theme.domain.model.ThemeId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SettingsThemeSelectionTest {
    private val active = theme("active")
    private val other = theme("other")

    @Test
    fun `preview selection prefers explicit choice over active theme`() {
        assertEquals(other, findSettingsPreviewTheme(listOf(active, other), other.id, active.id))
    }

    @Test
    fun `removed preview falls back to the active theme`() {
        assertEquals(active, findSettingsPreviewTheme(listOf(active), other.id, active.id))
    }

    @Test
    fun `an empty library uses the built in appearance`() {
        assertNull(findSettingsPreviewTheme(emptyList(), other.id, active.id))
    }

    @Test
    fun `no installed active theme does not silently select the first library item`() {
        assertNull(findSettingsPreviewTheme(listOf(other), null, null))
    }

    @Test
    fun `active theme is selected when the asynchronous library arrives`() {
        assertEquals(active, findSettingsPreviewTheme(listOf(other, active), null, active.id))
    }

    @Test
    fun `a removed theme without an installed active theme falls back to built in appearance`() {
        assertNull(findSettingsPreviewTheme(listOf(other), "removed", null))
    }

    private fun theme(id: String) = InstalledTheme(
        id, id, ThemeId.WEBOS, LauncherAppearance(installedThemeId = id)
    )
}
