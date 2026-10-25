package com.tv.launcher.custom.core.model.theme


class SetThemeUseCase(private val repository: ThemeRepository) {
    suspend operator fun invoke(themeId: ThemeId) = repository.setTheme(themeId)
}
