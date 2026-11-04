package com.tv.launcher.custom.files

import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FileBrowserTest {
    private fun tempDir(): File = Files.createTempDirectory("file-browser-test").toFile()

    private fun file(directory: File, name: String) = File(directory, name).apply { writeText("x") }

    @Test
    fun `themes only show directories and zip archives, folders first`() {
        val root = tempDir()
        file(root, "tema.zip")
        file(root, "foto.png")
        file(root, ".oculto.zip")
        File(root, "Exportados").mkdir()
        try {
            val entries = FileBrowser.list(root, FileBrowserMode.THEMES)
            assertEquals(listOf("Exportados", "tema.zip"), entries.map { it.name })
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `images accept png jpeg jpg and webp and ignore other files`() {
        val root = tempDir()
        listOf("a.png", "b.jpg", "c.jpeg", "d.webp", "e.zip", "f.txt", ".g.png").forEach { file(root, it) }
        try {
            assertEquals(
                listOf("a.png", "b.jpg", "c.jpeg", "d.webp"),
                FileBrowser.list(root, FileBrowserMode.IMAGES).map { it.name }
            )
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `assets share the image filter`() {
        val root = tempDir()
        file(root, "fondo.png")
        file(root, "tema.zip")
        try {
            assertEquals(listOf("fondo.png"), FileBrowser.list(root, FileBrowserMode.ASSETS).map { it.name })
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `navigation stops at the selected root`() {
        val root = tempDir()
        val child = File(root, "Descargas").apply { mkdir() }
        val grandChild = File(child, "Temas").apply { mkdir() }
        try {
            assertEquals(child, FileBrowser.parentWithin(grandChild, listOf(root)))
            assertNull(FileBrowser.parentWithin(root, listOf(root)))
            assertNull(FileBrowser.parentWithin(grandChild, listOf(File(root, "Otro"))))
            assertEquals("Descargas/Temas", FileBrowser.relativePath(root, grandChild))
            assertTrue(FileBrowser.isInside(grandChild, root))
            assertFalse(FileBrowser.isInside(File(root, "Otro"), child))
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `sizes are rendered for the picker`() {
        assertEquals("512 B", FileBrowser.formatSize(512))
        assertEquals("2 KB", FileBrowser.formatSize(2048))
        assertEquals("1.5 MB", FileBrowser.formatSize((1.5 * 1024 * 1024).toLong()))
    }
}
