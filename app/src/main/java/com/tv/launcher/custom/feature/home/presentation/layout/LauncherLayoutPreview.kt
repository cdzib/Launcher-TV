package com.tv.launcher.custom.feature.home.presentation.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import com.tv.launcher.custom.core.theme.LauncherTheme
import com.tv.launcher.custom.core.theme.LocalLauncherStyle
import com.tv.launcher.custom.feature.apps.domain.model.AppEntry
import com.tv.launcher.custom.feature.theme.domain.model.ThemeId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private val PREVIEW_APPS: ImmutableList<AppEntry> = persistentListOf(
    AppEntry("com.example.streaming", "Stream"),
    AppEntry("com.example.music", "Music"),
    AppEntry("com.example.games", "Games"),
    AppEntry("com.example.photos", "Photos"),
    AppEntry("com.example.settings", "Settings")
)

@Composable
internal fun LauncherLayoutPreview(
    themeId: ThemeId,
    content: @Composable (ImmutableList<AppEntry>, FocusRequester) -> Unit
) {
    LauncherTheme(themeId) {
        val focusRequester = remember { FocusRequester() }
        val palette = LocalLauncherStyle.current.palette
        Surface(
            modifier = Modifier.fillMaxSize(),
            colors = SurfaceDefaults.colors(
                containerColor = palette.background,
                contentColor = palette.onBackground
            )
        ) {
            Box(Modifier.fillMaxSize()) {
                content(PREVIEW_APPS, focusRequester)
            }
        }
    }
}
