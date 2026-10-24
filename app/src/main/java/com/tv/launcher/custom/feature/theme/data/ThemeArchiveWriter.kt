package com.tv.launcher.custom.feature.theme.data

import com.tv.launcher.custom.feature.theme.domain.model.LauncherAppearance
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal object ThemeArchiveWriter {
    fun write(
        appearance: LauncherAppearance,
        backgroundAsset: ByteArray?,
        composition: ThemeComposition? = null,
        name: String? = null,
        id: String = UUID.randomUUID().toString()
    ): ByteArray {
        if (backgroundAsset != null && backgroundAsset.size > 5 * 1024 * 1024) {
            throw ThemeArchiveException("Theme image exceeds the 5 MiB limit")
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
            put("formatVersion", if (composition == null) 1 else 2)
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
                if (backgroundAsset != null) {
                    zip.putNextEntry(ZipEntry("assets/background.png"))
                    zip.write(backgroundAsset)
                    zip.closeEntry()
                }
            }
            output.toByteArray()
        }
        if (archiveBytes.size > 20 * 1024 * 1024) throw ThemeArchiveException("Theme package exceeds the 20 MiB limit")
        return archiveBytes
    }
}
