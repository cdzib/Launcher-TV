package com.tv.launcher.custom.core.data.weather

import com.tv.launcher.custom.core.model.weather.CurrentWeather
import com.tv.launcher.custom.core.model.weather.WeatherCity
import com.tv.launcher.custom.core.model.weather.WeatherRepository
import com.tv.launcher.custom.core.model.weather.WeatherUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class OpenMeteoWeatherRepository @Inject constructor() : WeatherRepository {
    private val client = OpenMeteoClient()

    override suspend fun searchCities(query: String): List<WeatherCity> =
        withContext(Dispatchers.IO) { client.searchCities(query) }

    override suspend fun getCurrentWeather(city: WeatherCity, unit: WeatherUnit): CurrentWeather =
        withContext(Dispatchers.IO) { client.getCurrentWeather(city, unit) }
}
