package com.moukim.shjirati.data

import com.moukim.shjirati.data.local.PlantEntity
import com.moukim.shjirati.data.local.ShjiratiDao
import com.moukim.shjirati.data.local.WateringLogEntity
import com.moukim.shjirati.domain.PlantRepository
import kotlinx.coroutines.flow.Flow

class PlantRepositoryImpl(private val dao: ShjiratiDao) : PlantRepository {
    override fun observePlants(): Flow<List<PlantEntity>> = dao.observePlants()
    override suspend fun savePlant(plant: PlantEntity) = dao.upsertPlant(plant)
    override suspend fun deletePlant(plant: PlantEntity) = dao.deletePlant(plant)
    override fun observeWateringHistory(plantId: String): Flow<List<WateringLogEntity>> =
        dao.observeWateringHistory(plantId)
    override suspend fun logWatering(log: WateringLogEntity) = dao.insertWateringLog(log)
}
