package com.tv.launcher.custom.core.theme

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import com.tv.launcher.custom.core.ui.theme.LauncherTheme
import com.tv.launcher.custom.core.ui.theme.LocalLauncherStyle
import com.tv.launcher.custom.core.ui.components.InstalledAppsCanvasData
import org.junit.Rule
import com.tv.launcher.custom.core.model.theme.*
import com.tv.launcher.custom.core.ui.theme.toStyle
import org.junit.Assert.*
import org.junit.Test

class SectionAppearanceTest {
    @get:Rule val compose = createComposeRule()

    @Test fun sectionContentReceivesActiveStyleAndBackdrop() {
        var observed: com.tv.launcher.custom.core.ui.theme.LauncherStyle? = null
        var hasBackdrop = false
        compose.setContent {
            LauncherTheme(ThemeId.ROKU) {
                CompositionCanvas(
                    composition = ThemeComposition(listOf(ThemeBlock(ThemeBlockType.APPS_GRID,
                        cardStyle = CardStyle.GLASS, cornerRadius = 23)), bgStyle = BgStyle.LANDSCAPE),
                    data = InstalledAppsCanvasData(emptyList(), emptyList(), {},
                        onOpenApps = {}, onOpenSettings = {}, onUnavailableNavigation = {}),
                    modifier = Modifier.fillMaxSize(),
                    sectionContent = {
                        observed = LocalLauncherStyle.current
                        hasBackdrop = com.tv.launcher.custom.core.ui.components.LocalGlassBackdrop.current != null
                    }
                )
            }
        }
        compose.runOnIdle {
            assertTrue(observed!!.glassSurfaces)
            assertEquals(23.dp, observed!!.tileRadius)
            assertTrue(hasBackdrop)
        }
    }
    @Test fun appSurfacesInheritDeclarativeMaterialAndShape() {
        val composition = ThemeComposition(listOf(ThemeBlock(
            ThemeBlockType.APPS_GRID, cardStyle = CardStyle.GLASS, cornerRadius = 23
        )))
        val style = composition.resolveSectionStyle(ThemeId.ROKU.toStyle())
        assertTrue(style.glassSurfaces)
        assertEquals(23.dp, style.tileRadius)
    }

    @Test fun compositionsWithoutAppBlocksKeepProfileStyle() {
        val base = ThemeId.WEBOS.toStyle()
        assertEquals(base, ThemeComposition(emptyList()).resolveSectionStyle(base))
    }

    @Test fun filledCollectionsKeepOpaqueMaterialAndConfiguredCorners() {
        val base = ThemeId.WEBOS.toStyle()
        val style = ThemeComposition(listOf(ThemeBlock(ThemeBlockType.APPS_ROW,
            cardStyle = CardStyle.FILLED, cornerRadius = 4))).resolveSectionStyle(base)
        assertFalse(style.glassSurfaces)
        assertEquals(4.dp, style.tileRadius)
        assertEquals(base.palette, style.palette)
    }
}
