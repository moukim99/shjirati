package com.moukim.shjirati.data.weather

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlantWeatherAdvisorTest {

    private val tomato = PlantWeatherProfile(
        plantId = "solanum_lycopersicum",
        frostRiskMinC = 2.0,
        heatRiskMaxC = 35.0,
        source = "test"
    )

    @Test
    fun frost_warning_is_generated_for_sensitive_plant() {
        val day = WeatherDaily(
            dateEpochDay = 20_000L,
            temperatureMinC = 1.0,
            temperatureMaxC = 18.0,
            precipitationMm = 0.0,
            precipitationProbabilityPercent = 10,
            humidityMeanPercent = 60.0,
            windSpeedMaxKmh = 10.0,
            frostRisk = true,
            heatRisk = false,
            downloadedAtEpochMillis = 1L
        )

        val advice = PlantWeatherAdvisor.advise(tomato.plantId, day, tomato)

        assertEquals(PlantWeatherRisk.FROST, advice.risk)
        assertTrue(advice.message.contains("الصقيع"))
    }

    @Test
    fun heat_warning_is_generated_for_sensitive_plant() {
        val day = WeatherDaily(
            dateEpochDay = 20_001L,
            temperatureMinC = 18.0,
            temperatureMaxC = 36.0,
            precipitationMm = 0.0,
            precipitationProbabilityPercent = 0,
            humidityMeanPercent = 35.0,
            windSpeedMaxKmh = 20.0,
            frostRisk = false,
            heatRisk = true,
            downloadedAtEpochMillis = 1L
        )

        val advice = PlantWeatherAdvisor.advise(tomato.plantId, day, tomato)

        assertEquals(PlantWeatherRisk.HEAT, advice.risk)
        assertTrue(advice.message.contains("حراري"))
    }

    @Test
    fun normal_weather_has_no_plant_risk() {
        val day = WeatherDaily(
            dateEpochDay = 20_002L,
            temperatureMinC = 12.0,
            temperatureMaxC = 28.0,
            precipitationMm = 2.0,
            precipitationProbabilityPercent = 20,
            humidityMeanPercent = 55.0,
            windSpeedMaxKmh = 12.0,
            frostRisk = false,
            heatRisk = false,
            downloadedAtEpochMillis = 1L
        )

        val advice = PlantWeatherAdvisor.advise(tomato.plantId, day, tomato)

        assertEquals(PlantWeatherRisk.NONE, advice.risk)
    }
}
