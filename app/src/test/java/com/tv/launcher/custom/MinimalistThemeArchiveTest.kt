package com.tv.launcher.custom

import com.tv.launcher.custom.core.data.theme.ThemeArchiveReader
import com.tv.launcher.custom.core.data.theme.ThemeManifestValidator
import com.tv.launcher.custom.core.data.theme.ThemeArchiveWriter
import com.tv.launcher.custom.core.data.theme.InstalledThemeStore
import com.tv.launcher.custom.core.model.theme.LauncherAppearance
import com.tv.launcher.custom.core.theme.ThemePresets
import java.io.File
import java.util.UUID
import java.nio.file.Files
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class MinimalistThemeArchiveTest {
    @Test fun `four approved minimalist archives match their presets`() {
        listOf("orbita" to "Órbita", "bruma" to "Bruma", "horizonte" to "Horizonte", "estudio" to "Estudio").forEach { (slug, name) ->
            val preset = ThemePresets.all.single { it.name == name }
            val background = preset.backgroundAssetName?.let { File("../core/ui/src/main/res/drawable-nodpi/$it").readBytes() }
            val id = UUID.nameUUIDFromBytes("tv-launcher:minimalist:$slug".toByteArray()).toString()
            val file = File("src/main/assets/demo-themes/$slug.zip")
            if (System.getenv("GENERATE_MINIMALIST_THEMES") == "1") file.writeBytes(ThemeArchiveWriter.write(
                LauncherAppearance(backgroundColorArgb = preset.composition.bgColor?.toLong()?.and(0xFFFFFFFFL),
                    accentColorArgb = if (slug == "bruma") 0xFF253846 else 0xFFE8EDF1),
                background, preset.composition, name, id))
            val bytes = file.readBytes()
            val archive = ThemeArchiveReader.read(bytes)
            val manifest = ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)
            assertEquals(name, manifest.name)
            assertEquals(preset.composition, manifest.composition)
            assertEquals(id, manifest.id)
            if (background != null) assertArrayEquals(background, archive.assets.getValue("assets/background.png"))
            val root = Files.createTempDirectory("minimalist-import").toFile()
            try {
                val store = InstalledThemeStore(root)
                val installed = store.install(bytes)
                assertEquals(preset.composition, InstalledThemeStore(root).load(installed.id)?.composition)
                if (background != null) assertArrayEquals(background,
                    File(java.net.URI(InstalledThemeStore(root).load(installed.id)!!.appearance.backgroundImageUri!!)).readBytes())
                assertArrayEquals(bytes, store.archiveBytes(installed.id))
            } finally { root.deleteRecursively() }
        }
    }
}
