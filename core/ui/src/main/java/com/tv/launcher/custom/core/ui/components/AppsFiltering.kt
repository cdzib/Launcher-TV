package com.tv.launcher.custom.core.ui.components

import android.content.pm.ApplicationInfo
import com.tv.launcher.custom.core.model.apps.AppEntry

/**
 * Content filters shared by theme navigation and the Apps screen. Categories come from the
 * package manager; package hints cover stores and TVs that leave CATEGORY_UNDEFINED.
 */
object AppsFiltering {
    private val VIDEO_HINTS = listOf(
        "netflix", "youtube", "prime", "disney", "hbo", "max", "plex", "crunchyroll", "twitch",
        "vlc", "kodi", "filmin", "movistar", "atresplayer", "mubi", "paramount", "apple.tv",
        "starplus", "directv", "izzi", "claro", "flow", "tv"
    )
    private val AUDIO_HINTS = listOf(
        "spotify", "deezer", "music", "tunein", "radio", "soundcloud", "pandora", "audible", "podcast"
    )
    private val GAME_HINTS = listOf(
        "game", "xbox", "stadia", "geforce", "nvidia", "retro", "emulator", "emulador"
    )

    fun apply(
        apps: List<AppEntry>,
        filter: AppsFilter,
        categories: Map<String, Int>,
        favorites: Set<String>,
        recents: List<String>
    ): List<AppEntry> = when (filter) {
        AppsFilter.ALL -> apps
        AppsFilter.FOR_YOU -> ordered(apps, recents + favorites.toList())
        AppsFilter.FAVORITES -> ordered(apps, favorites.toList())
        AppsFilter.RECENTS -> ordered(apps, recents)
        AppsFilter.VIDEO -> apps.filter { matches(it, categories, ApplicationInfo.CATEGORY_VIDEO, VIDEO_HINTS) }
        AppsFilter.AUDIO -> apps.filter { matches(it, categories, ApplicationInfo.CATEGORY_AUDIO, AUDIO_HINTS) }
        AppsFilter.GAMES -> apps.filter { matches(it, categories, ApplicationInfo.CATEGORY_GAME, GAME_HINTS) }
    }

    private fun ordered(apps: List<AppEntry>, packages: List<String>): List<AppEntry> {
        val byPackage = apps.associateBy { it.packageName }
        return packages.mapNotNull(byPackage::get).distinctBy { it.packageName }
    }

    private fun matches(
        app: AppEntry,
        categories: Map<String, Int>,
        category: Int,
        hints: List<String>
    ): Boolean {
        if (categories[app.packageName] == category) return true
        val packageName = app.packageName.lowercase()
        val label = app.label.lowercase()
        return hints.any { packageName.contains(it) || label.contains(it) }
    }
}
