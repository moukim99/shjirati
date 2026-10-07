package com.moukim.shjirati.data.local

import androidx.room.Entity
import androidx.room.Index

@Entity(tableName = "weather_daily", primaryKeys = ["dateEpochDay", "latitude", "longitude"], indices = [Index("dateEpochDay"), Index("downloadedAtEpochMillis")])
data class WeatherDailyEntity(
    val dateEpochDay: Long, val latitude: Double, val longitude: Double,
    val temperatureMinC: Double?, val temperatureMaxC: Double?, val precipitationMm: Double?,
    val precipitationProbabilityPercent: Int?, val humidityMeanPercent: Double?, val windSpeedMaxKmh: Double?,
    val frostRisk: Boolean, val heatRisk: Boolean, val downloadedAtEpochMillis: Long,
)
