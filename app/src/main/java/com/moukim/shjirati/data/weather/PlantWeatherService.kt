package com.moukim.shjirati.data.weather

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Combines locally cached weather with the plant-specific weather profile.
 * It deliberately does not own location selection or permissions.
 */
class PlantWeatherService(
    private val weatherRepository: WeatherRepository,
    context: Context
) {
    private val profileRepository = PlantWeatherProfileRepository(context)

    fun observeAdvice(
        plantId: String,
        location: WeatherLocation
    ): Flow<List<PlantWeatherAdvice>> {
        val profile = profileRepository.getByPlantId(plantId)
        return weatherRepository.observe(location).map { days ->
            PlantWeatherAdvisor.adviseAll(plantId, days, profile)
        }
    }

    suspend fun refresh(
        plantId: String,
        location: WeatherLocation
    ): WeatherResult {
        return weatherRepository.refresh(location)
    }

    fun profileFor(plantId: String): PlantWeatherProfile? =
        profileRepository.getByPlantId(plantId)
}
