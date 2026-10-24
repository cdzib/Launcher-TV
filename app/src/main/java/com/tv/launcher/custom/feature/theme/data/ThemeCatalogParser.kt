package com.tv.launcher.custom.feature.theme.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

data class RemoteTheme(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val downloadUrl: String,
    val sha256: String
)

internal object ThemeCatalogParser {
    private const val MAX_CATALOG_BYTES = 1024 * 1024
    private const val MAX_THEMES = 100
    private val json = Json { isLenient = false; ignoreUnknownKeys = false }

    fun parse(catalogBytes: ByteArray): List<RemoteTheme> {
        if (catalogBytes.size > MAX_CATALOG_BYTES) throw ThemeCatalogException("Catalog exceeds 1 MiB")
        val root = try {
            json.parseToJsonElement(catalogBytes.toString(Charsets.UTF_8)).jsonObject
        } catch (_: Exception) {
            throw ThemeCatalogException("Catalog is not valid JSON")
        }
        if (root.keys != setOf("formatVersion", "themes")) throw ThemeCatalogException("Invalid catalog fields")
        if (root["formatVersion"]?.jsonPrimitive?.intOrNull != 1) {
            throw ThemeCatalogException("Unsupported catalog version")
        }
        val items = try {
            root["themes"]?.jsonArray ?: throw ThemeCatalogException("Catalog themes are required")
        } catch (exception: ThemeCatalogException) {
            throw exception
        } catch (_: Exception) {
            throw ThemeCatalogException("Catalog themes must be an array")
        }
        if (items.size > MAX_THEMES) throw ThemeCatalogException("Catalog has more than 100 themes")

        val themes = items.map(::parseTheme)
        if (themes.map { it.id }.distinct().size != themes.size) throw ThemeCatalogException("Duplicate theme id")
        return themes
    }

    private fun parseTheme(element: kotlinx.serialization.json.JsonElement): RemoteTheme {
        val item = try {
            element.jsonObject
        } catch (_: Exception) {
            throw ThemeCatalogException("Theme entry must be an object")
        }
        val requiredKeys = setOf("id", "name", "description", "version", "downloadUrl", "sha256")
        if (item.keys != requiredKeys) throw ThemeCatalogException("Invalid theme entry fields")
        val id = item.requiredString("id")
        try {
            if (UUID.fromString(id).toString() != id) throw IllegalArgumentException()
        } catch (_: IllegalArgumentException) {
            throw ThemeCatalogException("Theme id must be a lowercase UUID")
        }
        val name = item.requiredString("name")
        val description = item.requiredString("description", allowEmpty = true)
        val version = item.requiredString("version")
        val url = item.requiredString("downloadUrl")
        val sha256 = item.requiredString("sha256")
        if (name.length !in 1..40 || description.length > 160 || version.length !in 1..32) {
            throw ThemeCatalogException("Theme metadata exceeds allowed lengths")
        }
        HttpsUrlValidator.validate(url)
        if (!sha256.matches(Regex("[A-Fa-f0-9]{64}"))) throw ThemeCatalogException("Invalid SHA-256")
        return RemoteTheme(id, name, description, version, url, sha256.lowercase())
    }

    private fun JsonObject.requiredString(key: String, allowEmpty: Boolean = false): String {
        val value = this[key]?.jsonPrimitive?.contentOrNull ?: throw ThemeCatalogException("Missing '$key'")
        if (!allowEmpty && value.isBlank()) throw ThemeCatalogException("Missing '$key'")
        return value
    }
}

internal class ThemeCatalogException(message: String) : Exception(message)
