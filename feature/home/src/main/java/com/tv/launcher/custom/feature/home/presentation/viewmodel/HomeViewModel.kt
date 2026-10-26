package com.tv.launcher.custom.feature.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tv.launcher.custom.core.ui.R
import com.tv.launcher.custom.core.model.DomainResult
import com.tv.launcher.custom.core.model.apps.LaunchAppUseCase
import com.tv.launcher.custom.core.model.apps.ObserveAppsUseCase
import com.tv.launcher.custom.feature.home.presentation.state.HomeEffect
import com.tv.launcher.custom.feature.home.presentation.state.HomeIntent
import com.tv.launcher.custom.feature.home.presentation.state.HomeUiState
import com.tv.launcher.custom.feature.home.presentation.state.WeatherUiState
import com.tv.launcher.custom.core.model.theme.*
import com.tv.launcher.custom.core.model.weather.WeatherRepository
import com.tv.launcher.custom.core.model.weather.WeatherSettings
import com.tv.launcher.custom.core.model.weather.WeatherSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeApps: ObserveAppsUseCase,
    private val launchApp: LaunchAppUseCase,
    private val observeAppearance: ObserveAppearanceUseCase,
    private val setAppearance: SetAppearanceUseCase,
    private val weatherRepository: WeatherRepository,
    private val weatherSettingsRepository: WeatherSettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()
    private val _weatherState = MutableStateFlow(WeatherUiState())
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    private var observeJob: Job? = null
    private var currentAppearance = LauncherAppearance()
    private val appearanceUpdateMutex = Mutex()
    private var citySearchJob: Job? = null
    private var weatherRefreshJob: Job? = null

    init {
        observe()
        observeWeatherSettings()
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.Launch -> launch(intent.packageName)
            HomeIntent.CycleTheme -> cycleTheme()
            is HomeIntent.SelectTheme -> updateAppearance {
                copy(themeId = intent.value, installedThemeId = null, backgroundColorArgb = null)
            }
            is HomeIntent.SelectAccent -> updateAppearance { copy(accent = intent.value, accentColorArgb = null) }
            is HomeIntent.SelectBackground -> updateAppearance { copy(background = intent.value, backgroundImageUri = null, backgroundColorArgb = null) }
            is HomeIntent.SelectBackgroundImage -> updateAppearance { copy(backgroundImageUri = intent.uri, backgroundColorArgb = null) }
            is HomeIntent.SelectBackgroundColor -> updateAppearance { copy(backgroundImageUri = null, backgroundColorArgb = intent.argb) }
            HomeIntent.ClearCustomBackground -> updateAppearance { copy(backgroundImageUri = null, backgroundColorArgb = null) }
            is HomeIntent.ApplyImportedTheme -> updateAppearance { intent.appearance.copy(shortcuts = shortcuts) }
            is HomeIntent.SelectTileShape -> updateAppearance { copy(tileShape = intent.value) }
            is HomeIntent.SelectShortcut -> {
                val apps = (_state.value as? HomeUiState.Success)?.apps.orEmpty()
                if (intent.slot in 0..4 && (intent.packageName.isEmpty() || apps.any { it.packageName == intent.packageName })) {
                    updateAppearance { copy(shortcuts = shortcuts + (intent.slot to intent.packageName)) }
                }
            }
            HomeIntent.Retry -> observe()
            is HomeIntent.SearchWeatherCities -> searchWeatherCities(intent.query)
            is HomeIntent.UpdateWeatherCityQuery -> _weatherState.update {
                it.copy(cityQuery = intent.query, cities = emptyList(), hasWeatherError = false)
            }
            is HomeIntent.SelectWeatherCity -> saveWeatherSettings { copy(city = intent.city, isEnabled = true) }
            is HomeIntent.SetWeatherUnit -> saveWeatherSettings { copy(unit = intent.unit) }
            is HomeIntent.SetWeatherEnabled -> saveWeatherSettings { copy(isEnabled = intent.isEnabled) }
            HomeIntent.ClearWeatherSettings -> saveWeatherSettings { WeatherSettings(unit = unit) }
            HomeIntent.RefreshWeather -> refreshWeather(_weatherState.value.settings)
        }
    }

    private fun observeWeatherSettings() {
        viewModelScope.launch {
            weatherSettingsRepository.observeSettings().collect { settings ->
                _weatherState.update { it.copy(settings = settings) }
                if (settings.isEnabled && settings.city != null) refreshWeather(settings)
                else {
                    weatherRefreshJob?.cancel()
                    _weatherState.update { it.copy(currentWeather = null, isLoadingWeather = false, hasWeatherError = false) }
                }
            }
        }
    }

    private fun searchWeatherCities(query: String) {
        citySearchJob?.cancel()
        _weatherState.update { it.copy(cityQuery = query, cities = emptyList(), isSearchingCities = false, hasWeatherError = false) }
        if (query.trim().length < MIN_WEATHER_QUERY_LENGTH) return
        citySearchJob = viewModelScope.launch {
            _weatherState.update { it.copy(isSearchingCities = true) }
            try {
                val results = weatherRepository.searchCities(query)
                _weatherState.update { it.copy(cities = results, isSearchingCities = false) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _weatherState.update { it.copy(isSearchingCities = false, hasWeatherError = true) }
            }
        }
    }

    private fun saveWeatherSettings(transform: WeatherSettings.() -> WeatherSettings) {
        viewModelScope.launch {
            val settings = transform(_weatherState.value.settings)
            if (settings.isEnabled && settings.city == null) {
                _weatherState.update { it.copy(hasWeatherError = true) }
                return@launch
            }
            try {
                weatherSettingsRepository.saveSettings(settings)
                _weatherState.update { it.copy(settings = settings, hasWeatherError = false) }
                if (!settings.isEnabled || settings.city == null) {
                    weatherRefreshJob?.cancel()
                    _weatherState.update { it.copy(currentWeather = null, isLoadingWeather = false) }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _weatherState.update { it.copy(hasWeatherError = true) }
            }
        }
    }

    private fun refreshWeather(settings: WeatherSettings) {
        val city = settings.city ?: return
        weatherRefreshJob?.cancel()
        weatherRefreshJob = viewModelScope.launch {
            _weatherState.update { it.copy(isLoadingWeather = true, hasWeatherError = false) }
            try {
                val weather = weatherRepository.getCurrentWeather(city, settings.unit)
                _weatherState.update { it.copy(currentWeather = weather, isLoadingWeather = false) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _weatherState.update { it.copy(isLoadingWeather = false, hasWeatherError = true) }
            }
        }
    }

    private fun observe() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            combine(observeApps(), observeAppearance()) { apps, appearance -> apps to appearance }
                .catch { _state.update { current -> HomeUiState.Error(current.themeId) } }
                .collect { (apps, appearance) ->
                    currentAppearance = appearance
                    _state.value = if (apps.isEmpty()) {
                        HomeUiState.Empty(appearance.themeId)
                    } else {
                        HomeUiState.Success(apps.toImmutableList(), appearance.themeId, appearance)
                    }
                }
        }
    }

    private fun launch(packageName: String) {
        viewModelScope.launch {
            if (launchApp(packageName) is DomainResult.Failure) {
                _effect.send(HomeEffect.ShowMessage(R.string.error_launch))
            }
        }
    }

    private fun cycleTheme() {
        val all = ThemeId.entries
        val current = currentAppearance
        val next = all[(current.themeId.ordinal + 1) % all.size]
        updateAppearance { copy(themeId = next) }
    }

    private fun updateAppearance(transform: LauncherAppearance.() -> LauncherAppearance) {
        viewModelScope.launch {
            appearanceUpdateMutex.lock()
            try {
                val updated = currentAppearance.transform()
                try {
                    setAppearance(updated)
                    currentAppearance = updated
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    _effect.send(HomeEffect.ShowMessage(R.string.appearance_save_failed))
                }
            } finally {
                appearanceUpdateMutex.unlock()
            }
        }
    }

    private companion object {
        const val MIN_WEATHER_QUERY_LENGTH = 2
    }
}
