package com.tv.launcher.custom.core.data.theme

import com.tv.launcher.custom.core.model.theme.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ThemeArchiveWriter {
    fun write(
        appearance: LauncherAppearance,
        backgroundAsset: ByteArray?,
        composition: ThemeComposition? = null,
        name: String? = null,
        id: String = UUID.randomUUID().toString(),
        additionalAssets: Map<String, ByteArray> = emptyMap()
    ): ByteArray {
        if (backgroundAsset != null && backgroundAsset.size > 5 * 1024 * 1024) {
            throw ThemeArchiveException("Theme image exceeds the 5 MiB limit")
        }
        val allAssets = additionalAssets.toMutableMap()
        if (backgroundAsset != null) allAssets["assets/background.png"] = backgroundAsset
        if (allAssets.size > 10 || allAssets.values.any { it.size > 5 * 1024 * 1024 }) {
            throw ThemeArchiveException("Theme asset limits exceeded")
        }
        if (allAssets.keys.any { !it.matches(Regex("assets/[A-Za-z0-9_-]+\\.(png|jpg|jpeg|webp)")) }) {
            throw ThemeArchiveException("Invalid theme asset path")
        }
        val background = if (backgroundAsset == null) {
            buildJsonObject {
                put("type", "solid")
                put("argb", "#%08X".format(appearance.backgroundColorArgb ?: 0xFF101820L))
            }
        } else {
            buildJsonObject {
                put("type", "asset")
                put("path", "assets/background.png")
            }
        }
        val manifest = buildJsonObject {
            put("formatVersion", when {
                composition == null -> 1
                composition.requiresFormatV3() -> 3
                else -> 2
            })
            put("id", id)
            put("name", name ?: "${appearance.themeId.name.replace('_', ' ')} Demo")
            put("layout", appearance.themeId.name)
            appearance.accentColorArgb?.let { put("accentArgb", "#%08X".format(it)) }
            put("background", background)
            put("tileShape", appearance.tileShape.name.lowercase())
            composition?.let { put("composition", ThemeCompositionValidator.encode(it)) }
        }

        val archiveBytes = ByteArrayOutputStream().use { output ->
            ZipOutputStream(output).use { zip ->
                zip.putNextEntry(ZipEntry("manifest.json"))
                zip.write(Json.encodeToString(kotlinx.serialization.json.JsonObject.serializer(), manifest).toByteArray(Charsets.UTF_8))
                zip.closeEntry()
                allAssets.forEach { (path, bytes) ->
                    zip.putNextEntry(ZipEntry(path))
                    zip.write(bytes)
                    zip.closeEntry()
                }
            }
            output.toByteArray()
        }
        if (archiveBytes.size > 20 * 1024 * 1024) throw ThemeArchiveException("Theme package exceeds the 20 MiB limit")
        val archive = ThemeArchiveReader.read(archiveBytes)
        ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)
        return archiveBytes
    }
}
