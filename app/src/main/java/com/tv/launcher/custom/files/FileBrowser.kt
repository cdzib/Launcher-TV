package com.tv.launcher.custom.files

import java.io.File

enum class FileBrowserMode { THEMES, IMAGES, ASSETS }

/**
 * File-system rules for the in-app picker used on TVs without a documents provider.
 * Kept free of Android types so the navigation and filtering stay unit-testable.
 */
object FileBrowser {
    private val imageExtensions = setOf("png", "jpg", "jpeg", "webp")

    fun supports(file: File, mode: FileBrowserMode): Boolean {
        if (!file.isFile) return false
        val extension = file.extension.lowercase()
        return when (mode) {
            FileBrowserMode.THEMES -> extension == "zip"
            FileBrowserMode.IMAGES, FileBrowserMode.ASSETS -> extension in imageExtensions
        }
    }

    fun list(directory: File, mode: FileBrowserMode): List<File> {
        val children = runCatching { directory.listFiles() }.getOrNull() ?: return emptyList()
        return children.asSequence()
            .filterNot { it.name.startsWith(".") }
            .filter { it.isDirectory || supports(it, mode) }
            .sortedWith(compareBy<File>({ !it.isDirectory }, { it.name.lowercase() }))
            .toList()
    }

    fun parentWithin(current: File, roots: List<File>): File? {
        val root = roots.firstOrNull { isInside(current, it) } ?: return null
        if (samePath(current, root)) return null
        return current.parentFile
    }

    fun isInside(file: File, root: File): Boolean {
        val rootPath = root.absolutePath.trimEnd(File.separatorChar)
        val path = file.absolutePath
        return path == rootPath || path.startsWith(rootPath + File.separator)
    }

    fun relativePath(root: File, current: File): String =
        if (samePath(root, current)) "" else current.absolutePath.removePrefix(root.absolutePath).trimStart(File.separatorChar)

    fun formatSize(bytes: Long): String = when {
        bytes >= 1024L * 1024L -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        bytes >= 1024L -> "%d KB".format(bytes / 1024L)
        else -> "$bytes B"
    }

    private fun samePath(a: File, b: File) =
        a.absolutePath.trimEnd(File.separatorChar) == b.absolutePath.trimEnd(File.separatorChar)
}
