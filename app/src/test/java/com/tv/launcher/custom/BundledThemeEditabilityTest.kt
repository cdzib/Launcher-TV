package com.tv.launcher.custom

import com.tv.launcher.custom.core.data.theme.ThemeArchiveReader
import com.tv.launcher.custom.core.data.theme.ThemeManifestValidator
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

class BundledThemeEditabilityTest {
    @Test
    fun `solo los paquetes con composicion declarada son bases editables`() {
        val directory = File("src/main/assets/demo-themes")
        val editableArchives = directory.listFiles().orEmpty().filter { it.extension == "zip" }.filter { file ->
            val archive = ThemeArchiveReader.read(file.readBytes())
            ThemeManifestValidator.parse(archive.manifest, archive.assets.keys).composition != null
        }.map { it.name }.toSet()

        assertEquals(setOf(
            "launch-dock-demo.zip", "side-stream-demo.zip", "nova-minimal-premium.zip",
            "custom-layout-validation.zip", "webos-editable.zip", "android-tv-editable.zip",
            "google-tv-editable.zip", "fire-tv-editable.zip", "roku-editable.zip",
            "tizen-editable.zip", "apple-tv-editable.zip", "free-my-tv-editable.zip"
        ), editableArchives)
    }
}
