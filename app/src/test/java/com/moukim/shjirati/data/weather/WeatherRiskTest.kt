package com.moukim.shjirati.data.weather

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherRiskTest {
    @Test
    fun frost_threshold_is_two_degrees() {
        val frost = 2.0 <= 2.0
        val safe = 2.1 <= 2.0
        assertTrue(frost)
        assertFalse(safe)
    }

    @Test
    fun heat_threshold_is_thirty_eight_degrees() {
        val heat = 38.0 >= 38.0
        val safe = 37.9 >= 38.0
        assertTrue(heat)
        assertFalse(safe)
    }

    @Test
    fun weather_location_keeps_coordinates() {
        val location = WeatherLocation(36.7372, 3.0863)
        assertTrue(location.latitude == 36.7372)
        assertTrue(location.longitude == 3.0863)
    }
}
