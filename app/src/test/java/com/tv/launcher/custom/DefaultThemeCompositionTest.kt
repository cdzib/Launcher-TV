package com.tv.launcher.custom

import com.tv.launcher.custom.core.model.theme.ThemeId
import com.tv.launcher.custom.core.theme.defaultCompositionFor
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultThemeCompositionTest {
    @Test
    fun `todo layout tiene una composicion declarativa para la primera instalacion`() {
        ThemeId.entries.forEach { themeId ->
            val composition = defaultCompositionFor(themeId)
            assertTrue("$themeId requiere al menos un bloque", composition.blocks.isNotEmpty())
            assertTrue("$themeId requiere contenido dinámico",
                composition.blocks.any { it.appContent != null || it.usesCardLayout() })
        }
    }

    @Test
    fun `cada layout resuelve una composicion distinta`() {
        val first = ThemeId.entries.map { defaultCompositionFor(it) }
        assertTrue("Las composiciones por defecto deben ser distintas", first.toSet().size == ThemeId.entries.size)
    }
}
