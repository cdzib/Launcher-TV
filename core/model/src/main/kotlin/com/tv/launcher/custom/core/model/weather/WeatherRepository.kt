package com.tv.launcher.custom.core.model.weather

interface WeatherRepository {
    suspend fun searchCities(query: String): List<WeatherCity>

    suspend fun getCurrentWeather(city: WeatherCity, unit: WeatherUnit): CurrentWeather
}
