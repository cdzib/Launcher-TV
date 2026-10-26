package com.tv.launcher.custom.core.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.semantics.SemanticsActions
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.tv.material3.Text
import com.tv.launcher.custom.core.model.apps.AppEntry
import com.tv.launcher.custom.core.model.theme.ThemeId
import com.tv.launcher.custom.core.model.theme.ThemeComposition
import com.tv.launcher.custom.core.model.theme.TopBarConfig
import com.tv.launcher.custom.core.theme.layout.*
import com.tv.launcher.custom.core.ui.components.*
import com.tv.launcher.custom.core.ui.theme.LauncherTheme
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith

/** No system actions or installed applications are needed by this fixture. */
@RunWith(AndroidJUnit4::class)
class LauncherSectionContentTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun textMenusKeepDeviceSettingsAndPersonalizationAsSeparateActions() {
        var profile by mutableStateOf(ThemeId.ROKU)
        var settingsCalls = 0
        var personalizationCalls = 0
        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
        val settingsLabel = resources.getString(com.tv.launcher.custom.core.ui.R.string.system_settings)
        val personalizationLabel = resources.getString(com.tv.launcher.custom.core.ui.R.string.launcher_personalization)
        compose.setContent {
            LauncherTheme(profile) {
                val apps = persistentListOf(AppEntry("sample.one", "App de prueba"))
                val focus = remember { FocusRequester() }
                if (profile == ThemeId.ROKU) {
                    SideGridLayout(apps, focus, {}, { settingsCalls++ }, onOpenPersonalization = { personalizationCalls++ })
                } else {
                    AppleTvLayout(apps, focus, {}, { settingsCalls++ }, onOpenPersonalization = { personalizationCalls++ })
                }
            }
        }
        listOf(ThemeId.ROKU, ThemeId.APPLE_TV).forEachIndexed { index, theme ->
            compose.runOnIdle { profile = theme }
            compose.onNodeWithText(settingsLabel).performClick()
            compose.runOnIdle {
                assertEquals(index + 1, settingsCalls)
                assertEquals(index, personalizationCalls)
            }
            compose.onNodeWithText(personalizationLabel).performClick()
            compose.runOnIdle { assertEquals(index + 1, personalizationCalls) }
        }
    }

    @Test
    fun eachThemeMountsOnlyTheActiveSectionWithoutADialog() {
        var profile by mutableStateOf(ThemeId.WEBOS)
        var section by mutableStateOf(LauncherSection.APPS)
        compose.setContent {
            LauncherTheme(profile) {
                CompositionLocalProvider(
                    LocalLauncherSection provides section,
                    LocalOpenHome provides { section = LauncherSection.HOME },
                    LocalOpenApps provides { section = LauncherSection.APPS },
                    LocalOpenSearch provides { section = LauncherSection.SEARCH },
                    LocalLauncherSectionContent provides if (section == LauncherSection.HOME) null else {
                        @Composable {
                            Box(Modifier.fillMaxSize().testTag(section.name)) { Text(section.name) }
                        }
                    }
                ) { ThemeContent(profile) }
            }
        }
        ThemeId.entries.forEach { theme ->
            compose.runOnIdle { profile = theme; section = LauncherSection.APPS }
            compose.onAllNodesWithTag("APPS").assertCountEquals(1)
            compose.onAllNodes(isDialog()).assertCountEquals(0)
            compose.onAllNodesWithTag("SEARCH").assertCountEquals(0)
            compose.runOnIdle { section = LauncherSection.SEARCH }
            compose.onAllNodesWithTag("APPS").assertCountEquals(0)
            compose.onAllNodesWithTag("SEARCH").assertCountEquals(1)
            compose.runOnIdle { section = LauncherSection.HOME }
            compose.onAllNodesWithTag("APPS").assertCountEquals(0)
            compose.onAllNodesWithTag("SEARCH").assertCountEquals(0)
        }
    }

    @Test
    fun declarativeTabsSwitchContentAndHomeRestoresTheOriginalBlocks() {
        compose.setContent {
            var section by remember { mutableStateOf(LauncherSection.HOME) }
            val data = remember {
                InstalledAppsCanvasData(
                    emptyList(), emptyList(), {},
                    onOpenApps = { section = LauncherSection.APPS }, onOpenSettings = {},
                    onUnavailableNavigation = {},
                    onOpenSearch = { section = LauncherSection.SEARCH },
                    onOpenHome = { section = LauncherSection.HOME }
                )
            }
            LauncherTheme(ThemeId.GOOGLE_TV) {
                CompositionLocalProvider(LocalLauncherSection provides section) {
                    CompositionCanvas(
                        composition = ThemeComposition(
                            blocks = emptyList(),
                            topBar = TopBarConfig(tabs = "Inicio,Buscar,Apps", showClock = false, showSettings = false)
                        ),
                        data = data, interactive = true,
                        sectionContent = if (section == LauncherSection.HOME) null else {
                            @Composable { Box(Modifier.testTag(section.name)) { Text("Contenido") } }
                        }
                    )
                }
            }
        }
        compose.onNodeWithText("Apps").performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        compose.onNodeWithText("Apps").assertIsFocused()
        compose.onAllNodesWithTag("APPS").assertCountEquals(1)
        compose.onNodeWithText("Buscar").performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        compose.onNodeWithText("Buscar").assertIsFocused()
        compose.onAllNodesWithTag("SEARCH").assertCountEquals(1)
        compose.onAllNodesWithTag("APPS").assertCountEquals(0)
        compose.onNodeWithText("Inicio").performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        compose.onAllNodesWithTag("SEARCH").assertCountEquals(0)
        compose.onAllNodes(isDialog()).assertCountEquals(0)
    }
}

@Composable
private fun ThemeContent(profile: ThemeId) {
    val apps = persistentListOf(AppEntry("sample.one", "App de prueba"))
    val focus = remember { FocusRequester() }
    when (profile) {
        ThemeId.WEBOS -> WebOsLayout(apps, focus, {}, {})
        ThemeId.FIRE_TV -> TopNavLayout(apps, focus, {}, {})
        ThemeId.ROKU -> SideGridLayout(apps, focus, {}, {})
        ThemeId.TIZEN -> TizenLayout(apps, focus, {}, {})
        ThemeId.ANDROID_TV -> AndroidTvLayout(apps, focus, {}, {})
        ThemeId.GOOGLE_TV -> GoogleTvLayout(apps, focus, {}, {})
        ThemeId.APPLE_TV -> AppleTvLayout(apps, focus, {}, {})
        ThemeId.FREE_MY_TV -> FreeMyTvLayout(apps, apps.first(), focus, {}, {})
    }
}
