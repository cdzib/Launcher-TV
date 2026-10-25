package com.tv.launcher.custom.core.data.theme

import com.tv.launcher.custom.core.model.theme.*
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class InstalledThemeStoreTest {
    @Test
    fun `instala enumera carga y elimina temas sin tocar los otros`() {
        val root = Files.createTempDirectory("installed-themes-test").toFile()
        try {
            val store = InstalledThemeStore(root)
            val first = store.install(ThemeArchiveWriter.write(LauncherAppearance(themeId = ThemeId.WEBOS), null))
            val second = store.install(ThemeArchiveWriter.write(LauncherAppearance(themeId = ThemeId.ROKU), null))

            assertEquals(setOf(first.id, second.id), store.list().map { it.id }.toSet())
            assertEquals(ThemeId.WEBOS, store.load(first.id)?.appearance?.themeId)
            assertTrue(store.remove(first.id))
            assertNull(store.load(first.id))
            assertNotNull(store.load(second.id))
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `no permite duplicar ni eliminar un id que no sea uuid`() {
        val root = Files.createTempDirectory("installed-themes-test").toFile()
        try {
            val store = InstalledThemeStore(root)
            val packageBytes = ThemeArchiveWriter.write(LauncherAppearance(themeId = ThemeId.WEBOS), null)
            val installed = store.install(packageBytes)

            assertThrows(ThemeArchiveException::class.java) { store.install(packageBytes) }
            assertFalse(store.remove("../../outside"))
            assertNotNull(store.load(installed.id))
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `conserva y vuelve a resolver el asset privado de un tema instalado`() {
        val root = Files.createTempDirectory("installed-themes-test").toFile()
        try {
            val store = InstalledThemeStore(root)
            val imageBytes = ByteArray(24).also { bytes ->
                byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A).copyInto(bytes)
                bytes[19] = 1
                bytes[23] = 1
            }
            val packageBytes = ThemeArchiveWriter.write(LauncherAppearance(), imageBytes)
            val installed = store.install(packageBytes)
            val backgroundUri = installed.appearance.backgroundImageUri

            assertNotNull(backgroundUri)
            assertEquals(backgroundUri, store.load(installed.id)?.appearance?.backgroundImageUri)
            assertTrue(store.remove(installed.id))
            assertNull(store.load(installed.id))
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `inicializa un tema demo solo una vez y respeta su eliminacion`() {
        val root = Files.createTempDirectory("installed-themes-test").toFile()
        try {
            val store = InstalledThemeStore(root)
            val defaultPackage = ThemeArchiveWriter.write(LauncherAppearance(themeId = ThemeId.WEBOS), null)

            store.initializeDefaultTheme(defaultPackage)
            val defaultThemeId = store.list().single().id
            store.remove(defaultThemeId)
            store.initializeDefaultTheme(defaultPackage)

            assertTrue(store.list().isEmpty())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `actualiza solo con autorizacion y guarda la version del catalogo`() {
        val root = Files.createTempDirectory("installed-themes-test").toFile()
        try {
            val store = InstalledThemeStore(root)
            val packageBytes = ThemeArchiveWriter.write(LauncherAppearance(themeId = ThemeId.WEBOS), null)
            val installed = store.install(packageBytes, version = "1.0")
            assertThrows(ThemeArchiveException::class.java) { store.install(packageBytes, version = "2.0") }

            val updated = store.install(packageBytes, version = "2.0", replaceExisting = true)
            assertEquals(installed.id, updated.id)
            assertEquals("2.0", store.load(installed.id)?.version)
            assertEquals(1, store.list().size)
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `edita un tema instalado conservando su id y reemplazando su composicion`() {
        val root = Files.createTempDirectory("installed-themes-edit-test").toFile()
        try {
            val store = InstalledThemeStore(root)
            val themeId = "f0d3e5a2-743c-4a1e-90cb-28775a60acb2"
            val originalComposition = ThemeComposition(listOf(ThemeBlock(ThemeBlockType.TITLE)))
            val editedComposition = ThemeComposition(
                blocks = listOf(
                    ThemeBlock(ThemeBlockType.TITLE, text = "Inicio", textSize = 36, align = BlockAlign.CENTER),
                    ThemeBlock(ThemeBlockType.APPS_GRID, maxItems = 16, columns = 4, aspect = CardAspect.SQUARE, labelPos = LabelPos.NONE)
                ),
                spacing = 16,
                padding = 32,
                bgColor = 0xFF010203.toInt()
            )
            val originalPackage = ThemeArchiveWriter.write(
                LauncherAppearance(themeId = ThemeId.WEBOS), null,
                composition = originalComposition, name = "Original", id = themeId
            )
            store.install(originalPackage)

            val editedPackage = ThemeArchiveWriter.write(
                LauncherAppearance(themeId = ThemeId.WEBOS, accentColorArgb = 0xFF123456L), null,
                composition = editedComposition, name = "Editado", id = themeId
            )
            store.install(editedPackage, replaceExisting = true)

            val savedTheme = store.load(themeId)
            assertEquals("Editado", savedTheme?.name)
            assertEquals(editedComposition, savedTheme?.composition)
            assertEquals(0xFF123456L, savedTheme?.appearance?.accentColorArgb)
            assertEquals(listOf(themeId), store.list().map { it.id })
        } finally {
            root.deleteRecursively()
        }
    }
}
