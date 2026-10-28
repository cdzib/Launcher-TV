package com.tv.launcher.custom

import com.tv.launcher.custom.core.data.theme.ThemeArchiveException
import com.tv.launcher.custom.core.data.theme.ThemeCompositionValidator
import com.tv.launcher.custom.core.data.theme.ThemeArchiveWriter
import com.tv.launcher.custom.core.data.theme.ThemeArchiveReader
import com.tv.launcher.custom.core.data.theme.ThemeManifestValidator
import com.tv.launcher.custom.core.model.theme.*
import org.junit.Assert.*
import org.junit.Test

class GlassLayoutCodecTest {
    @Test
    fun `standalone landscape and inset semantics require a v3 archive`() {
        listOf(
            ThemeComposition(listOf(ThemeBlock(ThemeBlockType.TITLE)), bgStyle = BgStyle.LANDSCAPE),
            ThemeComposition(listOf(ThemeBlock(ThemeBlockType.TITLE)), rail = RailConfig(topInset = 80, height = 300))
        ).forEach { composition ->
            val archive = ThemeArchiveReader.read(ThemeArchiveWriter.write(LauncherAppearance(ThemeId.WEBOS), null, composition))
            assertTrue(archive.manifest.contains("\"formatVersion\":3"))
            assertThrows(ThemeArchiveException::class.java) {
                ThemeManifestValidator.parse(archive.manifest.replace("\"formatVersion\":3", "\"formatVersion\":2"), archive.assets.keys)
            }
        }
    }
    @Test
    fun `overlay offsets glass and rail bounds survive encoding`() {
        val composition = ThemeComposition(
            blocks = listOf(ThemeBlock(ThemeBlockType.HERO, appContent = AppContent.FEATURED_APP, cardStyle = CardStyle.GLASS)),
            bgStyle = BgStyle.LANDSCAPE,
            rail = RailConfig(topInset = 91, height = 326, glass = true),
            widgets = listOf(ThemeWidget(ThemeWidgetType.CLOCK, width = 180, height = 80,
                overlay = true, offsetX = 20, offsetY = 14, glass = false))
        )
        assertEquals(composition, ThemeCompositionValidator.parse(ThemeCompositionValidator.encode(composition).toString()))
        assertTrue(composition.requiresFormatV3())
    }

    @Test
    fun `old composition defaults remain unchanged`() {
        val composition = ThemeCompositionValidator.parse("""{"blocks":[{"type":"title","text":"Apps"}],"rail":{}}""")
        assertFalse(composition.requiresFormatV3())
        assertEquals(0, composition.rail?.topInset)
        assertNull(composition.rail?.height)
        assertTrue(composition.widgets.isEmpty())
    }

    @Test
    fun `unsafe overlay geometry is rejected`() {
        listOf("\"offsetX\":-1", "\"offsetY\":361", "\"width\":99", "\"height\":59", "\"overlay\":\"true\"").forEach { field ->
            assertThrows(ThemeArchiveException::class.java) {
                ThemeCompositionValidator.parse("""{"blocks":[{"type":"title"}],"widgets":[{"type":"CLOCK",$field}]}""")
            }
        }
    }
}
