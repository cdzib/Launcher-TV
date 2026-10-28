package com.tv.launcher.custom

import com.tv.launcher.custom.core.data.theme.InstalledThemeStore
import com.tv.launcher.custom.core.data.theme.ThemeArchiveReader
import com.tv.launcher.custom.core.data.theme.ThemeManifestValidator
import com.tv.launcher.custom.core.theme.ThemePresets
import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Test
import com.tv.launcher.custom.core.data.theme.ThemeArchiveWriter
import com.tv.launcher.custom.core.model.theme.LauncherAppearance
import com.tv.launcher.custom.core.model.theme.ThemeId

class LiquidGlassThemeArchiveTest {
    @Test
    fun `rebuild bundled archive when explicitly requested`() {
        org.junit.Assume.assumeTrue(System.getenv("UPDATE_LIQUID_GLASS_ARCHIVE") == "1")
        val preset = ThemePresets.all.single { it.name == "macOS Liquid Glass" }
        File("src/main/assets/demo-themes/macos-liquid-glass.zip").writeBytes(ThemeArchiveWriter.write(
            appearance = LauncherAppearance(ThemeId.APPLE_TV),
            backgroundAsset = File("../core/ui/src/main/res/drawable-nodpi/glass_landscape.png").readBytes(),
            composition = preset.composition,
            name = preset.name,
            id = "712191f1-d960-5c2b-8d4a-061e668f33bc"
        ))
    }
    @Test
    fun `glass composition uses sidebar and installed featured content without desktop chrome`() {
        val composition = ThemePresets.byName("macOS Liquid Glass")!!
        assertEquals(com.tv.launcher.custom.core.model.theme.RailPosition.LEFT, composition.rail?.position)
        assertEquals(null, composition.topBar)
        assertEquals(com.tv.launcher.custom.core.model.theme.AppContent.FEATURED_APP, composition.blocks.first().appContent)
    }
    @Test
    fun `bundled glass archive matches preset and survives installation`() {
        val bytes = File("src/main/assets/demo-themes/macos-liquid-glass.zip").readBytes()
        val archive = ThemeArchiveReader.read(bytes)
        val manifest = ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)
        val preset = ThemePresets.all.single { it.name == "macOS Liquid Glass" }
        assertEquals(preset.name, manifest.name)
        assertEquals(preset.composition, manifest.composition)
        org.junit.Assert.assertArrayEquals(
            File("../core/ui/src/main/res/drawable-nodpi/glass_landscape.png").readBytes(),
            archive.assets.getValue("assets/background.png")
        )
        org.junit.Assert.assertTrue(archive.assets.getValue("assets/background.png").take(8).toByteArray()
            .contentEquals(byteArrayOf(-119, 80, 78, 71, 13, 10, 26, 10)))

        val directory = Files.createTempDirectory("glass-theme-import").toFile()
        try {
            val store = InstalledThemeStore(directory)
            val installed = store.install(bytes)
            assertEquals(preset.composition, store.load(installed.id)?.composition)
        } finally {
            directory.deleteRecursively()
        }
    }
}
