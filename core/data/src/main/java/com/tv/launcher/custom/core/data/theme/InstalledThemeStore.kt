package com.tv.launcher.custom.core.data.theme

import com.tv.launcher.custom.core.model.theme.*
import java.io.File
import java.util.UUID

class InstalledThemeStore(private val rootDirectory: File) {
    fun initializeDefaultTheme(archiveBytes: ByteArray) {
        ensureRootDirectory()
        val initializedMarker = File(rootDirectory, ".default-theme-initialized")
        if (initializedMarker.exists()) return
        if (list().isEmpty()) install(archiveBytes)
        initializedMarker.writeText("initialized")
    }

    fun install(
        archiveBytes: ByteArray,
        version: String? = null,
        replaceExisting: Boolean = false
    ): InstalledTheme {
        val archive = ThemeArchiveReader.read(archiveBytes)
        val manifest = ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)
        ensureRootDirectory()

        val installedDirectory = File(rootDirectory, manifest.id)
        if (installedDirectory.exists() && !replaceExisting) throw ThemeArchiveException("Theme is already installed")

        val stagingDirectory = File(rootDirectory, ".pending-${UUID.randomUUID()}")
        try {
            check(stagingDirectory.mkdir()) { "Could not create staging directory" }
            File(stagingDirectory, "package.zip").writeBytes(archiveBytes)
            val assetsDirectory = File(stagingDirectory, "assets").apply { mkdir() }
            archive.assets.forEach { (path, content) ->
                val safeAssetName = path.substringAfterLast('/')
                File(assetsDirectory, safeAssetName).writeBytes(content)
            }
            version?.let { File(stagingDirectory, "catalog-version.txt").writeText(it) }
            val backupDirectory = File(rootDirectory, ".backup-${UUID.randomUUID()}")
            val hasBackup = installedDirectory.exists()
            if (hasBackup) {
                val historyDirectory = File(rootDirectory, ".history").apply { mkdirs() }
                val pendingBackup = File(historyDirectory, ".pending-${UUID.randomUUID()}")
                try {
                    File(installedDirectory, "package.zip").copyTo(pendingBackup)
                    check(pendingBackup.renameTo(File(historyDirectory, "${manifest.id}.zip"))) {
                        "Could not preserve previous theme archive"
                    }
                } finally {
                    pendingBackup.delete()
                }
            }
            if (hasBackup && !installedDirectory.renameTo(backupDirectory)) {
                throw IllegalStateException("Could not stage existing theme")
            }
            if (!stagingDirectory.renameTo(installedDirectory)) {
                if (hasBackup) backupDirectory.renameTo(installedDirectory)
                throw IllegalStateException("Could not finalize theme install")
            }
            if (hasBackup) backupDirectory.deleteRecursively()
        } catch (exception: Exception) {
            stagingDirectory.deleteRecursively()
            throw ThemeArchiveException("Could not install theme package")
        }

        return load(manifest.id) ?: throw ThemeArchiveException("Installed theme could not be read")
    }

    fun list(): List<InstalledTheme> {
        ensureRootDirectory()
        return rootDirectory.listFiles()
            .orEmpty()
            .asSequence()
            .filter { it.isDirectory && isUuid(it.name) }
            .mapNotNull { load(it.name) }
            .sortedBy { it.name.lowercase() }
            .toList()
    }

    /** Last archive before replacement, kept outside the installed theme library. */
    fun backupArchiveBytes(id: String): ByteArray? {
        if (!isUuid(id)) return null
        return File(rootDirectory, ".history/$id.zip").takeIf { it.isFile }?.readBytes()
    }

    fun load(id: String): InstalledTheme? {
        if (!isUuid(id)) return null
        val themeDirectory = File(rootDirectory, id)
        if (!isDirectChild(themeDirectory)) return null
        val packageFile = File(themeDirectory, "package.zip")
        if (!packageFile.isFile || packageFile.length() > MAX_ARCHIVE_BYTES) return null
        return runCatching {
            val archive = ThemeArchiveReader.read(packageFile.readBytes())
            val manifest = ThemeManifestValidator.parse(archive.manifest, archive.assets.keys)
            val imageUri = (manifest.background as? ThemeManifestBackground.Asset)?.let { background ->
                val assetName = background.path.substringAfterLast('/')
                val imageFile = File(File(themeDirectory, "assets"), assetName)
                if (!imageFile.isFile) return null
                imageFile.toURI().toString()
            }
            val assetUris = archive.assets.keys.associateWith { assetPath ->
                val assetName = assetPath.substringAfterLast('/')
                val assetFile = File(File(themeDirectory, "assets"), assetName)
                if (!assetFile.isFile) return null
                assetFile.toURI().toString()
            }
            InstalledTheme(
                id = manifest.id,
                name = manifest.name,
                layout = manifest.layout,
                appearance = manifest.toAppearance(imageUri, manifest.id),
                version = File(themeDirectory, "catalog-version.txt").takeIf { it.isFile }?.readText(),
                composition = manifest.composition,
                assetUris = assetUris
            )
        }.getOrNull()
    }

    fun remove(id: String): Boolean {
        if (!isUuid(id)) return false
        val themeDirectory = File(rootDirectory, id)
        if (!isDirectChild(themeDirectory) || !themeDirectory.exists()) return false
        return themeDirectory.deleteRecursively()
    }

    fun archiveBytes(id: String): ByteArray? {
        if (!isUuid(id)) return null
        val packageFile = File(File(rootDirectory, id), "package.zip")
        if (!isDirectChild(packageFile.parentFile!!) || !packageFile.isFile || packageFile.length() > MAX_ARCHIVE_BYTES) return null
        return runCatching { packageFile.readBytes() }.getOrNull()
    }

    private fun ensureRootDirectory() {
        if (!rootDirectory.exists() && !rootDirectory.mkdirs()) {
            throw ThemeArchiveException("Could not create installed themes directory")
        }
    }

    private fun isDirectChild(file: File): Boolean =
        file.canonicalFile.parentFile == rootDirectory.canonicalFile

    private fun isUuid(value: String): Boolean = runCatching {
        UUID.fromString(value).toString() == value
    }.getOrDefault(false)

    private companion object {
        const val MAX_ARCHIVE_BYTES = 20L * 1024 * 1024
    }
}
