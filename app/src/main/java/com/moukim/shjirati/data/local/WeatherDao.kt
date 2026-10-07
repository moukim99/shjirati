package com.moukim.shjirati.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_daily WHERE latitude = :latitude AND longitude = :longitude ORDER BY dateEpochDay ASC")
    fun observeDaily(latitude: Double, longitude: Double): Flow<List<WeatherDailyEntity>>

    @Query("SELECT * FROM weather_daily WHERE latitude = :latitude AND longitude = :longitude AND dateEpochDay BETWEEN :fromEpochDay AND :toEpochDay ORDER BY dateEpochDay ASC")
    suspend fun getDaily(latitude: Double, longitude: Double, fromEpochDay: Long, toEpochDay: Long): List<WeatherDailyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDaily(items: List<WeatherDailyEntity>)

    @Query("DELETE FROM weather_daily WHERE latitude = :latitude AND longitude = :longitude AND dateEpochDay < :beforeEpochDay")
    suspend fun deleteOlderThan(latitude: Double, longitude: Double, beforeEpochDay: Long)
}
