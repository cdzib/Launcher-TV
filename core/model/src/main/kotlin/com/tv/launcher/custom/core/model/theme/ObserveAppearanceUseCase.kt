package com.tv.launcher.custom.core.model.theme


class ObserveAppearanceUseCase(private val repository: ThemeRepository) {
    operator fun invoke() = repository.observeAppearance()
}
