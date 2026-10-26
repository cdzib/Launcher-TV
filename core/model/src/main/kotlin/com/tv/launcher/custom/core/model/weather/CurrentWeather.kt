package com.tv.launcher.custom.core.model.weather

import java.time.Instant

data class CurrentWeather(
    val temperature: Double,
    val unit: WeatherUnit,
    val weatherCode: Int,
    val isDay: Boolean,
    val observedAt: Instant
) {
    init {
        require(temperature.isFinite()) { "Temperature must be finite" }
        require(weatherCode >= 0) { "Weather code must be non-negative" }
    }
}
