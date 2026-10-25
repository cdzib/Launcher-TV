package com.tv.launcher.custom.core.data.theme

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

internal object HttpsUrlValidator {
    fun validate(rawUrl: String): URI {
        val uri = try {
            URI(rawUrl)
        } catch (_: Exception) {
            throw ThemeCatalogException("Invalid URL")
        }
        if (!uri.scheme.equals("https", ignoreCase = true) || uri.host.isNullOrBlank() ||
            uri.rawUserInfo != null || uri.rawFragment != null
        ) {
            throw ThemeCatalogException("Only absolute HTTPS URLs are allowed")
        }
        return uri
    }
}

class ThemeDownloadClient(
    private val openConnection: (URL) -> HttpURLConnection = { it.openConnection() as HttpURLConnection }
) {
    fun download(url: String, maxBytes: Int): ByteArray {
        require(maxBytes > 0)
        var currentUrl = HttpsUrlValidator.validate(url)

        for (redirectCount in 0..MAX_REDIRECTS) {
            val connection = try {
                openConnection(currentUrl.toURL())
            } catch (_: Exception) {
                throw ThemeCatalogException("Could not open download connection")
            }
            try {
                connection.instanceFollowRedirects = false
                connection.connectTimeout = TIMEOUT_MS
                connection.readTimeout = TIMEOUT_MS
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/zip, application/json, */*")
                val responseCode = connection.responseCode
                if (responseCode in REDIRECT_CODES) {
                    if (redirectCount == MAX_REDIRECTS) throw ThemeCatalogException("Too many redirects")
                    val redirect = connection.getHeaderField("Location")
                        ?: throw ThemeCatalogException("Redirect location is missing")
                    currentUrl = HttpsUrlValidator.validate(currentUrl.resolve(redirect).toString())
                    continue
                }
                if (responseCode !in 200..299) throw ThemeCatalogException("Download failed with HTTP $responseCode")
                if (connection.contentLengthLong > maxBytes) throw ThemeCatalogException("Download exceeds size limit")
                return connection.inputStream.use { it.readBounded(maxBytes) }
            } catch (exception: ThemeCatalogException) {
                throw exception
            } catch (_: Exception) {
                throw ThemeCatalogException("Download failed")
            } finally {
                connection.disconnect()
            }
        }
        throw ThemeCatalogException("Too many redirects")
    }

    private fun InputStream.readBounded(maxBytes: Int): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val count = read(buffer)
            if (count < 0) return output.toByteArray()
            if (output.size() + count > maxBytes) throw ThemeCatalogException("Download exceeds size limit")
            output.write(buffer, 0, count)
        }
    }

    private companion object {
        const val MAX_REDIRECTS = 3
        const val TIMEOUT_MS = 15_000
        val REDIRECT_CODES = setOf(301, 302, 303, 307, 308)
    }
}

fun verifySha256(bytes: ByteArray, expectedHex: String): Boolean {
    val actual = java.security.MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { byte -> "%02x".format(byte) }
    return actual.equals(expectedHex, ignoreCase = true)
}
