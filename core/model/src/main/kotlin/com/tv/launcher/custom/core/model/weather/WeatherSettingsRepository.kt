package com.tv.launcher.custom.core.model.weather

import kotlinx.coroutines.flow.Flow

interface WeatherSettingsRepository {
    fun observeSettings(): Flow<WeatherSettings>

    suspend fun saveSettings(settings: WeatherSettings)
}
