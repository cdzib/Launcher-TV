package com.tv.launcher.custom.core.data.weather

import com.tv.launcher.custom.core.model.weather.CurrentWeather
import com.tv.launcher.custom.core.model.weather.WeatherCity
import com.tv.launcher.custom.core.model.weather.WeatherUnit
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class OpenMeteoClient(
    private val openConnection: (URL) -> HttpURLConnection = { it.openConnection() as HttpURLConnection }
) {
    fun searchCities(query: String): List<WeatherCity> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.length < MIN_QUERY_LENGTH) return emptyList()
        val url = URL(
            "https://$GEOCODING_HOST/v1/search?name=${encodeQuery(normalizedQuery)}" +
                "&count=$MAX_CITY_RESULTS&language=es&format=json"
        )
        val root = requestJson(url, GEOCODING_HOST)
        val results = root["results"] ?: return emptyList()
        val cityResults = try {
            results.jsonArray
        } catch (_: Exception) {
            throw OpenMeteoException("Invalid city search response")
        }
        return cityResults.map(::mapCity)
    }

    fun getCurrentWeather(city: WeatherCity, unit: WeatherUnit): CurrentWeather {
        val url = URL(
            "https://$FORECAST_HOST/v1/forecast?latitude=${city.latitude}&longitude=${city.longitude}" +
                "&current=temperature_2m,weather_code,is_day&temperature_unit=${unit.temperatureUnit}&timezone=auto"
        )
        val root = requestJson(url, FORECAST_HOST)
        val current = root.requiredObject("current")
        val currentUnits = root.requiredObject("current_units")
        val temperatureUnit = currentUnits.requiredString("temperature_2m")
        val expectedUnit = if (unit == WeatherUnit.CELSIUS) "°C" else "°F"
        if (temperatureUnit != expectedUnit) throw OpenMeteoException("Unexpected temperature unit")

        val temperature = current.requiredDouble("temperature_2m")
        val weatherCode = current.requiredInt("weather_code")
        val isDay = current.requiredInt("is_day")
        if (isDay !in 0..1) throw OpenMeteoException("Invalid day/night value")
        val observedAt = try {
            val localTime = LocalDateTime.parse(current.requiredString("time"))
            val utcOffsetSeconds = root.requiredInt("utc_offset_seconds")
            localTime.toInstant(ZoneOffset.ofTotalSeconds(utcOffsetSeconds))
        } catch (_: Exception) {
            throw OpenMeteoException("Invalid observation time")
        }
        return CurrentWeather(temperature, unit, weatherCode, isDay == 1, observedAt)
    }

    private fun requestJson(url: URL, expectedHost: String): JsonObject {
        if (url.protocol != "https" || url.host != expectedHost) {
            throw OpenMeteoException("Unapproved weather endpoint")
        }
        val connection = try {
            openConnection(url)
        } catch (_: Exception) {
            throw OpenMeteoException("Could not connect to Open-Meteo")
        }
        try {
            connection.instanceFollowRedirects = false
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            val statusCode = connection.responseCode
            if (statusCode !in 200..299) throw OpenMeteoException("Open-Meteo returned HTTP $statusCode")
            if (connection.contentLengthLong > MAX_RESPONSE_BYTES) {
                throw OpenMeteoException("Open-Meteo response is too large")
            }
            val body = connection.inputStream.use { it.readBounded(MAX_RESPONSE_BYTES) }
            return try {
                JSON.parseToJsonElement(body.toString(Charsets.UTF_8)).jsonObject
            } catch (_: Exception) {
                throw OpenMeteoException("Open-Meteo returned invalid JSON")
            }
        } catch (exception: OpenMeteoException) {
            throw exception
        } catch (_: Exception) {
            throw OpenMeteoException("Open-Meteo request failed")
        } finally {
            connection.disconnect()
        }
    }

    private fun mapCity(element: kotlinx.serialization.json.JsonElement): WeatherCity {
        val city = try {
            element.jsonObject
        } catch (_: Exception) {
            throw OpenMeteoException("Invalid city in search response")
        }
        return try {
            WeatherCity(
                id = city.requiredLong("id"),
                name = city.requiredString("name"),
                country = city.requiredString("country"),
                latitude = city.requiredDouble("latitude"),
                longitude = city.requiredDouble("longitude"),
                region = city["admin1"]?.jsonPrimitive?.content?.takeIf(String::isNotBlank)
            )
        } catch (_: Exception) {
            throw OpenMeteoException("Invalid city in search response")
        }
    }

    private fun JsonObject.requiredString(key: String): String =
        this[key]?.jsonPrimitive?.content?.takeIf(String::isNotBlank)
            ?: throw OpenMeteoException("Missing '$key' in Open-Meteo response")

    private fun JsonObject.requiredDouble(key: String): Double =
        this[key]?.jsonPrimitive?.doubleOrNull?.takeIf(Double::isFinite)
            ?: throw OpenMeteoException("Invalid '$key' in Open-Meteo response")

    private fun JsonObject.requiredInt(key: String): Int =
        this[key]?.jsonPrimitive?.intOrNull
            ?: throw OpenMeteoException("Invalid '$key' in Open-Meteo response")

    private fun JsonObject.requiredLong(key: String): Long =
        this[key]?.jsonPrimitive?.content?.toLongOrNull()
            ?: throw OpenMeteoException("Invalid '$key' in Open-Meteo response")

    private fun JsonObject.requiredObject(key: String): JsonObject = try {
        this[key]?.jsonObject ?: throw OpenMeteoException("Missing '$key' in Open-Meteo response")
    } catch (exception: OpenMeteoException) {
        throw exception
    } catch (_: Exception) {
        throw OpenMeteoException("Invalid '$key' in Open-Meteo response")
    }

    private fun encodeQuery(value: String): String =
        URLEncoder.encode(value, Charsets.UTF_8.name())

    private fun InputStream.readBounded(maxBytes: Int): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val count = read(buffer)
            if (count < 0) return output.toByteArray()
            if (output.size() + count > maxBytes) throw OpenMeteoException("Open-Meteo response is too large")
            output.write(buffer, 0, count)
        }
    }

    private companion object {
        const val GEOCODING_HOST = "geocoding-api.open-meteo.com"
        const val FORECAST_HOST = "api.open-meteo.com"
        const val MIN_QUERY_LENGTH = 2
        const val MAX_CITY_RESULTS = 10
        const val MAX_RESPONSE_BYTES = 256 * 1024
        const val TIMEOUT_MS = 10_000
        val JSON = Json { isLenient = false; ignoreUnknownKeys = true }
    }
}

internal class OpenMeteoException(message: String) : Exception(message)
