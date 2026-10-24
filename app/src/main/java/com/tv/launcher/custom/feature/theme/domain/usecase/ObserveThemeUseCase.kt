package com.tv.launcher.custom.feature.theme.domain.usecase

import com.tv.launcher.custom.feature.theme.domain.model.ThemeId
import com.tv.launcher.custom.feature.theme.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveThemeUseCase @Inject constructor(private val repository: ThemeRepository) {
    operator fun invoke(): Flow<ThemeId> = repository.observeTheme()
}
