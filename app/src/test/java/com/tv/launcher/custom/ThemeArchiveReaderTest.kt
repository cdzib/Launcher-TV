package com.tv.launcher.custom.feature.theme.data

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import com.tv.launcher.custom.feature.theme.domain.model.LauncherAppearance
import com.tv.launcher.custom.feature.theme.domain.model.ThemeId

class ThemeArchiveReaderTest {
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
    fun `exporta composicion v2 que sobrevive round trip`() {
        val composition = ThemeComposition(listOf(
            ThemeBlock(ThemeBlockType.TITLE),
            ThemeBlock(ThemeBlockType.APPS_GRID, maxItems = 16, columns = 4)
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
