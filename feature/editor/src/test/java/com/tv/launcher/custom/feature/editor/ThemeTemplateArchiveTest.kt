package com.tv.launcher.custom.feature.editor

import com.tv.launcher.custom.core.data.theme.ThemeArchiveReader
import com.tv.launcher.custom.core.data.theme.ThemeArchiveWriter
import com.tv.launcher.custom.core.data.theme.ThemeManifestValidator
import com.tv.launcher.custom.core.model.theme.LauncherAppearance
import com.tv.launcher.custom.core.theme.ThemePresets
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeTemplateArchiveTest {
    @Test
    fun `cada plantilla editable conserva su nombre y composicion al exportar e importar zip`() {
        ThemePresets.all.forEach { preset ->
            val bytes = ThemeArchiveWriter.write(LauncherAppearance(), null,
                composition = preset.composition, name = preset.name)
            val archive = ThemeArchiveReader.read(bytes)
            val manifest = ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)
            assertEquals(preset.name, manifest.name)
            assertEquals("ZIP round trip: ${preset.name}", preset.composition, manifest.composition)
        }
    }
}
