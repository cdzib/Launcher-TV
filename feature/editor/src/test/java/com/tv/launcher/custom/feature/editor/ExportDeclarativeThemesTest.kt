package com.tv.launcher.custom.feature.editor

import com.tv.launcher.custom.core.data.theme.ThemeArchiveWriter
import com.tv.launcher.custom.core.data.theme.ThemeArchiveReader
import com.tv.launcher.custom.core.data.theme.ThemeManifestValidator
import com.tv.launcher.custom.core.model.theme.LauncherAppearance
import com.tv.launcher.custom.core.model.theme.ThemeId
import com.tv.launcher.custom.core.theme.ThemePresets
import java.io.File
import org.junit.Test

/** Regenerates the bundled declarative archives used by the migration (run manually). */
class ExportDeclarativeThemesTest {
    private data class Source(val fileName: String, val themeId: ThemeId, val accent: Long, val background: Long)

    private val sources = mapOf(
        "Android TV" to Source("android-tv-editable.zip", ThemeId.ANDROID_TV, 0xFF81C784, 0xFF071326),
        "Google TV" to Source("google-tv-editable.zip", ThemeId.GOOGLE_TV, 0xFF8AB4F8, 0xFF101827),
        "Fire TV" to Source("fire-tv-editable.zip", ThemeId.FIRE_TV, 0xFFFFB74D, 0xFF111111),
        "Roku" to Source("roku-editable.zip", ThemeId.ROKU, 0xFFCE93D8, 0xFF35134D),
        "Tizen" to Source("tizen-editable.zip", ThemeId.TIZEN, 0xFF4FC3F7, 0xFF08090B),
        "webOS" to Source("webos-editable.zip", ThemeId.WEBOS, 0xFFFFC078, 0xFF08070D),
        "Apple tvOS" to Source("apple-tv-editable.zip", ThemeId.APPLE_TV, 0xFFE0E0E0, 0xFF090807),
        "Free My TV" to Source("free-my-tv-editable.zip", ThemeId.FREE_MY_TV, 0xFF168CFF, 0xFF090D19)
    )

    @Test
    fun export() {
        val output = File("build/declarative-themes").apply { mkdirs() }
        output.listFiles().orEmpty().forEach { it.delete() }
        ThemePresets.all.filter { it.name in sources }.forEach { preset ->
            val source = sources.getValue(preset.name)
            val bytes = ThemeArchiveWriter.write(
                LauncherAppearance(
                    themeId = source.themeId,
                    accentColorArgb = source.accent,
                    backgroundColorArgb = source.background
                ),
                null,
                composition = preset.composition,
                name = "${preset.name} · Editable"
            )
            val file = File(output, source.fileName)
            file.writeBytes(bytes)
            val archive = ThemeArchiveReader.read(file.readBytes())
            val manifest = ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)
            org.junit.Assert.assertEquals("${preset.name} round trip", preset.composition, manifest.composition)
            org.junit.Assert.assertEquals(source.themeId, manifest.layout)
        }
        org.junit.Assert.assertEquals(sources.size, output.listFiles().orEmpty().count { it.extension == "zip" })
    }
}
