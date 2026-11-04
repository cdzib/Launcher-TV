package com.tv.launcher.custom

import com.tv.launcher.custom.core.data.theme.ThemeArchiveReader
import com.tv.launcher.custom.core.data.theme.ThemeManifestValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import com.tv.launcher.custom.core.model.theme.ThemeBlockType
import org.junit.Test
import java.io.File

class NovaMinimalPremiumThemeTest {
    @Test
    fun bundledArchiveContainsAValidMinimalPremiumComposition() {
        val archiveFile = File("src/main/assets/demo-themes/nova-minimal-premium.zip")
        assertTrue("The bundled theme archive must exist", archiveFile.isFile)

        val archive = ThemeArchiveReader.read(archiveFile.readBytes())
        val manifest = ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)

        assertEquals("NOVA · Minimal Premium", manifest.name)
        assertEquals("GOOGLE_TV", manifest.layout.name)
        assertTrue(manifest.composition?.topBar != null)
        val blocks = manifest.composition?.blocks.orEmpty()
        assertEquals(
            listOf(ThemeBlockType.TITLE, ThemeBlockType.FAVORITES_ROW, ThemeBlockType.APPS_ROW),
            blocks.map { it.type }
        )
        assertEquals("Inicio", blocks.first().text)
        assertEquals(8, blocks.last().maxItems)
    }
}
