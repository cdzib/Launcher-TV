package com.tv.launcher.custom.feature.theme.data

import com.tv.launcher.custom.feature.theme.domain.model.ThemeId
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

internal data class ThemeManifest(
    val id: String,
    val name: String,
    val layout: ThemeId,
    val accentArgb: Long?,
    val background: ThemeManifestBackground,
    val tileShape: String,
    val composition: ThemeComposition? = null
)

internal sealed interface ThemeManifestBackground {
    data class Solid(val argb: Long) : ThemeManifestBackground
    data class Asset(val path: String) : ThemeManifestBackground
}

internal object ThemeManifestValidator {
    private val json = Json { isLenient = false; ignoreUnknownKeys = false }
    private val rootKeys = setOf("formatVersion", "id", "name", "layout", "accentArgb", "background", "tileShape")

    fun parse(manifestText: String, availableAssets: Set<String>): ThemeManifest {
        val root = try {
            json.parseToJsonElement(manifestText).jsonObject
        } catch (_: Exception) {
            throw ThemeArchiveException("Theme manifest is not valid JSON")
        }
        val formatVersion = root.requiredInt("formatVersion")
        val validKeys = when (formatVersion) {
            1 -> root.keys == rootKeys || root.keys == rootKeys - "accentArgb"
            2 -> root.keys == rootKeys + "composition" || root.keys == (rootKeys - "accentArgb") + "composition"
            else -> false
        }
        if (!validKeys) {
            throw ThemeArchiveException("Theme manifest fields do not match format v1")
        }

        val id = root.requiredString("id")
        try {
            if (UUID.fromString(id).toString() != id.lowercase()) throw IllegalArgumentException()
        } catch (_: IllegalArgumentException) {
            throw ThemeArchiveException("Theme id must be a UUID")
        }

        val name = root.requiredString("name")
        if (name.length !in 1..40 || name.any(Char::isISOControl)) {
            throw ThemeArchiveException("Theme name must contain 1 to 40 printable characters")
        }
        val layout = ThemeId.entries.firstOrNull { it.name == root.requiredString("layout") }
            ?: throw ThemeArchiveException("Unknown built-in layout")
        val accent = root["accentArgb"]?.let { it.jsonPrimitive.colorArgb("accentArgb") }
        val tileShape = root.requiredString("tileShape")
        if (tileShape !in setOf("profile", "rounded", "square")) {
            throw ThemeArchiveException("Unknown tile shape")
        }

        val backgroundObject = try {
            root["background"]?.jsonObject ?: throw ThemeArchiveException("Background is required")
        } catch (exception: ThemeArchiveException) {
            throw exception
        } catch (_: Exception) {
            throw ThemeArchiveException("Background must be an object")
        }
        val background = when (backgroundObject.requiredString("type")) {
            "solid" -> {
                if (backgroundObject.keys != setOf("type", "argb")) throw ThemeArchiveException("Invalid solid background fields")
                ThemeManifestBackground.Solid(backgroundObject.getValue("argb").jsonPrimitive.colorArgb("background.argb"))
            }
            "asset" -> {
                if (backgroundObject.keys != setOf("type", "path")) throw ThemeArchiveException("Invalid asset background fields")
                val path = backgroundObject.requiredString("path")
                if (!path.matches(Regex("assets/[A-Za-z0-9_-]+\\.(png|jpg|jpeg|webp)")) || path !in availableAssets) {
                    throw ThemeArchiveException("Background asset is missing or unsafe")
                }
                ThemeManifestBackground.Asset(path)
            }
            else -> throw ThemeArchiveException("Unknown background type")
        }

        val composition = if (formatVersion == 2) {
            val raw = runCatching { root.getValue("composition").toString() }
                .getOrElse { throw ThemeArchiveException("Theme composition is required") }
            ThemeCompositionValidator.parse(raw)
        } else null
        return ThemeManifest(id, name, layout, accent, background, tileShape, composition)
    }

    private fun JsonObject.requiredString(key: String): String =
        this[key]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
            ?: throw ThemeArchiveException("Manifest field '$key' is required")

    private fun JsonObject.requiredInt(key: String): Int =
        this[key]?.jsonPrimitive?.intOrNull ?: throw ThemeArchiveException("Manifest field '$key' must be an integer")

    private fun JsonPrimitive.colorArgb(field: String): Long {
        val value = contentOrNull ?: throw ThemeArchiveException("Manifest field '$field' must be a color")
        if (!value.matches(Regex("#[0-9A-Fa-f]{8}"))) throw ThemeArchiveException("Manifest field '$field' must use #AARRGGBB")
        return value.substring(1).toLong(16)
    }
}
