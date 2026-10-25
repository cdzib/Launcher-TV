package com.tv.launcher.custom.core.model.theme


class SetAppearanceUseCase(private val repository: ThemeRepository) {
    suspend operator fun invoke(appearance: LauncherAppearance) = repository.setAppearance(appearance)
}
