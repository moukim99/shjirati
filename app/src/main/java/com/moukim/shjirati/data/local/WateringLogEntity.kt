package com.moukim.shjirati.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watering_logs")
data class WateringLogEntity(
    @PrimaryKey val id: String,
    val plantId: String,
    val wateredAtEpochMillis: Long,
    val amountMl: Int? = null,
    val durationMinutes: Int? = null,
    val note: String? = null
)
