package com.tv.launcher.custom

import com.tv.launcher.custom.core.ui.theme.LayoutKind
import com.tv.launcher.custom.core.model.apps.AppEntry
import com.tv.launcher.custom.feature.search.filterSearchApps
import com.tv.launcher.custom.feature.search.resolveSelectedSearchApp
import com.tv.launcher.custom.feature.search.shouldUseFocusSearchLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSearchTest {
    private val apps = listOf(AppEntry("video", "YouTube"), AppEntry("music", "YouTube Music"), AppEntry("plex", "Plex"))

    @Test
    fun `search ignores case and surrounding whitespace while preserving order`() {
        assertEquals(apps.take(2), filterSearchApps(apps, "  yOuTuBe  "))
    }

    @Test
    fun `selection survives filtering but never points to a missing result`() {
        assertEquals(apps[1], resolveSelectedSearchApp(apps, "music"))
        assertEquals(apps[0], resolveSelectedSearchApp(apps.take(1), "music"))
        assertNull(resolveSelectedSearchApp(emptyList(), "music"))
    }

    @Test
    fun `blank queries include every app and unmatched queries include none`() {
        assertEquals(apps, filterSearchApps(apps, "   "))
        assertEquals(emptyList<AppEntry>(), filterSearchApps(apps, "missing"))
        assertEquals(emptyList<AppEntry>(), filterSearchApps(emptyList(), "YouTube"))
    }

    @Test
    fun `selection uses package identity rather than duplicate labels or old objects`() {
        val renamed = apps[1].copy(label = "New label")
        val duplicate = apps[0].copy(label = "New label")
        assertEquals(renamed, resolveSelectedSearchApp(listOf(duplicate, renamed), "music"))
        assertEquals(duplicate, resolveSelectedSearchApp(listOf(duplicate, renamed), null))
    }

    @Test
    fun `focus search layout is reserved for roku and apple profiles`() {
        assertTrue(shouldUseFocusSearchLayout(LayoutKind.SIDE_GRID))
        assertTrue(shouldUseFocusSearchLayout(LayoutKind.APPLE_HOME))
        LayoutKind.values()
            .filterNot { it == LayoutKind.SIDE_GRID || it == LayoutKind.APPLE_HOME }
            .forEach { layout ->
                assertFalse(layout.name, shouldUseFocusSearchLayout(layout))
            }
    }
}
