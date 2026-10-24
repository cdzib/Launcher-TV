package com.tv.launcher.custom.feature.theme.domain.usecase

import com.tv.launcher.custom.feature.theme.domain.repository.ThemeRepository
import javax.inject.Inject

class ObserveAppearanceUseCase @Inject constructor(private val repository: ThemeRepository) {
    operator fun invoke() = repository.observeAppearance()
}
