package com.tv.launcher.custom.feature.editor

import com.tv.launcher.custom.core.model.theme.*
import org.junit.Assert.*
import org.junit.Test

class ThemeCreationDraftTest {
    @Test fun `copiar una base conserva composicion y apariencia sin reutilizar identidad instalada`() {
        val appearance = LauncherAppearance(themeId = ThemeId.ROKU, installedThemeId = "source-id")
        val composition = ThemeComposition(listOf(ThemeBlock(ThemeBlockType.TITLE, text = "Mi portada")))
        val draft = createThemeCreationDraft("Copia de Mi tema", appearance, composition)
        assertEquals("Copia de Mi tema", draft.name)
        assertEquals(composition, draft.composition)
        assertEquals(appearance.copy(installedThemeId = null), draft.appearance)
        assertNull(draft.appearance.installedThemeId)
    }

    @Test fun `no convierte un diseño nativo en una plantilla arbitraria`() {
        assertThrows(IllegalArgumentException::class.java) {
            createThemeCreationDraft("Copia", LauncherAppearance(), null)
        }
    }
}
