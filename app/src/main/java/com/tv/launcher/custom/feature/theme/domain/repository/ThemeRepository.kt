package com.tv.launcher.custom.feature.theme.domain.repository

import com.tv.launcher.custom.feature.theme.domain.model.ThemeId
import com.tv.launcher.custom.feature.theme.domain.model.LauncherAppearance
import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    fun observeTheme(): Flow<ThemeId>
    suspend fun setTheme(themeId: ThemeId)
    fun observeAppearance(): Flow<LauncherAppearance>
    suspend fun setAppearance(appearance: LauncherAppearance)
}
