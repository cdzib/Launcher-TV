package com.tv.launcher.custom

import com.tv.launcher.custom.core.data.theme.InstalledThemeStore
import com.tv.launcher.custom.core.data.theme.ThemeArchiveReader
import com.tv.launcher.custom.core.data.theme.ThemeArchiveWriter
import com.tv.launcher.custom.core.data.theme.ThemeManifestValidator
import com.tv.launcher.custom.core.model.theme.LauncherAppearance
import com.tv.launcher.custom.core.model.theme.ThemeId
import com.tv.launcher.custom.core.model.theme.requiresFormatV3
import com.tv.launcher.custom.core.theme.ThemePresets
import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VidaaThemeArchiveTest {
    private val background = File("../core/ui/src/main/res/drawable-nodpi/vidaa_waves.png")
    private val archiveFile = File("src/main/assets/demo-themes/vidaa.zip")
    private val id = "a4b1d0c7-6d2e-5f3a-9c18-2f7b6e4d9a01"

    @Test
    fun `rebuild bundled archive when explicitly requested`() {
        org.junit.Assume.assumeTrue(System.getenv("UPDATE_VIDAA_ARCHIVE") == "1")
        val preset = ThemePresets.all.single { it.name == "VIDAA" }
        archiveFile.writeBytes(ThemeArchiveWriter.write(
            appearance = LauncherAppearance(ThemeId.WEBOS, accentColorArgb = 0xFFE84AA9),
            backgroundAsset = background.readBytes(),
            composition = preset.composition,
            name = preset.name,
            id = id
        ))
    }

    @Test
    fun `vidaa composition mirrors the hero, app grid and top picks rows`() {
        val composition = ThemePresets.byName("VIDAA")!!
        assertEquals(com.tv.launcher.custom.core.model.theme.BgStyle.WAVES, composition.bgStyle)
        assertEquals(com.tv.launcher.custom.core.model.theme.TopBarStyle.ICONS, composition.topBar?.style)
        assertEquals(7, composition.blocks.single {
            it.type == com.tv.launcher.custom.core.model.theme.ThemeBlockType.APPS_GRID
        }.columns)
        assertTrue(composition.requiresFormatV3())
    }

    @Test
    fun `bundled vidaa archive matches preset and survives installation`() {
        val preset = ThemePresets.all.single { it.name == "VIDAA" }
        val bytes = archiveFile.readBytes()
        val archive = ThemeArchiveReader.read(bytes)
        val manifest = ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)
        assertEquals(preset.name, manifest.name)
        assertEquals(preset.composition, manifest.composition)
        assertEquals(id, manifest.id)
        assertArrayEquals(background.readBytes(), archive.assets.getValue("assets/background.png"))

        val directory = Files.createTempDirectory("vidaa-theme-import").toFile()
        try {
            val store = InstalledThemeStore(directory)
            val installed = store.install(bytes)
            val loaded = InstalledThemeStore(directory).load(installed.id)
            assertEquals(preset.composition, loaded?.composition)
            assertArrayEquals(background.readBytes(),
                File(java.net.URI(loaded!!.appearance.backgroundImageUri!!)).readBytes())
        } finally {
            directory.deleteRecursively()
        }
    }
}
