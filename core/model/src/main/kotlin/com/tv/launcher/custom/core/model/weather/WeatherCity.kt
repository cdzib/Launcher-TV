package com.tv.launcher.custom.core.model.weather

data class WeatherCity(
    val id: Long,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val region: String? = null
) {
    val displayName: String
        get() = listOf(name, region, country).filterNot { it.isNullOrBlank() }.joinToString(", ")

    init {
        require(id >= 0) { "City id must be non-negative" }
        require(name.isNotBlank()) { "City name must not be blank" }
        require(country.isNotBlank()) { "Country must not be blank" }
        require(latitude.isFinite() && latitude in -90.0..90.0) { "Latitude is outside valid bounds" }
        require(longitude.isFinite() && longitude in -180.0..180.0) { "Longitude is outside valid bounds" }
    }
}
