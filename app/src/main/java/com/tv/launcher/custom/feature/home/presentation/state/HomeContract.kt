package com.tv.launcher.custom.feature.home.presentation.state

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.tv.launcher.custom.feature.apps.domain.model.AppEntry
import com.tv.launcher.custom.feature.theme.domain.model.ThemeId
import com.tv.launcher.custom.feature.theme.domain.model.LauncherAppearance
import com.tv.launcher.custom.feature.theme.domain.model.AccentChoice
import com.tv.launcher.custom.feature.theme.domain.model.BackgroundChoice
import com.tv.launcher.custom.feature.theme.domain.model.TileShapeChoice
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface HomeUiState {
    val themeId: ThemeId

    data class Loading(override val themeId: ThemeId = ThemeId.WEBOS) : HomeUiState
    data class Empty(override val themeId: ThemeId) : HomeUiState
    data class Error(override val themeId: ThemeId) : HomeUiState
    data class Success(
        val apps: ImmutableList<AppEntry>,
        override val themeId: ThemeId,
        val appearance: LauncherAppearance = LauncherAppearance(themeId = themeId)
    ) : HomeUiState
}

sealed interface HomeIntent {
    data class Launch(val packageName: String) : HomeIntent
    data object CycleTheme : HomeIntent
    data class SelectTheme(val value: ThemeId) : HomeIntent
    data class SelectAccent(val value: AccentChoice) : HomeIntent
    data class SelectBackground(val value: BackgroundChoice) : HomeIntent
    data class SelectBackgroundImage(val uri: String) : HomeIntent
    data class SelectBackgroundColor(val argb: Long) : HomeIntent
    data object ClearCustomBackground : HomeIntent
    data class ApplyImportedTheme(val appearance: LauncherAppearance) : HomeIntent
    data class SelectTileShape(val value: TileShapeChoice) : HomeIntent
    data class SelectShortcut(val slot: Int, val packageName: String) : HomeIntent
    data object Retry : HomeIntent
}

sealed interface HomeEffect {
    data class ShowMessage(@StringRes val messageRes: Int) : HomeEffect
}
