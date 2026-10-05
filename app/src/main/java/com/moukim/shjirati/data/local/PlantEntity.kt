package com.moukim.shjirati.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class PlantCategory { TREE, SEEDLING, VEGETABLE, OTHER }

@Entity(tableName = "plants", indices = [Index("category"), Index("name")])
data class PlantEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: PlantCategory,
    val species: String? = null,
    val imageUri: String? = null,
    val plantedAtEpochMillis: Long? = null,
    val location: String? = null,
    val notes: String? = null,
    val wateringIntervalDays: Int? = null,
    val wateringDaysMask: Int = 0,
    val wateringHour: Int = 18,
    val wateringMinute: Int = 0,
    val wateringAmountMl: Int? = null,
    val wateringDurationMinutes: Int? = null,
    val seasonalScheduleEnabled: Boolean = false,
    val springIntervalDays: Int? = null,
    val summerIntervalDays: Int? = null,
    val autumnIntervalDays: Int? = null,
    val winterIntervalDays: Int? = null,
    val lastWateredAtEpochMillis: Long? = null,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)
