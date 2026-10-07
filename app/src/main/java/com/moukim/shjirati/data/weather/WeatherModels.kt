package com.moukim.shjirati.data.weather

data class WeatherDaily(
    val dateEpochDay: Long, val temperatureMinC: Double?, val temperatureMaxC: Double?,
    val precipitationMm: Double?, val precipitationProbabilityPercent: Int?,
    val humidityMeanPercent: Double?, val windSpeedMaxKmh: Double?,
    val frostRisk: Boolean, val heatRisk: Boolean, val downloadedAtEpochMillis: Long,
)

data class WeatherLocation(val latitude: Double, val longitude: Double)

sealed interface WeatherResult {
    data class Fresh(val days: List<WeatherDaily>) : WeatherResult
    data class Failure(val cachedDays: List<WeatherDaily>, val message: String) : WeatherResult
}
