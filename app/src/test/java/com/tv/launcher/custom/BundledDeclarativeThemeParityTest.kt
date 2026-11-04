package com.tv.launcher.custom

import com.tv.launcher.custom.core.data.theme.ThemeArchiveReader
import com.tv.launcher.custom.core.data.theme.ThemeManifestValidator
import com.tv.launcher.custom.core.model.theme.AppContent
import com.tv.launcher.custom.core.model.theme.BlockPlacement
import com.tv.launcher.custom.core.model.theme.ThemeBlockType
import com.tv.launcher.custom.core.model.theme.ThemeId
import com.tv.launcher.custom.core.theme.ThemePresets
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Los paquetes incluidos son declarativos: cada ZIP editable debe coincidir con su plantilla.
 * Los layouts nativos quedan solo como fallback para paquetes v1 ya instalados.
 */
class BundledDeclarativeThemeParityTest {
    private data class Case(val preset: String, val editable: String, val layout: ThemeId)

    private val cases = listOf(
        Case("Android TV", "android-tv-editable.zip", ThemeId.ANDROID_TV),
        Case("Google TV", "google-tv-editable.zip", ThemeId.GOOGLE_TV),
        Case("Fire TV", "fire-tv-editable.zip", ThemeId.FIRE_TV),
        Case("Roku", "roku-editable.zip", ThemeId.ROKU),
        Case("Tizen", "tizen-editable.zip", ThemeId.TIZEN),
        Case("webOS", "webos-editable.zip", ThemeId.WEBOS),
        Case("Apple tvOS", "apple-tv-editable.zip", ThemeId.APPLE_TV),
        Case("Free My TV", "free-my-tv-editable.zip", ThemeId.FREE_MY_TV)
    )

    private fun manifest(file: String) = File("src/main/assets/demo-themes/$file").readBytes().let {
        val archive = ThemeArchiveReader.read(it)
        ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)
    }

    @Test
    fun `cada zip editable coincide con su plantilla y declara su layout`() {
        cases.forEach { case ->
            val editable = manifest(case.editable)
            val preset = ThemePresets.all.single { it.name == case.preset }
            assertEquals(case.preset, editable.name.removeSuffix(" · Editable"))
            assertEquals(case.layout, editable.layout)
            assertEquals(preset.composition, editable.composition)
            assertNotNull("${case.editable} debe tener composición", editable.composition)
        }
    }

    @Test
    fun `los paquetes incluidos son todos editables`() {
        val bundled = File("src/main/assets/demo-themes").listFiles().orEmpty()
            .filter { it.extension == "zip" }
            .filterNot { it.name == "custom-layout-validation.zip" }
        assertTrue("Quedan paquetes sin composición: ${bundled.joinToString { it.name }}",
            bundled.all { manifest(it.name).composition != null })
    }

    @Test
    fun `las plantillas migradas declaran las capacidades dinamicas necesarias`() {
        fun composition(name: String) = ThemePresets.all.single { it.name == name }.composition

        assertEquals(AppContent.FEATURED_APP, composition("webOS").blocks.first().appContent)
        assertEquals(ThemeBlockType.NAV_ROW, composition("Fire TV").blocks[1].type)
        assertEquals(2, composition("Fire TV").blocks.count { it.type == ThemeBlockType.APPS_ROW })
        assertEquals(8, composition("Fire TV").blocks.last().itemOffset)
        assertEquals("Roku", composition("Roku").rail?.logo)
        assertEquals(3, composition("Roku").blocks.single().columns)
        assertEquals(3, composition("Apple tvOS").rail?.appShortcutCount)
        assertEquals(BlockPlacement.BOTTOM, composition("Apple tvOS").blocks.last().placement)
        assertEquals(AppContent.APP_ICON_ROW, composition("Free My TV").blocks.last().appContent)
        assertEquals(0, composition("Free My TV").blocks[1].bgAlpha)
    }
}
