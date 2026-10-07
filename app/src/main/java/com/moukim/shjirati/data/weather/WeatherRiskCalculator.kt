package com.moukim.shjirati.data.weather

object WeatherRiskCalculator {
    fun frostRisk(temperatureMinC: Double?): Boolean =
        temperatureMinC != null && temperatureMinC <= 2.0

    fun heatRisk(temperatureMaxC: Double?): Boolean =
        temperatureMaxC != null && temperatureMaxC >= 38.0
}
