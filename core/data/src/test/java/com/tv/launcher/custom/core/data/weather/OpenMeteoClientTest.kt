package com.tv.launcher.custom.core.data.weather

import com.tv.launcher.custom.core.model.weather.WeatherCity
import com.tv.launcher.custom.core.model.weather.WeatherUnit
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class OpenMeteoClientTest {
    @Test
    fun `search maps selected cities and encodes query`() {
        lateinit var requestedUrl: URL
        val response = """{"results":[{"id":3523349,"name":"Mérida","admin1":"Yucatán","country":"México","latitude":20.9674,"longitude":-89.6237}]}"""
        val client = OpenMeteoClient { url ->
            requestedUrl = url
            FakeHttpURLConnection(url, response)
        }

        val cities = client.searchCities("Mérida, MX")

        assertEquals("geocoding-api.open-meteo.com", requestedUrl.host)
        assertTrue(requestedUrl.query.contains("M%C3%A9rida%2C+MX"))
        assertEquals("Mérida, Yucatán, México", cities.single().displayName)
        assertEquals(-89.6237, cities.single().longitude, 0.00001)
    }

    @Test
    fun `short city search does not open a connection`() {
        val client = OpenMeteoClient { throw AssertionError("No request expected") }

        assertTrue(client.searchCities(" M ").isEmpty())
    }

    @Test
    fun `forecast maps fahrenheit WMO code day state and observation time`() {
        lateinit var requestedUrl: URL
        val response = """{"utc_offset_seconds":-18000,"current":{"time":"2026-10-25T12:15","temperature_2m":71.6,"weather_code":3,"is_day":1},"current_units":{"temperature_2m":"°F"}}"""
        val client = OpenMeteoClient { url ->
            requestedUrl = url
            FakeHttpURLConnection(url, response)
        }

        val weather = client.getCurrentWeather(
            WeatherCity(44, "Cancún", "México", 21.1619, -86.8515),
            WeatherUnit.FAHRENHEIT
        )

        assertEquals("api.open-meteo.com", requestedUrl.host)
        assertTrue(requestedUrl.query.contains("temperature_unit=fahrenheit"))
        assertEquals(71.6, weather.temperature, 0.01)
        assertEquals(3, weather.weatherCode)
        assertTrue(weather.isDay)
        assertEquals("2026-10-25T17:15:00Z", weather.observedAt.toString())
    }

    @Test
    fun `rejects malformed forecast instead of returning placeholder weather`() {
        val client = OpenMeteoClient { url ->
            FakeHttpURLConnection(url, """{"current":{"temperature_2m":24}}""")
        }

        assertThrows(OpenMeteoException::class.java) {
            client.getCurrentWeather(
                WeatherCity(44, "Cancún", "México", 21.1619, -86.8515),
                WeatherUnit.CELSIUS
            )
        }
    }

    private class FakeHttpURLConnection(
        url: URL,
        private val body: String,
        private val status: Int = HttpURLConnection.HTTP_OK
    ) : HttpURLConnection(url) {
        override fun connect() = Unit
        override fun disconnect() = Unit
        override fun usingProxy(): Boolean = false
        override fun getResponseCode(): Int = status
        override fun getInputStream() = ByteArrayInputStream(body.toByteArray(Charsets.UTF_8))
    }
}
