package com.tv.launcher.custom.core.data.theme

import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeCatalogParserTest {
    private val validId = "f0d3e5a2-743c-4a1e-90cb-28775a60acb2"
    private val validCatalog = """{"formatVersion":1,"themes":[{"id":"$validId","name":"Aurora","description":"Demo theme","version":"1.0.0","downloadUrl":"https://example.org/aurora.zip","sha256":"${"a".repeat(64)}"}]}"""

    @Test
    fun `parsea catalogo remoto y normaliza checksum`() {
        val theme = ThemeCatalogParser.parse(validCatalog.toByteArray()).single()
        assertEquals(validId, theme.id)
        assertEquals("Aurora", theme.name)
        assertEquals("a".repeat(64), theme.sha256)
    }

    @Test
    fun `rechaza http duplicados y version desconocida`() {
        assertThrows(ThemeCatalogException::class.java) {
            ThemeCatalogParser.parse(validCatalog.replace("https://", "http://").toByteArray())
        }
        assertThrows(ThemeCatalogException::class.java) {
            val themeEntry = validCatalog.substringAfter("\"themes\":[").substringBeforeLast("]}")
            ThemeCatalogParser.parse("""{"formatVersion":1,"themes":[$themeEntry,$themeEntry]}""".toByteArray())
        }
        assertThrows(ThemeCatalogException::class.java) {
            ThemeCatalogParser.parse(validCatalog.replace("\"formatVersion\":1", "\"formatVersion\":2").toByteArray())
        }
    }

    @Test
    fun `rechaza urls inseguras y checksums invalidos`() {
        assertThrows(ThemeCatalogException::class.java) { HttpsUrlValidator.validate("http://example.org/theme.zip") }
        assertThrows(ThemeCatalogException::class.java) { HttpsUrlValidator.validate("https://user:pass@example.org/theme.zip") }
        assertThrows(ThemeCatalogException::class.java) {
            ThemeCatalogParser.parse(validCatalog.replace("a".repeat(64), "xyz").toByteArray())
        }
    }

    @Test
    fun `permite redirects solo https y limita bytes descargados`() {
        val connections = mutableListOf(
            FakeHttpConnection(URL("https://example.org/catalog"), 302, byteArrayOf(), mapOf("Location" to "https://cdn.example.org/catalog")),
            FakeHttpConnection(URL("https://cdn.example.org/catalog"), 200, "ok".toByteArray())
        )
        val client = ThemeDownloadClient { connections.removeAt(0) }
        assertEquals("ok", client.download("https://example.org/catalog", 10).decodeToString())

        val downgrade = ThemeDownloadClient {
            FakeHttpConnection(it, 302, byteArrayOf(), mapOf("Location" to "http://example.org/file.zip"))
        }
        assertThrows(ThemeCatalogException::class.java) { downgrade.download("https://example.org/file.zip", 10) }

        val oversized = ThemeDownloadClient {
            FakeHttpConnection(it, 200, ByteArray(11) { 1 })
        }
        assertThrows(ThemeCatalogException::class.java) { oversized.download("https://example.org/file.zip", 10) }
    }

    @Test
    fun `verifica sha256`() {
        val payload = "theme zip".toByteArray()
        assertTrue(verifySha256(payload, "${java.security.MessageDigest.getInstance("SHA-256").digest(payload).joinToString("") { "%02x".format(it) }}"))
        assertFalse(verifySha256(payload, "0".repeat(64)))
    }

    private class FakeHttpConnection(
        url: URL,
        private val responseCodeValue: Int,
        private val body: ByteArray,
        private val responseHeaders: Map<String, String> = emptyMap()
    ) : HttpURLConnection(url) {
        override fun connect() = Unit
        override fun disconnect() = Unit
        override fun usingProxy() = false
        override fun getResponseCode() = responseCodeValue
        override fun getHeaderField(name: String): String? = responseHeaders.entries
            .firstOrNull { it.key.equals(name, ignoreCase = true) }?.value
        override fun getContentLengthLong(): Long = body.size.toLong()
        override fun getInputStream() = ByteArrayInputStream(body)
    }
}
