package com.tv.launcher.custom.feature.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tv.launcher.custom.R
import com.tv.launcher.custom.core.domain.DomainResult
import com.tv.launcher.custom.feature.apps.domain.usecase.LaunchAppUseCase
import com.tv.launcher.custom.feature.apps.domain.usecase.ObserveAppsUseCase
import com.tv.launcher.custom.feature.home.presentation.state.HomeEffect
import com.tv.launcher.custom.feature.home.presentation.state.HomeIntent
import com.tv.launcher.custom.feature.home.presentation.state.HomeUiState
import com.tv.launcher.custom.feature.theme.domain.model.ThemeId
import com.tv.launcher.custom.feature.theme.domain.model.LauncherAppearance
import com.tv.launcher.custom.feature.theme.domain.model.AccentChoice
import com.tv.launcher.custom.feature.theme.domain.model.BackgroundChoice
import com.tv.launcher.custom.feature.theme.domain.model.TileShapeChoice
import com.tv.launcher.custom.feature.theme.domain.usecase.ObserveAppearanceUseCase
import com.tv.launcher.custom.feature.theme.domain.usecase.SetAppearanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeApps: ObserveAppsUseCase,
    private val launchApp: LaunchAppUseCase,
    private val observeAppearance: ObserveAppearanceUseCase,
    private val setAppearance: SetAppearanceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var observeJob: Job? = null
    private var currentAppearance = LauncherAppearance()

    init {
        observe()
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.Launch -> launch(intent.packageName)
            HomeIntent.CycleTheme -> cycleTheme()
            is HomeIntent.SelectTheme -> updateAppearance { copy(themeId = intent.value, installedThemeId = null) }
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
        val updated = currentAppearance.transform()
        currentAppearance = updated
        viewModelScope.launch { setAppearance(updated) }
    }
}
