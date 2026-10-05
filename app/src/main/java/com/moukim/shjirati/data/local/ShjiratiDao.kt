package com.moukim.shjirati.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShjiratiDao {
    @Query("SELECT * FROM plants ORDER BY name COLLATE NOCASE")
    fun observePlants(): Flow<List<PlantEntity>>

    @Query("SELECT * FROM plants WHERE id = :id")
    suspend fun getPlant(id: String): PlantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlant(plant: PlantEntity)

    @Delete
    suspend fun deletePlant(plant: PlantEntity)

    @Query("SELECT * FROM watering_logs WHERE plantId = :plantId ORDER BY wateredAtEpochMillis DESC")
    fun observeWateringHistory(plantId: String): Flow<List<WateringLogEntity>>

    @Insert
    suspend fun insertWateringLog(log: WateringLogEntity)
}
