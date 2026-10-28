package com.tv.launcher.custom.core.data.theme

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import com.tv.launcher.custom.core.model.theme.*

class ThemeArchiveReaderTest {
    @Test
    fun `exports legacy compositions as v2 and extended layouts as v3 without losing fields`() {
        val legacy = ThemeComposition(listOf(ThemeBlock(ThemeBlockType.TITLE)), topBar = TopBarConfig(), rail = RailConfig())
        val layouts = listOf(
            legacy to 2,
            legacy.copy(rail = RailConfig(position = RailPosition.RIGHT)) to 3,
            legacy.copy(rail = RailConfig(position = RailPosition.BOTTOM, glass = true)) to 3,
            legacy.copy(topBar = TopBarConfig(position = TopBarPosition.BOTTOM)) to 3,
            legacy.copy(topBar = TopBarConfig(glass = true)) to 3,
            legacy.copy(widgets = listOf(ThemeWidget(ThemeWidgetType.CLOCK))) to 3
        )
        layouts.forEach { (composition, version) ->
            val archive = ThemeArchiveReader.read(ThemeArchiveWriter.write(LauncherAppearance(ThemeId.WEBOS), null, composition))
            val root = kotlinx.serialization.json.Json.parseToJsonElement(archive.manifest) as kotlinx.serialization.json.JsonObject
            assertEquals(version.toString(), root.getValue("formatVersion").toString())
            assertEquals(composition, ThemeManifestValidator.parse(archive.manifest, emptySet()).composition)
            if (version == 3) assertThrows(ThemeArchiveException::class.java) {
                ThemeManifestValidator.parse(archive.manifest.replace("\"formatVersion\":3", "\"formatVersion\":2"), emptySet())
            }
        }
    }

    @Test
    fun `lee manifiesto requerido y activos con extension soportada`() {
        val archive = zip(
            "manifest.json" to "{\"formatVersion\":1}".toByteArray(),
            "assets/background.png" to pngHeader(width = 1920, height = 1080)
        )

        val decoded = ThemeArchiveReader.read(archive)

        assertEquals("{\"formatVersion\":1}", decoded.manifest)
        assertEquals(1, decoded.assets.size)
    }

    @Test
    fun `rechaza manifiesto ausente y rutas inseguras`() {
        assertThrows(ThemeArchiveException::class.java) {
            ThemeArchiveReader.read(zip("assets/unused.png" to pngHeader(width = 1, height = 1)))
        }
        assertThrows(ThemeArchiveException::class.java) {
            ThemeArchiveReader.read(zip("assets/../manifest.json" to "{}".toByteArray()))
        }
    }

    @Test
    fun `rechaza extension que no coincide con el mime`() {
        assertThrows(ThemeArchiveException::class.java) {
            ThemeArchiveReader.read(zip(
                "manifest.json" to "{}".toByteArray(),
                "assets/code.png" to "not an image".toByteArray()
            ))
        }
    }

    @Test
    fun `rechaza imagenes que exceden las dimensiones permitidas`() {
        assertThrows(ThemeArchiveException::class.java) {
            ThemeArchiveReader.read(zip(
                "manifest.json" to "{}".toByteArray(),
                "assets/large.png" to pngHeader(width = 4097, height = 1080)
            ))
        }
    }

    @Test
    fun `valida campos enum UUID y referencia a assets del manifiesto`() {
        val manifest = """{"formatVersion":1,"id":"f0d3e5a2-743c-4a1e-90cb-28775a60acb2","name":"Mi tema","layout":"WEBOS","background":{"type":"asset","path":"assets/background.png"},"tileShape":"rounded"}"""
        val decoded = ThemeArchiveReader.read(zip(
            "manifest.json" to manifest.toByteArray(),
            "assets/background.png" to pngHeader(width = 640, height = 360)
        ))
        val parsed = ThemeManifestValidator.parse(decoded.manifest, decoded.assets.keys)
        assertEquals("Mi tema", parsed.name)
        assertEquals("assets/background.png", (parsed.background as ThemeManifestBackground.Asset).path)

        assertThrows(ThemeArchiveException::class.java) {
            ThemeManifestValidator.parse(manifest.replace("WEBOS", "UNKNOWN"), decoded.assets.keys)
        }
        assertThrows(ThemeArchiveException::class.java) {
            ThemeManifestValidator.parse(manifest.replace("assets/background.png", "assets/missing.png"), decoded.assets.keys)
        }
        assertThrows(ThemeArchiveException::class.java) {
            ThemeManifestValidator.parse(manifest.replace("\"rounded\"", "\"plugin\""), decoded.assets.keys)
        }
    }

    @Test
    fun `rechaza campos desconocidos y colores con formato incorrecto`() {
        val template = """{"formatVersion":1,"id":"f0d3e5a2-743c-4a1e-90cb-28775a60acb2","name":"Tema","layout":"WEBOS","background":{"type":"solid","argb":"#FF112233"},"tileShape":"profile"}"""
        val parsed = ThemeManifestValidator.parse(template, emptySet())
        assertEquals(0xFF112233L, (parsed.background as ThemeManifestBackground.Solid).argb)

        assertThrows(ThemeArchiveException::class.java) {
            ThemeManifestValidator.parse(template.replace("\"profile\"", "\"profile\",\"script\":\"bad\""), emptySet())
        }
        assertThrows(ThemeArchiveException::class.java) {
            ThemeManifestValidator.parse(template.replace("#FF112233", "#123"), emptySet())
        }
    }

    @Test
    fun `exporta apariencia como paquete que puede validarse e importarse`() {
        val appearance = LauncherAppearance(themeId = ThemeId.GOOGLE_TV, backgroundColorArgb = 0xFF123456L)
        val bytes = ThemeArchiveWriter.write(appearance, backgroundAsset = null)
        val archive = ThemeArchiveReader.read(bytes)
        val manifest = ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)
        assertEquals(ThemeId.GOOGLE_TV, manifest.layout)
        assertEquals(0xFF123456L, (manifest.background as ThemeManifestBackground.Solid).argb)
    }

    @Test
    fun `exporta Free My TV como theme zip importable`() {
        val bytes = ThemeArchiveWriter.write(
            appearance = LauncherAppearance(themeId = ThemeId.FREE_MY_TV),
            backgroundAsset = null,
            name = "Free My TV"
        )

        val archive = ThemeArchiveReader.read(bytes)
        val manifest = ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)

        assertEquals(ThemeId.FREE_MY_TV, manifest.layout)
    }

    @Test
    fun `exporta composicion v2 que sobrevive round trip`() {
        val composition = ThemeComposition(listOf(
            ThemeBlock(ThemeBlockType.TITLE),
            ThemeBlock(ThemeBlockType.APPS_GRID, maxItems = 16, columns = 4),
            ThemeBlock(ThemeBlockType.FAVORITES_ROW, maxItems = 8, placement = BlockPlacement.BOTTOM)
        ))
        val bytes = ThemeArchiveWriter.write(
            LauncherAppearance(themeId = ThemeId.WEBOS),
            backgroundAsset = null,
            composition = composition,
            name = "Mi composición"
        )
        val archive = ThemeArchiveReader.read(bytes)
        val manifest = ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)

        assertEquals(composition, manifest.composition)
    }

    @Test
    fun `zip multimedia solo se acepta cuando todos los recursos declarados existen`() {
        val manifest = """{"formatVersion":2,"id":"40f9ac12-c6dd-4e7d-b8d5-18455d68f971","name":"Media","layout":"WEBOS","background":{"type":"solid","argb":"#FF101820"},"tileShape":"rounded","composition":[{"type":"media_row","items":[{"label":"Canal","imageAsset":"assets/channel.png"}]}]}"""
        val channelAsset = pngHeader(width = 640, height = 360)

        assertThrows(ThemeArchiveException::class.java) {
            ThemeManifestValidator.parse(manifest, emptySet())
        }

        val archive = ThemeArchiveReader.read(zip(
            "manifest.json" to manifest.toByteArray(),
            "assets/channel.png" to channelAsset
        ))
        val parsedManifest = ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)

        assertEquals("assets/channel.png", parsedManifest.composition?.blocks?.single()?.contentItems?.single()?.imageAsset)
    }

    @Test
    fun `exporta media row y su arte como un paquete importable`() {
        val channelAsset = pngHeader(width = 640, height = 360)
        val composition = ThemeComposition(listOf(
            ThemeBlock(
                type = ThemeBlockType.MEDIA_ROW,
                text = "Canales del tema",
                contentItems = listOf(ThemeContentItem("Canal local", "assets/channel.png"))
            )
        ))

        val bytes = ThemeArchiveWriter.write(
            appearance = LauncherAppearance(themeId = ThemeId.WEBOS),
            backgroundAsset = null,
            composition = composition,
            name = "Media declarativa",
            additionalAssets = mapOf("assets/channel.png" to channelAsset)
        )

        val archive = ThemeArchiveReader.read(bytes)
        val manifest = ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)

        assertEquals(composition, manifest.composition)
        assertEquals(channelAsset.size, archive.assets.getValue("assets/channel.png").size)
    }

    @Test
    fun `zip de integracion personalizado valida layout fondo y composicion`() {
        val bytes = javaClass.getResourceAsStream("/custom-layout-validation.zip")!!.use { it.readBytes() }
        val archive = ThemeArchiveReader.read(bytes)
        val manifest = ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)

        assertEquals("Custom Layout Preview", manifest.name)
        assertEquals(ThemeId.ROKU, manifest.layout)
        assertEquals(0xFF35134DL, (manifest.background as ThemeManifestBackground.Solid).argb)
        assertEquals(4, manifest.composition?.blocks?.size)
        val previewAppearance = manifest.toAppearance()
        assertEquals(ThemeId.ROKU, previewAppearance.themeId)
        assertEquals(0xFF35134DL, previewAppearance.backgroundColorArgb)
        assertEquals(0xFFCE93D8L, previewAppearance.accentColorArgb)
        assertEquals(ThemeId.ROKU, manifest.toAppearance(installedThemeId = manifest.id).themeId)
    }

    private fun zip(vararg files: Pair<String, ByteArray>): ByteArray = ByteArrayOutputStream().use { output ->
        ZipOutputStream(output).use { zip ->
            files.forEach { (name, bytes) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        output.toByteArray()
    }

    private companion object {
        fun pngHeader(width: Int, height: Int): ByteArray = ByteArray(24).also { bytes ->
            byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A).copyInto(bytes)
            bytes[16] = (width ushr 24).toByte()
            bytes[17] = (width ushr 16).toByte()
            bytes[18] = (width ushr 8).toByte()
            bytes[19] = width.toByte()
            bytes[20] = (height ushr 24).toByte()
            bytes[21] = (height ushr 16).toByte()
            bytes[22] = (height ushr 8).toByte()
            bytes[23] = height.toByte()
        }
    }
}
