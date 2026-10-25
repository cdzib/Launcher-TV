package com.tv.launcher.custom.core.data.theme

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

class ThemeArchiveException(message: String) : Exception(message)

data class ThemeArchive(
    val manifest: String,
    val assets: Map<String, ByteArray>
)

object ThemeArchiveReader {
    private const val MAX_ARCHIVE_BYTES = 20 * 1024 * 1024
    private const val MAX_ASSET_BYTES = 5 * 1024 * 1024
    private const val MAX_ASSET_COUNT = 10
    private const val MAX_MANIFEST_BYTES = 64 * 1024

    fun read(archiveBytes: ByteArray): ThemeArchive {
        if (archiveBytes.size > MAX_ARCHIVE_BYTES) {
            throw ThemeArchiveException("Theme archive exceeds the 20 MiB limit")
        }

        var manifest: String? = null
        val assets = linkedMapOf<String, ByteArray>()
        val seenEntries = mutableSetOf<String>()
        var totalUncompressedBytes = 0L

        try {
            ZipInputStream(ByteArrayInputStream(archiveBytes)).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    val name = entry.name
                    validateEntryName(name)
                    if (!seenEntries.add(name)) throw ThemeArchiveException("Duplicate ZIP entry")
                    if (entry.isDirectory) throw ThemeArchiveException("Directories are not allowed")

                    val limit = if (name == "manifest.json") MAX_MANIFEST_BYTES else MAX_ASSET_BYTES
                    val content = readBounded(zip, limit)
                    totalUncompressedBytes += content.size
                    if (totalUncompressedBytes > MAX_ARCHIVE_BYTES) {
                        throw ThemeArchiveException("Expanded archive exceeds the 20 MiB limit")
                    }

                    if (name == "manifest.json") {
                        manifest = content.toString(Charsets.UTF_8)
                    } else {
                        if (!name.startsWith("assets/")) {
                            throw ThemeArchiveException("Only manifest.json and assets/ files are allowed")
                        }
                        if (assets.size >= MAX_ASSET_COUNT) {
                            throw ThemeArchiveException("Theme archive has too many assets")
                        }
                        validateAssetType(name, content)
                        validateImageDimensions(name, content)
                        assets[name] = content
                    }
                    zip.closeEntry()
                }
            }
        } catch (exception: ThemeArchiveException) {
            throw exception
        } catch (exception: Exception) {
            throw ThemeArchiveException("Invalid theme ZIP archive")
        }

        return ThemeArchive(
            manifest = manifest ?: throw ThemeArchiveException("manifest.json is required"),
            assets = assets
        )
    }

    private fun validateEntryName(name: String) {
        if (name.isBlank() || name.startsWith('/') || '\\' in name ||
            name.split('/').any { it == ".." || it == "." || it.isEmpty() }
        ) {
            throw ThemeArchiveException("Unsafe ZIP entry path")
        }
        if (name != "manifest.json" && !name.matches(Regex("assets/[A-Za-z0-9_-]+\\.(png|jpg|jpeg|webp)"))) {
            throw ThemeArchiveException("Unsupported ZIP entry")
        }
    }

    private fun validateAssetType(name: String, bytes: ByteArray) {
        val valid = when (name.substringAfterLast('.').lowercase()) {
            "png" -> bytes.startsWith(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))
            "jpg", "jpeg" -> bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()
            "webp" -> bytes.size >= 12 && bytes.copyOfRange(0, 4).toString(Charsets.US_ASCII) == "RIFF" &&
                bytes.copyOfRange(8, 12).toString(Charsets.US_ASCII) == "WEBP"
            else -> false
        }
        if (!valid) throw ThemeArchiveException("Asset MIME does not match its file extension")
    }

    private fun validateImageDimensions(name: String, bytes: ByteArray) {
        val dimensions = when (name.substringAfterLast('.').lowercase()) {
            "png" -> if (bytes.size >= 24) readBigEndianInt(bytes, 16) to readBigEndianInt(bytes, 20) else null
            "jpg", "jpeg" -> jpegDimensions(bytes)
            "webp" -> webpDimensions(bytes)
            else -> null
        } ?: throw ThemeArchiveException("Image dimensions could not be read")

        if (dimensions.first !in 1..4096 || dimensions.second !in 1..4096) {
            throw ThemeArchiveException("Image dimensions must be between 1 and 4096 pixels")
        }
    }

    private fun jpegDimensions(bytes: ByteArray): Pair<Int, Int>? {
        var index = 2
        while (index + 4 < bytes.size) {
            if (bytes[index] != 0xFF.toByte()) return null
            val marker = bytes[index + 1].toInt() and 0xFF
            index += 2
            if (marker == 0xD8 || marker == 0xD9 || marker in 0xD0..0xD7) continue
            if (index + 2 > bytes.size) return null
            val segmentLength = readBigEndianShort(bytes, index)
            if (segmentLength < 2 || index + segmentLength > bytes.size) return null
            if (marker in setOf(0xC0, 0xC1, 0xC2, 0xC3, 0xC5, 0xC6, 0xC7, 0xC9, 0xCA, 0xCB, 0xCD, 0xCE, 0xCF)) {
                if (segmentLength < 7) return null
                return readBigEndianShort(bytes, index + 5) to readBigEndianShort(bytes, index + 3)
            }
            index += segmentLength
        }
        return null
    }

    private fun webpDimensions(bytes: ByteArray): Pair<Int, Int>? {
        if (bytes.size < 30) return null
        return when (bytes.copyOfRange(12, 16).toString(Charsets.US_ASCII)) {
            "VP8X" -> {
                val width = readLittleEndian24(bytes, 24) + 1
                val height = readLittleEndian24(bytes, 27) + 1
                width to height
            }
            "VP8 " -> {
                if (bytes[23] != 0x9D.toByte() || bytes[24] != 0x01.toByte() || bytes[25] != 0x2A.toByte()) return null
                (readLittleEndianShort(bytes, 26) and 0x3FFF) to (readLittleEndianShort(bytes, 28) and 0x3FFF)
            }
            "VP8L" -> {
                if (bytes[20] != 0x2F.toByte()) return null
                val b1 = bytes[21].toInt() and 0xFF
                val b2 = bytes[22].toInt() and 0xFF
                val b3 = bytes[23].toInt() and 0xFF
                val b4 = bytes[24].toInt() and 0xFF
                (1 + b1 + ((b2 and 0x3F) shl 8)) to
                    (1 + (b2 ushr 6) + (b3 shl 2) + ((b4 and 0x0F) shl 10))
            }
            else -> null
        }
    }

    private fun readBigEndianInt(bytes: ByteArray, offset: Int): Int =
        ((bytes[offset].toInt() and 0xFF) shl 24) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
            (bytes[offset + 3].toInt() and 0xFF)

    private fun readBigEndianShort(bytes: ByteArray, offset: Int): Int =
        ((bytes[offset].toInt() and 0xFF) shl 8) or (bytes[offset + 1].toInt() and 0xFF)

    private fun readLittleEndianShort(bytes: ByteArray, offset: Int): Int =
        (bytes[offset].toInt() and 0xFF) or ((bytes[offset + 1].toInt() and 0xFF) shl 8)

    private fun readLittleEndian24(bytes: ByteArray, offset: Int): Int =
        (bytes[offset].toInt() and 0xFF) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 16)

    private fun readBounded(input: ZipInputStream, maxBytes: Int): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            if (output.size() + count > maxBytes) throw ThemeArchiveException("ZIP entry exceeds size limit")
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean =
        size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }
}
