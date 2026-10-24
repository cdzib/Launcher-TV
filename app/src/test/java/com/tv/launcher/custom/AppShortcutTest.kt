package com.tv.launcher.custom

import com.tv.launcher.custom.feature.apps.domain.model.AppEntry
import com.tv.launcher.custom.feature.home.presentation.components.resolveShortcut
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppShortcutTest {
    private val apps = listOf(AppEntry("a", "A"), AppEntry("b", "B"))

    @Test
    fun `sin configuracion usa apps instaladas y deja vacios los slots restantes`() {
        assertEquals(apps[0], resolveShortcut(apps, emptyMap(), 0))
        assertNull(resolveShortcut(apps, emptyMap(), 4))
    }

    @Test
    fun `una asignacion conserva su app aunque cambie el orden`() {
        assertEquals(apps[1], resolveShortcut(apps.reversed(), mapOf(0 to "b"), 0))
    }

    @Test
    fun `una app eliminada y un slot vacio no abren otra app por accidente`() {
        assertNull(resolveShortcut(apps, mapOf(0 to "removed.app"), 0))
        assertNull(resolveShortcut(apps, mapOf(0 to ""), 0))
        assertNull(resolveShortcut(apps, emptyMap(), -1))
    }
}
