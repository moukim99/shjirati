package com.moukim.shjirati.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.moukim.shjirati.notifications.WateringNotificationScheduler
import com.moukim.shjirati.data.local.PlantCategory
import com.moukim.shjirati.data.local.PlantEntity
import com.moukim.shjirati.data.local.WateringLogEntity
import com.moukim.shjirati.domain.PlantRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class HomeViewModel(private val repository: PlantRepository) : ViewModel() {
    val plants: StateFlow<List<PlantEntity>> = repository.observePlants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun savePlant(
        name: String,
        category: PlantCategory,
        location: String?,
        notes: String?,
        imageUri: String?,
        plantedAtEpochMillis: Long?,
        expectedDateEpochMillis: Long?,
        isFruitBearing: Boolean,
        icon: String?,
        wateringIntervalDays: Int?,
        wateringDaysMask: Int,
        wateringHour: Int,
        wateringMinute: Int,
        seasonalEnabled: Boolean,
        springIntervalDays: Int?,
        summerIntervalDays: Int?,
        autumnIntervalDays: Int?,
        winterIntervalDays: Int?
    ) {
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            repository.savePlant(
                PlantEntity(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    category = category,
                    location = location,
                    notes = notes,
                    imageUri = imageUri,
                    plantedAtEpochMillis = plantedAtEpochMillis,
                    expectedDateEpochMillis = expectedDateEpochMillis,
                    isFruitBearing = isFruitBearing,
                    icon = icon,
                    wateringIntervalDays = wateringIntervalDays,
                    wateringDaysMask = wateringDaysMask,
                    wateringHour = wateringHour,
                    wateringMinute = wateringMinute,
                    seasonalScheduleEnabled = seasonalEnabled,
                    springIntervalDays = springIntervalDays,
                    summerIntervalDays = summerIntervalDays,
                    autumnIntervalDays = autumnIntervalDays,
                    winterIntervalDays = winterIntervalDays,
                    createdAtEpochMillis = now,
                    updatedAtEpochMillis = now
                )
            )
        }
    }

    fun updatePlant(
        plant: PlantEntity,
        name: String,
        category: PlantCategory,
        location: String?,
        notes: String?,
        imageUri: String?,
        plantedAtEpochMillis: Long?,
        expectedDateEpochMillis: Long?,
        isFruitBearing: Boolean,
        icon: String?,
        wateringIntervalDays: Int?,
        wateringDaysMask: Int,
        wateringHour: Int,
        wateringMinute: Int,
        seasonalEnabled: Boolean,
        springIntervalDays: Int?,
        summerIntervalDays: Int?,
        autumnIntervalDays: Int?,
        winterIntervalDays: Int?
    ) {
        viewModelScope.launch {
            val updated = plant.copy(
                name = name,
                category = category,
                location = location,
                notes = notes,
                imageUri = imageUri,
                plantedAtEpochMillis = plantedAtEpochMillis,
                expectedDateEpochMillis = expectedDateEpochMillis,
                isFruitBearing = isFruitBearing,
                icon = icon,
                wateringIntervalDays = wateringIntervalDays,
                wateringDaysMask = wateringDaysMask,
                wateringHour = wateringHour,
                wateringMinute = wateringMinute,
                seasonalScheduleEnabled = seasonalEnabled,
                springIntervalDays = springIntervalDays,
                summerIntervalDays = summerIntervalDays,
                autumnIntervalDays = autumnIntervalDays,
                winterIntervalDays = winterIntervalDays,
                updatedAtEpochMillis = System.currentTimeMillis()
            )
            repository.savePlant(updated)
            currentContext?.let { context ->
                WateringNotificationScheduler.cancel(context, plant.id)
                WateringNotificationScheduler.schedule(context, updated)
            }
        }
    }

    fun deletePlant(context: Context, plant: PlantEntity) {
        viewModelScope.launch {
            WateringNotificationScheduler.cancel(context, plant.id)
            repository.deletePlant(plant)
        }
    }

    private var currentContext: Context? = null

    fun attachContext(context: Context) {
        currentContext = context.applicationContext
    }

    fun scheduleAll(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            plants.value.forEach { WateringNotificationScheduler.schedule(context, it) }
        }
    }

    fun water(plant: PlantEntity) {
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            repository.logWatering(
                WateringLogEntity(
                    UUID.randomUUID().toString(),
                    plant.id,
                    now,
                    plant.wateringAmountMl,
                    plant.wateringDurationMinutes
                )
            )
            repository.savePlant(
                plant.copy(
                    lastWateredAtEpochMillis = now,
                    updatedAtEpochMillis = now
                )
            )
        }
    }

    companion object {
        fun factory(repository: PlantRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HomeViewModel(repository) as T
        }
    }
}
