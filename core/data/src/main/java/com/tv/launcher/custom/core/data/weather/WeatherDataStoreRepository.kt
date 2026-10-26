package com.tv.launcher.custom.core.data.weather

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tv.launcher.custom.core.model.weather.WeatherCity
import com.tv.launcher.custom.core.model.weather.WeatherSettings
import com.tv.launcher.custom.core.model.weather.WeatherSettingsRepository
import com.tv.launcher.custom.core.model.weather.WeatherUnit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val Context.weatherPreferences by preferencesDataStore(name = "weather_preferences")

@Singleton
class WeatherDataStoreRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : WeatherSettingsRepository {
    override fun observeSettings(): Flow<WeatherSettings> = context.weatherPreferences.data.map { preferences ->
        decodeSettings(preferences[SETTINGS_KEY])
    }

    override suspend fun saveSettings(settings: WeatherSettings) {
        context.weatherPreferences.edit { preferences ->
            preferences[SETTINGS_KEY] = encodeSettings(settings)
        }
    }

    private fun encodeSettings(settings: WeatherSettings): String {
        val values = mutableMapOf<String, JsonElement>(
            "enabled" to JsonPrimitive(settings.isEnabled),
            "unit" to JsonPrimitive(settings.unit.name)
        )
        settings.city?.let { city ->
            values["city"] = JsonObject(buildMap {
                put("id", JsonPrimitive(city.id))
                put("name", JsonPrimitive(city.name))
                put("country", JsonPrimitive(city.country))
                put("latitude", JsonPrimitive(city.latitude))
                put("longitude", JsonPrimitive(city.longitude))
                city.region?.let { put("region", JsonPrimitive(it)) }
            })
        }
        return JSON.encodeToString(JsonObject.serializer(), JsonObject(values))
    }

    private fun decodeSettings(rawSettings: String?): WeatherSettings {
        if (rawSettings.isNullOrBlank()) return WeatherSettings()
        return runCatching {
            val values = JSON.parseToJsonElement(rawSettings).jsonObject
            val unit = values["unit"]?.jsonPrimitive?.content
                ?.let { stored -> WeatherUnit.entries.firstOrNull { it.name == stored } }
                ?: WeatherUnit.CELSIUS
            val city = values["city"]?.jsonObject?.let { item ->
                WeatherCity(
                    id = item.getValue("id").jsonPrimitive.content.toLong(),
                    name = item.getValue("name").jsonPrimitive.content,
                    country = item.getValue("country").jsonPrimitive.content,
                    latitude = item.getValue("latitude").jsonPrimitive.doubleOrNull ?: error("Invalid latitude"),
                    longitude = item.getValue("longitude").jsonPrimitive.doubleOrNull ?: error("Invalid longitude"),
                    region = item["region"]?.jsonPrimitive?.content
                )
            }
            WeatherSettings(
                city = city,
                unit = unit,
                isEnabled = values["enabled"]?.jsonPrimitive?.booleanOrNull ?: false
            )
        }.getOrDefault(WeatherSettings())
    }

    private companion object {
        val SETTINGS_KEY = stringPreferencesKey("settings_json")
        val JSON = Json { isLenient = false; ignoreUnknownKeys = true }
    }
}
