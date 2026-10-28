package com.tv.launcher.custom.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.tv.launcher.custom.core.model.theme.ThemeId
import com.tv.launcher.custom.core.ui.theme.LauncherTheme
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SearchBackgroundTest {
    @get:Rule val compose = createComposeRule()

    @Test fun embeddedLayoutsPreserveHostWallpaperButStandaloneRemainsOpaque() {
        val profile = mutableStateOf(ThemeId.ROKU)
        val embedded = mutableStateOf(true)
        compose.setContent {
            LauncherTheme(profile.value) {
                Box(Modifier.fillMaxSize().background(Color.Magenta)) {
                    AppSearchScreen(emptyList(), {}, {}, embedded = embedded.value)
                }
            }
        }
        // Both focus and base layouts used to paint their own opaque backgrounds.
        listOf(ThemeId.ROKU, ThemeId.WEBOS).forEach { theme ->
            compose.runOnIdle { profile.value = theme; embedded.value = true }
            val background = compose.onRoot().captureToImage().toPixelMap()[1, 1]
            // Window/color transforms may dim the capture; the host hue must survive.
            assertTrue(background.red > .5f && background.blue > .5f && background.green < .05f)
            compose.runOnIdle { embedded.value = false }
            assertNotEquals(Color.Magenta, compose.onRoot().captureToImage().toPixelMap()[1, 1])
        }
    }
}
