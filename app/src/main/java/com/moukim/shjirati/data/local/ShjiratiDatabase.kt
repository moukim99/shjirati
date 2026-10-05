package com.moukim.shjirati.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter

class ShjiratiConverters {
    @TypeConverter fun categoryToString(value: PlantCategory): String = value.name
    @TypeConverter fun stringToCategory(value: String): PlantCategory = PlantCategory.valueOf(value)
}

@Database(
    entities = [PlantEntity::class, WateringLogEntity::class],
    version = 1,
    exportSchema = true
)
@androidx.room.TypeConverters(ShjiratiConverters::class)
abstract class ShjiratiDatabase : RoomDatabase() {
    abstract fun dao(): ShjiratiDao
}
