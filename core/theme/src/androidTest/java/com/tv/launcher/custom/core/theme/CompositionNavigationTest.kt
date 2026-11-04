package com.tv.launcher.custom.core.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.tv.material3.Button
import androidx.tv.material3.Text
import com.tv.launcher.custom.core.model.theme.*
import com.tv.launcher.custom.core.ui.components.CanvasData
import com.tv.launcher.custom.core.ui.components.LocalCanvasWeatherContent
import com.tv.launcher.custom.core.ui.theme.LauncherTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)
class CompositionNavigationTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun weatherUsesOneHostSlotAndDisabledWeatherDoesNotReserveAnEmptyRegion() {
        var enabled by mutableStateOf(true)
        compose.setContent {
            LauncherTheme(ThemeId.WEBOS) {
                CompositionLocalProvider(LocalCanvasWeatherContent provides if (enabled) {
                    { modifier -> Text("Sample weather", modifier) }
                } else null) {
                    CompositionCanvas(
                        ThemeComposition(emptyList(), topBar = TopBarConfig(showClock = false),
                            widgets = listOf(ThemeWidget(ThemeWidgetType.WEATHER))),
                        PlaceholderCanvasData, Modifier.fillMaxSize(),
                        sectionContent = { Box(Modifier.fillMaxSize().testTag("section")) }
                    )
                }
            }
        }
        compose.onAllNodesWithText("Sample weather").assertCountEquals(1)
        val withWeather = compose.onNodeWithTag("section").fetchSemanticsNode().boundsInRoot
        compose.runOnIdle { enabled = false }
        compose.onAllNodesWithText("Sample weather").assertCountEquals(0)
        val withoutWeather = compose.onNodeWithTag("section").fetchSemanticsNode().boundsInRoot
        assertTrue(withoutWeather.height > withWeather.height)
    }

    @Test
    fun bottomDockSupportsDpadTraversalActivationAndExit() {
        var activated = ""
        val data = object : CanvasData by PlaceholderCanvasData {
            override fun onNavClick(label: String) { activated = label }
        }
        compose.setContent {
            LauncherTheme(ThemeId.WEBOS) {
                CompositionCanvas(
                    ThemeComposition(emptyList(), rail = RailConfig(position = RailPosition.BOTTOM,
                        items = "Inicio,Buscar,Apps", width = 160, glass = true)),
                    data, Modifier.fillMaxSize(), interactive = true,
                    sectionContent = { Button(onClick = {}) { Text("Content") } }
                )
            }
        }
        compose.onNodeWithText("Inicio").performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        compose.onNodeWithText("Inicio").performKeyInput { pressKey(Key.DirectionRight) }
        compose.onNodeWithText("Buscar").assertIsFocused()
        compose.onNodeWithText("Buscar").performKeyInput { pressKey(Key.DirectionRight) }
        compose.onNodeWithText("Apps").assertIsFocused()
        compose.onNodeWithText("Apps").performKeyInput { pressKey(Key.DirectionCenter) }
        compose.runOnIdle { assertEquals("Apps", activated) }
        compose.onNodeWithText("Apps").performKeyInput { pressKey(Key.DirectionUp) }
        compose.onNodeWithText("Content").assertIsFocused()
    }

    @Test
    fun overlayRailsReserveSpaceForSectionContentAtEveryEdge() {
        var position by mutableStateOf(RailPosition.LEFT)
        compose.setContent {
            LauncherTheme(ThemeId.WEBOS) {
                CompositionCanvas(
                    ThemeComposition(emptyList(), rail = RailConfig(position = position,
                        items = "Inicio", width = 160, overlay = true)),
                    PlaceholderCanvasData, Modifier.fillMaxSize(),
                    sectionContent = { Box(Modifier.fillMaxSize().testTag("section")) }
                )
            }
        }
        RailPosition.entries.forEach { edge ->
            compose.runOnIdle { position = edge }
            val content = compose.onNodeWithTag("section").fetchSemanticsNode().boundsInRoot
            val rail = compose.onNodeWithText("Inicio").fetchSemanticsNode().boundsInRoot
            assertTrue(content.width > 0 && content.height > 0)
            assertTrue(when (edge) {
                RailPosition.LEFT -> content.left >= rail.right
                RailPosition.RIGHT -> content.right <= rail.left
                RailPosition.BOTTOM -> content.bottom <= rail.top
            })
        }
    }

    @Test
    fun verticalRailsSupportDpadTraversalAndExitTowardContent() {
        var position by mutableStateOf(RailPosition.LEFT)
        compose.setContent {
            LauncherTheme(ThemeId.WEBOS) {
                CompositionCanvas(
                    ThemeComposition(emptyList(), rail = RailConfig(position = position, items = "Inicio,Buscar", width = 160)),
                    PlaceholderCanvasData, Modifier.fillMaxSize(), interactive = true,
                    sectionContent = { Button(onClick = {}) { Text("Content") } }
                )
            }
        }
        listOf(RailPosition.LEFT, RailPosition.RIGHT).forEach { edge ->
            compose.runOnIdle { position = edge }
            compose.onNodeWithText("Inicio").performSemanticsAction(SemanticsActions.RequestFocus) { it() }
            compose.onNodeWithText("Inicio").performKeyInput { pressKey(Key.DirectionDown) }
            compose.onNodeWithText("Buscar").assertIsFocused()
            compose.onNodeWithText("Buscar").performKeyInput {
                pressKey(if (edge == RailPosition.LEFT) Key.DirectionRight else Key.DirectionLeft)
            }
            compose.onNodeWithText("Content").assertIsFocused()
        }
    }

    @Test
    fun bottomTopBarIsBelowSectionContent() {
        compose.setContent {
            LauncherTheme(ThemeId.WEBOS) {
                CompositionCanvas(
                    ThemeComposition(emptyList(), topBar = TopBarConfig(position = TopBarPosition.BOTTOM,
                        tabs = "Inicio", showSearch = false, showClock = false, showSettings = false)),
                    PlaceholderCanvasData, Modifier.fillMaxSize(),
                    sectionContent = { Box(Modifier.fillMaxSize().testTag("section")) }
                )
            }
        }
        val content = compose.onNodeWithTag("section").fetchSemanticsNode().boundsInRoot
        val bar = compose.onNodeWithText("Inicio").fetchSemanticsNode().boundsInRoot
        assertTrue(content.bottom <= bar.top)
    }
}
