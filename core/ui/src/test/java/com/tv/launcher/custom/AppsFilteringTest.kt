package com.tv.launcher.custom

import android.content.pm.ApplicationInfo
import com.tv.launcher.custom.core.model.apps.AppEntry
import com.tv.launcher.custom.core.ui.components.AppsFilter
import com.tv.launcher.custom.core.ui.components.AppsFiltering
import org.junit.Assert.assertEquals
import org.junit.Test

class AppsFilteringTest {
    private val apps = listOf(
        AppEntry("com.netflix.mediaclient", "Netflix"),
        AppEntry("com.spotify.music", "Spotify"),
        AppEntry("com.example.puzzle", "Puzzle"),
        AppEntry("com.example.notes", "Notes"),
        AppEntry("com.example.tv", "Cine")
    )

    private val categories = mapOf(
        "com.netflix.mediaclient" to ApplicationInfo.CATEGORY_UNDEFINED,
        "com.spotify.music" to ApplicationInfo.CATEGORY_UNDEFINED,
        "com.example.puzzle" to ApplicationInfo.CATEGORY_GAME,
        "com.example.notes" to ApplicationInfo.CATEGORY_PRODUCTIVITY,
        "com.example.tv" to ApplicationInfo.CATEGORY_VIDEO
    )

    private fun filter(value: AppsFilter, favorites: Set<String> = emptySet(), recents: List<String> = emptyList()) =
        AppsFiltering.apply(apps, value, categories, favorites, recents).map { it.packageName }

    @Test
    fun `video filter uses package manager category and known streaming hints`() {
        assertEquals(
            listOf("com.netflix.mediaclient", "com.example.tv"),
            filter(AppsFilter.VIDEO)
        )
    }

    @Test
    fun `audio and games filters match category and hints`() {
        assertEquals(listOf("com.spotify.music"), filter(AppsFilter.AUDIO))
        assertEquals(listOf("com.example.puzzle"), filter(AppsFilter.GAMES))
    }

    @Test
    fun `for you merges recents and favorites without duplicates`() {
        assertEquals(
            listOf("com.example.notes", "com.netflix.mediaclient", "com.example.tv"),
            filter(
                AppsFilter.FOR_YOU,
                favorites = setOf("com.example.tv", "com.netflix.mediaclient"),
                recents = listOf("com.example.notes", "com.netflix.mediaclient")
            )
        )
    }

    @Test
    fun `favorites and recents keep their requested order`() {
        assertEquals(
            listOf("com.example.tv", "com.spotify.music"),
            filter(AppsFilter.FAVORITES, favorites = setOf("com.example.tv", "com.spotify.music"))
        )
        assertEquals(
            listOf("com.spotify.music", "com.netflix.mediaclient"),
            filter(AppsFilter.RECENTS, recents = listOf("com.spotify.music", "com.netflix.mediaclient"))
        )
    }
}
