package com.tv.launcher.custom.feature.home.presentation.state

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.tv.launcher.custom.core.model.apps.AppEntry
import com.tv.launcher.custom.core.model.theme.*
import com.tv.launcher.custom.core.model.weather.*
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
    data class SearchWeatherCities(val query: String) : HomeIntent
    data class UpdateWeatherCityQuery(val query: String) : HomeIntent
    data class SelectWeatherCity(val city: WeatherCity) : HomeIntent
    data class SetWeatherUnit(val unit: WeatherUnit) : HomeIntent
    data class SetWeatherEnabled(val isEnabled: Boolean) : HomeIntent
    data object ClearWeatherSettings : HomeIntent
    data object RefreshWeather : HomeIntent
}

@Immutable
data class WeatherUiState(
    val settings: WeatherSettings = WeatherSettings(),
    val cityQuery: String = "",
    val cities: List<WeatherCity> = emptyList(),
    val isSearchingCities: Boolean = false,
    val currentWeather: CurrentWeather? = null,
    val isLoadingWeather: Boolean = false,
    val hasWeatherError: Boolean = false
)

sealed interface HomeEffect {
    data class ShowMessage(@StringRes val messageRes: Int) : HomeEffect
}
