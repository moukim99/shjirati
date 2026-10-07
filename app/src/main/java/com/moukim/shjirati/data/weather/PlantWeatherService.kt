package com.moukim.shjirati.data.weather

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Combines the saved garden location, cached weather and plant-specific profile.
 * Location selection remains a user-controlled setting and never requests permission.
 */
class PlantWeatherService(
    private val weatherRepository: WeatherRepository,
    context: Context
) {
    private val profileRepository = PlantWeatherProfileRepository(context)

    fun currentLocation(): WeatherLocation? =
        weatherRepository.currentLocation()

    fun observeAdvice(plantId: String): Flow<List<PlantWeatherAdvice>> {
        val profile = profileRepository.getByPlantId(plantId)
        return weatherRepository.observe().map { days ->
            PlantWeatherAdvisor.adviseAll(plantId, days, profile)
        }
    }

    suspend fun refresh(): WeatherResult =
        weatherRepository.refresh()

    suspend fun cachedWeather(): List<WeatherDaily> =
        weatherRepository.getCached()

    fun profileFor(plantId: String): PlantWeatherProfile? =
        profileRepository.getByPlantId(plantId)
}
