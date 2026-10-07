package com.moukim.shjirati.data.weather

data class PlantWeatherProfile(
    val plantId: String,
    val frostRiskMinC: Double,
    val heatRiskMaxC: Double,
    val source: String
)
