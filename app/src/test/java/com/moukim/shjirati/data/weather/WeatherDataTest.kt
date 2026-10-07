package com.moukim.shjirati.data.weather

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WeatherDataTest {
    @Test
    fun weather_model_preserves_agricultural_risk_flags() {
        val day = WeatherDaily(
            dateEpochDay = LocalDate.of(2026, 10, 7).toEpochDay(),
            temperatureMinC = 1.5,
            temperatureMaxC = 27.0,
            precipitationMm = 4.2,
            precipitationProbabilityPercent = 70,
            humidityMeanPercent = 68.0,
            windSpeedMaxKmh = 21.0,
            frostRisk = true,
            heatRisk = false,
            downloadedAtEpochMillis = 123L,
        )
        assertTrue(day.frostRisk)
        assertEquals(70, day.precipitationProbabilityPercent)
        assertEquals(4.2, day.precipitationMm, 0.001)
    }

    @Test
    fun location_is_kept_as_decimal_coordinates() {
        val location = WeatherLocation(36.2639, 6.7243)
        assertEquals(36.2639, location.latitude, 0.000001)
        assertEquals(6.7243, location.longitude, 0.000001)
    }
}
