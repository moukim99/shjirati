package com.moukim.shjirati.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter

class ShjiratiConverters {
    @TypeConverter fun categoryToString(value: PlantCategory): String = value.name
    @TypeConverter fun stringToCategory(value: String): PlantCategory = PlantCategory.valueOf(value)
}

@Database(
    entities = [PlantEntity::class, WateringLogEntity::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
    exportSchema = true
)
@androidx.room.TypeConverters(ShjiratiConverters::class)
abstract class ShjiratiDatabase : RoomDatabase() {
    abstract fun dao(): ShjiratiDao
}
