package com.tv.launcher.custom.core.model.theme

import kotlinx.coroutines.flow.Flow

class ObserveThemeUseCase(private val repository: ThemeRepository) {
    operator fun invoke(): Flow<ThemeId> = repository.observeTheme()
}
