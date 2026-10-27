package com.tv.launcher.custom.core.data.theme

import com.tv.launcher.custom.core.model.theme.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class WebOsCompositionContractTest {
    @Test fun `preserva fuentes dinamicas de apps en el contrato declarativo`() {
        val composition = ThemeComposition(listOf(
            ThemeBlock(ThemeBlockType.HERO, appContent = AppContent.FEATURED_APP, startInset = 48, endInset = 12),
            ThemeBlock(ThemeBlockType.CHIPS_ROW, appContent = AppContent.SHORTCUTS),
            ThemeBlock(ThemeBlockType.FAVORITES_ROW, maxItems = 20, appContent = AppContent.APP_ICONS),
            ThemeBlock(ThemeBlockType.TITLE, appContent = AppContent.SHORTCUT_EDITOR),
            ThemeBlock(ThemeBlockType.APPS_ROW, maxItems = 20, appContent = AppContent.APP_CARDS)
        ), topPadding = 24, rail = RailConfig(style = RailStyle.ICONS, width = 56, overlay = true))
        assertEquals(composition, ThemeCompositionValidator.parse(ThemeCompositionValidator.encode(composition).toString()))
    }

    @Test fun `rechaza un contenido de apps desconocido`() {
        assertThrows(ThemeArchiveException::class.java) {
            ThemeCompositionValidator.parse("""{"blocks":[{"type":"hero","appContent":"unknown"}]}""")
        }
    }

    @Test fun `rechaza roles de apps que no corresponden al tipo de bloque`() {
        assertThrows(ThemeArchiveException::class.java) {
            ThemeCompositionValidator.parse("""{"blocks":[{"type":"title","appContent":"SHORTCUTS"}]}""")
        }
    }
}
