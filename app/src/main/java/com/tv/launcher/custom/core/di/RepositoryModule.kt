package com.tv.launcher.custom.core.di

import com.tv.launcher.custom.core.apps.InstalledAppsRepository
import com.tv.launcher.custom.core.model.apps.*
import com.tv.launcher.custom.core.data.theme.DataStoreThemeRepository
import com.tv.launcher.custom.core.model.theme.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindApps(impl: InstalledAppsRepository): AppsRepository
    @Binds abstract fun bindTheme(impl: DataStoreThemeRepository): ThemeRepository

    companion object {
        @Provides fun provideObserveApps(repository: AppsRepository) = ObserveAppsUseCase(repository)
        @Provides fun provideLaunchApp(repository: AppsRepository) = LaunchAppUseCase(repository)
        @Provides fun provideObserveAppearance(repository: ThemeRepository) = ObserveAppearanceUseCase(repository)
        @Provides fun provideSetAppearance(repository: ThemeRepository) = SetAppearanceUseCase(repository)
    }
}
