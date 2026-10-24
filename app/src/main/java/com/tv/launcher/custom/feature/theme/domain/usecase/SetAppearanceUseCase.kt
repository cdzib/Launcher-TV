package com.tv.launcher.custom.feature.theme.domain.usecase

import com.tv.launcher.custom.feature.theme.domain.model.LauncherAppearance
import com.tv.launcher.custom.feature.theme.domain.repository.ThemeRepository
import javax.inject.Inject

class SetAppearanceUseCase @Inject constructor(private val repository: ThemeRepository) {
    suspend operator fun invoke(appearance: LauncherAppearance) = repository.setAppearance(appearance)
}
