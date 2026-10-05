package com.moukim.shjirati.domain

import com.moukim.shjirati.data.local.PlantEntity
import com.moukim.shjirati.data.local.WateringLogEntity
import kotlinx.coroutines.flow.Flow

interface PlantRepository {
    fun observePlants(): Flow<List<PlantEntity>>
    suspend fun savePlant(plant: PlantEntity)
    suspend fun deletePlant(plant: PlantEntity)
    fun observeWateringHistory(plantId: String): Flow<List<WateringLogEntity>>
    suspend fun logWatering(log: WateringLogEntity)
}
