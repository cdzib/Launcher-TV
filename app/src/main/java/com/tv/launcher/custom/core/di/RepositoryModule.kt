package com.tv.launcher.custom.core.di

import com.tv.launcher.custom.feature.apps.data.InstalledAppsRepository
import com.tv.launcher.custom.feature.apps.domain.repository.AppsRepository
import com.tv.launcher.custom.feature.theme.data.DataStoreThemeRepository
import com.tv.launcher.custom.feature.theme.domain.repository.ThemeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindApps(impl: InstalledAppsRepository): AppsRepository
    @Binds abstract fun bindTheme(impl: DataStoreThemeRepository): ThemeRepository
}
