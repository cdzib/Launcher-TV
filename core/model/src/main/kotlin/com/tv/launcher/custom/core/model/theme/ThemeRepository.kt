package com.tv.launcher.custom.core.model.theme

import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    fun observeTheme(): Flow<ThemeId>
    suspend fun setTheme(themeId: ThemeId)
    fun observeAppearance(): Flow<LauncherAppearance>
    suspend fun setAppearance(appearance: LauncherAppearance)
}
