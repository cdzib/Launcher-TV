package com.tv.launcher.custom

import com.tv.launcher.custom.core.data.theme.*
import com.tv.launcher.custom.core.model.theme.*
import com.tv.launcher.custom.core.theme.ThemePresets
import java.io.File
import java.nio.file.Files
import org.junit.Assert.*
import org.junit.Test

class WebOsEditableThemeTest {
    @Test fun `zip webos usa exactamente la composicion disponible en crear tema`() {
        val archive = ThemeArchiveReader.read(File("src/main/assets/demo-themes/webos-editable.zip").readBytes())
        val manifest = ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)
        assertEquals(ThemeId.WEBOS, manifest.layout)
        assertEquals(ThemePresets.all.single { it.name == "webOS" }.composition, manifest.composition)
        assertEquals(listOf(AppContent.FEATURED_APP, AppContent.SHORTCUTS, AppContent.APP_ICONS,
            AppContent.SHORTCUT_EDITOR, AppContent.APP_CARDS), manifest.composition!!.blocks.map { it.appContent })

        val directory = Files.createTempDirectory("webos-editable-import").toFile()
        try {
            val imported = InstalledThemeStore(directory).install(File("src/main/assets/demo-themes/webos-editable.zip").readBytes())
            val reopened = InstalledThemeStore(directory).load(imported.id)
            assertEquals(manifest.composition, reopened?.composition)
            assertEquals("webOS · Editable", reopened?.name)
        } finally {
            directory.deleteRecursively()
        }
    }
}
