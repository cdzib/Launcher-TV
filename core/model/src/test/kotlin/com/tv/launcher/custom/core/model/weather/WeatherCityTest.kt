package com.tv.launcher.custom.core.model.weather

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class WeatherCityTest {
    @Test
    fun `accepts valid city coordinates and normalized display fields`() {
        val city = WeatherCity(
            id = 1L,
            name = "Mérida",
            country = "México",
            latitude = 20.9674,
            longitude = -89.6237
        )

        assertEquals("Mérida, México", city.displayName)
    }

    @Test
    fun `rejects blank names and coordinates outside earth bounds`() {
        assertThrows(IllegalArgumentException::class.java) {
            WeatherCity(1L, " ", "México", 20.0, -89.0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            WeatherCity(1L, "Mérida", "México", 91.0, -89.0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            WeatherCity(1L, "Mérida", "México", 20.0, 181.0)
        }
    }
}
