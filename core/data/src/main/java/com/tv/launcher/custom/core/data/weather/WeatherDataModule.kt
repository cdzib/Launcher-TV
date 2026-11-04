package com.tv.launcher.custom.core.data.weather

import com.tv.launcher.custom.core.model.weather.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class WeatherDataModule {
    @Binds
    abstract fun bindWeatherRepository(repository: OpenMeteoWeatherRepository): WeatherRepository

    @Binds
    abstract fun bindWeatherSettingsRepository(
        repository: WeatherDataStoreRepository
    ): com.tv.launcher.custom.core.model.weather.WeatherSettingsRepository
}
