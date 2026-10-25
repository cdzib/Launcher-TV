package com.tv.launcher.custom.feature.home

import com.tv.launcher.custom.core.model.apps.AppEntry
import com.tv.launcher.custom.core.model.theme.ThemeId
import com.tv.launcher.custom.core.ui.theme.LayoutKind
import com.tv.launcher.custom.core.ui.theme.toStyle
import com.tv.launcher.custom.feature.home.presentation.resolveFreeMyTvFavoriteApps
import com.tv.launcher.custom.feature.home.presentation.resolveFreeMyTvFeaturedApp
import org.junit.Assert.assertEquals
import org.junit.Test

class FreeMyTvAppSelectionTest {
    private val netflix = AppEntry("com.netflix", "Netflix")
    private val youtube = AppEntry("com.youtube", "YouTube")
    private val primeVideo = AppEntry("com.prime", "Prime Video")

    @Test
    fun `FREE MY TV selecciona su layout integrado`() {
        assertEquals(LayoutKind.FREE_MY_TV, ThemeId.FREE_MY_TV.toStyle().layout)
    }

    @Test
    fun `hero prioriza Netflix y usa primera app instalada como fallback`() {
        assertEquals(netflix, resolveFreeMyTvFeaturedApp(listOf(youtube, netflix)))
        assertEquals(youtube, resolveFreeMyTvFeaturedApp(listOf(youtube, primeVideo)))
        assertEquals(null, resolveFreeMyTvFeaturedApp(emptyList()))
    }

    @Test
    fun `favoritos respetan orden de accesos y omiten paquetes no instalados`() {
        val apps = listOf(youtube, netflix, primeVideo)

        val result = resolveFreeMyTvFavoriteApps(
            apps = apps,
            shortcuts = mapOf(2 to primeVideo.packageName, 0 to netflix.packageName, 1 to "missing.package")
        )

        assertEquals(listOf(netflix, primeVideo), result)
    }

    @Test
    fun `sin favoritos validos muestra apps instaladas en orden de launcher`() {
        val apps = listOf(youtube, netflix)

        assertEquals(apps, resolveFreeMyTvFavoriteApps(apps, emptyMap()))
        assertEquals(apps, resolveFreeMyTvFavoriteApps(apps, mapOf(0 to "missing.package")))
    }
}
