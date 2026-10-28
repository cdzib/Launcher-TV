package com.tv.launcher.custom

import com.tv.launcher.custom.core.model.apps.AppEntry
import com.tv.launcher.custom.core.model.theme.ThemeBlock
import com.tv.launcher.custom.core.model.theme.ThemeBlockType
import com.tv.launcher.custom.core.ui.components.InstalledAppsCanvasData
import org.junit.Assert.assertEquals
import org.junit.Test

class MinimalistCanvasDataTest {
    @Test fun `favorites never fall back to installed or fabricated apps`() {
        val data = InstalledAppsCanvasData(listOf(AppEntry("org.example.real", "Real")), emptyList(), {}, {}, {}, {})
        assertEquals(0, data.itemCount(ThemeBlock(ThemeBlockType.FAVORITES_ROW)))
        assertEquals(1, data.itemCount(ThemeBlock(ThemeBlockType.APPS_ROW)))
        assertEquals("Real", data.label(ThemeBlock(ThemeBlockType.APPS_GRID), 0))
    }

    @Test fun `empty installed applications do not fabricate launch targets`() {
        val data = InstalledAppsCanvasData(emptyList(), emptyList(), { error("No app may be launched") }, {}, {}, {})
        val block = ThemeBlock(ThemeBlockType.APPS_ROW)
        assertEquals(0, data.itemCount(block))
        data.onItemClick(block, 0)
    }
}
