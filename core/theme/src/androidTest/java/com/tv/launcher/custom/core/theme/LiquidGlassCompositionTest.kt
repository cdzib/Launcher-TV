package com.tv.launcher.custom.core.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.tv.material3.Text
import com.tv.launcher.custom.core.model.theme.*
import com.tv.launcher.custom.core.ui.components.CanvasData
import com.tv.launcher.custom.core.ui.components.LocalCanvasWeatherContent
import com.tv.launcher.custom.core.ui.theme.LauncherTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalTestApi::class)
class LiquidGlassCompositionTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun glassLayoutSeparatesHeroSidebarAndHostWeatherAndLaunchesFeaturedInstalledApp() {
        var launched = -1
        val data = object : CanvasData by PlaceholderCanvasData {
            override fun itemCount(block: ThemeBlock) = 3
            override fun onItemClick(block: ThemeBlock, index: Int) { launched = index }
        }
        compose.setContent {
            LauncherTheme(ThemeId.APPLE_TV) {
                CompositionLocalProvider(LocalCanvasWeatherContent provides { modifier ->
                    Text("Host weather", modifier)
                }) {
                    CompositionCanvas(ThemePresets.byName("macOS Liquid Glass")!!, data,
                        Modifier.fillMaxSize(), interactive = true)
                }
            }
        }
        val hero = compose.onNodeWithText("APLICACIÓN DESTACADA")
        val heroBounds = hero.fetchSemanticsNode().boundsInRoot
        val railBounds = compose.onNodeWithText("Inicio").fetchSemanticsNode().boundsInRoot
        val weatherBounds = compose.onNodeWithText("Host weather").fetchSemanticsNode().boundsInRoot
        val appsBounds = compose.onNodeWithText("APLICACIONES").fetchSemanticsNode().boundsInRoot
        assertTrue(railBounds.right <= heroBounds.left)
        assertTrue(weatherBounds.left >= heroBounds.right)
        assertTrue(appsBounds.top >= heroBounds.bottom)
        compose.onAllNodesWithText("Liquid Glass").assertCountEquals(0)
        hero.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        hero.assertIsFocused().performKeyInput { pressKey(Key.DirectionCenter) }
        compose.runOnIdle { assertEquals(0, launched) }
    }

    @Test
    fun absentWeatherDoesNotFabricateWeatherOrHdmiControls() {
        compose.setContent {
            LauncherTheme(ThemeId.APPLE_TV) {
                val data = object : CanvasData by PlaceholderCanvasData {
                    override fun itemCount(block: ThemeBlock) = 3
                }
                CompositionCanvas(ThemePresets.byName("macOS Liquid Glass")!!, data,
                    Modifier.fillMaxSize())
            }
        }
        compose.onNodeWithText("APLICACIÓN DESTACADA").assertExists()
        compose.onAllNodes(hasText("HDMI", substring = true)).assertCountEquals(0)
        compose.onAllNodes(hasText("°", substring = true)).assertCountEquals(0)
    }
}
