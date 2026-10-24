package com.tv.launcher.custom.feature.theme.domain.usecase

import com.tv.launcher.custom.feature.theme.domain.model.ThemeId
import com.tv.launcher.custom.feature.theme.domain.repository.ThemeRepository
import javax.inject.Inject

class SetThemeUseCase @Inject constructor(private val repository: ThemeRepository) {
    suspend operator fun invoke(themeId: ThemeId) = repository.setTheme(themeId)
}
