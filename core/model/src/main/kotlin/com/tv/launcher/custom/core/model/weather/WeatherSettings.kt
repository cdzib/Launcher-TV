package com.tv.launcher.custom.core.model.weather

data class WeatherSettings(
    val city: WeatherCity? = null,
    val unit: WeatherUnit = WeatherUnit.CELSIUS,
    val isEnabled: Boolean = false
)
